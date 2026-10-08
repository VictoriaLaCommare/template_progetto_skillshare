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

    @Test
    @DisplayName("Visualizzazione storico di un utente senza scambi")
    void testStoricoVuoto() {
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);
        
        // 1. Registriamo un nuovo utente (che non ha scambi)
        regPage.apri(BASE_URL).vaiAlFormRegistrazione();
        String username1 = "nuovo" + System.currentTimeMillis();
        String email1 = username1 + "@example.com";

        regPage.compila("Luca", "Neri", username1, email1, "01/01/2000", "passwordSicura123").registrati();
        regPage.compilaLogin(email1, "passwordSicura123").accedi();
        
        // 2. Pubblica un annuncio per farsi vedere in bacheca
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
        annunciPage.compila("Lezioni di Prova", "Descrizione", "Cucina", "Informatica").pubblica();

        // 3. Andiamo in Bacheca e clicchiamo sul nostro stesso nome
        driver.findElement(By.id("nav-btn-proposte")).click();
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
        driver.findElement(By.id("nav-btn-logout")).click();
    }

    @Test
    @DisplayName("Visualizzazione storico di un utente con scambi pregressi")
    void testStoricoConDati() {
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);
        
        // 1. Registriamo Utente A e pubblichiamo un annuncio
        regPage.apri(BASE_URL).vaiAlFormRegistrazione();
        String userA = "off" + System.currentTimeMillis();
        String emailA = userA + "@example.com";
        regPage.compila("Anna", "Gialli", userA, emailA, "01/01/1990", "passwordSicu123").registrati();
        regPage.compilaLogin(emailA, "passwordSicu123").accedi();
        
        // Pubblicazione annuncio e logout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        new GestoreAnnunciPage(driver, TIMEOUT).compila("Annuncio Serio", "Descrizione", "Pittura", "Scultura").pubblica();
        driver.findElement(By.id("nav-btn-logout")).click();

        // 2. Registriamo Utente B
        regPage.apri(BASE_URL).vaiAlFormRegistrazione();
        String userB = "ric" + System.currentTimeMillis();
        String emailB = userB + "@example.com";
        regPage.compila("Marco", "Blu", userB, emailB, "01/01/1995", "passwordsicur456").registrati();
        regPage.compilaLogin(emailB, "passwordsicur456").accedi();

        // 3. Utente B va in bacheca, richiede lo scambio ad Utente A
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        driver.findElement(By.id("nav-btn-proposte")).click();
        
        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
        bachecaPage.cliccaRichiediScambioDi(userA);

        // Assert: Verifica che l'alert confermi il successo della richiesta
        String testoAlert = bachecaPage.getTestoAlertEAccetta();
        assertTrue(testoAlert.toLowerCase().contains("successo"), "La richiesta è fallita. L'alert dice: " + testoAlert);

        // 4. Utente B clicca sul profilo di Utente A
        bachecaPage.cliccaProfiloAutore(userA);

        // 5. Interagiamo con il popup del profilo di Utente A e mostriamo lo storico
        ProfiloUtentePage profiloPage = new ProfiloUtentePage(driver, TIMEOUT);

        profiloPage.cliccaMostraStorico();

        // Assert
        assertTrue(profiloPage.isTabellaStoricoVisibile(), "La tabella dello storico deve essere visibile");
        assertFalse(profiloPage.isMessaggioVuotoVisibile(), "Il messaggio di storico vuoto non deve apparire");

        profiloPage.chiudiProfilo();
        driver.findElement(By.id("nav-btn-logout")).click();
    }
}