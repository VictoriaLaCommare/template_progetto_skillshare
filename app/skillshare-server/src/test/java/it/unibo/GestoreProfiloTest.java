package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.AbstractRemoteServiceServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// TEST DELLA SERVLET
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GestoreProfiloTest{

    // Configurazione standard per testare una GWT Servlet 
    @Mock private ServletConfig servletConfig;
    @Mock private ServletContext servletContext;
    @Mock private HttpServletRequest request;

    private GestoreProfiloImpl gestoreProfilo;
    private Utente utenteDiTest;

    @BeforeAll
    static void testmode(){
        DatabaseCore.enableTestMode();
    }

    @BeforeEach
    public void setUp() throws Exception{
        DatabaseUtente.resetPerTest();
        // Inizializzazione finta del web server
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getServerInfo()).thenReturn("MockServer/1.0");
        when(request.getHeader("User-Agent")).thenReturn("MockBrowser/1.0");

        gestoreProfilo = new GestoreProfiloImpl();
        gestoreProfilo.init(servletConfig);

        ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();
        threadLocal.set(request);

        Field field = AbstractRemoteServiceServlet.class.getDeclaredField("perThreadRequest");
        field.setAccessible(true);
        field.set(gestoreProfilo, threadLocal);

        //Preparazione dell'utente di test 
        utenteDiTest = new Utente("Ludovica", "Govoni");
        utenteDiTest.setMail("ludovica.govoni@gmail.com" + System.currentTimeMillis());
        utenteDiTest.setUsername("govonsx" + System.currentTimeMillis());
        utenteDiTest.setBio("Nel tempo libero creo capi all'uncinetto");
        utenteDiTest.setTagCompetenze("Uncinetto, Maglia, Ricamo");
    }

    @AfterAll
    static void testmodeoff() {
        DatabaseCore.disableTestMode();
    }

    // TEST DI ACCETTAZIONE 
    @Test
    void aggiornaProfilo_ConDatiCorretti_DeveTornareUtenteAggiornato() {
        try {
            Utente utenteSalvato = gestoreProfilo.aggiornaProfilo(utenteDiTest);
            
            assertNotNull(utenteSalvato, "L'utente salvato non deve essere nullo");
            assertEquals("Nel tempo libero creo capi all'uncinetto", utenteSalvato.getBio());
            assertEquals("Uncinetto, Maglia, Ricamo", utenteSalvato.getTagCompetenze());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test 
    @DisplayName ("Sicurezza: Il profilo restituito non deve contenere la password")
    void testOttieniProfiloUtente_Sicurezza() throws Exception {
        
        utenteDiTest.setPassword("passwordSicura123");

        // Salva l'utente di test nel database
        gestoreProfilo.aggiornaProfilo(utenteDiTest);

        // Recupera il profilo dell'utente
        Utente utenteRecuperato = gestoreProfilo.ottieniProfiloUtente(utenteDiTest.getUsername());

        assertNotNull(utenteRecuperato, "Il profilo dell'utente non deve essere nullo");

        assertEquals(utenteDiTest.getUsername(), utenteRecuperato.getUsername());
        assertEquals(utenteDiTest.getNome(), utenteRecuperato.getNome());

        // Verifica che la password non sia presente nel profilo restituito
        assertNull(utenteRecuperato.getPassword(), "La password non deve essere presente nel profilo restituito");
    }

    @Test
    @DisplayName("Storico Scambi: Recupero solo le richieste in cui l'utente è coinvolto")
    void testOttieniStoricoScambi_ConDati() throws Exception {
        
        GestoreRichiesteServiceImpl gestoreRichieste = new GestoreRichiesteServiceImpl();
        utenteDiTest.setUsername("govonsx");
        gestoreProfilo.aggiornaProfilo(utenteDiTest);

        
        // Creazione di 3 richieste diverse.
        RichiestaScambio req1 = new RichiestaScambio("", "annuncio1", "govonsx", "mario_rossi");
        RichiestaScambio req2 = new RichiestaScambio("", "annuncio2", "luigi_verdi", "govonsx");
        RichiestaScambio req3 = new RichiestaScambio("", "annuncio3", "terzo_utente", "quarto_utente"); 

        // Salvataggio nel database
        gestoreRichieste.inviaRichiesta(req1);
        gestoreRichieste.inviaRichiesta(req2);
        gestoreRichieste.inviaRichiesta(req3);

        // Richiesta dello storico scambi per l'utente
        List<RichiestaScambio> storico = gestoreProfilo.ottieniStoricoScambi(utenteDiTest.getUsername());

        // Verifichiamo che non ci dia "null"
        assertNotNull(storico, "Lo storico non deve essere nullo");
        
        // Verifichiamo che ci dia esattamente 2 richieste, quelle in cui l'utente è coinvolto
        assertEquals(2, storico.size(), "Lo storico deve contenere esattamente 2 richieste");

        // Facciamo un ciclo su tutti i risultati restituiti:
        for (RichiestaScambio r : storico) {
            // Se in una delle richieste restituite "govonsx" non compare né a destra né a sinistra, il test fallisce!
            assertTrue(
                r.getRichiedente().equals(utenteDiTest.getUsername()) || r.getRicevente().equals(utenteDiTest.getUsername()),
                "Errore: L'utente target deve essere il richiedente o il ricevente nella richiesta"
            );
        }
    }

    @Test
    @DisplayName("Storico Scambi: Un utente nuovo riceve una lista vuota senza errori")
    void testOttieniStoricoScambi_Vuoto() throws Exception {
        
        // Richiesta dello storico scambi per un utente che non esiste nel database
        List<RichiestaScambio> storico = gestoreProfilo.ottieniStoricoScambi("utente_fantasma_mai_esistito");

        // Ci assicuriamo che il server non ci abbia restituito "null"
        assertNotNull(storico, "Il metodo deve restituire una lista vuota in sicurezza, non null");

        // Ci assicuriamo che il server ci abbia restituito una vera lista, ma completamente vuota
        assertTrue(storico.isEmpty(), "La lista dello storico deve essere vuota");
    }
}
