package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import java.util.List;

public class PubblicaAnnunciGui {

    //Collegamento con il backend degli annunci
    private final GestoreAnnunciAsync rpcService = GWT.create(GestoreAnnunci.class);
    
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

    public void mostra(Utente utenteLoggato) {
        VerticalPanel mainContainer = new VerticalPanel();
        mainContainer.setWidth("90%");
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainContainer.getElement().getStyle().setProperty("margin", "20px auto");
        mainContainer.setSpacing(0);

        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(15);
        panel.setWidth("100%");
        panel.getElement().getStyle().setProperty("margin", "20px auto");
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        // Aggiungiamo la Navbar centralizzata in cima
        if (utenteLoggato != null) {
            panel.add(new AppNavbar(utenteLoggato, "bacheca"));
        }

        HTML bannerDashboard = new HTML(
        "<div style=\"background-color: #1b263b; color: #ffffff; padding: 25px; border-radius: 12px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); font-family: 'Segoe UI', Tahoma, sans-serif; margin-bottom: 25px;\">" +
        "<h1 style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 28px;\">Bacheca Annunci</h1>" +
        "<p style=\"margin: 0; font-size: 16px; color: #e0f2f1;\">Condividi le tue competenze o cerca aiuto nella community!</p>" +
        "</div>"
        );
        
        panel.add(bannerDashboard);

        //Campo titolo
        TextBox txtTitolo = new TextBox();
        txtTitolo.getElement().setPropertyString("placeholder", "Titolo annuncio");
        txtTitolo.getElement().setId("annuncio-titolo");
        txtTitolo.setWidth("300px");
        
        Label errTitolo = new Label();
        errTitolo.getElement().getStyle().setColor("red");
        HorizontalPanel rowTitolo = new HorizontalPanel();
        rowTitolo.setSpacing(5);
        rowTitolo.add(txtTitolo);
        rowTitolo.add(errTitolo);

        //Campo descrizione
        TextArea txtDescrizione = new TextArea();
        txtDescrizione.getElement().setPropertyString("placeholder", "Descrizione dettagliata...");
        txtDescrizione.getElement().setId("annuncio-desc");
        txtDescrizione.setVisibleLines(4);
        txtDescrizione.setWidth("300px");
        
        Label errDescrizione = new Label();
        errDescrizione.getElement().getStyle().setColor("red");
        HorizontalPanel rowDescrizione = new HorizontalPanel();
        rowDescrizione.setSpacing(5);
        rowDescrizione.add(txtDescrizione);
        rowDescrizione.add(errDescrizione);

        //Competenza Offerta
        TextBox txtOfferta = new TextBox();
        txtOfferta.getElement().setPropertyString("placeholder", "Competenza offerta");
        txtOfferta.getElement().setId("annuncio-offerta");
        txtOfferta.setWidth("300px");
        
        Label errOfferta = new Label();
        errOfferta.getElement().getStyle().setColor("red");
        HorizontalPanel rowOfferta = new HorizontalPanel();
        rowOfferta.setSpacing(5);
        rowOfferta.add(txtOfferta);
        rowOfferta.add(errOfferta);

        //Competenza Richiesta
        TextBox txtRichiesta = new TextBox();
        txtRichiesta.getElement().setPropertyString("placeholder", "Competenza richiesta");
        txtRichiesta.getElement().setId("annuncio-richiesta");
        txtRichiesta.setWidth("300px");
        
        Label errRichiesta = new Label();
        errRichiesta.getElement().getStyle().setColor("red");
        HorizontalPanel rowRichiesta = new HorizontalPanel();
        rowRichiesta.setSpacing(5);
        rowRichiesta.add(txtRichiesta);
        rowRichiesta.add(errRichiesta);

        //Bottone di invio e messaggi di stato
        Button btnPubblica = new Button("Pubblica Annuncio");
        btnPubblica.getElement().setId("annuncio-submit");

        Label lblMessaggio = new Label();
        lblMessaggio.getElement().setId("annuncio-message");

        //Pannello per la lista degli annunci esistenti
        VerticalPanel panelListaAnnunci = new VerticalPanel();
        panelListaAnnunci.setSpacing(5);
        panelListaAnnunci.setWidth("100%");
        panelListaAnnunci.add(new Label("Caricamento annunci in corso..."));
        
        caricaAnnunci(panelListaAnnunci);

        btnPubblica.addClickHandler(event -> {
            // Reset dei messaggi di errore
            errTitolo.setText("");
            errDescrizione.setText("");
            errOfferta.setText("");
            errRichiesta.setText("");
            lblMessaggio.setText("");

            // Validazione lato client
            boolean titoloOk = txtTitolo.getText() != null && !txtTitolo.getText().trim().isEmpty();
            boolean descOk = txtDescrizione.getText() != null && !txtDescrizione.getText().trim().isEmpty();
            boolean offertaOk = txtOfferta.getText() != null && !txtOfferta.getText().trim().isEmpty();
            boolean richiestaOk = txtRichiesta.getText() != null && !txtRichiesta.getText().trim().isEmpty();

            evidenzia(txtTitolo, titoloOk);
            if (!titoloOk) errTitolo.setText("Il titolo è obbligatorio");

            evidenzia(txtDescrizione, descOk);
            if (!descOk) errDescrizione.setText("La descrizione è obbligatoria");

            evidenzia(txtOfferta, offertaOk);
            if (!offertaOk) errOfferta.setText("Inserisci una competenza offerta");

            evidenzia(txtRichiesta, richiestaOk);
            if (!richiestaOk) errRichiesta.setText("Inserisci una competenza richiesta");

            if (!titoloOk || !descOk || !offertaOk || !richiestaOk) {
                mostraErrore(lblMessaggio, "Controlla gli errori segnalati accanto ai campi e riprova.");
                return;
            }

            // Creazione e popolamento dell'oggetto Annuncio
            Annuncio nuovoAnnuncio = new Annuncio();
            nuovoAnnuncio.setTitolo(txtTitolo.getText());
            nuovoAnnuncio.setId(String.valueOf(System.currentTimeMillis()));
            nuovoAnnuncio.setDescrizione(txtDescrizione.getText());
            nuovoAnnuncio.setCompetenzaOfferta(txtOfferta.getText());
            nuovoAnnuncio.setCompetenzaRichiesta(txtRichiesta.getText());
            nuovoAnnuncio.setAutoreUsername(utenteLoggato.getUsername());

            //Chiamata RPC al server
            rpcService.pubblicaAnnuncio(nuovoAnnuncio, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    mostraErrore(lblMessaggio, "Errore durante la pubblicazione: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Boolean result) {
                    mostraSuccesso(lblMessaggio, "Annuncio pubblicato con successo!");
                    
                    //Ripristino bordi e pulizia form
                    txtTitolo.setText("");
                    txtDescrizione.setText("");
                    txtOfferta.setText("");
                    txtRichiesta.setText("");

                    evidenzia(txtTitolo, true);
                    evidenzia(txtDescrizione, true);
                    evidenzia(txtOfferta, true);
                    evidenzia(txtRichiesta, true);

                    //Ricarica la lista degli annunci
                    caricaAnnunci(panelListaAnnunci);
                }
            });
        });

