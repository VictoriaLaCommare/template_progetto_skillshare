package it.unibo.acceptance;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
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
            annunciPage.apriMieiAnnunci();
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

    @Test
    @DisplayName("Richiesta di scambio: dopo la prima richiesta il bottone sparisce e una seconda richiesta non è possibile")
    void testSecondaRichiestaNonPossibile() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);

        String id = String.valueOf(System.currentTimeMillis());
        String usernameAutore = "aut_" + id;
        String emailAutore = usernameAutore + "@example.com";
        String usernameRichiedente = "ric_" + id;
        String emailRichiedente = usernameRichiedente + "@example.com";
        String password = "passwordSicura123";
        String titolo = "Annuncio Doppia Richiesta " + id;

        try {
            // L'utente creato in @BeforeEach non serve a questo test: si esce e si usano due utenti nuovi
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();

            // Autore: si registra, accede e pubblica l'annuncio
            regPage.apri(BASE_URL).vaiAlFormRegistrazione();
            regPage.compila("Mario", "Rossi", usernameAutore, emailAutore, "01/01/2000", password).registrati();
            regPage.compilaLogin(emailAutore, password).accedi();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
            new GestoreAnnunciPage(driver, TIMEOUT)
                    .compila(titolo, "Descrizione Test", "Inglese", "Spagnolo").pubblica();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();

            // Richiedente: si registra, accede e invia la prima richiesta
            regPage.apri(BASE_URL).vaiAlFormRegistrazione();
            regPage.compila("Luigi", "Verdi", usernameRichiedente, emailRichiedente, "01/01/1995", password).registrati();
            regPage.compilaLogin(emailRichiedente, password).accedi();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();

            BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
            assertTrue(bachecaPage.isTabellaVisibile(), "La bacheca dovrebbe essere visibile");

            bachecaPage.cliccaRichiediScambioDi(usernameAutore); // clic mirato: ignora gli altri annunci
            String alertTesto = bachecaPage.getTestoAlertEAccetta();
            assertTrue(alertTesto.toLowerCase().contains("successo"),
                    "La prima richiesta deve partire. Testo ricevuto: " + alertTesto);

            // Il bottone viene sostituito dalla scritta di attesa
            By etichettaAttesa = By.xpath("//*[contains(text(), 'In attesa di accettazione')]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(etichettaAttesa));

            // Esce dalla bacheca e ci rientra: lo stato viene riletto dal server
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Le Mie Richieste']")));
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();

            BachecaPropostePage bachecaRientrata = new BachecaPropostePage(driver, TIMEOUT);
            assertTrue(bachecaRientrata.isTabellaVisibile(), "La bacheca dovrebbe essere visibile");

            wait.until(ExpectedConditions.visibilityOfElementLocated(etichettaAttesa));

            // WHEN: tenta di inviare una seconda richiesta per lo stesso annuncio
            // THEN: il bottone non esiste più e il tentativo va in timeout
            assertThrows(TimeoutException.class,
                    () -> bachecaRientrata.cliccaRichiediScambioDi(usernameAutore),
                    "Il bottone 'Richiedi Scambio' non deve più essere disponibile per questo annuncio");

            // AND: nessun alert, quindi nessuna seconda richiesta è partita
            assertThrows(NoAlertPresentException.class, () -> driver.switchTo().alert(),
                    "Nessun alert atteso: la seconda richiesta non deve essere stata inviata");
        } finally {
            eliminaAnnuncioDi(emailAutore, password, titolo);
        }
    }

    private void eliminaAnnuncioDi(String email, String password, String titolo) {
        try {
            RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);
            regPage.apri(BASE_URL);
            new WebDriverWait(driver, TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
            regPage.compilaLogin(email, password).accedi();

            GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
            annunciPage.apriMieiAnnunci();
            if (annunciPage.isAnnuncioPresente(titolo)) {
                annunciPage.eliminaAnnuncio(titolo);
            }
        } catch (Exception ignored) {
            // Pulizia best effort
        }
    }

    @Test
    @DisplayName("Verifica filtraggio annunci consigliati tramite il pulsante 'Per Te' nei Filtri Avanzati")
    void testFiltraggioAnnunciPerTe() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        RegistrazionePage regPage = new RegistrazionePage(driver, TIMEOUT);

        String id = String.valueOf(System.currentTimeMillis());
        String userMario = "mario_" + id;
        String emailMario = userMario + "@test.com";
        String userLuigi = "luigi_" + id;
        String emailLuigi = userLuigi + "@test.com";
        String userWario = "wario_" + id;
        String emailWario = userWario + "@test.com";
        String password = "passwordSicura123";

        String titoloMario = "Corso Java " + id;
        String titoloLuigi = "Pizza Napoletana " + id;

        try {
            // Logout dall'utente registrato di default nel @BeforeEach
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();

            // 1. Mario pubblica un annuncio con competenza offerta "Informatica"
            regPage.apri(BASE_URL).vaiAlFormRegistrazione();
            regPage.compila("Mario", "Rossi", userMario, emailMario, "01/01/2000", password).registrati();
            regPage.compilaLogin(emailMario, password).accedi();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
            new GestoreAnnunciPage(driver, TIMEOUT)
                    .compila(titoloMario, "Insegno Java base", "Informatica", "Inglese").pubblica();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();

            // 2. Luigi pubblica un annuncio con competenza offerta "Cucina"
            regPage.apri(BASE_URL).vaiAlFormRegistrazione();
            regPage.compila("Luigi", "Verdi", userLuigi, emailLuigi, "01/01/1995", password).registrati();
            regPage.compilaLogin(emailLuigi, password).accedi();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
            new GestoreAnnunciPage(driver, TIMEOUT)
                    .compila(titoloLuigi, "Insegno impasti", "Cucina", "Spagnolo").pubblica();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();

            // 3. Wario accede e aggiunge "Informatica" in Wishlist
            regPage.apri(BASE_URL).vaiAlFormRegistrazione();
            regPage.compila("Wario", "Bros", userWario, emailWario, "01/01/1990", password).registrati();
            regPage.compilaLogin(emailWario, password).accedi();

            WishlistPage wishlistPage = new WishlistPage(driver, TIMEOUT);
            wishlistPage.apriDallaNavbar();
            wishlistPage.vaiAlFormAggiunta();
            wishlistPage.compila("Informatica", "Informatica","Desidero imparare Informatica").aggiungi();
            wishlistPage.messaggioContiene("aggiunto");

            // 4. Navigazione in Bacheca e filtro "Per Te"
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();
            BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
            assertTrue(bachecaPage.isTabellaVisibile(), "La tabella delle proposte dovrebbe essere visibile.");
            bachecaPage.cliccaAnnunciPerTe();

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(text(), '" + titoloLuigi + "')]")));

            // 5. Asserzioni
            assertTrue(bachecaPage.isAnnuncioPresenteInTabella(titoloMario),
                    "L'annuncio di Mario deve apparire nei risultati 'Per Te'.");

            assertFalse(bachecaPage.isAnnuncioPresenteInTabella(titoloLuigi),
                    "L'annuncio di Luigi NON deve apparire nei risultati 'Per Te'.");

        } finally {
            eliminaAnnuncioDi(emailMario, password, titoloMario);
            eliminaAnnuncioDi(emailLuigi, password, titoloLuigi);
        }
    }
}