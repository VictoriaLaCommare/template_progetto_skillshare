package it.unibo;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServizioRichiestoTest {

    @Test
    @DisplayName("Costruttore completo: tutti i campi vengono valorizzati")
    public void testCostruttoreCompleto() {
        ServizioRichiesto s = new ServizioRichiesto("Javascript", "Programmazione", "Aiuto con le promise"/*, true*/);

        assertEquals("Javascript", s.getNomeServizio());
        assertEquals("Programmazione", s.getTagCompetenza());
        assertEquals("Aiuto con le promise", s.getDescrizione());
    }

    @Test
    @DisplayName("Costruttore vuoto (richiesto da GWT RPC): valori di default")
    public void testCostruttoreVuoto() {
        ServizioRichiesto s = new ServizioRichiesto();

        assertNull(s.getNomeServizio());
        assertNull(s.getTagCompetenza());
        assertNull(s.getDescrizione());
    }

    @Test
    @DisplayName("Setter: aggiornano i campi")
    public void testSetter() {
        ServizioRichiesto s = new ServizioRichiesto();

        s.setNomeServizio("Inglese");
        s.setTagCompetenza("Lingue");
        s.setDescrizione("Conversazione");

        assertEquals("Inglese", s.getNomeServizio());
        assertEquals("Lingue", s.getTagCompetenza());
        assertEquals("Conversazione", s.getDescrizione());
    }
}