package it.unibo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchmakingServiceImplTest {

    private MatchmakingServiceImpl matchmakingService;
    private HTreeMap<String, Utente> utentiDBTest;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        //Attiva la modalità test per isolare l'ambiente
        DatabaseCore.enableTestMode();
        DB dbTest = DatabaseCore.getDB();
        
        //Inizializza la mappa degli utenti per inserire i dati di prova
        utentiDBTest = (HTreeMap<String, Utente>) dbTest.hashMap("utenti", Serializer.STRING, Serializer.JAVA).createOrOpen();

        //Creazione degli utenti mock
        Utente anna = new Utente("Anna", "Bianchi");
        anna.setMail("anna@unibo.it");
        anna.setTagCompetenze("java, python");
        anna.setPassword("passwordSegreta");

        Utente marco = new Utente("Marco", "Rossi");
        marco.setMail("marco@unibo.it");
        marco.setTagCompetenze("javascript, react");

        Utente luigi = new Utente("Luigi", "Verdi");
        luigi.setMail("luigi@unibo.it");
        luigi.setTagCompetenze("java, chitarra");

        //Salvataggio nel DB in memoria
        utentiDBTest.put(anna.getMail(), anna);
        utentiDBTest.put(marco.getMail(), marco);
        utentiDBTest.put(luigi.getMail(), luigi);
        DatabaseCore.commit();

        //Inizializzazione del servizio (non serve chiamare init() perché i metodi interni 
        // richiamano in automatico initDb() in modo sicuro)
        matchmakingService = new MatchmakingServiceImpl();
    }

    @AfterEach
    void tearDown() {
        // Pulisce e chiude il DB in memoria alla fine di ogni test
        DatabaseCore.disableTestMode();
    }

    @Test
    void testTrovaProfiliCompatibili_MatchTrovatoESicurezza() throws Exception {
        //Anna cerca qualcuno che sappia usare "java".
        List<Utente> risultati = matchmakingService.trovaProfiliCompatibili("anna@unibo.it", "java");

        assertNotNull(risultati);
        assertEquals(1, risultati.size(), "Dovrebbe trovare solo 1 utente compatibile (Luigi)"); 
        
        Utente match = risultati.get(0);
        assertEquals("Luigi", match.getNome()); 
        
        //Verifica cruciale: Anna non deve essere suggerita a se stessa
        assertNotEquals("anna@unibo.it", match.getMail()); 
        
        //Verifica di sicurezza: il server deve aver rimosso la password (metodo senzaPassword)
        assertNull(match.getPassword(), "La password non deve mai essere inviata al client");
    }

    @Test
    void testTrovaProfiliCompatibili_NessunMatch() throws Exception {
        //Ricerca di una competenza inesistente nel database
        List<Utente> risultati = matchmakingService.trovaProfiliCompatibili("anna@unibo.it", "cinese");
        
        assertNotNull(risultati);
        assertTrue(risultati.isEmpty(), "La lista dei risultati deve essere vuota");
    }

    @Test
    void testSalvaProfilo_Successo() throws Exception {
        //Anna salva il profilo di Marco
        Boolean risultato = matchmakingService.salvaProfilo("anna@unibo.it", "marco@unibo.it");
        assertTrue(risultato, "Il salvataggio deve restituire true");

        //Verifica tramite l'altro metodo esposto 
        List<String> salvati = matchmakingService.getProfiliSalvati("anna@unibo.it");
        assertEquals(1, salvati.size());
        assertTrue(salvati.contains("marco@unibo.it"));
    }

    @Test
    void testSalvaProfilo_AutoSalvataggio_LanciaEccezione() {
        //Anna prova a salvare se stessa
        Exception exception = assertThrows(Exception.class, () -> {
            matchmakingService.salvaProfilo("anna@unibo.it", "anna@unibo.it");
        });
        
        //Verifica che il messaggio di errore sia quello previsto
        assertEquals("Non puoi salvare il tuo stesso profilo", exception.getMessage());
    }
}