package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;


public class CreaWishlistGUI {

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
            mainPanel.add(new AppNavbar(utenteLoggato, "aggiungi_servizio"));
        }

        HTML titolo = new HTML(
            "<div>" +
            "<h1>Crea la tua Wishlist</h1>" +
            "<p>Specifica il servizio che cerchi e aggiungilo alla lista dei desideri!</p>" +
            "</div>"
        );
        titolo.addStyleName("stylePannello");

        mainPanel.add(titolo);

        //Form di inserimento
        VerticalPanel formPanel = new VerticalPanel();
        formPanel.addStyleName("formPanel-wishlist");

        //Servizio Richiesto
        Label lblServizio = new Label("Servizio Richiesto:");
        lblServizio.addStyleName("lbl-wishlist");
        TextBox txtServizio = new TextBox();
        txtServizio.getElement().setId("txt-servizio");
        //txtServizio.setWidth("100%");
        txtServizio.addStyleName("larghezzaMax");

        //Tag Competenza
        Label lblTag = new Label("Tag Competenza:");
        lblTag.addStyleName("lbl-wishlist");
        TextBox txtTag = new TextBox();
        txtTag.getElement().setId("txt-tag");
        txtTag.addStyleName("larghezzaMax");

        //Descrizione
        Label lblDescrizione = new Label("Descrizione dettagliata:");
        lblDescrizione.addStyleName("lbl-wishlist");
        TextArea txtDescrizione = new TextArea();
        txtDescrizione.getElement().setId("txt-descrizione");
        txtDescrizione.addStyleName("txtDescrizione-wishlist");
        txtDescrizione.setVisibleLines(5);

        Label lblMessaggio = new Label();
        lblMessaggio.getElement().setId("msg-wishlist");
        lblMessaggio.addStyleName("lblMessaggio-wishlist");

        Button btnSalva = new Button("💾 Aggiungi alla Wishlist");
        btnSalva.getElement().setId("btn-aggiungi-servizio");
        btnSalva.addStyleName("btnAggiungiWishlist-wishlist");

        //Azione al click del bottone
        btnSalva.addClickHandler(event -> {
            String servizio = txtServizio.getText().trim();
            String tag = txtTag.getText().trim();
            String descrizione = txtDescrizione.getText().trim();

            if (servizio.isEmpty() || tag.isEmpty() || descrizione.isEmpty()) {
                lblMessaggio.setText("Errore: Compila tutti i campi di testo.");
                lblMessaggio.addStyleName("testo-errore");
                return;
            }

            //Crea elemento della wishlist
            ServizioRichiesto nuovaWishlist = new ServizioRichiesto(servizio, tag, descrizione);

            //Chiamata al backend
            wishlistService.aggiungiServizio(utenteLoggato.getMail(), nuovaWishlist, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    lblMessaggio.setText("Errore di comunicazione: " + caught.getMessage());
                    lblMessaggio.addStyleName("testo-errore");
                }

                @Override
                public void onSuccess(Boolean success) {
                    if (success) {
                        lblMessaggio.setText("Elemento aggiunto alla Wishlist!");
                        lblMessaggio.addStyleName("testo-successo");
                        // Svuota i campi dopo il salvataggio
                        txtServizio.setText("");
                        txtTag.setText("");
                        txtDescrizione.setText("");
                    } else {
                        lblMessaggio.setText("Errore durante il salvataggio.");
                        lblMessaggio.addStyleName("testo-errore");
                    }
                }
            });
        });

        //Aggiunta dei widget al form
        formPanel.add(lblServizio);
        formPanel.add(txtServizio);
        formPanel.add(lblTag);
        formPanel.add(txtTag);
        formPanel.add(lblDescrizione);
        formPanel.add(txtDescrizione); 
        formPanel.add(btnSalva);
        formPanel.add(lblMessaggio);
        
        mainPanel.add(formPanel);

        Button btnMie = new Button("📋 La mia wishlist");
        btnMie.addClickHandler(e -> new MiaWishlistGUI().mostra(utenteLoggato));
        mainPanel.add(btnMie);


        mainContainer.add(mainPanel);

        //Aggiorna la vista
        RootPanel.get().clear();
        RootPanel.get().add(mainContainer);
    }
}