        //Aggiunta degli elementi al pannello principale
        panel.add(rowTitolo);
        panel.add(rowDescrizione);
        panel.add(rowOfferta);
        panel.add(rowRichiesta);
        panel.add(btnPubblica);
        panel.add(lblMessaggio);
        panel.add(new HTML("<hr/>"));
        panel.add(new Label("Annunci disponibili:"));
        panel.add(panelListaAnnunci);

        mainContainer.add(panel);

        //Pulizia e stampa finale
        RootPanel.get().clear();
        RootPanel.get().add(mainContainer);
    }

    private void caricaAnnunci(VerticalPanel container) {
        rpcService.ottieniTuttiGliAnnunci(new AsyncCallback<List<Annuncio>>() {
            @Override
            public void onFailure(Throwable caught) {
                container.clear();
                container.add(new Label("Impossibile caricare gli annunci."));
            }

            @Override
            public void onSuccess(List<Annuncio> annunci) {
                container.clear();
                if (annunci == null || annunci.isEmpty()) {
                    container.add(new Label("Nessun annuncio presente al momento."));
                    return;
                }
                for (Annuncio a : annunci) {
                    HTML cardAnnuncio = new HTML(
                        "<div style=\"background: #f8f9fa; border-left: 4px solid #1b263b; padding: 12px 15px; margin-bottom: 12px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); width: 100%; box-sizing: border-box;\">" +
                        "<b style=\"font-size: 16px; color: #1b263b;\">" + a.getTitolo() + "</b> <span style=\"color: #666; font-size: 13px;\">(Autore: " + a.getAutoreUsername() + ")</span><br/>" +
                        "<span style=\"color: #333;\"><b>Offre:</b> " + a.getCompetenzaOfferta() + " | <b>Richiede:</b> " + a.getCompetenzaRichiesta() + "</span><br/>" +
                        "<p style=\"margin: 5px 0 0 0; color: #555; font-size: 14px;\"><em>" + a.getDescrizione() + "</em></p>" +
                        "</div>"
                    );
                    container.add(cardAnnuncio);
                }
            }
        });
    }
}