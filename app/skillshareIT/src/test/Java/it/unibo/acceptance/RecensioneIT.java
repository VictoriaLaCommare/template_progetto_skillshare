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

/**
 Test di accettazione del front end per l'UC 13 sulle Recensioni.
 Ruoli:

 */
public class RecensioneIT {
    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final String PWD_U1 = "password123";
    private static final String PWD_U2 = "password456";
    private static WebDriver driver;

    private WebDriverWait wait;
    private RegistrazionePage regPage;
    //U1 =ricevente U2 = richiedente
    private String usernameU1;
    private String emailU1;
    private String usernameU2;
    private String emailU2;
    private String titoloAnnuncio;

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
    void preparaDatiTest() {
        wait = new WebDriverWait(driver, TIMEOUT);
        regPage = new RegistrazionePage(driver, TIMEOUT);

        // Suffisso unico: utenti e annuncio non si scontrano con quelli degli altri test
        String id = String.valueOf(System.currentTimeMillis());
        usernameU1 = "rec1_" + id;
        emailU1 = usernameU1 + "@example.com";
        usernameU2 = "rec2_" + id;
        emailU2 = usernameU2 + "@example.com";
        titoloAnnuncio = "Annuncio Recensioni " + id;
    }

    // Elimina l'annuncio di U1: gli altri IT cliccano il primo "Richiedi Scambio" della bacheca
    // e non devono trovarsi un annuncio lasciato da questa classe.
    @AfterEach
    void pulizia() {
        try {
            if (emailU1 != null && titoloAnnuncio != null) {
                regPage.apri(BASE_URL);
                wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
                regPage.compilaLogin(emailU1, PWD_U1).accedi();

                GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
                if (annunciPage.isAnnuncioPresente(titoloAnnuncio)) {
                    annunciPage.eliminaAnnuncio(titoloAnnuncio);
                }

                WebElement btnLogout = driver.findElement(By.id("nav-btn-logout"));
                if (btnLogout.isDisplayed()) {
                    btnLogout.click();
                }
            }
        } catch (Exception ignored) {
            // Se il test si è interrotto prima, la pulizia è best effort
        }
    }

    // Precondizione: la recensione è disponibile solo a scambio completato, a entrambi
    @Test
    @DisplayName("Precondizione: il bottone recensione compare solo a scambio completato, per entrambi gli utenti")
    void bottoneRecensioneSoloACompletamento_perEntrambiGliUtenti() {
        creaRichiestaInviata();

        // U1 accetta: scambio in corso, nessuna recensione possibile
        accedi(emailU1, PWD_U1);
        GestisciRichiestePage richieste = apriRichieste();
        richieste.accettaPrimaRichiesta();
        richieste.getTestoAlertEAccetta();

        // L'attesa di btn-termina garantisce che la lista sia stata ricaricata
        assertTrue(richieste.isBottoneTerminaVisibile(), "Dopo l'accettazione deve comparire 'Dichiara Terminazione'");
        assertTrue(richieste.isBottoneRecensioneAssente(), "A scambio in corso non si può ancora recensire");

        // Prima conferma di U1: scambio ancora non completato
        richieste.dichiaraPrimaTerminazione();
        richieste.getTestoAlertEAccetta();
        assertTrue(richieste.isMessaggioAttesaVisibile(), "Dopo la prima conferma deve comparire l'attesa della controparte");
        assertTrue(richieste.isBottoneRecensioneAssente(), "Con una sola conferma non si può ancora recensire");
        logout();

        // Seconda conferma di U2: scambio Completato
        accedi(emailU2, PWD_U2);
        richieste = apriRichieste();
        richieste.dichiaraPrimaTerminazione();
        richieste.getTestoAlertEAccetta();
        assertTrue(richieste.isBottoneRecensioneVisibile(), "U2 (richiedente) deve poter lasciare la recensione");
        logout();

        // Anche U1 (ricevente) deve vedere il bottone
        accedi(emailU1, PWD_U1);
        richieste = apriRichieste();
        assertTrue(richieste.isBottoneRecensioneVisibile(), "U1 (ricevente) deve poter lasciare la recensione");
    }


