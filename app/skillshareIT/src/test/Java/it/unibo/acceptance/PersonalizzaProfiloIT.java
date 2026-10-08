package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class PersonalizzaProfiloIT {

    private static final String BASE_URL =
            System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static WebDriver driver;
    private PersonalizzaProfiloPage pagina;

    @BeforeAll
    public static void avviaBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @AfterAll
    public static void chiudiBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void vaiAllaPagina() {
        pagina = new PersonalizzaProfiloPage(driver, TIMEOUT).apri(BASE_URL);
    }

    @AfterEach
    void puliziaAnnunci() {
        try {
            // Se un test viene riattivato e richiede il login, effettuiamo il logout
            WebElement btnLogout = driver.findElement(By.id("nav-btn-logout"));
            if (btnLogout != null && btnLogout.isDisplayed()) {
                btnLogout.click();
            }
        } catch (Exception ignored) {
            // Viene ignorato se il bottone di logout non è presente nella pagina corrente
        }
    }

    //@Test
    public void datiValidi_aggiornamentoCompletatoConSuccesso() {
        pagina.compila("Nel tempo libero creo capi all'uncinetto", 
                       "https://via.placeholder.com/150", 
                       "Uncinetto, Maglia, Ricamo");
        pagina.salva();

        assertTrue(pagina.testoMessaggio().toLowerCase().contains("successo"),
                "Messaggio ottenuto: " + pagina.testoMessaggio());
    }

    //@Test
    public void bioNonValida_evidenziaErrore() {
        // Creiamo una bio esageratamente lunga (es. più di 250/500 caratteri) per far scattare l'errore di validazione
        String bioTroppoLunga = "a".repeat(1000); 
        
        pagina.compila(bioTroppoLunga, "https://via.placeholder.com/150", "Cucito");
        pagina.salva();

        assertTrue(pagina.testoMessaggio().toLowerCase().contains("controlla gli errori") ||
                   pagina.testoMessaggio().toLowerCase().contains("non è valida"),
                "Messaggio ottenuto: " + pagina.testoMessaggio());
    }
}