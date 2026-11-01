package it.unibo;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class MiaWishlistGUI {

    private Utente utenteLoggato;
    private final WishlistServiceAsync wishlistService = GWT.create(WishlistService.class);

    public void mostra(Utente utente) {
        this.utenteLoggato = utente;

        VerticalPanel mainContainer = new VerticalPanel();
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainContainer.addStyleName("mainContainer");

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.addStyleName("pannelloPrincipale");

        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "wishlist"));
        }

        HTML titolo = new HTML(
            "<div>" +
            "<h1>La Mia Wishlist</h1>" +
            "<p>Tutti i servizi che hai richiesto</p>" +
            "</div>"
        );
        titolo.addStyleName("stylePannello");

        mainPanel.add(titolo);

        //Collegamento all'interfaccia per aggiungere servizi alla wishlist
        Button btnAggiungi = new Button("➕ Aggiungi servizio");
        btnAggiungi.getElement().setId("btn-vai-aggiungi-servizio");
        btnAggiungi.addStyleName("btnAggiungi-wishlist");
        btnAggiungi.addClickHandler(event -> new CreaWishlistGUI().mostra(utenteLoggato));

        // Contenitore delle card, riempito dopo la risposta del server
        VerticalPanel listaPanel = new VerticalPanel();
        listaPanel.getElement().setId("lista-wishlist");
        listaPanel.setHorizontalAlignment(HasAutoHorizontalAlignment.ALIGN_CENTER);
        listaPanel.addStyleName("listaPanel-wishlist");

        Label lblStato = new Label("Caricamento...");
        lblStato.addStyleName("lbl-wishlist");
        listaPanel.add(lblStato);

        mainPanel.add(listaPanel);
        mainPanel.add(btnAggiungi);

        mainContainer.add(mainPanel);

        RootPanel.get().clear();
        RootPanel.get().add(mainContainer);

        caricaWishlist(listaPanel, lblStato);
    }

    private void caricaWishlist(VerticalPanel listaPanel, Label lblStato) {
        wishlistService.getWishlistUtente(utenteLoggato.getMail(), new AsyncCallback<ArrayList<ServizioRichiesto>>() {
            @Override
            public void onFailure(Throwable caught) {
                lblStato.setText("Errore nel caricamento: " + caught.getMessage());
                lblStato.addStyleName("testo-errore");
            }

            @Override
            public void onSuccess(ArrayList<ServizioRichiesto> wishlist) {
                listaPanel.clear();

                if (wishlist == null || wishlist.isEmpty()) {
                    Label vuoto = new Label("Wishlist vuota.");
                    vuoto.getElement().setId("msg-wishlist-vuota");
                    vuoto.addStyleName("lbl-wishlist");
                    listaPanel.add(vuoto);
                    return;
                }

                for(ServizioRichiesto w : wishlist){
                    //Crea contenitore per raggruppare la wishlist e i suoi suggerimenti
                    VerticalPanel bloccoWishlist = new VerticalPanel();
                    bloccoWishlist.addStyleName("larghezzaMax");
                    
                    // Aggiunge la tua card originale
                    bloccoWishlist.add(creaCard(w));
                    
                    // Aggiunge sotto i profili suggeriti per QUEL tag
                    ProfiliSuggeritiWidget suggerimenti = new ProfiliSuggeritiWidget(utenteLoggato.getMail(), w.getTagCompetenza());
                    bloccoWishlist.add(suggerimenti);
                    
                    // Aggiunge tutto alla lista principale
                    listaPanel.add(bloccoWishlist);
                }
            }
        });
    }

    private HTML creaCard(ServizioRichiesto w) {
        // htmlEscape evita che il testo inserito dall'utente venga interpretato come HTML
        String servizio = SafeHtmlUtils.htmlEscape(w.getNomeServizio());
        String tag = SafeHtmlUtils.htmlEscape(w.getTagCompetenza());
        String descrizione = SafeHtmlUtils.htmlEscape(w.getDescrizione());

        HTML card = new HTML(
            "<div class='contenitore'>" +
            "<h3 class='titolo'>" + servizio + "</h3>" +
            "<span class='tag'>" + tag + "</span>" +
            "<p class='paragrafo'>" + descrizione + "</p>" +
            "</div>"
        );
        //card.setWidth("100%");
        card.addStyleName("card-wishlist");
        card.addStyleName("larghezzaMax");
        card.addStyleName("card-servizio");
        return card;
    }
}