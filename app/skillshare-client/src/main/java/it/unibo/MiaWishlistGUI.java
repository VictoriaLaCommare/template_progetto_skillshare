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
        mainContainer.setWidth("90%");
        mainContainer.getElement().getStyle().setProperty("margin", "20px auto");
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.setSpacing(15);
        mainPanel.getElement().getStyle().setProperty("fontFamily", "Segoe UI, Tahoma, sans-serif");

        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "wishlist"));
        }

        HTML titolo = new HTML(
            "<div style=\"background-color: #1b263b; color: #ffffff; padding: 25px; border-radius: 12px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); margin-bottom: 25px;\">" +
            "<h1 style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 28px; font-weight: bold;\">La mia Wishlist</h1>" +
            "<p style=\"margin: 0; font-size: 16px; color: #e0f2f1;\">Tutti i servizi che hai richiesto</p>" +
            "</div>"
        );
        mainPanel.add(titolo);

        //Collegamento all'interfaccia per aggiungere servizi alla wishlist
        Button btnAggiungi = new Button("➕ Aggiungi servizio");
        btnAggiungi.getElement().getStyle().setProperty("background", "#ffffff");
        btnAggiungi.getElement().getStyle().setProperty("color", "#1b263b");
        btnAggiungi.getElement().getStyle().setProperty("border", "2px solid #1b263b");
        btnAggiungi.getElement().getStyle().setProperty("padding", "10px 20px");
        btnAggiungi.getElement().getStyle().setProperty("borderRadius", "4px");
        btnAggiungi.getElement().getStyle().setProperty("fontWeight", "bold");
        btnAggiungi.getElement().getStyle().setProperty("cursor", "pointer");
        btnAggiungi.addClickHandler(event -> new CreaWishlistGUI().mostra(utenteLoggato));
        mainPanel.add(btnAggiungi);

        // Contenitore delle card, riempito dopo la risposta del server
        VerticalPanel listaPanel = new VerticalPanel();
        listaPanel.setWidth("50%");
        listaPanel.setSpacing(10);

        Label lblStato = new Label("Caricamento...");
        lblStato.getElement().getStyle().setProperty("fontWeight", "bold");
        listaPanel.add(lblStato);

        mainPanel.add(listaPanel);
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
                lblStato.getElement().getStyle().setProperty("color", "red");
            }

            @Override
            public void onSuccess(ArrayList<ServizioRichiesto> wishlist) {
                listaPanel.clear();

                if (wishlist == null || wishlist.isEmpty()) {
                    Label vuoto = new Label("Wishlist vuota.");
                    vuoto.getElement().getStyle().setProperty("fontWeight", "bold");
                    listaPanel.add(vuoto);
                    return;
                }

                for (ServizioRichiesto w : wishlist) {
                    listaPanel.add(creaCard(w));
                }
            }
        });
    }

    private HTML creaCard(ServizioRichiesto w) {
        // htmlEscape evita che il testo inserito dall'utente venga interpretato come HTML
        String servizio = SafeHtmlUtils.htmlEscape(w.getNomeServizio());
        String tag = SafeHtmlUtils.htmlEscape(w.getTagCompetenza());
        String descrizione = SafeHtmlUtils.htmlEscape(w.getDescrizione());
        String visibilita = w.isPubblica() ? "Pubblica" : "Privata";
        String coloreBadge = w.isPubblica() ? "#2e7d32" : "#757575";

        HTML card = new HTML(
            "<div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);\">" +
            "<h3 style=\"margin: 0 0 8px 0; color: #1b263b;\">" + servizio + "</h3>" +
            "<span style=\"display: inline-block; background-color: #1b263b; color: #ffffff; padding: 2px 10px; border-radius: 12px; font-size: 12px; margin-right: 6px;\">" + tag + "</span>" +
            "<span style=\"display: inline-block; background-color: " + coloreBadge + "; color: #ffffff; padding: 2px 10px; border-radius: 12px; font-size: 12px;\">" + visibilita + "</span>" +
            "<p style=\"margin: 12px 0 0 0; color: #333333;\">" + descrizione + "</p>" +
            "</div>"
        );
        card.setWidth("100%");
        return card;
    }
}