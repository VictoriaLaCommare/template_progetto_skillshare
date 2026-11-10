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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//TEST DELLA SERVLET ANNUNCI
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GestoreAnnunciTest {

    // Configurazione standard per testare una GWT Servlet
    @Mock private ServletConfig servletConfig;
    @Mock private ServletContext servletContext;
    @Mock private HttpServletRequest request;

    private GestoreAnnunciImpl gestore;
    private Annuncio annuncioValido;

    @BeforeAll
    static void testmode() {
        // Attiviamo il database fittizio per i test
        DatabaseCore.enableTestMode();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Inizializzazione fittizio del Web Server
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getServerInfo()).thenReturn("MockServer/1.0");
        when(request.getHeader("User-Agent")).thenReturn("MockBrowser/1.0");

        gestore = new GestoreAnnunciImpl();
        gestore.init(servletConfig);

        // Iniezione della request fittizio tramite reflection
        ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();
        threadLocal.set(request);

        Field field = AbstractRemoteServiceServlet.class.getDeclaredField("perThreadRequest");
        field.setAccessible(true);
        field.set(gestore, threadLocal);

        // Prepariamo l'annuncio per il test
        annuncioValido = new Annuncio(
            "id_test_01",
            "Scambio lezioni di chitarra",
            "Offro lezioni base di chitarra in cambio di ripetizioni di Java.",
            "Chitarra",
            "Programmazione Java",
            "utente_test_01"
        );
    }

    @AfterAll
    static void testmodeoff() {
        // Spegniamo il database di test
        DatabaseCore.disableTestMode();
    }

    // TEST DI ACCETTAZIONE
    @Test
    void testPubblicazioneERecuperoAnnuncio() {
        try {
            //Verifichiamo che il salvataggio vada a buon fine tornando true
            assertTrue(gestore.pubblicaAnnuncio(annuncioValido));

            //Recuperiamo gli annunci e verifichiamo che i dati siano intatti
            List<Annuncio> tuttiGliAnnunci = gestore.ottieniTuttiGliAnnunci();
            assertNotNull(tuttiGliAnnunci);

            boolean trovato = false;
            for (Annuncio a : tuttiGliAnnunci) {
                if (a.getId().equals(annuncioValido.getId())) {
                    trovato = true;
                    assertEquals("Scambio lezioni di chitarra", a.getTitolo());
                    assertEquals("utente_test_01", a.getAutoreUsername());
                    break;
                }
            }
            assertTrue(trovato, "L'annuncio appena creato deve essere nel database");

        } catch (Exception e) {
            fail("Il test ha lanciato un'eccezione imprevista: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Recupero annunci con database popolato")
    void testVisualizzazioneProposteDisponibili(){
        try {
            Annuncio proposta1 = new Annuncio("id_test_01", "Scambio Bici", "Offro bici in cambio di un monopattino", "Bici", "Monopattino", "mario_rossi");
            Annuncio proposta2 = new Annuncio("id_test_02", "Lezioni di Inglese", "Offro lezioni in cambio di ripetizioni di matematica", "Inglese", "Matematica", "giulia_bianchi");
            
            // Pubblicazione delle proposte
            gestore.pubblicaAnnuncio(proposta1);
            gestore.pubblicaAnnuncio(proposta2);

            List<Annuncio> proposteDisponibili = gestore.ottieniTuttiGliAnnunci();

            assertNotNull(proposteDisponibili, "La lista delle proposte non deve essere null");
            assertTrue(proposteDisponibili.size() >= 2, "La lista delle proposte deve contenere almeno 2 elementi");

            boolean trovatoProposta1 = false;
            boolean trovatoProposta2 = false;
            for (Annuncio a : proposteDisponibili) {
                if (a.getId().equals(proposta1.getId())) {
                    trovatoProposta1 = true;
                }
                if (a.getId().equals(proposta2.getId())) {
                    trovatoProposta2 = true;
                }
            }
            assertTrue(trovatoProposta1, "La proposta 1 deve essere presente nella lista");
            assertTrue(trovatoProposta2, "La proposta 2 deve essere presente nella lista");
        } catch (Exception e) {
            fail("Il test ha lanciato un'eccezione imprevista: " + e.getMessage());
        }
    }

    @Test 
    @DisplayName("Nessuna proposta disponibile")
    void testNessunaPropostaDisponibile(){
        try {
            List<Annuncio> proposteDisponibili = gestore.ottieniTuttiGliAnnunci();

            // Il sistema restituisce una lista valida (vuota []) senza dare errori
            assertNotNull(proposteDisponibili, "La lista delle proposte non deve essere null");
        } catch (Exception e) {
            fail("Il test ha lanciato un'eccezione imprevista: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test recupero annunci di uno specifico utente")
    void testOttieniAnnunciUtente() {
        try {
            // Prepariamo due annunci di autori differenti
            Annuncio annuncioMio = new Annuncio("id_mario_1", "Ripetizioni Matematica", "Offro aiuto", "Matematica", "Fisica", "mario_rossi");
            Annuncio annuncioAltro = new Annuncio("id_altro_1", "Corso di Inglese", "Offro inglese", "Inglese", "Italiano", "luigi_verdi");
            
            gestore.pubblicaAnnuncio(annuncioMio);
            gestore.pubblicaAnnuncio(annuncioAltro);

            // Richiediamo solo gli annunci dell'utente "mario_rossi"
            List<Annuncio> mieiAnnunci = gestore.ottieniAnnunciUtente("mario_rossi");

            assertNotNull(mieiAnnunci, "La lista degli annunci dell'utente non deve essere null");
            assertFalse(mieiAnnunci.isEmpty(), "La lista non deve essere vuota");
            
            // Verifichiamo che tutti gli annunci restituiti appartengano effettivamente a mario_rossi
            for (Annuncio a : mieiAnnunci) {
                assertEquals("mario_rossi", a.getAutoreUsername(), "L'autore deve corrispondere all'utente richiesto");
            }
        } catch (Exception e) {
            fail("Il test ha lanciato un'eccezione imprevista: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test modifica di un annuncio esistente")
    void testModificaAnnuncio() {
        try {
            // Pubblichiamo l'annuncio iniziale
            Annuncio annuncioDaModificare = new Annuncio("id_mod_01", "Titolo Vecchio", "Descrizione vecchia", "Offerta1", "Richiesta1", "utente_test");
            gestore.pubblicaAnnuncio(annuncioDaModificare);

            // Modifichiamo i campi dell'annuncio
            annuncioDaModificare.setTitolo("Titolo Aggiornato");
            annuncioDaModificare.setDescrizione("Descrizione aggiornata con successo");

            // Eseguiamo la modifica sul server tramite RPC
            boolean risultatoModifica = gestore.modificaAnnuncio(annuncioDaModificare);
            assertTrue(risultatoModifica, "La modifica dell'annuncio deve andare a buon fine");

            // Verifichiamo che i dati siano stati effettivamente aggiornati nel database
            List<Annuncio> tutti = gestore.ottieniTuttiGliAnnunci();
            for (Annuncio a : tutti) {
                if (a.getId().equals("id_mod_01")) {
                    assertEquals("Titolo Aggiornato", a.getTitolo());
                    assertEquals("Descrizione aggiornata con successo", a.getDescrizione());
                    break;
                }
            }
        } catch (Exception e) {
            fail("Il test ha lanciato un'eccezione imprevista: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test eliminazione di un annuncio")
    void testEliminaAnnuncio() {
        try {
            // Pubblichiamo un annuncio da eliminare
            Annuncio annuncioDaEliminare = new Annuncio("id_del_01", "Da Cancellare", "Test eliminazione", "Offerta", "Richiesta", "utente_test");
            gestore.pubblicaAnnuncio(annuncioDaEliminare);

            // Verifichiamo che esista prima di eliminarlo
            assertFalse(gestore.ottieniTuttiGliAnnunci().isEmpty());

            // Eseguiamo l'eliminazione passandogli l'id dell'annuncio
            boolean risultatoEliminazione = gestore.eliminaAnnuncio("id_del_01", "utente_test");
            assertTrue(risultatoEliminazione, "L'eliminazione dell'annuncio deve restituire true");

            // Verifichiamo che l'annuncio non sia più presente nel sistema
            List<Annuncio> listaAggiornata = gestore.ottieniTuttiGliAnnunci();
            boolean ancoraPresente = false;
            for (Annuncio a : listaAggiornata) {
                if (a.getId().equals("id_del_01")) {
                    ancoraPresente = true;
                    break;
                }
            }
            assertFalse(ancoraPresente, "L'annuncio eliminato non deve più trovarsi nel database");
        } catch (Exception e) {
            fail("Il test ha lanciato un'eccezione imprevista: " + e.getMessage());
        }
    }
}