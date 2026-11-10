package it.unibo;

import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GestoreRichiesteServiceImplTest {
    private final GestoreRichiesteServiceImpl servizio = new GestoreRichiesteServiceImpl();

    // Accensione database in-memory per i test
    @BeforeAll
    public static void testmode() {
        DatabaseCore.enableTestMode();
    }

    // Spegnimento database di test
    @AfterAll
    public static void testmodeoff() {
        DatabaseCore.disableTestMode();
    }

    // Metodo per portare una richiesta direttamente allo stato "Accettata"
    private RichiestaScambio creaRichiestaAccettata(GestoreRichiesteServiceImpl servizio,
                                                    String richiedente, String ricevente,
                                                    String idAnnuncio) throws Exception {
        RichiestaScambio req = new RichiestaScambio("", idAnnuncio, richiedente, ricevente);
        servizio.inviaRichiesta(req);
        servizio.accettaRichiesta(req.getIdRichiesta(), ricevente);
        return req;
    }

    //Metodo per portare una richiesta allo stato "Completato" facendo confermare entrambi gli utenti
    private RichiestaScambio creaRichiestaCompletata(String richiedente, String ricevente,
                                                     String idAnnuncio) throws Exception {
        RichiestaScambio req = creaRichiestaAccettata(servizio, richiedente, ricevente, idAnnuncio);
        servizio.dichiaraTerminazione(req.getIdRichiesta(), richiedente);
        servizio.dichiaraTerminazione(req.getIdRichiesta(), ricevente);
        return req;
    }

    @Test
    @DisplayName("Invia Richiesta: Scenario di successo")
    public void testInviaRichiesta() throws Exception {
        // Prepariamo i dati ed effettuiamo l'invio
        RichiestaScambio richiesta = new RichiestaScambio("", "annuncio123", "Mario", "Luigi");
        Boolean risultato = servizio.inviaRichiesta(richiesta);

        // Verifichiamo il buon esito e la generazione automatica dell'ID
        assertTrue(risultato, "La richiesta valida dovrebbe restituire true");
        assertNotNull(richiesta.getIdRichiesta(), "Il server avrebbe dovuto generare un ID per la richiesta");
    }

    @Test
    @DisplayName("Invia Richiesta: Blocco auto-scambio")
    public void testBloccoAutoScambio() throws Exception {
        // Un utente prova a inviare una richiesta verso il proprio annuncio
        RichiestaScambio richiesta = new RichiestaScambio("", "annuncio456", "Mario", "Mario");
        Boolean risultato = servizio.inviaRichiesta(richiesta);

        // Il server deve bloccare l'operazione
        assertFalse(risultato, "La richiesta di auto-scambio dovrebbe restituire false");
    }

    @Test
    @DisplayName("Ottieni Richieste Ricevute: Filtra correttamente per ricevente e stato In sospeso")
    public void testOttieniRichiesteRicevute() throws Exception {
        RichiestaScambio req1 = new RichiestaScambio("", "ann1", "Mario", "Luigi");
        RichiestaScambio req2 = new RichiestaScambio("", "ann2", "Anna", "Luigi");

        servizio.inviaRichiesta(req1);
        servizio.inviaRichiesta(req2);

        // Recuperiamo le richieste in arrivo per Luigi
        List<RichiestaScambio> ricevute = servizio.ottieniRichiesteRicevute("Luigi");

        assertNotNull(ricevute, "La lista delle richieste ricevute non deve essere null");
        assertTrue(ricevute.size() >= 2, "Dovrebbero esserci almeno 2 richieste in sospeso per Luigi");
    }

    @Test
    @DisplayName("Accetta Richiesta: Il ricevente accetta con successo")
    public void testAccettaRichiesta_Successo() throws Exception {
        RichiestaScambio req = new RichiestaScambio("", "ann-acc1", "Richiedente_A1", "Ricevente_A1");
        servizio.inviaRichiesta(req);

        // Il legittimo ricevente accetta lo scambio
        Boolean esito = servizio.accettaRichiesta(req.getIdRichiesta(), "Ricevente_A1");
        assertTrue(esito, "Il ricevente deve poter accettare la richiesta");

        // Controlliamo che lo stato si sia aggiornato correttamente nelle ricevute
        List<RichiestaScambio> ricevute = servizio.ottieniRichiesteRicevute("Ricevente_A1");
        boolean trovata = ricevute.stream()
                .anyMatch(r -> r.getIdRichiesta().equals(req.getIdRichiesta())
                        && "Accettata".equals(r.getStato()));
        assertTrue(trovata, "La richiesta deve comparire come 'Accettata' nelle richieste ricevute");
    }

    @Test
    @DisplayName("Accetta Richiesta: Il richiedente non può accettare la propria richiesta")
    public void testAccettaRichiesta_RichiedenteNonPuoAccettare() throws Exception {
        RichiestaScambio req = new RichiestaScambio("", "ann-acc2", "Richiedente_A2", "Ricevente_A2");
        servizio.inviaRichiesta(req);

        // Tentativo non autorizzato del richiedente di accettare la propria richiesta
        Boolean esito = servizio.accettaRichiesta(req.getIdRichiesta(), "Richiedente_A2");
        assertFalse(esito, "Il richiedente non deve poter accettare la propria richiesta");
    }

    @Test
    @DisplayName("Accetta Richiesta: Fallisce per ID inesistente")
    public void testAccettaRichiesta_IdInesistente() throws Exception {
        Boolean esito = servizio.accettaRichiesta("id-inesistente-acc", "Ricevente_A3");
        assertFalse(esito, "Accettare una richiesta con ID inesistente deve restituire false");
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Il ricevente rifiuta con successo")
    public void testRifiutaRichiesta_Ricevente() throws Exception {
        RichiestaScambio req = new RichiestaScambio("id_req_rifiuta_richie", "ann-rif1", "Richiedente_R1", "Ricevente_R1");
        servizio.inviaRichiesta(req);

        Boolean esito = servizio.rifiutaRichiesta(req.getIdRichiesta(), "Ricevente_R1");
        assertTrue(esito, "Il ricevente deve poter rifiutare la richiesta");
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Il richiedente può annullare la propria richiesta")
    public void testRifiutaRichiesta_Richiedente() throws Exception {
        RichiestaScambio req = new RichiestaScambio("id_req_rifiuta_rice", "ann-rif2", "Richiedente_R2", "Ricevente_R2");
        servizio.inviaRichiesta(req);

        Boolean esito = servizio.rifiutaRichiesta(req.getIdRichiesta(), "Richiedente_R2");
        assertTrue(esito, "Il richiedente deve poter annullare la propria richiesta");
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Fallisce per ID inesistente")
    public void testRifiutaRichiesta_IdInesistente() throws Exception {
        Boolean esito = servizio.rifiutaRichiesta("id-inesistente-rif", "Ricevente_R3");
        assertFalse(esito, "Rifiutare una richiesta con ID inesistente deve restituire false");
    }

    @Test
    @DisplayName("Scenario 1: Chiusura definitiva, richiedente conferma prima")
    public void testDichiaraTerminazione_TA1_ChiusuraDefinitiva_RichiedenteConfermaPrima() throws Exception {
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T1a", "Ricevente_T1a", "ann-ta1a");
        String id = req.getIdRichiesta();

        // Prima conferma da parte del richiedente
        Boolean primaConferma = servizio.dichiaraTerminazione(id, "Richiedente_T1a");
        assertTrue(primaConferma, "La prima conferma deve avere successo");

        // Seconda conferma da parte del ricevente
        Boolean secondaConferma = servizio.dichiaraTerminazione(id, "Ricevente_T1a");
        assertTrue(secondaConferma, "La seconda conferma deve avere successo");

        // Verifichiamo che lo scambio sia correttamente completato
        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Richiedente_T1a");
        RichiestaScambio richiestaFinal = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id)).findFirst().orElse(null);

        assertNotNull(richiestaFinal);
        assertEquals("Completato", richiestaFinal.getStato());
        assertTrue(richiestaFinal.isConfermataTerminazioneRichiedente());
        assertTrue(richiestaFinal.isConfermataTerminazioneRicevente());
    }

    @Test
    @DisplayName("Scenario 1: Chiusura definitiva, ricevente conferma prima")
    public void testDichiaraTerminazione_TA1_ChiusuraDefinitiva_RiceventeConfermaPrima() throws Exception {
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T1b", "Ricevente_T1b", "ann-ta1b");
        String id = req.getIdRichiesta();

        // Inversione: conferma prima il ricevente e poi il richiedente
        servizio.dichiaraTerminazione(id, "Ricevente_T1b");
        Boolean secondaConferma = servizio.dichiaraTerminazione(id, "Richiedente_T1b");
        assertTrue(secondaConferma);

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Richiedente_T1b");
        RichiestaScambio richiestaFinal = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id)).findFirst().orElse(null);

        assertNotNull(richiestaFinal);
        assertEquals("Completato", richiestaFinal.getStato());
        assertTrue(richiestaFinal.isConfermataTerminazioneRichiedente());
        assertTrue(richiestaFinal.isConfermataTerminazioneRicevente());
    }

    @Test
    @DisplayName("Scenario 2: Attesa controparte — solo il richiedente conferma")
    public void testDichiaraTerminazione_TA2_AttesaControparte_Richiedente() throws Exception {
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T2a", "Ricevente_T2a", "ann-ta2a");
        String id = req.getIdRichiesta();

        // Solo il richiedente dichiara la terminazione
        Boolean esito = servizio.dichiaraTerminazione(id, "Richiedente_T2a");
        assertTrue(esito, "La conferma deve avere successo");

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Richiedente_T2a");
        RichiestaScambio richiestaAggiornata = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id)).findFirst().orElse(null);

        assertNotNull(richiestaAggiornata);
        // Lo stato non è ancora completato ma in attesa dell'altra parte
        assertNotEquals("Completato", richiestaAggiornata.getStato());
        assertEquals("In attesa di conferma dall'altro utente", richiestaAggiornata.getStato());
        assertFalse(richiestaAggiornata.isConfermataTerminazioneRicevente());
    }

    @Test
    @DisplayName("Scenario 2: Attesa controparte — solo il ricevente conferma")
    public void testDichiaraTerminazione_TA2_AttesaControparte_Ricevente() throws Exception {
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T2b", "Ricevente_T2b", "ann-ta2b");
        String id = req.getIdRichiesta();

        // Solo il ricevente dichiara la terminazione
        Boolean esito = servizio.dichiaraTerminazione(id, "Ricevente_T2b");
        assertTrue(esito);

        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Ricevente_T2b");
        RichiestaScambio richiestaAggiornata = tutte.stream()
                .filter(r -> r.getIdRichiesta().equals(id)).findFirst().orElse(null);

        assertNotNull(richiestaAggiornata);
        assertNotEquals("Completato", richiestaAggiornata.getStato());
        assertEquals("In attesa di conferma dall'altro utente", richiestaAggiornata.getStato());
        assertFalse(richiestaAggiornata.isConfermataTerminazioneRichiedente());
    }

    @Test
    @DisplayName("Utente non coinvolto non può dichiarare terminazione")
    public void testDichiaraTerminazione_UtenteNonAutorizzato() throws Exception {
        RichiestaScambio req = creaRichiestaAccettata(servizio, "Richiedente_T3", "Ricevente_T3", "ann-ta3");

        // Un utente estraneo prova a confermare la chiusura dello scambio
        Boolean esito = servizio.dichiaraTerminazione(req.getIdRichiesta(), "utente_estraneo");
        assertFalse(esito, "Un utente non coinvolto deve ricevere false");
    }

    @Test
    @DisplayName("Impossibile dichiarare terminazione per ID inesistente")
    public void testDichiaraTerminazione_IdInesistente() throws Exception {
        Boolean esito = servizio.dichiaraTerminazione("id-inesistente-term", "Richiedente_T5");
        assertFalse(esito);
    }

    @Test
    @DisplayName("Ottieni tutte le richieste: include sia quelle come richiedente che come ricevente")
    public void testOttieniTutteLeRichieste_EntrambiRuoli() throws Exception {
        RichiestaScambio comeRichiedente = new RichiestaScambio("", "ann-all1", "Utente_Target_OTR", "AltroUtente_OTR1");
        RichiestaScambio comeRicevente   = new RichiestaScambio("", "ann-all2", "AltroUtente_OTR2", "Utente_Target_OTR");
        RichiestaScambio nonCoinvolto    = new RichiestaScambio("", "ann-all3", "Utente_C_OTR", "Utente_D_OTR");

        servizio.inviaRichiesta(comeRichiedente);
        servizio.inviaRichiesta(comeRicevente);
        servizio.inviaRichiesta(nonCoinvolto);

        // Verifichiamo che compaiano tutte le interazioni in cui l'utente è coinvolto
        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("Utente_Target_OTR");
        assertNotNull(tutte);
        assertTrue(tutte.size() >= 2);
        assertTrue(tutte.stream().anyMatch(r -> r.getIdRichiesta().equals(comeRichiedente.getIdRichiesta())));
        assertTrue(tutte.stream().anyMatch(r -> r.getIdRichiesta().equals(comeRicevente.getIdRichiesta())));
        assertFalse(tutte.stream().anyMatch(r -> r.getIdRichiesta().equals(nonCoinvolto.getIdRichiesta())));
    }

    @Test
    @DisplayName("Ottieni tutte le richieste: utente senza richieste riceve lista vuota")
    public void testOttieniTutteLeRichieste_NessunaCoinvolta() throws Exception {
        List<RichiestaScambio> tutte = servizio.ottieniTutteLeRichieste("utente_senza_richieste_XYZ");
        assertNotNull(tutte);
        assertTrue(tutte.stream()
                .noneMatch(r -> r.getRichiedente().equals("utente_senza_richieste_XYZ")
                        || r.getRicevente().equals("utente_senza_richieste_XYZ")));
    }

    // Test Recensioni e Statistiche

    @Test
    @DisplayName("Recensione Scenario 1: invio completato, profilo aggiornato")
    public void testRecensione_Scenario1_InvioSuccesso() throws Exception {
        //GIVEN: uno scambio regolarmente concluso e completato
        RichiestaScambio req = creaRichiestaCompletata("RicRec_S1", "RiceRec_S1", "ann-rec-s1");

        //WHEN: l'utente lascia una valutazione positiva con commento
        Recensione rec = Recensione.creaRecensione();
        rec.setValutazione(4);
        rec.setCommento("Ottimo scambio, persona affidabile.");
        Boolean esito = servizio.inviaRecensione(req.getIdRichiesta(), "RiceRec_S1", rec);

        assertTrue(esito, "Il sistema deve confermare il salvataggio della valutazione");

        //THEN: verifichiamo direttamente sul DB che le statistiche dell'utente siano aggiornate
        GestoreLeveling leveling = new GestoreLeveling(DatabaseCore.getDB());
        StatisticheUtente stats = leveling.getStatistiche("RiceRec_S1");

        assertEquals(1, stats.getNumeroRecensioni(), "Deve risultare 1 recensione registrata nel profilo");
        assertEquals(4.0, stats.getMediaRecensioni(), 0.01, "La media deve corrispondere alla valutazione inviata");
    }

    @Test
    @DisplayName("Recensione,Controllo sui valori ai limiti (1 e 5 stelle)")
    public void testRecensione_Scenario1_ValoriLimite() throws Exception {
        // Test con valutazione minima valida (1 stella)
        RichiestaScambio req1 = creaRichiestaCompletata("RicRec_L1a", "RiceRec_L1a", "ann-rec-l1a");
        Recensione rec1 = Recensione.creaRecensione();
        rec1.setValutazione(1);
        assertTrue(servizio.inviaRecensione(req1.getIdRichiesta(), "RiceRec_L1a", rec1));

        // Test con valutazione massima valida (5 stelle)
        RichiestaScambio req5 = creaRichiestaCompletata("RicRec_L1b", "RiceRec_L1b", "ann-rec-l1b");
        Recensione rec5 = Recensione.creaRecensione();
        rec5.setValutazione(5);
        assertTrue(servizio.inviaRecensione(req5.getIdRichiesta(), "RiceRec_L1b", rec5));

        // Un valore fuori range deve sollevare un'eccezione
        assertThrows(IllegalArgumentException.class, () -> {
            Recensione recInvalida = Recensione.creaRecensione();
            recInvalida.setValutazione(6);
        }, "Valutazione > 5 deve sollevare IllegalArgumentException");
    }

    @Test
    @DisplayName("Recensione, Scenario 2: annullamento, profilo invariato")
    public void testRecensione_Scenario2_Annullamento() throws Exception {
        // GIVEN: scambio completato
        //RichiestaScambio req = creaRichiestaCompletata("RicRec_S2", "RiceRec_S2", "ann-rec-s2");
        GestoreLeveling leveling = new GestoreLeveling(DatabaseCore.getDB());

        // Salviamo lo stato iniziale delle statistiche
        StatisticheUtente statsPrima = leveling.getStatistiche("RiceRec_S2");
        int recensioniPrima = statsPrima.getNumeroRecensioni();
        double mediaPrima = statsPrima.getMediaRecensioni();

        // WHEN: l'utente decide di non lasciare la recensione (il metodo non viene invocato)

        // THEN: le statistiche del profilo non devono subire variazioni
        StatisticheUtente statsDopo = leveling.getStatistiche("RiceRec_S2");
        assertEquals(recensioniPrima, statsDopo.getNumeroRecensioni(), "Il numero di recensioni non deve cambiare dopo l'annullamento");
        assertEquals(mediaPrima, statsDopo.getMediaRecensioni(), 0.01, "La media delle recensioni non deve cambiare dopo l'annullamento");
    }

    @Test
    @DisplayName("Recensione su richiesta non completata fallisce")
    public void testRecensione_RobustezzaStatoErrato() throws Exception {
        // Richiesta solo accettata ma non ancora completata
        RichiestaScambio req = creaRichiestaAccettata(servizio, "RicRec_Rob", "RiceRec_Rob", "ann-rec-rob");

        Recensione rec = Recensione.creaRecensione();
        rec.setValutazione(3);

        // Il servizio deve impedire l'invio della recensione se lo scambio è ancora in corso
        Boolean esito = servizio.inviaRecensione(req.getIdRichiesta(), "RiceRec_Rob", rec);
        assertFalse(esito, "Non deve essere possibile inviare una recensione su uno scambio non completato");
    }

    @Test
    @DisplayName("Statistiche – Calcolo corretto della media recensioni con più valutazioni")
    public void testCalcoloMediaRecensioniMultiplo() throws Exception {
        // GIVEN: un utente che riceve due recensioni distinte in scambi differenti
        RichiestaScambio req1 = creaRichiestaCompletata("U1", "UtenteValutato", "ann-med1");
        Recensione rec1 = Recensione.creaRecensione();
        rec1.setValutazione(2);
        servizio.inviaRecensione(req1.getIdRichiesta(), "UtenteValutato", rec1);

        RichiestaScambio req2 = creaRichiestaCompletata("U2", "UtenteValutato", "ann-med2");
        Recensione rec2 = Recensione.creaRecensione();
        rec2.setValutazione(4);
        servizio.inviaRecensione(req2.getIdRichiesta(), "UtenteValutato", rec2);

        // THEN: la media calcolata sul DB deve rispecchiare esattamente la media matematica (2 + 4) / 2 = 3.0
        GestoreLeveling leveling = new GestoreLeveling(DatabaseCore.getDB());
        StatisticheUtente stats = leveling.getStatistiche("UtenteValutato");

        assertEquals(2, stats.getNumeroRecensioni(), "Le recensioni totali registrate devono essere 2");
        assertEquals(3.0, stats.getMediaRecensioni(), 0.01, "La media tra 2 e 4 deve risultare esattamente 3.0");
    }

    @Test
    @DisplayName("Verifica attestazione e progressione dei livelli utente")
    public void testAttestazioneLivelliUtente() throws Exception {
        GestoreLeveling leveling = new GestoreLeveling(DatabaseCore.getDB());
        String utente = "UtenteLivelli";

        //GIVEN: un utente appena registrato (0 scambi completati) parte dal livello base
        StatisticheUtente statsIniziale = leveling.getStatistiche(utente);
        assertNotNull(statsIniziale.getLivello(), "Il livello iniziale non deve essere nullo");

        //WHEN: l'utente porta a termine con successo diversi scambi con relative recensioni positive
        for (int i = 1; i <= 3; i++) {
            RichiestaScambio req = creaRichiestaCompletata("Controparte" + i, utente, "ann-lvl" + i);
            Recensione rec = Recensione.creaRecensione();
            rec.setValutazione(5);
            servizio.inviaRecensione(req.getIdRichiesta(), utente, rec);
        }

        // THEN: il sistema aggiorna le statistiche e l'attestazione del livello
        StatisticheUtente statsAggiornata = leveling.getStatistiche(utente);
        assertTrue(statsAggiornata.getScambiCompletati() >= 3, "Il conteggio degli scambi completati deve essere almeno 3");
        assertNotNull(statsAggiornata.getLivello(), "Il sistema deve aver attestato un livello valido");
        assertEquals(statsAggiornata.getMediaRecensioni(),5); //la media deve essere correttamente calcolata sulla base degli scambi completati
    }
}