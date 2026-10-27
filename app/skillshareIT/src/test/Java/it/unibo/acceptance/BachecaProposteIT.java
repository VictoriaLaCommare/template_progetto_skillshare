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
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Richieste Ricevute']")));
            wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();

            BachecaPropostePage bachecaRientrata = new BachecaPropostePage(driver, TIMEOUT);
            assertTrue(bachecaRientrata.isTabellaVisibile(), "La bacheca dovrebbe essere visibile");

            // La tabella viene disegnata con il bottone e sistemata solo quando arriva la risposta del server.
            // Si aspetta la scritta prima di tentare il clic: da lì lo stato è definitivo, altrimenti
            // il test potrebbe cliccare il bottone nell'attimo in cui esiste ancora e inviare una seconda richiesta.
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

    // Elimina l'annuncio con l'utente che lo ha pubblicato: gli altri IT cliccano il primo
    // "Richiedi Scambio" della bacheca e non devono trovarsi annunci lasciati da questo test
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
            // Pulizia best effort: se il test si è interrotto la sessione può essere in qualsiasi stato
        }
    }
}