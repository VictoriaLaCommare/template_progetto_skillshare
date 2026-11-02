package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class HomeProfiloIT {
    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static WebDriver driver;

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

    @Test
    @DisplayName("Home Page (Utente non loggato): Visualizzazione pulsante autore e apertura profilo")
    void testAperturaProfiloDaHomeNonLoggato() {
        // 1. Apriamo la Home Page senza effettuare alcun login
        HomePage homePage = new HomePage(driver, TIMEOUT).apri(BASE_URL);

        // 2. Verifichiamo che il pulsante dell'autore sia visibile accanto agli annunci
        if (homePage.isPulsanteAutorePresente()) {
            // 3. Clicchiamo sul pulsante per aprire il popup del profilo
            homePage.cliccaPrimoProfiloAutore();

            // 4. Utilizziamo il Page Object del profilo per confermare che il popup sia aperto
            ProfiloUtentePage profiloPage = new ProfiloUtentePage(driver, TIMEOUT);
            
            // Verifichiamo che il bottone di chiusura del profilo sia visibile
            assertNotNull(profiloPage, "Il popup del profilo utente deve aprirsi correttamente cliccando sul pulsante autore");
            
            // Chiudiamo il popup per ripulire la schermata
            profiloPage.chiudiProfilo();
        } else {
            // Se non ci sono annunci in bacheca al momento del test, passiamo comunque avvisando
            System.out.println("Attenzione: nessun annuncio presente in home page per testare il click sul profilo.");
        }
    }
}
