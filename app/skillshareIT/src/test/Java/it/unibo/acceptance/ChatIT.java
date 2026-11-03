package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class ChatIT {
    public static String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static WebDriver driver;

    private String emailUtente1;
    private String titoloAnnuncio;
    private final String password = "password123";

    @BeforeAll
    static void avviaBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))){
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @AfterAll
    static void chiudiBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterEach
    void puliziaAnnunci() {
        try {
            if (titoloAnnuncio != null && emailUtente1 != null) {
                // 1. Rientriamo con l'Utente 1 per eliminare l'annuncio di prova dal DB
                RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);
                registrazionePage.apri(BASE_URL);
                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
                
                WebElement btnVaiLogin = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));
                btnVaiLogin.click();
                
                registrazionePage.compilaLogin(emailUtente1, password).accedi();

                // 2. Eliminazione l'annuncio se ancora presente
                GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
                if (annunciPage.isAnnuncioPresente(titoloAnnuncio)) {
                    annunciPage.eliminaAnnuncio(titoloAnnuncio);
                }

                // 3. Logout
                WebElement btnLogout = driver.findElement(By.id("nav-btn-logout"));
                if (btnLogout != null && btnLogout.isDisplayed()) {
                    btnLogout.click();
                }
            }
        } catch (Exception ignored) {
            // Gestisce eventuali fallimenti di pulizia se il test si è interrotto prima
        }
    }

    @Test
    @DisplayName("Chat (UC10): Flusso completo di invio richiesta, accettazione e chat bidirezionale")
    void testFlussoChatEndToEnd() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);

        String timestamp = String.valueOf(System.currentTimeMillis());
        emailUtente1 = "mario_chat_" + timestamp + "@example.com";
        String emailUtente2 = "luigi_chat_" + timestamp + "@example.com";
        titoloAnnuncio = "Lezioni di Calcio " + timestamp;

        // FASE 1: UTENTE 1 (Mario) - Registrazione, Login e Pubblicazione Annuncio
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        registrazionePage.compila("Mario", "Rossi", "mario" + timestamp, emailUtente1, "01/01/2000", password).registrati();
        registrazionePage.compilaLogin(emailUtente1, password).accedi();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
        annunciPage.compila(titoloAnnuncio, "Offro ripetizioni di tattica calcistica", "Calcio", "Tattica").pubblica();
        
        driver.findElement(By.id("nav-btn-logout")).click();

        // FASE 2: UTENTE 2 (Luigi) - Registrazione, Login e Invio Richiesta mirata
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        registrazionePage.compila("Luigi", "Verdi", "luigi" + timestamp, emailUtente2, "01/01/1995", password).registrati();
        registrazionePage.compilaLogin(emailUtente2, password).accedi();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        driver.findElement(By.id("nav-btn-proposte")).click();
        
        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
        assertTrue(bachecaPage.isTabellaVisibile(), "La bacheca proposte dovrebbe essere visibile");
        
        bachecaPage.cliccaRichiediScambioPerTitolo(titoloAnnuncio);
        
        bachecaPage.getTestoAlertEAccetta();

        driver.findElement(By.id("nav-btn-logout")).click();

        String usernameUtente1 = "mario" + timestamp;
        String usernameUtente2 = "luigi" + timestamp;

        // FASE 3: UTENTE 1 (Mario) - Rientro, Accettazione, Apertura Chat e Invio Messaggio
        registrazionePage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        registrazionePage.compilaLogin(emailUtente1, password).accedi();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[text()='Le Mie Richieste']")
        ));
        
        WebElement btnAccetta = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Accetta')]")
        ));
        btnAccetta.click();

        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}

        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-chat"))).click();
        WebElement btnApriChat = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), 'Scambio con: " + usernameUtente2 + "')]//following::button[contains(., 'Apri Chat')][1]")
        ));
        btnApriChat.click();

        ChatPage chatPage1 = new ChatPage(driver, TIMEOUT);
        String messaggioInviato = "Ciao Luigi, concordiamo la lezione per domani?";
        chatPage1.scriviMessaggio(messaggioInviato).invia();

        assertTrue(chatPage1.isMessaggioPresente(messaggioInviato), "Mario deve vedere il suo messaggio inviato nella chat");

        chatPage1.chiudiChat();
        driver.findElement(By.id("nav-btn-logout")).click();

        // FASE 4: UTENTE 2 (Luigi) - Accesso alla chat e Verifica ricezione
        registrazionePage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        registrazionePage.compilaLogin(emailUtente2, password).accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-chat"))).click();
        WebElement btnApriChatLuigi = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), 'Scambio con: " + usernameUtente1 + "')]//following::button[contains(., 'Apri Chat')][1]")        ));
        btnApriChatLuigi.click();

        ChatPage chatPage2 = new ChatPage(driver, TIMEOUT);
        assertTrue(chatPage2.isMessaggioPresente(messaggioInviato), "Luigi deve trovare la chat avviata e visualizzare il messaggio ricevuto da Mario");
        chatPage2.chiudiChat();
    }
}