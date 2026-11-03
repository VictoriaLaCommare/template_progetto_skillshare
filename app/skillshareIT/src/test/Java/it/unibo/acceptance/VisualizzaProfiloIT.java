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

public class VisualizzaProfiloIT {
    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static WebDriver driver;
    private WebDriverWait wait;

    private String emailUtenteVuoto;
    private String emailUtenteA;
    
    private String titoloAnnuncioVuoto;
    private String titoloAnnuncioConDati;

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

    @BeforeEach
    void setupWait() {
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterEach
    void puliziaAnnunci() {
        try {
            RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);
            
            // Pulizia per testStoricoVuoto
            if (emailUtenteVuoto != null) {
                regPage.apri(BASE_URL);
                wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
                regPage.compilaLogin(emailUtenteVuoto, "passwordSicura123").accedi();
                
                GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
                annunciPage.apriMieiAnnunci();
                if (annunciPage.isAnnuncioPresente(titoloAnnuncioVuoto)) {
                    annunciPage.eliminaAnnuncio(titoloAnnuncioVuoto);
                }
                driver.findElement(By.id("nav-btn-logout")).click();
                emailUtenteVuoto = null;
            }
            
            // Pulizia per testStoricoConDati (Utente A)
            if (emailUtenteA != null && titoloAnnuncioConDati != null) {
                regPage.apri(BASE_URL);
                wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
                regPage.compilaLogin(emailUtenteA, "passwordSicu123").accedi();
                
                GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
                annunciPage.apriMieiAnnunci();
                if (annunciPage.isAnnuncioPresente(titoloAnnuncioConDati)) {
                    annunciPage.eliminaAnnuncio(titoloAnnuncioConDati);
                }
                driver.findElement(By.id("nav-btn-logout")).click();
                emailUtenteA = null;
            }
        } catch (Exception ignored) {
            // Gestione di eventuali eccezioni se il test si interrompe prima del previsto
        }
    }

    @Test
    @DisplayName("Visualizzazione storico di un utente senza scambi")
    void testStoricoVuoto() {
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);
        
        // 1. Registriamo un nuovo utente (che non ha scambi)
        regPage.apri(BASE_URL).vaiAlFormRegistrazione();
        String username1 = "nuovo" + System.currentTimeMillis();
        emailUtenteVuoto = username1 + "@example.com";
        regPage.compila("Luca", "Neri", username1, emailUtenteVuoto, "01/01/2000", "passwordSicura123").registrati();
        regPage.compilaLogin(emailUtenteVuoto, "passwordSicura123").accedi();
        
        // 2. Pubblica un annuncio per farsi vedere in bacheca
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
        
        titoloAnnuncioVuoto = "Lezioni di Prova " + System.currentTimeMillis();
        annunciPage.compila(titoloAnnuncioVuoto, "Descrizione", "Cucina", "Informatica").pubblica();

        // 3. Andiamo in Bacheca e clicchiamo sul nostro stesso nome
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();
        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);

        // Clicchiamo sul profilo dell'autore dell'annuncio
        bachecaPage.cliccaProfiloAutore(username1);

        // 4. Interagiamo con il popup del profilo
        ProfiloUtentePage profiloPage = new ProfiloUtentePage(driver, TIMEOUT);
        profiloPage.cliccaMostraStorico();

        // Assert
        assertTrue(profiloPage.isMessaggioVuotoVisibile(), "Deve apparire il messaggio 'Nessuno scambio passato'");
        assertFalse(profiloPage.isTabellaStoricoVisibile(), "La tabella non deve essere visibile se lo storico è vuoto");
        
        profiloPage.chiudiProfilo();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));
    }

    @Test
    @DisplayName("Visualizzazione storico di un utente con scambi pregressi")
    void testStoricoConDati() {
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);
        
        // 1. Registriamo Utente A e pubblichiamo un annuncio
        regPage.apri(BASE_URL).vaiAlFormRegistrazione();
        String userA = "off" + System.currentTimeMillis();
        emailUtenteA = userA + "@example.com";
        regPage.compila("Anna", "Gialli", userA, emailUtenteA, "01/01/1990", "passwordSicu123").registrati();
        regPage.compilaLogin(emailUtenteA, "passwordSicu123").accedi();
        
        // Pubblicazione annuncio e logout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        
        titoloAnnuncioConDati = "Annuncio Serio " + System.currentTimeMillis();
        new GestoreAnnunciPage(driver, TIMEOUT).compila(titoloAnnuncioConDati, "Descrizione", "Pittura", "Scultura").pubblica();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));

        // 2. Registriamo Utente B
        regPage.apri(BASE_URL).vaiAlFormRegistrazione();
        String userB = "ric" + System.currentTimeMillis();
        String emailB = userB + "@example.com";
        regPage.compila("Marco", "Blu", userB, emailB, "01/01/1995", "passwordsicur456").registrati();
        regPage.compilaLogin(emailB, "passwordsicur456").accedi();

        // 3. Utente B va in bacheca, richiede lo scambio ad Utente A
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();
        
        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
        bachecaPage.cliccaRichiediScambioPerTitolo(titoloAnnuncioConDati);

        // Assert: Verifica che l'alert confermi il successo della richiesta
        String testoAlert = bachecaPage.getTestoAlertEAccetta();
        assertTrue(testoAlert.toLowerCase().contains("successo"), "La richiesta è fallita. L'alert dice: " + testoAlert);
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));

        // Rientro dell'Utente A per ACCETTARE la richiesta ricevuta
        regPage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        regPage.compilaLogin(emailUtenteA, "passwordSicu123").accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Le Mie Richieste']")));
        
        WebElement btnAccetta = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Accetta')]")
        ));
        btnAccetta.click();
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));

        // 4. Rientriamo con Utente B per verificare lo storico completato dell'Utente A
        regPage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        regPage.compilaLogin(emailB, "passwordsicur456").accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));

        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();
        
        bachecaPage.cliccaProfiloAutore(userA);

        // 5. Interagiamo con il popup del profilo di Utente A e mostriamo lo storico
        ProfiloUtentePage profiloPage = new ProfiloUtentePage(driver, TIMEOUT);
        profiloPage.cliccaMostraStorico();

        // Assert
        assertTrue(profiloPage.isTabellaStoricoVisibile(), "La tabella dello storico deve essere visibile");
        assertFalse(profiloPage.isMessaggioVuotoVisibile(), "Il messaggio di storico vuoto non deve apparire");

        profiloPage.chiudiProfilo();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();
    }
}