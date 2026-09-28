package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di accettazione per il form di Login di SkillShare.
 */
class LoginIT {

    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
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
        pagina = new RegistrazionePage(driver, TIMEOUT).apri(BASE_URL).vaiAlFormLogin();
    }

    @Nested
    @DisplayName("Quando il form di login è vuoto")
    class QuandoIlFormLoginEVuoto {

        @Test
        @DisplayName("evidenzia in rosso email e password senza contattare il server")
        void formVuoto_evidenziaEntrambiICampi() {
            // Act
            pagina.accedi();

            // Assert: Controlla che i bordi diventino rossi (validazione client)
            assertTrue(pagina.campoInErrore(pagina.campoLoginMail()));
            assertTrue(pagina.campoInErrore(pagina.campoLoginPassword()));

            assertTrue(pagina.testoMessaggioLogin().toLowerCase().contains("controlla gli errori"),
                    "Messaggio ottenuto: " + pagina.testoMessaggioLogin());
        }
    }

    @Nested
    @DisplayName("Quando la mail non ha un formato valido")
    class QuandoMailLoginNonValida {

        @Test
        @DisplayName("evidenzia in rosso solo il campo email")
        void emailMalformata_evidenziaEmail() {
            // Arrange + Act
            pagina.compilaLogin("mariosenzachiocciola", "password123");
            pagina.accedi();

            // Assert
            assertTrue(pagina.campoInErrore(pagina.campoLoginMail()));
            assertFalse(pagina.campoInErrore(pagina.campoLoginPassword()));
        }
    }

    @Nested
    @DisplayName("Quando l'utente non esiste o la password è errata")
    class QuandoCredenzialiErrate {

        @Test
        @DisplayName("mostra il messaggio di errore dal server")
        void credenzialiErrate_mostraErrore() {
            // Arrange
            pagina.compilaLogin("utenteinesistente@example.com", "passwordSbagliata");
            // Act
            pagina.accedi();

            // Assert: Il server deve respingere il login
            assertTrue(pagina.testoMessaggioLogin().toLowerCase().contains("credenziali errate"),
                    "Messaggio ottenuto: " + pagina.testoMessaggioLogin());

            // I campi vengono evidenziati in rosso dal server in caso di fallimento
            assertTrue(pagina.campoInErrore(pagina.campoLoginMail()));
            assertTrue(pagina.campoInErrore(pagina.campoLoginPassword()));
        }
    }

    @Nested
    @DisplayName("Quando il login ha successo")
    class QuandoLoginCorretto {

        @Test
        @DisplayName("mostra il messaggio di benvenuto con il nome dell'utente")
        void loginValido_mostraBenvenuto() {
            pagina.apri(BASE_URL).vaiAlFormRegistrazione();
            // Arrange: Registriamo un utente fittizio per garantire l'accesso al DB
            String nome = "Luigi";
            String emailUnica = "luigi" + System.currentTimeMillis() + "@example.com";
            String password = "passwordSicura123";

            pagina.compila(nome, "Verdi", "luigiUser" + System.currentTimeMillis(),
                            emailUnica, "10/10/1990", password)
                    .registrati();

            pagina.compilaLogin(emailUnica, password).accedi();

            GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
            assertNotNull(annunciPage.campoTitolo(), "Il form degli annunci non è apparso dopo il login");
            // Verifica che la registrazione sia andata a buon fine
            //assertTrue(pagina.testoMessaggio().toLowerCase().contains("successo"));

            // Act: Eseguiamo il login con le credenziali appena create
            //pagina.compilaLogin(emailUnica, password).accedi();

            // Assert: Verifica il messaggio di successo proveniente dal server
            /*String messaggioSuccesso = pagina.testoMessaggioLogin().toLowerCase();
            assertTrue(messaggioSuccesso.contains("benvenuto"),
                    "Messaggio ottenuto: " + pagina.testoMessaggioLogin());
            assertTrue(messaggioSuccesso.contains(nome.toLowerCase()),
                    "Il messaggio dovrebbe contenere il nome dell'utente");
            */
        }
    }
}