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
        mainContainer.setWidth("90%");
        mainContainer.getElement().getStyle().setProperty("margin", "20px auto");
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainContainer.setSpacing(0);

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.setSpacing(15);
        mainPanel.getElement().getStyle().setProperty("fontFamily", "Segoe UI, Tahoma, sans-serif");

        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "aggiungi_servizio"));
        }

        HTML titolo = new HTML(
            "<div style=\"background-color: #1b263b; color: #ffffff; padding: 25px; border-radius: 12px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); font-family: 'Segoe UI', Tahoma, sans-serif; margin-bottom: 25px;\">" +
            "<h1 style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 28px; font-weight: bold;\">Crea la tua Wishlist</h1>" +
            "<p style=\"margin: 0; font-size: 16px; color: #e0f2f1;\">Specifica il servizio che cerchi e aggiungilo alla lista dei desideri!</p>" +
            "</div>"
        );
        mainPanel.add(titolo);

        //Form di inserimento
        VerticalPanel formPanel = new VerticalPanel();
        formPanel.setWidth("50%");
        formPanel.setSpacing(10);
        formPanel.getElement().getStyle().setProperty("background", "#f8f9fa");
        formPanel.getElement().getStyle().setProperty("padding", "20px");
        formPanel.getElement().getStyle().setProperty("borderRadius", "8px");
        formPanel.getElement().getStyle().setProperty("boxShadow", "0 2px 4px rgba(0,0,0,0.1)");

        //Servizio Richiesto
        Label lblServizio = new Label("Servizio Richiesto:");
        lblServizio.getElement().getStyle().setProperty("fontWeight", "bold");
        TextBox txtServizio = new TextBox();
        txtServizio.getElement().setId("txt-servizio");
        txtServizio.setWidth("100%");

        //Tag Competenza
        Label lblTag = new Label("Tag Competenza:");
        lblTag.getElement().getStyle().setProperty("fontWeight", "bold");
        TextBox txtTag = new TextBox();
        txtTag.getElement().setId("txt-tag");
        txtTag.setWidth("100%");

        //Descrizione
        Label lblDescrizione = new Label("Descrizione dettagliata:");
        lblDescrizione.getElement().getStyle().setProperty("fontWeight", "bold");
        TextArea txtDescrizione = new TextArea();
        txtDescrizione.getElement().setId("txt-descrizione");
        txtDescrizione.setWidth("100%");
        txtDescrizione.setVisibleLines(5);

        //Checkbox per il parametro booleano isPubblica
        CheckBox chkPubblica = new CheckBox("Rendi pubblico");
        chkPubblica.getElement().setId("chk-pubblica"); 
        chkPubblica.getElement().getStyle().setProperty("fontWeight", "bold");
        chkPubblica.setValue(true); // Impostiamo di default su true

        Label lblMessaggio = new Label();
        lblMessaggio.getElement().setId("msg-wishlist");
        lblMessaggio.getElement().getStyle().setProperty("fontWeight", "bold");
        lblMessaggio.getElement().getStyle().setProperty("marginTop", "10px");

        Button btnSalva = new Button("💾 Aggiungi alla Wishlist");
        btnSalva.getElement().setId("btn-aggiungi-servizio");
        btnSalva.getElement().getStyle().setProperty("background", "#ffffff");
        btnSalva.getElement().getStyle().setProperty("color", "#1b263b");
        btnSalva.getElement().getStyle().setProperty("padding", "10px 20px");
        btnSalva.getElement().getStyle().setProperty("border", "2px solid #1b263b");
        btnSalva.getElement().getStyle().setProperty("borderRadius", "4px");
        btnSalva.getElement().getStyle().setProperty("cursor", "pointer");

        //Azione al click del bottone
        btnSalva.addClickHandler(event -> {
            String servizio = txtServizio.getText().trim();
            String tag = txtTag.getText().trim();
            String descrizione = txtDescrizione.getText().trim();
            boolean isPubblica = chkPubblica.getValue();

            if (servizio.isEmpty() || tag.isEmpty() || descrizione.isEmpty()) {
                lblMessaggio.setText("Errore: Compila tutti i campi di testo.");
                lblMessaggio.getElement().getStyle().setProperty("color", "red");
                return;
            }

            //Crea elemento della wishlist
            ServizioRichiesto nuovaWishlist = new ServizioRichiesto(servizio, tag, descrizione, isPubblica);

            //Chiamata al backend
            wishlistService.aggiungiServizio(utenteLoggato.getMail(), nuovaWishlist, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    lblMessaggio.setText("Errore di comunicazione: " + caught.getMessage());
                    lblMessaggio.getElement().getStyle().setProperty("color", "red");
                }

                @Override
                public void onSuccess(Boolean success) {
                    if (success) {
                        lblMessaggio.setText("Elemento aggiunto alla Wishlist!");
                        lblMessaggio.getElement().getStyle().setProperty("color", "green");
                        // Svuota i campi dopo il salvataggio
                        txtServizio.setText("");
                        txtTag.setText("");
                        txtDescrizione.setText("");
                        chkPubblica.setValue(true);
                    } else {
                        lblMessaggio.setText("Errore durante il salvataggio.");
                        lblMessaggio.getElement().getStyle().setProperty("color", "red");
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
        formPanel.add(chkPubblica); 
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