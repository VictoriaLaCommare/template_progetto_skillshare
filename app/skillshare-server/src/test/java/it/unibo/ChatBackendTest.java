package it.unibo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatBackendTest {

    private ChatServiceImpl chatService; 
    private String testIdRichiesta;

    @BeforeAll
    public static void testmode() {
        DatabaseCore.enableTestMode();
    }
    
    @BeforeEach
    public void setUp() {
        chatService = new ChatServiceImpl();
        testIdRichiesta = "test-chat-123";
    }

    @Test
    public void testInviaERecuperaMessaggio() throws Exception {
        String mittente = "utenteTest";
        String testo = "Ciao, questo è un messaggio di test!";
        Messaggio nuovoMessaggio = new Messaggio(testIdRichiesta, mittente, testo, new Date());

        Messaggio risultato = chatService.inviaMessaggio(nuovoMessaggio);
        
        assertNotNull(risultato, "Il messaggio restituito non deve essere nullo");
        assertEquals(testo, risultato.getTesto(), "Il testo del messaggio deve corrispondere");
        assertEquals(mittente, risultato.getMittente(), "Il mittente deve corrispondere");

        List<Messaggio> storico = chatService.getStoricoMessaggi(testIdRichiesta);
        assertNotNull(storico, "Lo storico non deve essere nullo");
        assertFalse(storico.isEmpty(), "Lo storico non deve essere vuoto");

       boolean trovato = false;
        for (Messaggio m : storico) {
            if (m.getTesto().equals(testo) && m.getMittente().equals(mittente)) {
                trovato = true;
                break; 
            }
        }
        
        assertTrue(trovato, "Il messaggio inviato deve essere presente nello storico");
    }
}