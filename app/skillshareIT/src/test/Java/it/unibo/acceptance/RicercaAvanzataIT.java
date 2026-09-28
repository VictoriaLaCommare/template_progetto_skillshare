package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RicercaAvanzataIT {

    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static WebDriver driver;
    private GestoreAnnunciPage paginaAnnunci; 
    private List<String> annunciDaPulire;

    @BeforeAll
    static void avviaBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))) {
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
    void setupEAccesso() {
        annunciDaPulire = new ArrayList<>();
        
        // 1. Partiamo dalla pagina di Login
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT).apri(BASE_URL).vaiAlFormRegistrazione();

        // 2. Registrazione e accesso al volo
        String emailUnica = "test_ricerca_" + System.currentTimeMillis() + "@example.com";
        regPage.compila("Test", "Filtri", "user" + System.currentTimeMillis(), emailUnica, "01/01/2000", "passwordSicura123").registrati();
        regPage.compilaLogin(emailUnica, "passwordSicura123").accedi();

        paginaAnnunci = new GestoreAnnunciPage(driver, TIMEOUT);

        // 3. Ci fermiamo qui: la dashboard principale è pronta e visibile
        new WebDriverWait(driver, TIMEOUT)
            .until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
    }

    @Test
    @DisplayName("La ricerca avanzata per competenza offerta filtra correttamente gli annunci")
    void ricercaAvanzata_filtraPerCompetenzaOfferta() {
        String titoloJava = "Lezione Java " + System.currentTimeMillis();
        String titoloPython = "Lezione Python " + System.currentTimeMillis();
        
        annunciDaPulire.add(titoloJava);
        annunciDaPulire.add(titoloPython);

        // 1. Pubblichiamo gli annunci dalla schermata principale (dove il form è attivo)
        paginaAnnunci.compila(titoloJava, "Offro Java", "Java", "C++");
        paginaAnnunci.pubblica();

        paginaAnnunci.compila(titoloPython, "Offro Python", "Python", "Ruby");
        paginaAnnunci.pubblica();

        // 2. Ora ci spostiamo nella bacheca delle proposte
        new WebDriverWait(driver, TIMEOUT)
            .until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte")))
            .click();

        // 3. Apriamo i filtri avanzati
        new WebDriverWait(driver, TIMEOUT)
            .until(ExpectedConditions.elementToBeClickable(By.id("btn-filtri")))
            .click();

        // 4. Compiliamo i filtri di ricerca e cerchiamo
        new WebDriverWait(driver, TIMEOUT)
            .until(ExpectedConditions.visibilityOfElementLocated(By.id("tB-Offerta")));
            
        paginaAnnunci.compilaFiltri("Java", "", ""); 
        paginaAnnunci.cliccaCercaAvanzata();

        // 5. Verifiche finali
        assertTrue(paginaAnnunci.isAnnuncioPresente(titoloJava), "L'annuncio Java DEVE essere visibile.");
        assertFalse(paginaAnnunci.isAnnuncioPresente(titoloPython), "L'annuncio Python NON DEVE essere visibile.");
    }
}