package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class PersonalizzaProfiloGui {

    //Collegamento con il backend
    private final GestoreProfiloAsync rpcService = GWT.create(GestoreProfilo.class);
    
    private void evidenzia(Widget w, boolean valido) {
        if (valido) {
            w.removeStyleName("input-errore");
        } else {
            w.addStyleName("input-errore");
        }
    }

    private void mostraErrore(Label lbl, String testo) {
        lbl.removeStyleName("testo-successo");
        lbl.addStyleName("testo-errore");
        lbl.setText(testo);
    }

    private void mostraSuccesso(Label lbl, String testo) {
        lbl.removeStyleName("testo-errore");
        lbl.addStyleName("testo-successo");
        lbl.setText(testo);
    }

    //Riceviamo l'utente attualmente loggato -> precondizione casi d'uso
    public void mostra(Utente utenteLoggato) {
        VerticalPanel mainContainer = new VerticalPanel();
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainContainer.addStyleName("mainContainer");

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.addStyleName("pannelloPrincipale");

        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "profilo"));
        }

        HTML titolo = new HTML(
            "<div>" +
            "<h1>Personalizza il tuo Profilo</h1>" +
            "<p>Aggiorna i tuoi dati per farti conoscere meglio dalla community!</p>" +
            "</div>"
        );
        titolo.addStyleName("stylePannello");

        mainPanel.add(titolo);

        //Bottone per passare alla vista di visualizzazione del proprio profilo
        Button btnVisualizzaProfilo = new Button("👀 Visualizza il mio profilo pubblico");
        btnVisualizzaProfilo.addStyleName("btnVisualizzaProfilo-personalizzaProfilo");
        
        btnVisualizzaProfilo.addClickHandler(event -> {
            //Apre il popup/vista del profilo passando l'utente loggato e il proprio username
            new ProfiloUtenteGUI().mostra(utenteLoggato, utenteLoggato.getUsername());
        });

        mainPanel.add(btnVisualizzaProfilo);

        //Inserimento la bio in una TextArea
        TextArea txtBio = new TextArea();
        txtBio.getElement().setPropertyString("placeholder", "Scrivi una breve bio su di te...");
        txtBio.getElement().setId("prof-bio");
        txtBio.setVisibleLines(4);
        txtBio.addStyleName("txtBio-personalizzaProfilo");
        if (utenteLoggato.getBio() != null) txtBio.setText(utenteLoggato.getBio()); //Precompilazione con la bio attuale dell'utente
        
        Label errBio = new Label();
        errBio.addStyleName("testo-errore");
        HorizontalPanel rowBio = new HorizontalPanel();
        rowBio.addStyleName("form-row");
        rowBio.add(txtBio);
        rowBio.add(errBio);

        //Inserimento URL immagine nella TextBox
        TextBox txtFoto = new TextBox();
        txtFoto.getElement().setPropertyString("placeholder", "URL della tua foto profilo");
        txtFoto.getElement().setId("prof-foto");
        txtFoto.addStyleName("larghezzaBox");
        if (utenteLoggato.getImmagineProfilo() != null) txtFoto.setText(utenteLoggato.getImmagineProfilo());
        
        Label errFoto = new Label();
        errFoto.addStyleName("testo-errore");
        HorizontalPanel rowFoto = new HorizontalPanel();
        rowFoto.addStyleName("form-row");
        rowFoto.add(txtFoto);
        rowFoto.add(errFoto);

        //Tag competenze
        TextBox txtTag = new TextBox();
        txtTag.getElement().setPropertyString("placeholder", "Competenze");
        txtTag.getElement().setId("prof-tag");
        txtTag.addStyleName("larghezzaBox");
        if (utenteLoggato.getTagCompetenze() != null) txtTag.setText(utenteLoggato.getTagCompetenze());
        
        Label errTag = new Label();
        errTag.addStyleName("testo-errore");
        HorizontalPanel rowTag = new HorizontalPanel();
        rowTag.addStyleName("form-row");
        rowTag.add(txtTag);
        rowTag.add(errTag);

        //Bottone e messaggi
        Button btnSalva = new Button("Salva modifiche");
        btnSalva.getElement().setId("prof-submit");

        Label lblMessaggio = new Label();
        lblMessaggio.getElement().setId("prof-message");

        btnSalva.addClickHandler(event -> {
            //Reset dei messaggi di errore
            errBio.setText("");
            errFoto.setText("");
            errTag.setText("");
            lblMessaggio.setText("");

            //Validazione lato client (sequenza alternativa 4a)
            boolean bioOk = FieldVerifier.isValidBio(txtBio.getText());
            boolean tagOk = FieldVerifier.isValidTag(txtTag.getText());
            
            evidenzia(txtBio, bioOk);
            if (!bioOk) errBio.setText("La bio non è valida o è troppo lunga");

            evidenzia(txtTag, tagOk);
            if (!tagOk) errTag.setText("Formato tag non valido");

            if (!bioOk || !tagOk) {
                mostraErrore(lblMessaggio, "Controlla gli errori segnalati accanto ai campi e riprova.");
                return;
            }

            //Aggiorniamo l'utente con i nuovi dati
            utenteLoggato.setBio(txtBio.getText());
            utenteLoggato.setTagCompetenze(txtTag.getText());
            utenteLoggato.setImmagineProfilo(txtFoto.getText()); 

            //Chiamata al server
            rpcService.aggiornaProfilo(utenteLoggato, new AsyncCallback<Utente>() {
                @Override
                public void onFailure(Throwable caught) {
                    mostraErrore(lblMessaggio, "Qualcosa è andato storto durante le modifiche: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Utente result) {
                    //Sequenza principale (5): successo
                    mostraSuccesso(lblMessaggio, "Profilo aggiornato con successo!");
                    
                    //I bordi tornano normali
                    evidenzia(txtBio, true);
                    evidenzia(txtFoto, true);
                    evidenzia(txtTag, true);
                }
            });
        });

        //Aggiunta righe al pannello centrale
        mainPanel.add(rowBio);
        mainPanel.add(rowFoto);
        mainPanel.add(rowTag);
        mainPanel.add(btnSalva);
        mainPanel.add(lblMessaggio);

        mainContainer.add(mainPanel);

        //Stampo a schermo pulendo quello che c'era prima
        RootPanel.get().clear();
        RootPanel.get().add(mainContainer);
    }
}