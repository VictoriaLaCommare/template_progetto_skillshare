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

public class BachecaProposteIT {
    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static WebDriver driver;

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
    void vaiAllaPagina() {
        // 1. Partiamo dalla pagina di Login
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT).apri(BASE_URL).vaiAlFormRegistrazione();
        String emailUnica = "bacheca" + System.currentTimeMillis() + "@example.com";

        regPage.compila("Test", "Bacheca", "user" + System.currentTimeMillis(), emailUnica, "01/01/2000", "passwordSicura123").registrati();
        regPage.compilaLogin(emailUnica, "passwordSicura123").accedi();

        // 2. Aspettiamo di essere reindirizzati alla bacheca dopo la registrazione e il login
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte")));
    }

    @AfterEach
    void puliziaAnnunci() {
        try {
            // 1. Rimozione dell'annuncio creato durante il test
            GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
            if (annunciPage.isAnnuncioPresente("Annuncio di Test Selenium")) {
                annunciPage.eliminaAnnuncio("Annuncio di Test Selenium");
            }
            // 2. Logout
            WebElement btnLogout = driver.findElement(By.id("nav-btn-logout"));
            if (btnLogout != null && btnLogout.isDisplayed()) {
                btnLogout.click();
            }
        } catch (Exception ignored) {
            // Gestisce in sicurezza eventuali eccezioni se il test fallisce prima di raggiungere la pagina annunci
        }
    }

    @Test
    @DisplayName("Pubblicazione di un annuncio e visualizzazione in Bacheca")
    void testVisualizzazioneProposte(){
        GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
        // 1. Compiliamo e pubblichiamo un annuncio
        annunciPage.compila("Annuncio di Test Selenium", "Descrizione Test", "Offerta Test", "Richiesta Test").pubblica();

        // 2. Clicchiamo sul bottone per andare alla Bacheca Proposte
        driver.findElement(By.id("nav-btn-proposte")).click();

        // Inizializziamo la pagina BachecaPropostePage
        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);

        // 3. Verifichiamo che la tabella delle proposte sia visibile
        assertTrue(bachecaPage.isTabellaVisibile(), "La tabella delle proposte dovrebbe essere visibile dopo la pubblicazione di un annuncio.");
        assertFalse(bachecaPage.isMessaggioVuotoVisibile(), "Il messaggio 'nessuna proposta' non dovrebbe essere visibile dopo la pubblicazione di un annuncio.");

    }
}
