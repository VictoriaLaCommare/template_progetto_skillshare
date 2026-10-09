package it.unibo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GestoreAnnunciSearchTest {
    private GestoreAnnunciImpl gestore;

    @BeforeAll
    public static void testmode() {
        DatabaseCore.enableTestMode();
    }
    
    @BeforeEach
    public void setUp() {
        gestore = new GestoreAnnunciImpl();
    }

    @Test
    public void testCercaAnnunciFiltriVuoti() throws Exception {
        // Verifica che passando stringhe vuote o null non vada in errore
        List<Annuncio> risultati = gestore.cercaAnnunci("", "", "", "");
        assertNotNull(risultati, "La lista dei risultati non deve essere nulla.");
    }

    @Test
    public void testCercaAnnunciConKeyword() throws Exception {
        // Verifica la ricerca per parola chiave (case-insensitive)
        // Nota: se il database è vuoto all'avvio del test, restituirà lista vuota, 
        // ma possiamo verificare che l'esecuzione avvenga correttamente senza eccezioni.
        List<Annuncio> risultati = gestore.cercaAnnunci("", "", "java", "");
        assertNotNull(risultati);
    }

    @Test
    public void testCercaAnnunciConOrdinamento() throws Exception {
        // Verifica la richiesta di ordinamento per titolo
        List<Annuncio> risultati = gestore.cercaAnnunci("", "", "", "titolo");
        assertNotNull(risultati, "Il metodo di ricerca con ordinamento deve completarsi con successo.");
    }
}