    @Test
    @DisplayName("Scenario 1: recensione inviata con successo e profilo dell'utente valutato aggiornato")
    void scenario1_invioRecensione_profiloAggiornato() {
        // GIVEN: scambio completato, U2 loggato sulla pagina richieste
        creaScambioCompletatoComeU2();

        // Stato iniziale del profilo di U1: uno scambio, nessuna recensione
        ProfiloUtentePage prima = apriProfiloDaBacheca(usernameU1);
        assertEquals(1, prima.getScambiCompletati(), "Lo scambio completato deve essere già conteggiato");
        assertEquals(0.0, prima.getMediaRecensioni(), 0.01, "Prima della recensione la media deve essere 0");
        assertEquals("Novizio", prima.getLivello());
        assertFalse(prima.haTag("Eccellente"), "Prima della recensione non ci sono tag di valutazione");
        prima.chiudiProfilo();

        GestisciRichiestePage richieste = apriRichieste();
        assertTrue(richieste.isBottoneRecensioneVisibile(), "Deve esserci il bottone 'Lascia Recensione'");
        richieste.cliccaLasciaRecensione();
        assertTrue(richieste.isFormRecensioneVisibile(), "Il click deve aprire il form di recensione");

        // Invio senza stelle: bloccato, il form resta aperto
        richieste.inviaRecensione();
        String alertSenzaStelle = richieste.getTestoAlertEAccetta();
        assertTrue(alertSenzaStelle.toLowerCase().contains("stella"),
                "Senza stelle l'invio va bloccato. Testo ricevuto: " + alertSenzaStelle);
        assertTrue(richieste.isFormRecensioneVisibile(), "Dopo il blocco il form deve restare aperto");

        // WHEN: 4 stelle, commento, invio
        richieste.selezionaStelle(4);
        assertEquals(4, richieste.getStelleSelezionate(), "Devono risultare piene esattamente 4 stelle");
        richieste.scriviCommento("Lezione chiara e puntuale.");
        richieste.inviaRecensione();

        // THEN: il sistema conferma il salvataggio
        String esito = richieste.getTestoAlertEAccetta();
        assertTrue(esito.toLowerCase().contains("successo"),
                "Il sistema deve confermare il salvataggio. Testo ricevuto: " + esito);

        // AND: il profilo di U1 si aggiorna con la nuova valutazione
        ProfiloUtentePage dopo = apriProfiloDaBacheca(usernameU1);
        assertEquals(1, dopo.getScambiCompletati(), "Gli scambi completati non devono cambiare");
        assertEquals(4.0, dopo.getMediaRecensioni(), 0.01, "La media deve riflettere le 4 stelle");
        assertEquals("Novizio", dopo.getLivello());
        assertTrue(dopo.haTag("Eccellente"), "Con media 4 deve comparire il tag 'Eccellente'");
        dopo.chiudiProfilo();
    }


    @Test
    @DisplayName("Scenario 2: interruzione della recensione, form azzerato e profilo invariato")
    void scenario2_interruzione_profiloInvariato() {
        // GIVEN: scambio completato e form di recensione aperto con dati inseriti
        GestisciRichiestePage richieste = creaScambioCompletatoComeU2();
        richieste.cliccaLasciaRecensione();
        assertTrue(richieste.isFormRecensioneVisibile(), "Il form deve aprirsi");
        richieste.selezionaStelle(2);
        richieste.scriviCommento("Meglio non inviare.");
        assertEquals(2, richieste.getStelleSelezionate());

        // WHEN: l'utente esce richiudendo il form
        richieste.cliccaLasciaRecensione();

        // THEN: form chiuso e svuotato
        assertTrue(richieste.isFormRecensioneChiuso(), "Il form deve chiudersi");
        assertEquals(0, richieste.getStelleSelezionate(), "Le stelle devono tornare vuote");
        assertEquals("", richieste.getCommento(), "Il commento deve essere svuotato");

        // Riaprendo, il form riparte pulito
        richieste.cliccaLasciaRecensione();
        assertTrue(richieste.isFormRecensioneVisibile());
        assertEquals(0, richieste.getStelleSelezionate(), "Riaprendo, nessuna stella deve essere selezionata");
        assertEquals("", richieste.getCommento(), "Riaprendo, il commento deve essere vuoto");

        // AND: il profilo di U1 non è cambiato
        ProfiloUtentePage profilo = apriProfiloDaBacheca(usernameU1);
        assertEquals(1, profilo.getScambiCompletati());
        assertEquals(0.0, profilo.getMediaRecensioni(), 0.01, "Nessuna recensione registrata: media ancora 0");
        assertFalse(profilo.haTag("Eccellente"), "Nessun tag di valutazione");
        assertFalse(profilo.haTag("Ben Valutato"), "Nessun tag di valutazione");
        assertFalse(profilo.haTag("Leggendario"), "Nessun tag di valutazione");
        profilo.chiudiProfilo();
    }

