package it.unibo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.*;

public class GestoreRichiesteServiceImplTest {

    // Accensione database finto
    @BeforeAll
    public static void testmode() {
        DatabaseCore.enableTestMode();
    }

    // Spegnimento database finto
    @AfterAll 
    public static void testmodeoff() {
        DatabaseCore.disableTestMode();
    }

    @Test
    @DisplayName("Invia Richiesta: Scenario di successo")
    public void testInviaRichiesta() throws Exception {

        // Preparazione dei dati di test
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio richiesta = new RichiestaScambio("", "annuncio123", "Mario", "Luigi");

        // Invio della richiesta
        Boolean risultato = servizio.inviaRichiesta(richiesta);

        // Controlliamo che il risultato sia true e che l'ID sia stato generato
        assertTrue(risultato,"La richiesta valida dovrebbe restituire true");
        assertNotNull(richiesta.getIdRichiesta(), "Il server avrebbe dovuto generare un ID per la richiesta");
    }

    @Test
    @DisplayName("Invia Richiesta: Blocco auto-scambio")
    public void testBloccoAutoScambio() throws Exception {

        // Preparazione dei dati di test
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio richiesta = new RichiestaScambio("", "annuncio456", "Mario", "Mario");

        // Invio della richiesta
        Boolean risultato = servizio.inviaRichiesta(richiesta);

        // Il server DEVE aver bloccato l'operazione restituendo false
        assertFalse(risultato, "La richiesta di auto-scambio dovrebbe restituire false");
    }

    @Test
    @DisplayName("Ottieni Richieste Ricevute: Filtra correttamente per ricevente e stato In sospeso")
    public void testOttieniRichiesteRicevute() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        
        RichiestaScambio req1 = new RichiestaScambio("", "ann1", "Mario", "Luigi");
        RichiestaScambio req2 = new RichiestaScambio("", "ann2", "Anna", "Luigi");
        
        servizio.inviaRichiesta(req1);
        servizio.inviaRichiesta(req2);

        List<RichiestaScambio> ricevute = servizio.ottieniRichiesteRicevute("Luigi");
        
        assertNotNull(ricevute, "La lista delle richieste ricevute non deve essere null");
        assertTrue(ricevute.size() >= 2, "Dovrebbero esserci almeno 2 richieste in sospeso per Luigi");
    }

    @Test
    @DisplayName("Aggiorna Stato Richiesta: Modifica lo stato e lo mantiene visibile come Accettata")
    public void testAggiornaStatoRichiesta() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = new RichiestaScambio("", "ann100", "Mario", "Luigi");
        servizio.inviaRichiesta(req);

        String idRichiesta = req.getIdRichiesta();
        
        // Aggiorniamo lo stato a "Accettata"
        Boolean aggiornato = servizio.aggiornaStatoRichiesta(idRichiesta, "Accettata");
        assertTrue(aggiornato, "L'aggiornamento dello stato della richiesta deve restituire true");

        // Verifichiamo che la richiesta compaia ancora tra quelle ricevute con lo stato aggiornato        
        List<RichiestaScambio> ricevute = servizio.ottieniRichiesteRicevute("Luigi");
        RichiestaScambio richiestaAggiornata = null;
        for (RichiestaScambio r : ricevute) {
            if (r.getIdRichiesta().equals(idRichiesta)) {
                richiestaAggiornata = r;
                break;
            }
        }

        assertNotNull(richiestaAggiornata, "La richiesta accettata deve rimanere visibile nelle ricevute");
        assertEquals("Accettata", richiestaAggiornata.getStato());
    }
}
