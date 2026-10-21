package it.unibo;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServizioRichiestoTest {

    @Test
    @DisplayName("Costruttore completo: tutti i campi vengono valorizzati")
    public void testCostruttoreCompleto() {
        ServizioRichiesto s = new ServizioRichiesto("Javascript", "Programmazione", "Aiuto con le promise", true);

        assertEquals("Javascript", s.getNomeServizio());
        assertEquals("Programmazione", s.getTagCompetenza());
        assertEquals("Aiuto con le promise", s.getDescrizione());
        assertTrue(s.isPubblica());
    }

    @Test
    @DisplayName("Costruttore completo: la visibilità privata viene conservata")
    public void testCostruttoreVisibilitaPrivata() {
        ServizioRichiesto s = new ServizioRichiesto("Chitarra", "Musica", "Lezioni", false);

        assertFalse(s.isPubblica());
    }

    @Test
    @DisplayName("Costruttore vuoto (richiesto da GWT RPC): valori di default")
    public void testCostruttoreVuoto() {
        ServizioRichiesto s = new ServizioRichiesto();

        assertNull(s.getNomeServizio());
        assertNull(s.getTagCompetenza());
        assertNull(s.getDescrizione());
        assertFalse(s.isPubblica(), "Di default il servizio non è pubblico");
    }

    @Test
    @DisplayName("Setter: aggiornano i campi")
    public void testSetter() {
        ServizioRichiesto s = new ServizioRichiesto();

        s.setNomeServizio("Inglese");
        s.setTagCompetenza("Lingue");
        s.setDescrizione("Conversazione");
        s.setPubblica(true);

        assertEquals("Inglese", s.getNomeServizio());
        assertEquals("Lingue", s.getTagCompetenza());
        assertEquals("Conversazione", s.getDescrizione());
        assertTrue(s.isPubblica());
    }

    @Test
    @DisplayName("Setter: la visibilità può essere cambiata da pubblica a privata")
    public void testSetterCambioVisibilita() {
        ServizioRichiesto s = new ServizioRichiesto("Cucina", "Hobby", "Ricette", true);

        s.setPubblica(false);

        assertFalse(s.isPubblica());
    }
}