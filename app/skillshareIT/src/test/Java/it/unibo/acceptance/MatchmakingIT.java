package it.unibo.acceptance;

import java.time.Duration;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

public class MatchmakingIT {
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

    @BeforeEach
    void registraEAccedi() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String email = "match_" + timestamp + "@example.com";
        RegistrazionePage registrazionePage = new RegistrazionePage(driver, TIMEOUT);
        registrazionePage.apri(BASE_URL).vaiAlFormRegistrazione();
        registrazionePage.compila("Mario", "Rossi", "mario" + timestamp, email, "01/01/2000", password).registrati();
        registrazionePage.compilaLogin(email, password).accedi();

        new WebDriverWait(driver, TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
    }

    @AfterEach
    void logout() {
        try {
            driver.findElement(By.id("nav-btn-logout")).click();
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Matchmaking: toggle salvataggio e rimozione (deselezione) profilo suggerito")
    void testToggleSalvataggioERimozioneProfilo() {
        // 1. Aggiungiamo un servizio in wishlist con un tag
        WishlistPage wishlist = new WishlistPage(driver, TIMEOUT);
        wishlist.apriDallaNavbar().vaiAlFormAggiunta();
        wishlist.compila("Corso Java", "Programmazione", "Studio avanzato").aggiungi();
        wishlist.messaggioContiene("aggiunto");

        // 2. Riapriamo la wishlist per visualizzare il widget del matchmaking
        wishlist.apriDallaNavbar();

        MatchmakingPage matchmaking = new MatchmakingPage(driver, TIMEOUT);
        matchmaking.attendiSuggerimentiVisibili();

        // 3. Verificiamo il comportamento in base alla presenza di profili nel sistema
        if (!matchmaking.getBottoniProfilo().isEmpty()) {
            // Test 1: Click per salvare il profilo
            matchmaking.cliccaPrimoBottoneProfilo();
            assertTrue(matchmaking.isPrimoProfiloSalvato(), "Il bottone deve passare allo stato 'Salvato'");

            // Test 2: Secondo click per rimuovere / deselezionare il profilo
            matchmaking.cliccaPrimoBottoneProfilo();
            assertTrue(matchmaking.isPrimoProfiloDaSalvare(), "Il bottone deve tornare allo stato iniziale 'Salva Profilo' dopo la rimozione");
        } else {
            assertTrue(matchmaking.isMessaggioNessunProfiloVisibile(), "Se non ci sono utenti registrati, deve apparire il messaggio di assenza risultati");
        }
    }
}