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

public class GestoreRichiesteIT {
    public static String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static WebDriver driver;

    private String emailUtente1;

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
            driver = null;
        }
    }

    @AfterEach
    void puliziaAnnunci() {
        try {
            if (emailUtente1 != null && driver != null) {
                RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);
                registrazionePage.apri(BASE_URL);
                WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

                WebElement btnVaiLogin = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));
                btnVaiLogin.click();

                registrazionePage.compilaLogin(emailUtente1, "password123").accedi();

                GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
                if (annunciPage.isAnnuncioPresente("Lezioni di Inglese")) {
                    annunciPage.eliminaAnnuncio("Lezioni di Inglese");
                }

                WebElement btnLogout = driver.findElement(By.id("nav-btn-logout"));
                if (btnLogout != null && btnLogout.isDisplayed()) {
                    btnLogout.click();
                }
            }
        } catch (Exception ignored) {
        }
    }

    @BeforeEach
    void assicuraDriverValido() {
        // Controllo di sicurezza: se per qualsiasi motivo la sessione è terminata viene riavviata
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))){
                options.addArguments("--headless=new");
            }
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
        }
    }


    @Test
    @DisplayName("Gestione Richieste (UC09): Visualizzazione e Accettazione richieste ricevute")
    void testNavigazioneEAccettazioneRichieste() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);

        // 1 Utente 1 (Ricevente): Registrazione, Login e Pubblicazione Annuncio
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        emailUtente1 = "utente1_" + System.currentTimeMillis() + "@example.com";
        registrazionePage.compila("Mario", "Rossi", "user1" + System.currentTimeMillis(), emailUtente1, "01/01/2000", "password123").registrati();
        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        GestoreAnnunciPage annunciPage = new GestoreAnnunciPage(driver, TIMEOUT);
        annunciPage.compila("Lezioni di Inglese", "Offro grammatica base", "Inglese", "Spagnolo").pubblica();

        driver.findElement(By.id("nav-btn-logout")).click();

        // 2 Utente 2 (Richiedente): Registrazione, Login e Invio Richiesta
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        String emailUtente2 = "utente2_" + System.currentTimeMillis() + "@example.com";
        registrazionePage.compila("Luigi", "Verdi", "user2" + System.currentTimeMillis(), emailUtente2, "01/01/1995", "password456").registrati();
        registrazionePage.compilaLogin(emailUtente2, "password456").accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        driver.findElement(By.id("nav-btn-proposte")).click();

        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
        assertTrue(bachecaPage.isTabellaVisibile(), "La bacheca dovrebbe essere visibile");
        bachecaPage.cliccaRichiediScambio();

        String alertTesto = bachecaPage.getTestoAlertEAccetta();
        assertTrue(alertTesto.toLowerCase().contains("successo"), "Il messaggio di alert dovrebbe confermare il successo.");

        driver.findElement(By.id("nav-btn-logout")).click();

        // 3 Rientro dell'Utente 1 (Ricevente) per verificare le richieste ricevute
        registrazionePage.apri(BASE_URL);
        WebElement btnVaiLogin = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login")));
        btnVaiLogin.click();

        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();

        WebElement navBtnRichieste = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))
        );
        navBtnRichieste.click();

        WebElement bannerTitolo = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[text()='Le Mie Richieste']"))
        );
        assertNotNull(bannerTitolo, "Il titolo della schermata Le Mie Richieste deve essere visibile");
    }

    // Scenario 2 – Attesa di conferma dalla controparte
    @Test
    @DisplayName("Scenario 2: Prima conferma: sistema registra e mostra attesa della controparte")
    void testTA2_AttesaConfermaControparte() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);

        //Setup: User1 (ricevente/autore) crea annuncio
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        emailUtente1 = "u1ta2_" + System.currentTimeMillis() + "@example.com";
        registrazionePage
                .compila("Mario", "Rossi", "u1ta2_" + System.currentTimeMillis(), emailUtente1, "01/01/2000", "password123")
                .registrati();
        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        new GestoreAnnunciPage(driver, TIMEOUT)
                .compila("Lezioni di Inglese", "Offro grammatica base", "Inglese", "Spagnolo").pubblica();
        driver.findElement(By.id("nav-btn-logout")).click();

        //Setup: User2 (richiedente) invia richiesta
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        String emailUtente2 = "u2ta2_" + System.currentTimeMillis() + "@example.com";
        registrazionePage
                .compila("Luigi", "Verdi", "u2ta2_" + System.currentTimeMillis(), emailUtente2, "01/01/1995", "password456")
                .registrati();
        registrazionePage.compilaLogin(emailUtente2, "password456").accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte")));
        driver.findElement(By.id("nav-btn-proposte")).click();
        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
        bachecaPage.cliccaRichiediScambio();
        bachecaPage.getTestoAlertEAccetta();
        driver.findElement(By.id("nav-btn-logout")).click();

        // User1 accetta la richiesta
        registrazionePage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))).click();
        GestisciRichiestePage richiestePage = new GestisciRichiestePage(driver, TIMEOUT);
        richiestePage.accettaPrimaRichiesta();
        richiestePage.getTestoAlertEAccetta();

        //WHEN: User1 ricevente dichiara terminazione prima conferma
        richiestePage.dichiaraPrimaTerminazione();
        String alertTerminazione = richiestePage.getTestoAlertEAccetta();

        //THEN: il sistema registra la singola conferma
        assertTrue(
                alertTerminazione.toLowerCase().contains("terminazione") ||
                        alertTerminazione.toLowerCase().contains("successo"),
                "L'alert deve confermare la registrazione della terminazione"
        );

        // User1 non vede più il pulsante poiché ha già confermato
        assertFalse(richiestePage.isBottoneTerminaVisibile(),
                "Dopo la propria conferma User1 non deve vedere btn-termina");

        //AND: il sistema mostra l'attesa della controparte (stato non è Completato)
        assertTrue(richiestePage.isMessaggioAttesaVisibile(),
                "Il sistema deve mostrare il messaggio di attesa della controparte");
    }

    // Scenario 1 – Chiusura definitiva dello scambio
    @Test
    @DisplayName(" Chiusura definitiva: entrambe le conferme portano a Completato")
    void ChiusuraDefinitiva() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);

        //Setup: User1 (ricevente/autore) crea annuncio
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        emailUtente1 = "u1ta1_" + System.currentTimeMillis() + "@example.com";
        String usernameU1 = "u1ta1_" + System.currentTimeMillis();
        registrazionePage
                .compila("Mario", "Rossi", usernameU1, emailUtente1, "01/01/2000", "password123")
                .registrati();
        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        new GestoreAnnunciPage(driver, TIMEOUT)
                .compila("Lezioni di Inglese", "Offro grammatica base", "Inglese", "Spagnolo").pubblica();
        driver.findElement(By.id("nav-btn-logout")).click();

        //Setup: User2 (richiedente) invia richiesta
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        String emailUtente2 = "u2ta1_" + System.currentTimeMillis() + "@example.com";
        registrazionePage
                .compila("Luigi", "Verdi", "u2ta1_" + System.currentTimeMillis(), emailUtente2, "01/01/1995", "password456")
                .registrati();
        registrazionePage.compilaLogin(emailUtente2, "password456").accedi();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-proposte")));
        driver.findElement(By.id("nav-btn-proposte")).click();
        BachecaPropostePage bachecaPage = new BachecaPropostePage(driver, TIMEOUT);
        bachecaPage.cliccaRichiediScambioDi(usernameU1);   // così evita gli altri annunci
        bachecaPage.getTestoAlertEAccetta();
        driver.findElement(By.id("nav-btn-logout")).click();

        //User1 accetta e dichiara terminazione
        registrazionePage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        registrazionePage.compilaLogin(emailUtente1, "password123").accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))).click();
        GestisciRichiestePage richiestePage = new GestisciRichiestePage(driver, TIMEOUT);
        richiestePage.accettaPrimaRichiesta();
        richiestePage.getTestoAlertEAccetta();       // dismiss alert accettazione
        richiestePage.dichiaraPrimaTerminazione();
        richiestePage.getTestoAlertEAccetta();       // dismiss alert prima conferma
        driver.findElement(By.id("nav-btn-logout")).click();

        //WHEN: User2 richiedente accede alle proprie richieste e dichiara terminazione
        registrazionePage.apri(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-login"))).click();
        registrazionePage.compilaLogin(emailUtente2, "password456").accedi();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-richieste"))).click();

        //User2 vede il btn-termina ma non ha ancora confermato
        assertTrue(richiestePage.isBottoneTerminaVisibile(),
                "User2 deve vedere il pulsante di terminazione per la richiesta in attesa");

        richiestePage.dichiaraPrimaTerminazione();
        String alertCompletamento = richiestePage.getTestoAlertEAccetta();

        //THEN: il sistema registra la seconda conferma con successo
        assertTrue(
                alertCompletamento.toLowerCase().contains("successo") ||
                        alertCompletamento.toLowerCase().contains("terminazione"),
                "L'alert deve confermare la registrazione della seconda terminazione"
        );

        //nessun btn-termina rimane visibile
        assertFalse(richiestePage.isBottoneTerminaVisibile(),
                "Dopo il completamento non devono esserci pulsanti di terminazione attivi");

    }

}