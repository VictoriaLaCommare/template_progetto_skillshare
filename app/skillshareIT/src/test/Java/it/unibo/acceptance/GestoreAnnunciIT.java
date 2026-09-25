package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class GestoreAnnunciIT {

    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static WebDriver driver;
    private GestoreAnnunciPage pagina;

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
        //pagina = new GestoreAnnunciPage(driver, TIMEOUT).apri(BASE_URL);
        // 1. Partiamo dalla pagina di Login
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT).apri(BASE_URL).vaiAlFormRegistrazione();

        // 2. Creiamo un utente al volo e facciamo l'accesso per sbloccare la bacheca
        String emailUnica = "test" + System.currentTimeMillis() + "@example.com";
        regPage.compila("Test", "User", "test" + System.currentTimeMillis(), emailUnica, "01/01/2000", "passwordSicura123").registrati();
        
        regPage.compilaLogin(emailUnica, "passwordSicura123").accedi();

        // 3. Ora siamo stati reindirizzati. Inizializziamo la pagina annunci (senza usare .apri() che ci riporterebbe al login!)
        pagina = new GestoreAnnunciPage(driver, TIMEOUT);
        
        // Aspettiamo che la bacheca sia effettivamente visibile prima di far partire i test
        new org.openqa.selenium.support.ui.WebDriverWait(driver, TIMEOUT)
            .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.id("annuncio-submit")));
    }

    @Nested
    @DisplayName("Quando il form è vuoto")
    class QuandoIlFormEVuoto {

        @Test
        @DisplayName("evidenzia in rosso tutti i campi obbligatori")
        void formVuoto_evidenziaTuttiICampi() {
            // Act
            pagina.pubblica();

            // Assert: tutti i campi devono avere il bordo rosso
            assertTrue(pagina.campoInErrore(pagina.campoTitolo()));
            assertTrue(pagina.campoInErrore(pagina.campoDescrizione()));
            assertTrue(pagina.campoInErrore(pagina.campoOfferta()));
            assertTrue(pagina.campoInErrore(pagina.campoRichiesta()));

            assertTrue(pagina.testoMessaggio().toLowerCase().contains("controlla gli errori"),
                    "Messaggio ottenuto: " + pagina.testoMessaggio());
        }
    }

    @Nested
    @DisplayName("Quando un solo campo è vuoto")
    class QuandoUnSoloCampoEVuoto {

        @Test
        @DisplayName("manca il titolo, evidenzia solo il campo titolo")
        void mancaTitolo_evidenziaSoloTitolo() {
            // Arrange + Act: Compilo tutto tranne il titolo
            pagina.compila("", "Una bellissima descrizione", "Java", "Python");
            pagina.pubblica();

            // Assert: solo il titolo è in errore
            assertTrue(pagina.campoInErrore(pagina.campoTitolo()));
            assertFalse(pagina.campoInErrore(pagina.campoDescrizione()));
            assertFalse(pagina.campoInErrore(pagina.campoOfferta()));
            assertFalse(pagina.campoInErrore(pagina.campoRichiesta()));
        }
    }

    @Nested
    @DisplayName("Quando i dati sono tutti validi")
    class QuandoIDatiSonoValidi {

        @Test
        @DisplayName("la pubblicazione va a buon fine e il form viene svuotato")
        void datiValidi_pubblicazioneCompletataConSuccesso() {
            // Arrange
            String titoloTest = "Lezione GWT " + System.currentTimeMillis();

            // Act
            pagina.compila(titoloTest, "Insegno GWT base", "GWT", "Spring Boot");
            pagina.pubblica();

            // Assert
            assertTrue(pagina.testoMessaggio().toLowerCase().contains("successo"),
                    "Messaggio ottenuto: " + pagina.testoMessaggio());
            
            // Verifica che il form sia stato pulito
            assertEquals("", pagina.campoTitolo().getAttribute("value"));
        }
    }

    @Nested
    @DisplayName("Gestione degli Annunci (Modifica ed Eliminazione)")
    class GestioneAnnunci {

        @Test
        @DisplayName("un annuncio pubblicato può essere eliminato correttamente")
        void annuncioPubblicato_puoiEliminarlo() {
            String titoloTest = "Annuncio da Eliminare " + System.currentTimeMillis();
            pagina.compila(titoloTest, "Descrizione per eliminazione", "Java", "Python");
            pagina.pubblica();

            assertTrue(pagina.testoMessaggio().toLowerCase().contains("successo"));
            assertTrue(pagina.isAnnuncioPresente(titoloTest), "L'annuncio deve essere visibile in lista.");

            pagina.eliminaAnnuncio(titoloTest);

            assertFalse(pagina.isAnnuncioPresente(titoloTest), "L'annuncio eliminato non deve più comparire.");
        }

        @Test
        @DisplayName("un annuncio pubblicato può essere modificato correttamente")
        void annuncioPubblicato_puoiModificarlo() {
            String titoloVecchio = "Annuncio Vecchio " + System.currentTimeMillis();
            String titoloNuovo = "Annuncio Modificato " + System.currentTimeMillis();

            pagina.compila(titoloVecchio, "Descrizione vecchia", "GWT", "Spring Boot");
            pagina.pubblica();

            assertTrue(pagina.testoMessaggio().toLowerCase().contains("successo"));
            assertTrue(pagina.isAnnuncioPresente(titoloVecchio));

            pagina.modificaAnnuncio(titoloVecchio, titoloNuovo, "Descrizione nuova e aggiornata");

            assertFalse(pagina.isAnnuncioPresente(titoloVecchio), "Il vecchio titolo non deve più essere visibile.");
            assertTrue(pagina.isAnnuncioPresente(titoloNuovo), "Il nuovo titolo modificato deve comparire in lista.");
        }
    }
}