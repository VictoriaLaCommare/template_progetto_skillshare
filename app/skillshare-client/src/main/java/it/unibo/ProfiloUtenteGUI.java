package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import java.util.List;

public class ProfiloUtenteGUI {
    private final GestoreProfiloAsync profiloService = GWT.create(GestoreProfilo.class);

    public void mostra(Utente utenteLoggato, String targetUsername) {
        
        final PopupPanel popup = new PopupPanel(false);
        popup.setGlassEnabled(true);

        popup.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
        popup.getElement().getStyle().setProperty("border", "none");
        popup.getElement().getStyle().setProperty("borderRadius", "12px");
        popup.getElement().getStyle().setProperty("padding", "20px");
        popup.getElement().getStyle().setProperty("boxShadow", "0 8px 16px rgba(0,0,0,0.2)");
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(15);
        mainPanel.setWidth("380px");
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.setSpacing(0);

        // Titolo del profilo
        HTML titolo = new HTML("<h3 style='margin: 0; color: #1b263b; border-bottom: 2px solid #1b263b; padding-bottom: 10px; width: 100%; text-align: center;'>Profilo di " + targetUsername + "</h3>");
        mainPanel.add(titolo);

        // Test di attesa per la risposta dal server
        Label lblStato = new Label("Caricamento profilo...");
        mainPanel.add(lblStato);

        // Chiamata al server per ottenere le informazioni del profilo
        profiloService.ottieniProfiloUtente(targetUsername, new AsyncCallback<Utente>() {
            @Override
            public void onFailure(Throwable caught) {
                lblStato.setText("Errore nel caricamento del profilo: " + caught.getMessage());
                lblStato.getElement().getStyle().setColor("red");
                aggiungiTastoChiudi(mainPanel, popup, utenteLoggato, null);
            }

            @Override
            public void onSuccess(Utente utenteTarget) {
                lblStato.setText(""); // Rimuovi il messaggio di caricamento
                if (utenteTarget == null) {
                    mainPanel.add(new Label("Profilo non trovato."));
                    aggiungiTastoChiudi(mainPanel, popup, utenteLoggato, null);
                    popup.center();
                    return;
                }

                costruisciProfiloPanel(mainPanel, utenteTarget, popup, utenteLoggato);

                popup.center();
            }
        });

        // Inserimento del contenutore nel popup e centratura
        popup.setWidget(mainPanel);
        popup.center();
    }

