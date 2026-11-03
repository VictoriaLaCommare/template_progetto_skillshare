package it.unibo;

import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

public class AutenticatoreProxyTest {

    private AutenticatoreProxy proxy;
    private Utente utenteValido;
    private Utente utenteNonValido;

    @BeforeAll
    public static void testmode() {
        DatabaseCore.enableTestMode();
    }

    @BeforeEach
    public void setUp() {
        DatabaseUtente.resetPerTest();
        GestorePassword passwordManager = new PasswordManager();
        GestoreAutenticazione autenticatoreReale = new AutenticatoreReale(passwordManager);
        proxy = new AutenticatoreProxy(autenticatoreReale);

        // utente valido
        utenteValido = new Utente("Mario", "Rossi");
        utenteValido.setUsername("mario123" + + System.currentTimeMillis());
        utenteValido.setMail("mario@mail.com" + System.currentTimeMillis());
        utenteValido.setPassword("passwordSicura123");
        utenteValido.setData(new Date());

        // utente non valido
        utenteNonValido = new Utente("L", "Ver");
        utenteNonValido.setUsername("");
        utenteNonValido.setMail("mariomailcom");
        utenteNonValido.setPassword("pas23");
        utenteNonValido.setData(new GregorianCalendar(1800, Calendar.DECEMBER, 31).getTime());
    }



    @Test
    public void registraUtente_datiValidi_deveTornareTrue() throws Exception {
        // Eseguiamo la registrazione (salverà nella RAM)
        Boolean risultato = proxy.registraUtente(utenteValido);
        assertTrue(risultato);
    }

    @Test
    public void registraUtente_nomeTroppoCorto_deveLanciareEccezione() {
        utenteNonValido.setNome("Ma"); // Non valido!
        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class,
                () -> proxy.registraUtente(utenteNonValido));
        assertTrue(eccezione.getMessage().contains("Nome non valido"));
    }

    @Test
    public void registraUtente_passwordMancante_deveLanciareEccezione() {
        utenteNonValido.setPassword("");
        assertThrows(IllegalArgumentException.class,
                () -> proxy.registraUtente(utenteNonValido));
    }
    // TEST LOGIN

    @Test
    @DisplayName("Login con credenziali corrette restituisce l'utente")
    public void effettuaLogin_credenzialiCorrette_deveTornareUtente() throws Exception {
        // Arrange: Registriamo l'utente nel sistema
        utenteValido.setMail("login" + System.currentTimeMillis() + "@mail.com");

        proxy.registraUtente(utenteValido);

        // Act: Tentiamo il login
        Utente utenteLoggato = proxy.effettuaLogin(utenteValido.getMail(), utenteValido.getPassword());

        // Assert: L'utente restituito non deve essere null e la mail deve coincidere
        assertNotNull(utenteLoggato, "L'utente loggato non dovrebbe essere null con credenziali corrette");
        assertEquals(utenteValido.getMail(), utenteLoggato.getMail());
    }

    @Test
    @DisplayName("Login con password errata restituisce null")
    public void effettuaLogin_passwordErrata_deveTornareNull() throws Exception {
        // Arrange: Registriamo l'utente
        utenteValido.setMail("errata" + System.currentTimeMillis() + "@mail.com");
        proxy.registraUtente(utenteValido);

        // Act: Tentiamo il login con password sbagliata
        Utente utenteLoggato = proxy.effettuaLogin(utenteValido.getMail(), "passwordSbagliatissima");

        // Assert: Il sistema deve respingere l'accesso restituendo null
        assertNull(utenteLoggato, "Il sistema deve restituire null se la password è errata");
    }

    @Test
    @DisplayName("Login con email inesistente restituisce null")
    public void effettuaLogin_utenteNonEsistente_deveTornareNull() throws Exception {
        // Act: Tentiamo il login con una mail mai registrata
        Utente utenteLoggato = proxy.effettuaLogin("utentefantasma@mail.com", "pass123");

        // Assert
        assertNull(utenteLoggato, "Il sistema deve restituire null se l'utente non esiste");
    }

    @AfterAll
    public static void testmodeoff() {
        DatabaseCore.disableTestMode();
        //DatabaseUtente.resetPerTest();
    }
}