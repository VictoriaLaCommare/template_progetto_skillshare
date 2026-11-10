package it.unibo;

import java.util.ArrayList;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class WishlistServiceImplTest {

    // Accensione database fittizio
    @BeforeAll
    public static void testmode() {
        DatabaseCore.enableTestMode();
    }

    // Spegnimento database fittizio
    @AfterAll
    public static void testmodeoff() {
        DatabaseCore.disableTestMode();
    }

    //Test sulla sequenza principale: aggiungi servizio
    @Test
    @DisplayName("Aggiungi Servizio: scenario di successo, il servizio viene salvato con tutti i campi")
    public void testAggiungiServizio_Successo() throws Exception {
        WishlistServiceImpl servizio = new WishlistServiceImpl();
        String mail = "wl_successo@test.it";
        ServizioRichiesto nuovo = new ServizioRichiesto("Javascript", "Programmazione", "Aiuto con le promise");

        Boolean esito = servizio.aggiungiServizio(mail, nuovo);

        assertTrue(esito, "L'aggiunta di un servizio valido deve restituire true");

        ArrayList<ServizioRichiesto> wishlist = servizio.getWishlistUtente(mail);
        assertEquals(1, wishlist.size(), "La wishlist deve contenere esattamente un servizio");

        ServizioRichiesto salvato = wishlist.get(0);
        assertEquals("Javascript", salvato.getNomeServizio());
        assertEquals("Programmazione", salvato.getTagCompetenza());
        assertEquals("Aiuto con le promise", salvato.getDescrizione());
    }

    @Test
    @DisplayName("Aggiungi Servizio: più servizi si accumulano nella stessa wishlist, in ordine")
    public void testAggiungiServizio_PiuServiziStessoUtente() throws Exception {
        WishlistServiceImpl servizio = new WishlistServiceImpl();
        String mail = "wl_multi@test.it";

        servizio.aggiungiServizio(mail, new ServizioRichiesto("Java", "Programmazione", "Primo"));
        servizio.aggiungiServizio(mail, new ServizioRichiesto("Inglese", "Lingue", "Secondo"));
        servizio.aggiungiServizio(mail, new ServizioRichiesto("Cucina", "Hobby", "Terzo"));

        ArrayList<ServizioRichiesto> wishlist = servizio.getWishlistUtente(mail);

        assertEquals(3, wishlist.size(), "Il secondo e il terzo servizio non devono sovrascrivere il primo");
        assertEquals("Java", wishlist.get(0).getNomeServizio());
        assertEquals("Inglese", wishlist.get(1).getNomeServizio());
        assertEquals("Cucina", wishlist.get(2).getNomeServizio());
    }

    @Test
    @DisplayName("Aggiungi Servizio: ogni utente vede solo la propria wishlist")
    public void testAggiungiServizio_UtentiDiversiSeparati() throws Exception {
        WishlistServiceImpl servizio = new WishlistServiceImpl();
        String mailA = "wl_utenteA@test.it";
        String mailB = "wl_utenteB@test.it";

        servizio.aggiungiServizio(mailA, new ServizioRichiesto("ServizioA", "TagA", "DescA"));
        servizio.aggiungiServizio(mailB, new ServizioRichiesto("ServizioB", "TagB", "DescB"));

        ArrayList<ServizioRichiesto> wishlistA = servizio.getWishlistUtente(mailA);
        ArrayList<ServizioRichiesto> wishlistB = servizio.getWishlistUtente(mailB);

        assertEquals(1, wishlistA.size());
        assertEquals(1, wishlistB.size());
        assertEquals("ServizioA", wishlistA.get(0).getNomeServizio());
        assertEquals("ServizioB", wishlistB.get(0).getNomeServizio());
    }

    @Test
    @DisplayName("Aggiungi Servizio: i dati restano salvati anche usando una nuova istanza del servizio")
    public void testAggiungiServizio_PersistenzaTraIstanze() throws Exception {
        String mail = "wl_persistenza@test.it";

        new WishlistServiceImpl().aggiungiServizio(mail,
                new ServizioRichiesto("Fotografia", "Arte", "Ritratti"));

        // Una nuova istanza simula una nuova richiesta al server: i dati devono essere nel DB
        ArrayList<ServizioRichiesto> wishlist = new WishlistServiceImpl().getWishlistUtente(mail);

        assertEquals(1, wishlist.size(), "Il servizio deve essere stato scritto nel database");
        assertEquals("Fotografia", wishlist.get(0).getNomeServizio());
    }

    // aggiungiServizio: casi limite sulla mail

    @Test
    @DisplayName("Aggiungi Servizio: mail nulla lancia un'eccezione")
    public void testAggiungiServizio_MailNulla() {
        WishlistServiceImpl servizio = new WishlistServiceImpl();

        assertThrows(Exception.class,
                () -> servizio.aggiungiServizio(null, new ServizioRichiesto("X", "Y", "Z")),
                "Con mail nulla il salvataggio deve fallire");
    }

    @Test
    @DisplayName("Aggiungi Servizio: mail vuota lancia un'eccezione")
    public void testAggiungiServizio_MailVuota() {
        WishlistServiceImpl servizio = new WishlistServiceImpl();

        assertThrows(Exception.class,
                () -> servizio.aggiungiServizio("", new ServizioRichiesto("X", "Y", "Z")),
                "Con mail vuota il salvataggio deve fallire");
    }

    // getWishlistUtente

    @Test
    @DisplayName("Get Wishlist: utente senza wishlist riceve una lista vuota, non null")
    public void testGetWishlist_UtenteSenzaWishlist() throws Exception {
        WishlistServiceImpl servizio = new WishlistServiceImpl();

        ArrayList<ServizioRichiesto> wishlist = servizio.getWishlistUtente("wl_nessuna@test.it");

        assertNotNull(wishlist, "La lista non deve essere null");
        assertTrue(wishlist.isEmpty(), "Un utente senza servizi deve avere una lista vuota");
    }

    @Test
    @DisplayName("Get Wishlist: mail nulla lancia un'eccezione")
    public void testGetWishlist_MailNulla() {
        WishlistServiceImpl servizio = new WishlistServiceImpl();

        assertThrows(Exception.class, () -> servizio.getWishlistUtente(null),
                "Con mail nulla la lettura deve fallire");
    }

    @Test
    @DisplayName("Get Wishlist: mail vuota lancia un'eccezione")
    public void testGetWishlist_MailVuota() {
        WishlistServiceImpl servizio = new WishlistServiceImpl();

        assertThrows(Exception.class, () -> servizio.getWishlistUtente(""),
                "Con mail vuota la lettura deve fallire");
    }
}