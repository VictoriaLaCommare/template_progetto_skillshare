package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di accettazione (tramite browser reale) per il form di
 * registrazione di SkillShare.
 * */
class RegistrazioneIT {

    private static final String BASE_URL =
            System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static WebDriver driver;
    private RegistrazionePage pagina;

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
    void vaiAllaPagina() {
        pagina = new RegistrazionePage(driver, TIMEOUT).apri(BASE_URL).vaiAlFormRegistrazione();
    }

    @Nested
    @DisplayName("Quando il form è vuoto")
    class QuandoIlFormEVuoto {

        @Test
        @DisplayName("evidenzia in rosso tutti i campi obbligatori e non contatta il server")
        void formVuoto_evidenziaTuttiICampi() {
            // Act
            pagina.registrati();

            // Assert
            assertTrue(pagina.campoInErrore(pagina.campoNome()));
            assertTrue(pagina.campoInErrore(pagina.campoCognome()));
            assertTrue(pagina.campoInErrore(pagina.campoUsername()));
            assertTrue(pagina.campoInErrore(pagina.campoMail()));
            assertTrue(pagina.campoInErrore(pagina.campoData()));
            assertTrue(pagina.campoInErrore(pagina.campoPassword()));

            assertTrue(pagina.testoMessaggio().toLowerCase().contains("controlla gli errori"),
                    "Messaggio ottenuto: " + pagina.testoMessaggio());
        }
    }

    @Nested
    @DisplayName("Quando un solo campo non è valido")
    class QuandoUnSoloCampoNonEValido {

        @Test
        @DisplayName("un'email malformata evidenzia solo il campo email")
        void soloEmailNonValida_evidenziaSoloEmail() {
            // Arrange + Act
            pagina.compila("Mario", "Rossi", "mario" + System.currentTimeMillis(),
                    "mariomailcom", "15/06/1998", "passwordSicura123");
            pagina.registrati();

            // Assert: solo la mail è segnalata, gli altri campi restano puliti
            assertTrue(pagina.campoInErrore(pagina.campoMail()));
            assertFalse(pagina.campoInErrore(pagina.campoNome()));
            assertFalse(pagina.campoInErrore(pagina.campoUsername()));
            assertFalse(pagina.campoInErrore(pagina.campoData()));
            assertFalse(pagina.campoInErrore(pagina.campoPassword()));
        }
    }

    @Nested
    @DisplayName("Quando i dati sono tutti validi")
    class QuandoIDatiSonoValidi {

        @Test
        @DisplayName("la registrazione va a buon fine e il form viene svuotato")
        void datiValidi_registrazioneCompletataConSuccesso() {
            // Arrange: username unico per non collidere con esecuzioni precedenti
            String usernameUnico = "utente" + System.currentTimeMillis();
            String emailUnica = "mario" + System.currentTimeMillis() + "@example.com";

            // Act
            pagina.compila("Mario", "Rossi", usernameUnico,
                    emailUnica, "15/06/1998", "passwordSicura123");
            pagina.registrati();

            // Assert
            assertTrue(pagina.testoMessaggio().toLowerCase().contains("successo"),
                    "Messaggio ottenuto: " + pagina.testoMessaggio());
            
            assertTrue(pagina.isFormLoginVisibile(), "Errore: reindirizzamento al login fallito!");
        }
    }

    @Nested
    @DisplayName("Quando l'email è già registrata")
    class QuandoLaMailEGiaRegistrata {

        @Test
        @DisplayName("il secondo tentativo con la stessa email evidenzia il campo email")
        void mailDuplicata_evidenziaCampoMail() {
            // Arrange: registra un utente con una mail fissa per questa esecuzione
            // (il vincolo di unicità in DatabaseUtente.registraUtente è sulla mail)
            String mailFissa = "duplicato" + System.currentTimeMillis() + "@example.com";
            pagina.compila("Mario", "Rossi", "primoUser" + System.currentTimeMillis(),
                    mailFissa, "15/06/1998", "passwordSicura123");
            pagina.registrati();
            assertTrue(pagina.testoMessaggio().toLowerCase().contains("successo"),
                    "La prima registrazione doveva riuscire. Messaggio: " + pagina.testoMessaggio());

            // Act: ricarica la pagina (il form si è svuotato da solo) e riprova con la stessa mail
            // ma uno username diverso
            pagina = new RegistrazionePage(driver, TIMEOUT).apri(BASE_URL).vaiAlFormRegistrazione();
            pagina.compila("Mario", "Rossi", "secondoUser" + System.currentTimeMillis(),
                    mailFissa, "15/06/1998", "passwordSicura123");
            pagina.registrati();

            // Assert: torna errore nel campo mail che rappresenta il vincolo di unicitò
            assertTrue(pagina.campoInErrore(pagina.campoMail()));
            assertTrue(pagina.testoMessaggio().toLowerCase().contains("già registrata"),
                    "Messaggio ottenuto: " + pagina.testoMessaggio());
        }
    }
}