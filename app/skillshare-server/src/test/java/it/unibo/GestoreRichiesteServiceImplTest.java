package it.unibo;

import java.util.List;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

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

        // Il server deve aver bloccato l'operazione restituendo false
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
    @DisplayName("Accetta Richiesta: Il ricevente accetta con successo")
    public void testAccettaRichiesta_Successo() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = new RichiestaScambio("", "ann-acc1", "Richiedente_A1", "Ricevente_A1");
        servizio.inviaRichiesta(req);

        Boolean esito = servizio.accettaRichiesta(req.getIdRichiesta(), "Ricevente_A1");

        assertTrue(esito, "Il ricevente deve poter accettare la richiesta");

        // La richiesta deve comparire nelle ricevute con stato "Accettata" (verifica Bug 1 risolto)
        List<RichiestaScambio> ricevute = servizio.ottieniRichiesteRicevute("Ricevente_A1");
        boolean trovata = ricevute.stream()
                .anyMatch(r -> r.getIdRichiesta().equals(req.getIdRichiesta())
                        && "Accettata".equals(r.getStato()));
        assertTrue(trovata, "La richiesta deve comparire come 'Accettata' nelle richieste ricevute");
    }

    @Test
    @DisplayName("Accetta Richiesta: Il richiedente non può accettare la propria richiesta")
    public void testAccettaRichiesta_RichiedenteNonPuoAccettare() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = new RichiestaScambio("", "ann-acc2", "Richiedente_A2", "Ricevente_A2");
        servizio.inviaRichiesta(req);

        Boolean esito = servizio.accettaRichiesta(req.getIdRichiesta(), "Richiedente_A2");

        assertFalse(esito, "Il richiedente non deve poter accettare la propria richiesta");
    }

    @Test
    @DisplayName("Accetta Richiesta: Fallisce per ID inesistente")
    public void testAccettaRichiesta_IdInesistente() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();

        Boolean esito = servizio.accettaRichiesta("id-inesistente-acc", "Ricevente_A3");

        assertFalse(esito, "Accettare una richiesta con ID inesistente deve restituire false");
    }

    // rifiutaRichiesta
    @Test
    @DisplayName("Rifiuta Richiesta: Il ricevente rifiuta con successo")
    public void testRifiutaRichiesta_Ricevente() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = new RichiestaScambio("id_req_rifiuta_richie", "ann-rif1", "Richiedente_R1", "Ricevente_R1");
        servizio.inviaRichiesta(req);

        Boolean esito = servizio.rifiutaRichiesta(req.getIdRichiesta(), "Ricevente_R1");

        assertTrue(esito, "Il ricevente deve poter rifiutare la richiesta");
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Il richiedente può annullare la propria richiesta")
    public void testRifiutaRichiesta_Richiedente() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = new RichiestaScambio("id_req_rifiuta_rice", "ann-rif2", "Richiedente_R2", "Ricevente_R2");
        servizio.inviaRichiesta(req);

        Boolean esito = servizio.rifiutaRichiesta(req.getIdRichiesta(), "Richiedente_R2");

        assertTrue(esito, "Il richiedente deve poter annullare la propria richiesta");
    }
    private RichiestaScambio creaRichiestaAccettata(GestoreRichiesteServiceImpl servizio,
                                                    String richiedente, String ricevente,
                                                    String idAnnuncio) throws Exception {
        RichiestaScambio req = new RichiestaScambio("", idAnnuncio, richiedente, ricevente);
        servizio.inviaRichiesta(req);
        servizio.accettaRichiesta(req.getIdRichiesta(), ricevente);
        return req;
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Fallisce per ID inesistente")
    public void testRifiutaRichiesta_IdInesistente() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();

        Boolean esito = servizio.rifiutaRichiesta("id-inesistente-rif", "Ricevente_R3");

        assertFalse(esito, "Rifiutare una richiesta con ID inesistente deve restituire false");
    }

    // =========================================================================
    // dichiaraTerminazione
    // =========================================================================


    // Scenario 1 – Chiusura definitiva dello scambio
    @Test
    @DisplayName("Scenario 1: Chiusura definitiva dello scambio, conferma prima del richiedente")
    public void testDichiaraTerminazione_TA1_ChiusuraDefinitiva_RichiedenteConfermaPrima() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T1a", "Ricevente_T1a", "ann-ta1a");
        String id = req.getIdRichiesta();

        //GIVEN: il richiedente ha già confermato
        Boolean primaConferma = servizio.dichiaraTerminazione(id, "Richiedente_T1a");
        assertTrue(primaConferma, "La prima conferma deve avere successo");

        //WHEN: il ricevente conferma
        Boolean secondaConferma = servizio.dichiaraTerminazione(id, "Ricevente_T1a");

        //THEN: il sistema registra la conferma
        assertTrue(secondaConferma, "La seconda conferma deve avere successo");

        //lo stato diventa "Completato"
        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Richiedente_T1a");
        RichiestaScambio richiestaFinal = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id))
                .findFirst().orElse(null);

        assertNotNull(richiestaFinal, "La richiesta deve essere recuperabile dopo il completamento");
        assertEquals("Completato", richiestaFinal.getStato(),
                "Dopo entrambe le conferme lo stato deve essere 'Completato'");
        //flag che attestano lo stato completato:
        assertTrue(richiestaFinal.isConfermataTerminazioneRichiedente(),
                "Il flag del richiedente deve essere true");
        assertTrue(richiestaFinal.isConfermataTerminazioneRicevente(),
                "Il flag del ricevente deve essere true");

    }

    @Test
    @DisplayName("Scenario 1: Chiusura definitiva dello scambio, conferma prima del Ricevente")
    public void testDichiaraTerminazione_TA1_ChiusuraDefinitiva_RiceventeConfermaPrima() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T1b", "Ricevente_T1b", "ann-ta1b");
        String id = req.getIdRichiesta();

        //GIVEN: il ricevente ha già confermato
        servizio.dichiaraTerminazione(id, "Ricevente_T1b");

        //WHEN: il richiedente conferma
        Boolean secondaConferma = servizio.dichiaraTerminazione(id, "Richiedente_T1b");

        //THEN
        assertTrue(secondaConferma);

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Richiedente_T1b");
        RichiestaScambio richiestaFinal = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id))
                .findFirst().orElse(null);

        assertNotNull(richiestaFinal);
        assertEquals("Completato", richiestaFinal.getStato());
        assertTrue(richiestaFinal.isConfermataTerminazioneRichiedente());
        assertTrue(richiestaFinal.isConfermataTerminazioneRicevente());
    }

    //Scenario 2 – Attesa di conferma dalla controparte

    @Test
    @DisplayName("Scenario 2: Attesa di conferma della controparte: solo il richiedente conferma")
    public void testDichiaraTerminazione_TA2_AttesaControparte_Richiedente() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        //GIVEN: lo scambio è in corso quindi la richiesta è stata accettata
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T2a", "Ricevente_T2a", "ann-ta2a");
        String id = req.getIdRichiesta();

        //WHEN: solo il richiedente conferma
        Boolean esito = servizio.dichiaraTerminazione(id, "Richiedente_T2a");

        //THEN: il sistema registra la singola conferma
        assertTrue(esito, "La conferma deve avere successo");

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Richiedente_T2a");
        RichiestaScambio richiestaAggiornata = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id))
                .findFirst().orElse(null);

        assertNotNull(richiestaAggiornata);

        //lo stato NON è "Completato" (badge/statistiche non aggiornate)
        assertNotEquals("Completato", richiestaAggiornata.getStato(),
                "Con una sola conferma lo stato non deve essere 'Completato'");

        // il sistema notifica l'attesa della controparte
        assertEquals("In attesa di conferma dall'altro utente", richiestaAggiornata.getStato(),
                "Il sistema deve indicare l'attesa della controparte");

        //il flag della controparte non è ancora registrato
        assertFalse(richiestaAggiornata.isConfermataTerminazioneRicevente(),
                "Il flag del ricevente non deve ancora essere true");
    }

    @Test
    @DisplayName("Scenario 2: Attesa di conferma della controparte: solo il richiedente conferma")
    public void testDichiaraTerminazione_TA2_AttesaControparte_Ricevente() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();

        //GIVEN: lo scambio è in corso quindi la richiesta è stata accettata
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T2b", "Ricevente_T2b", "ann-ta2b");
        String id = req.getIdRichiesta();

        //WHEN: solo il ricevente conferma
        Boolean esito = servizio.dichiaraTerminazione(id, "Ricevente_T2b");

        //THEN
        assertTrue(esito);

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Ricevente_T2b");
        RichiestaScambio richiestaAggiornata = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id))
                .findFirst().orElse(null);

        assertNotNull(richiestaAggiornata);
        assertNotEquals("Completato", richiestaAggiornata.getStato());
        assertEquals("In attesa di conferma dall'altro utente", richiestaAggiornata.getStato());
        assertFalse(richiestaAggiornata.isConfermataTerminazioneRichiedente());
    }

    @Test
    @DisplayName("Utente non coinvolto non può dichiarare terminazione")
    public void testDichiaraTerminazione_UtenteNonAutorizzato() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T3", "Ricevente_T3", "ann-ta3");
        String id = req.getIdRichiesta();

        Boolean esito = servizio.dichiaraTerminazione(id, "utente_estraneo");

        assertFalse(esito, "Un utente non coinvolto deve ricevere false");
    }

    @Test
    @DisplayName("Impossibile dichiarare terminazione per ID inesistente")
    public void testDichiaraTerminazione_IdInesistente() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();

        Boolean esito = servizio.dichiaraTerminazione("id-inesistente-term", "Richiedente_T5");

        assertFalse(esito, "Dichiarare terminazione su ID inesistente deve restituire false");
    }

    // ottieniTutteLeRichieste

    @Test
    @DisplayName("Ottieni Tutte Le Richieste quindi sia richieste come richiedente che come ricevente")
    public void testOttieniTutteLeRichieste_EntrambiRuoli() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();

        RichiestaScambio comeRichiedente = new RichiestaScambio("", "ann-all1", "Utente_Target_OTR", "AltroUtente_OTR1");
        RichiestaScambio comeRicevente   = new RichiestaScambio("", "ann-all2", "AltroUtente_OTR2", "Utente_Target_OTR");
        RichiestaScambio nonCoinvolto    = new RichiestaScambio("", "ann-all3", "Utente_C_OTR", "Utente_D_OTR");

        servizio.inviaRichiesta(comeRichiedente);
        servizio.inviaRichiesta(comeRicevente);
        servizio.inviaRichiesta(nonCoinvolto);

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Utente_Target_OTR");

        assertNotNull(tutte, "La lista non deve essere null");
        assertTrue(tutte.size() >= 2, "Devono comparire almeno le 2 richieste in cui l'utente è coinvolto");

        boolean trovaRichiedente = tutte.stream()
                .anyMatch(r -> r.getIdRichiesta().equals(comeRichiedente.getIdRichiesta()));
        boolean trovaRicevente = tutte.stream()
                .anyMatch(r -> r.getIdRichiesta().equals(comeRicevente.getIdRichiesta()));
        boolean trovaNonCoinvolto = tutte.stream()
                .anyMatch(r -> r.getIdRichiesta().equals(nonCoinvolto.getIdRichiesta()));

        assertTrue(trovaRichiedente,   "Deve includere la richiesta in cui l'utente è richiedente");
        assertTrue(trovaRicevente,     "Deve includere la richiesta in cui l'utente è ricevente");
        assertFalse(trovaNonCoinvolto, "Non deve includere richieste in cui l'utente non è coinvolto");
    }

    @Test
    @DisplayName("Ottieni Tutte Le Richieste: utente senza richieste riceve lista vuota")
    public void testOttieniTutteLeRichieste_NessunaCoinvolta() throws Exception {
        GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("utente_senza_richieste_XYZ");

        assertNotNull(tutte, "La lista non deve essere null anche se vuota");
        assertTrue(tutte.stream()
                        .noneMatch(r -> r.getRichiedente().equals("utente_senza_richieste_XYZ")
                        || r.getRicevente().equals("utente_senza_richieste_XYZ")),
                "Non devono comparire richieste non pertinenti");
    }
}
