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

public class GestoreRichiesteIT {
    public static String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static WebDriver driver;

    private String emailUtente1;

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
            if (emailUtente1 != null) {
                // 1. Rientriamo con l'Utente 1 (ricevente) per eliminare l'annuncio dal DB
                RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);
                registrazionePage.apri(BASE_URL);
                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
                
                WebElement btnVaiLogin = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));
                btnVaiLogin.click();
                
                registrazionePage.compilaLogin(emailUtente1, "password123").accedi();

                // 2. Elimino l'annuncio se ancora visibile
                GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
                if (annunciPage.isAnnuncioPresente("Lezioni di Inglese")) {
                    annunciPage.eliminaAnnuncio("Lezioni di Inglese");
                }

                // 3. Logout
                WebElement btnLogout = driver.findElement(By.id("nav-btn-logout"));
                if (btnLogout != null && btnLogout.isDisplayed()) {
                    btnLogout.click();
                }
            }
        } catch (Exception ignored) {
            // Gestisce eventuali eccezioni se il test si interrompe prima del dovuto
        }
    }

    @Test
    @DisplayName("Gestione Richieste (UC09): Visualizzazione e Accettazione richieste ricevute")
    void testNavigazioneEAccettazioneRichieste() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);

        // 1. Utente 1 (Ricevente): Registrazione, Login e Pubblicazione Annuncio
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        emailUtente1 = "utente1_" + System.currentTimeMillis() + "@example.com";
        registrazionePage.compila("Mario", "Rossi", "user1" + System.currentTimeMillis(), emailUtente1, "01/01/2000", "password123").registrati();
        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
        annunciPage.compila("Lezioni di Inglese", "Offro grammatica base", "Inglese", "Spagnolo").pubblica();

        driver.findElement(By.id("nav-btn-logout")).click();

        // 2. Utente 2 (Mittente): Registrazione, Login e Invio Richiesta
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        String emailUtente2 = "utente2_" + System.currentTimeMillis() + "@example.com";
        registrazionePage.compila("Luigi", "Verdi", "user2" + System.currentTimeMillis(), emailUtente2, "01/01/1995", "password456").registrati();
        registrazionePage.compilaLogin(emailUtente2, "password456").accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        driver.findElement(By.id("nav-btn-proposte")).click();

        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
        assertTrue(bachecaPage.isTabellaVisibile(), "La bacheca dovrebbe essere visibile");
        bachecaPage.cliccaRichiediScambio();

        String alertTesto = bachecaPage.getTestoAlertEAccetta();
        assertTrue(alertTesto.toLowerCase().contains("successo"), "Il messaggio di alert dovrebbe confermare il successo.");

        driver.findElement(By.id("nav-btn-logout")).click();

        // 3. Rientro dell'Utente 1 (Ricevente) per verificare le richieste ricevute
        registrazionePage.apri(BASE_URL);
        WebElement btnVaiLogin = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));
        btnVaiLogin.click();

        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();

        // Click sul pulsante della navbar per le richieste (UC09)
        WebElement navBtnRichieste = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))
        );
        navBtnRichieste.click();

        // Verifica che la schermata si sia caricata correttamente
        WebElement bannerTitolo = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[text()='Richieste Ricevute']"))
        );
        assertNotNull(bannerTitolo, "Il titolo della schermata Richieste Ricevute deve essere visibile");
    }
}