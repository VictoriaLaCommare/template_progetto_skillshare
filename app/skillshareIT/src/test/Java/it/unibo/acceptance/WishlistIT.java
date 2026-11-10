package it.unibo.acceptance;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

public class WishlistIT {
    public static String BASE_URL = System.getProperty("app.url", "http://localhost:8080/");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static WebDriver driver;

    private final String password = "password123";

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

    // Precondizione del caso d'uso: l'utente è autenticato. Ogni test usa un utente nuovo.
    @BeforeEach
    void registraEAccedi() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String email = "wishlist_" + timestamp + "@example.com";

        RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        registrazionePage.compila("Anna", "Bianchi", "anna" + timestamp, email, "01/01/2000", password).registrati();
        registrazionePage.compilaLogin(email, password).accedi();

        // Dopo il login si apre la bacheca: aspetto che sia pronta
        new WebDriverWait(driver, TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
    }

    @AfterEach
    void logout() {
        try {
            driver.findElement(By.id("nav-btn-logout")).click();
        } catch (Exception ignored) {
            // se il test si è interrotto su una pagina senza navbar, non serve fare altro
        }
    }

    @Test
    @DisplayName("Wishlist (UC14): un utente nuovo ha la wishlist vuota")
    void testWishlistVuotaPerUtenteNuovo() {
        WishlistPage wishlist = new WishlistPage(driver, TIMEOUT);

        wishlist.apriDallaNavbar();

        assertTrue(wishlist.isWishlistVuota(), "Un utente appena registrato non deve avere servizi");
    }

    @Test
    @DisplayName("Wishlist (UC14): sequenza principale, il servizio viene aggiunto e compare nella lista")
    void testAggiuntaServizio() {
        WishlistPage wishlist = new WishlistPage(driver, TIMEOUT);

        wishlist.apriDallaNavbar().vaiAlFormAggiunta();
        wishlist.compila("Javascript", "Programmazione", "Aiuto con le promise").aggiungi();

        assertTrue(wishlist.messaggioContiene("aggiunto"),
            "Il sistema deve notificare il successo");

        // Il servizio deve essere associato al profilo: lo ritrovo riaprendo la lista dal server
        wishlist.apriDallaNavbar();
        List<WebElement> cards = wishlist.getCards();

        assertEquals(1, cards.size(), "La wishlist deve contenere un solo servizio");
        String testo = cards.get(0).getText();
        assertTrue(testo.contains("Javascript"), "La card deve mostrare il servizio");
        assertTrue(testo.contains("Programmazione"), "La card deve mostrare il tag");
        assertTrue(testo.contains("Aiuto con le promise"), "La card deve mostrare la descrizione");
    }

    @Test
    @DisplayName("Wishlist (UC14): sequenza alternativa, campi obbligatori mancanti e poi completati")
    void testCampiObbligatoriMancanti() {
        WishlistPage wishlist = new WishlistPage(driver, TIMEOUT);

        wishlist.apriDallaNavbar().vaiAlFormAggiunta();

        // L'utente conferma omettendo tag e descrizione: il sistema segnala l'errore di validazione
        wishlist.compilaSoloServizio("Inglese").aggiungi();
       assertTrue(wishlist.messaggioContiene("Compila tutti i campi"),
            "Il sistema deve notificare l'errore di validazione");

        // L'utente inserisce i dati mancanti e procede con l'invio
        wishlist.completaCampiMancanti("Lingue", "Conversazione").aggiungi();
        assertTrue(wishlist.messaggioContiene("aggiunto"),
            "Completati i campi, il servizio deve essere aggiunto");

        wishlist.apriDallaNavbar();
        List<WebElement> cards = wishlist.getCards();
        assertEquals(1, cards.size(), "Il tentativo con campi mancanti non deve aver salvato nulla");
        assertTrue(cards.get(0).getText().contains("Inglese"));
    }
}