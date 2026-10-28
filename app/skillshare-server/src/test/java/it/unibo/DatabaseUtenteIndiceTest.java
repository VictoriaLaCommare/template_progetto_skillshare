package it.unibo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseUtenteIndiceTest {

    @BeforeAll
    static void testmode() {
        DatabaseCore.enableTestMode();
    }

    @AfterAll
    static void testmodeoff() {
        DatabaseCore.disableTestMode();
    }

    @BeforeEach
    void resetStato() {
        // Assicura che le mappe vengano ricaricate pulite prima di ogni test
        DatabaseUtente.resetPerTest();
    }

    @Test
    @DisplayName("L'Indice Secondario garantisce l'univocità dello Username")
    void testUnivocitaIndiceSecondario() throws Exception {
        //Registriamo il primo utente
        Utente u1 = new Utente("Mario", "Rossi");
        u1.setMail("mario@mail.com");
        u1.setUsername("SuperDev");
        assertTrue(DatabaseUtente.registraUtente(u1), "La prima registrazione deve avere successo");

        //Tentativo con stessa mail (deve fallire)
        Utente u2 = new Utente("Luigi", "Verdi");
        u2.setMail("mario@mail.com");
        u2.setUsername("AltroUser");
        Exception e1 = assertThrows(Exception.class, () -> DatabaseUtente.registraUtente(u2));
        assertTrue(e1.getMessage().contains("già registrato"));

        //Tentativo con mail diversa ma stesso username (deve fallire grazie all'indice)
        Utente u3 = new Utente("Wario", "Bros");
        u3.setMail("wario@mail.com"); // Mail libera
        u3.setUsername("SuperDev");   // Username occupato da Mario

        Exception e2 = assertThrows(Exception.class, () -> DatabaseUtente.registraUtente(u3));
        assertTrue(e2.getMessage().contains("username già in uso"),
                "Il database deve bloccare l'inserimento di uno username duplicato");
    }

    @Test
    @DisplayName("GestoreProfiloReale recupera utente usando l'indice secondario")
    void testRecuperoTramiteIndice() throws Exception {
        //Arrange: Inseriamo un utente nel DB
        Utente u = new Utente("Peach", "Toadstool");
        u.setMail("peach@mail.com");
        u.setUsername("PrincessPeach");
        u.setBio("Regina dei funghi");
        u.setPassword("segreto");
        DatabaseUtente.registraUtente(u);

        //Act: Lo recuperiamo con GestoreProfiloReale passandogli SOLO lo username
        GestoreProfiloReale gestoreProfilo = new GestoreProfiloReale();
        Utente recuperato = gestoreProfilo.ottieniProfiloUtente("PrincessPeach"); //utilizzo indice secondario

        //Assert: Verifica dei dati incrociati e della sicurezza
        assertNotNull(recuperato, "trova il profilo");
        assertEquals("peach@mail.com", recuperato.getMail(), "La mail deve essere recuperata correttamente dall'indice");
        assertEquals("Regina dei funghi", recuperato.getBio());
        assertNull(recuperato.getPassword(), "La password non deve mai essere passata al client");
    }
}