    private void costruisciProfiloPanel(VerticalPanel profiloPanel, Utente utenteTarget, PopupPanel popup, Utente utenteLoggato) {

        // Foto profilo
        String urlFoto = utenteTarget.getImmagineProfilo();
        if (urlFoto != null && !urlFoto.trim().isEmpty()) {
            Image imgProfilo = new Image(urlFoto);
            imgProfilo.setWidth("150px");
            imgProfilo.getElement().getStyle().setProperty("borderRadius", "50%");
            profiloPanel.add(imgProfilo);
        } else {
            Label lblNoFoto = new Label("Nessuna foto disponibile");
            profiloPanel.add(lblNoFoto);
        }


        // Nome e Cognome
        HTML info = new HTML("<h2 style='margin 5px 0; color: #1b263b;'>" + utenteTarget.getNome() + " " + utenteTarget.getCognome() + "</h2>" + 
                             "<p style='margin: 5px 0; color: #666;'>Username: " + utenteTarget.getUsername() + "</p>" +
                             "<p style='margin: 5px 0; color: #666;'>Email: " + utenteTarget.getMail() + "</p>");
        profiloPanel.add(info);

        // Bio
        String bio = utenteTarget.getBio() != null ? utenteTarget.getBio() : "Nessuna bio disponibile";
        profiloPanel.add(new HTML("<p style='font-style: italic; color: #555; text-align: center;'>" + bio + "</p>"));

        // Tag Competenze
        String tagCompetenze = utenteTarget.getTagCompetenze() != null ? utenteTarget.getTagCompetenze() : "Nessuna competenza specificata";
        profiloPanel.add(new HTML("<p style='color: #555; text-align: center;'>" + tagCompetenze + "</p>"));

        // Sezione Tag di livello e recensione
        //profiloPanel.add(new HTML("<hr style='width:80%; margin:15px 0;'/>"));

        Label lblCaricamentoStats = new Label("Caricamento statistiche...");
        profiloPanel.add(lblCaricamentoStats);

        profiloService.ottieniStatistiche(utenteTarget.getUsername(), new AsyncCallback<StatisticheUtente>() {
            @Override
            public void onFailure(Throwable caught) {
                lblCaricamentoStats.setText("Statistiche non disponibili.");
            }

            @Override
            public void onSuccess(StatisticheUtente stats) {
                lblCaricamentoStats.setText("");

                // Livello e scambi
                profiloPanel.add(new HTML(
                        "<p style='text-align:center; font-size:18px;'>" + stats.getLivello() + "</p>" +
                                "<p style='text-align:center; color:#555;'>Scambi completati: " + stats.getScambiCompletati() + "</p>" +
                                "<p style='text-align:center; color:#555;'>Media recensioni: " +
                                 stats.getMediaRecensioni() + " / 5</p>"
                ));

                // Tag assegnati dai Decorator
                if (!stats.getTag().isEmpty()) {
                    HorizontalPanel tagPanel = new HorizontalPanel();
                    tagPanel.setSpacing(5);
                    for (String tag : stats.getTag()) {
                        tagPanel.add(new HTML(
                                "<span style='background:#1b263b; color:white; padding:3px 8px;" +
                                        "border-radius:12px; font-size:12px;'>" + tag + "</span>"
                        ));
                    }
                    profiloPanel.add(tagPanel);
                }

                popup.center(); // ricalcola dimensioni popup
            }
        });


        // Storico scambi
        profiloPanel.add(new HTML("<hr style='width: 80%; margin: 10px 0 10px 0;' />"));

        // Bottone per mostrare lo storico degli scambi
        Button btnMostraStorico = new Button("Visualizza Storico Scambi");
        btnMostraStorico.getElement().setId("btn-mostra-storico");

        VerticalPanel storicoContainer = new VerticalPanel();
        storicoContainer.setWidth("80%");
        storicoContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        profiloPanel.add(btnMostraStorico);
        profiloPanel.add(storicoContainer);

        aggiungiTastoChiudi(profiloPanel, popup, utenteLoggato, utenteTarget);

        btnMostraStorico.addClickHandler(event -> {
            btnMostraStorico.setEnabled(false);
            storicoContainer.clear();
            storicoContainer.add(new Label("Caricamento storico..."));

            // Richiesta dello storico di questo utente al server
            profiloService.ottieniStoricoScambi(utenteTarget.getUsername(), new AsyncCallback<List<RichiestaScambio>>() {
                @Override
                public void onFailure(Throwable caught) {
                    storicoContainer.clear();
                    storicoContainer.add(new Label("Errore nel recupero dello storico."));
                    btnMostraStorico.setEnabled(true); // Accensione del bottone
                    popup.center();
                }

                @Override
                public void onSuccess(List<RichiestaScambio> storico) {
                    storicoContainer.clear();
                    
                    // storico vuoto
                    if (storico == null || storico.isEmpty()) {
                        Label lblVuoto = new Label("Nessuno scambio passato");
                        lblVuoto.getElement().setId("msg-storico-vuoto");
                        lblVuoto.getElement().getStyle().setProperty("color", "#666");
                        lblVuoto.getElement().getStyle().setProperty("fontStyle", "italic");
                        storicoContainer.add(lblVuoto);
                        popup.center();
                        return; // Fine.
                    }

                    // Creazione della tabella per mostrare lo storico
                    FlexTable tabellaStorico = new FlexTable();
                    tabellaStorico.getElement().setId("tabella-storico");
                    tabellaStorico.setWidth("100%");
                    tabellaStorico.setCellPadding(8);
                    tabellaStorico.getElement().getStyle().setProperty("borderCollapse", "collapse");
                    tabellaStorico.getElement().getStyle().setProperty("fontFamily", "sans-serif");
                    tabellaStorico.getElement().getStyle().setProperty("borderRadius", "12px");
                    tabellaStorico.getElement().getStyle().setProperty("overflow", "hidden");
                    
                    // Intestazioni della tabella
                    tabellaStorico.setHTML(0, 0, "<b>ID Annuncio</b>");
                    tabellaStorico.setHTML(0, 1, "<b>Richiedente</b>");
                    tabellaStorico.setHTML(0, 2, "<b>Ricevente</b>");
                    tabellaStorico.setHTML(0, 3, "<b>Stato</b>");
                    tabellaStorico.getRowFormatter().getElement(0).getStyle().setProperty("backgroundColor", "#1b263b");
                    tabellaStorico.getRowFormatter().getElement(0).getStyle().setProperty("color", "white");

                    // Corpo della tabella con i dati dello storico
                    int riga = 1;
                    for (RichiestaScambio req : storico) {
                        tabellaStorico.setText(riga, 0, req.getIdAnnuncio());
                        tabellaStorico.setText(riga, 1, req.getRichiedente());
                        tabellaStorico.setText(riga, 2, req.getRicevente());
                        tabellaStorico.setText(riga, 3, req.getStato());
                        tabellaStorico.getRowFormatter().getElement(riga).getStyle().setProperty("borderBottom", "1px solid #ddd");
                        riga++;
                    }
                    storicoContainer.add(tabellaStorico);

                    popup.center(); // Ricalcola la posizione del popup per adattarsi al nuovo contenuto
                }
            });
        });
    }