    // Helper di setup

    /** Registra l'utente, fa il login e attende la schermata iniziale. */
    private void registraEAccedi(String nome, String cognome, String username,
                                 String email, String dataNascita, String password) {
        regPage.apri(BASE_URL).vaiAlFormRegistrazione();
        regPage.compila(nome, cognome, username, email, dataNascita, password).registrati();
        regPage.compilaLogin(email, password).accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
    }

    /** Login di un utente già registrato (riparte dalla pagina iniziale). */
    private void accedi(String email, String password) {
        regPage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        regPage.compilaLogin(email, password).accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste")));
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-logout"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));
    }

    private GestisciRichiestePage apriRichieste() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Le Mie Richieste']")));
        return new GestisciRichiestePage(driver, TIMEOUT);
    }

    /** U1 pubblica un annuncio, U2 gli invia una richiesta. Si conclude con tutti gli utenti sloggati. */
    private void creaRichiestaInviata() {
        registraEAccedi("Mario", "Rossi", usernameU1, emailU1, "01/01/2000", PWD_U1);
        new GestoreAnnunciPage(driver, TIMEOUT)
                .compila(titoloAnnuncio, "Offro grammatica base", "Inglese", "Spagnolo").pubblica();
        logout();

        registraEAccedi("Luigi", "Verdi", usernameU2, emailU2, "01/01/1995", PWD_U2);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();
        BachecaPropostePage bacheca = new BachecaPropostePage(driver, TIMEOUT);
        assertTrue(bacheca.isTabellaVisibile(), "La bacheca dovrebbe essere visibile");
        bacheca.cliccaRichiediScambioDi(usernameU1); // clic mirato: ignora gli annunci degli altri test
        String alert = bacheca.getTestoAlertEAccetta();
        assertTrue(alert.toLowerCase().contains("successo"), "La richiesta deve partire. Testo: " + alert);
        logout();
    }

    /**
     Porta lo scambio fino a Completato: U1 accetta e conferma, U2 conferma.
     Si conclude con U2 loggato sulla pagina richieste, dove la card è già "Completato".
     */
    private GestisciRichiestePage creaScambioCompletatoComeU2() {
        creaRichiestaInviata();

        accedi(emailU1, PWD_U1);
        GestisciRichiestePage richiesteU1 = apriRichieste();
        richiesteU1.accettaPrimaRichiesta();
        richiesteU1.getTestoAlertEAccetta();
        richiesteU1.dichiaraPrimaTerminazione();
        richiesteU1.getTestoAlertEAccetta();
        logout();

        accedi(emailU2, PWD_U2);
        GestisciRichiestePage richiesteU2 = apriRichieste();
        richiesteU2.dichiaraPrimaTerminazione();
        richiesteU2.getTestoAlertEAccetta();
        return richiesteU2;
    }

    /** Apre dalla bacheca il popup del profilo e attende le statistiche (livello, scambi, media, tag). */
    private ProfiloUtentePage apriProfiloDaBacheca(String username) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte"))).click();
        new BachecaPropostePage(driver, TIMEOUT).cliccaProfiloAutore(username);
        return new ProfiloUtentePage(driver, TIMEOUT).attendiStatistiche();
    }
}
