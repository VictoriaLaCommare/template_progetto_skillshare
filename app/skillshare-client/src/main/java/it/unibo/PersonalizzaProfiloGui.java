package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class PersonalizzaProfiloGui {

    //Collegamento con il backend
    private final GestoreProfiloAsync rpcService = GWT.create(GestoreProfilo.class);
    
    private static final String COLORE_ERRORE = "2px solid #e53935";

    private void evidenzia(Widget w, boolean valido) {
        if (valido) {
            w.getElement().getStyle().clearProperty("border");
        } else {
            w.getElement().getStyle().setProperty("border", COLORE_ERRORE);
        }
    }

    private void mostraErrore(Label lbl, String testo) {
        lbl.getElement().getStyle().setColor("red");
        lbl.setText(testo);
    }

    private void mostraSuccesso(Label lbl, String testo) {
        lbl.getElement().getStyle().setColor("green");
        lbl.setText(testo);
    }

    //Riceviamo l'utente attualmente loggato -> precondizione casi d'uso
    public void mostra(Utente utenteLoggato) {
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(10);
        panel.setWidth("90%");                                                     
        panel.getElement().getStyle().setProperty("margin", "20px auto");          
        panel.add(new AppNavbar(utenteLoggato, "profilo"));                      

        panel.add(new HTML("<h1 style=\"background-color:rgb(0,100,100);color:rgb(0,0,255);\"> <em>Personalizza il tuo Profilo</em></h1> <p>Aggiorna i tuoi dati per farti conoscere meglio dalla community!</p>\n"));

        //Bottone per passare alla vista di visualizzazione del proprio profilo
        Button btnVisualizzaProfilo = new Button("👀 Visualizza il mio profilo pubblico");
        btnVisualizzaProfilo.getElement().setId("btn-vai-a-visualizza-profilo");
        btnVisualizzaProfilo.getElement().getStyle().setProperty("backgroundColor", "#1b263b");
        btnVisualizzaProfilo.getElement().getStyle().setProperty("color", "#ffffff");
        btnVisualizzaProfilo.getElement().getStyle().setProperty("padding", "8px 15px");
        btnVisualizzaProfilo.getElement().getStyle().setProperty("borderRadius", "4px");
        btnVisualizzaProfilo.getElement().getStyle().setProperty("cursor", "pointer");
        
        btnVisualizzaProfilo.addClickHandler(event -> {
            //Apre il popup/vista del profilo passando l'utente loggato e il proprio username
            new ProfiloUtenteGUI().mostra(utenteLoggato, utenteLoggato.getUsername());
        });

        panel.add(btnVisualizzaProfilo);

        //Inseriamo la bio in una TextArea
        TextArea txtBio = new TextArea();
        txtBio.getElement().setPropertyString("placeholder", "Scrivi una breve bio su di te...");
        txtBio.getElement().setId("prof-bio");
        txtBio.setVisibleLines(4);
        txtBio.setWidth("300px");
        if (utenteLoggato.getBio() != null) txtBio.setText(utenteLoggato.getBio()); // Pre-compiliamo se esiste già
        
        Label errBio = new Label();
        errBio.getElement().getStyle().setColor("red");
        HorizontalPanel rowBio = new HorizontalPanel();
        rowBio.setSpacing(5);
        rowBio.add(txtBio);
        rowBio.add(errBio);

        //Inserimento URL immagine nella TextBox
        TextBox txtFoto = new TextBox();
        txtFoto.getElement().setPropertyString("placeholder", "URL della tua foto profilo");
        txtFoto.getElement().setId("prof-foto");
        txtFoto.setWidth("300px");
        // Se avete aggiunto il campo foto in Utente.java, decommenta la riga sotto:
        if (utenteLoggato.getImmagineProfilo() != null) txtFoto.setText(utenteLoggato.getImmagineProfilo());
        
        Label errFoto = new Label();
        errFoto.getElement().getStyle().setColor("red");
        HorizontalPanel rowFoto = new HorizontalPanel();
        rowFoto.setSpacing(5);
        rowFoto.add(txtFoto);
        rowFoto.add(errFoto);

        //Tag competenze
        TextBox txtTag = new TextBox();
        txtTag.getElement().setPropertyString("placeholder", "Competenze");
        txtTag.getElement().setId("prof-tag");
        txtTag.setWidth("300px");
        if (utenteLoggato.getTagCompetenze() != null) txtTag.setText(utenteLoggato.getTagCompetenze());
        
        Label errTag = new Label();
        errTag.getElement().getStyle().setColor("red");
        HorizontalPanel rowTag = new HorizontalPanel();
        rowTag.setSpacing(5);
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
        panel.add(rowBio);
        panel.add(rowFoto);
        panel.add(rowTag);
        panel.add(btnSalva);
        panel.add(lblMessaggio);

        //Stampo a schermo pulendo quello che c'era prima
        RootPanel.get().clear();
        RootPanel.get().add(panel);
    }
}