    private void aggiungiTastoChiudi(VerticalPanel panel, PopupPanel popup, Utente utenteLoggato, Utente utenteTarget) {
        HorizontalPanel bottoniPanel = new HorizontalPanel();
        bottoniPanel.setSpacing(10);
        bottoniPanel.getElement().getStyle().setProperty("marginTop", "15px");

        // Se l'utente visualizzato è l'utente loggato, mostriamo anche il tasto per andare alla modifica
        if (utenteLoggato != null && utenteTarget != null && utenteLoggato.getMail().equals(utenteTarget.getMail())) {
            Button btnModifica = new Button("✏️ Modifica Profilo");
            btnModifica.getElement().setId("btn-vai-a-modifica-profilo");
            btnModifica.getElement().getStyle().setProperty("backgroundColor", "#1b263b");
            btnModifica.getElement().getStyle().setProperty("color", "#000000");
            btnModifica.getElement().getStyle().setProperty("padding", "6px 15px");
            btnModifica.getElement().getStyle().setProperty("cursor", "pointer");
            btnModifica.getElement().getStyle().setProperty("borderRadius", "4px");
            
            btnModifica.addClickHandler(event -> {
                popup.hide(); // Chiude il popup attuale
                new PersonalizzaProfiloGui().mostra(utenteLoggato); // Apre la schermata di personalizzazione
            });
            bottoniPanel.add(btnModifica);
        }

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.addClickHandler(event -> popup.hide());
        btnChiudi.getElement().setId("btn-chiudi-profilo");
        btnChiudi.getElement().getStyle().setProperty("padding", "6px 20px");
        btnChiudi.getElement().getStyle().setProperty("cursor", "pointer");
        bottoniPanel.add(btnChiudi);

        panel.add(bottoniPanel);
        panel.setCellHorizontalAlignment(bottoniPanel, HasHorizontalAlignment.ALIGN_CENTER);
    }
    
}
