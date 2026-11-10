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
        popup.addStyleName("popup-personalizzaProfilo");
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.addStyleName("mainPanel-personalizzaProfilo");

        // Titolo del profilo
        HTML titolo = new HTML("<h3>Profilo di " + targetUsername + "</h3>");
        titolo.addStyleName("titoloPopup-personalizzaProfilo");
        mainPanel.add(titolo);

        // Test di attesa per la risposta dal server
        Label lblStato = new Label("Caricamento profilo...");
        mainPanel.add(lblStato);

        // Chiamata al server per ottenere le informazioni del profilo
        profiloService.ottieniProfiloUtente(targetUsername, new AsyncCallback<Utente>() {
            @Override
            public void onFailure(Throwable caught) {
                lblStato.setText("Errore nel caricamento del profilo: " + caught.getMessage());
                lblStato.addStyleName("testo-errore");
                aggiungiTastoChiudi(mainPanel, popup, utenteLoggato, null);
            }

            @Override
            public void onSuccess(Utente utenteTarget) {
                lblStato.setText(""); // Rimozione del messaggio di caricamento
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

        // Inserimento del contenitore nel popup e centratura
        popup.setWidget(mainPanel);
        popup.center();
    }

    private void costruisciProfiloPanel(VerticalPanel profiloPanel, Utente utenteTarget, PopupPanel popup, Utente utenteLoggato) {

        // Foto profilo
        profiloPanel.add(new HTML("<br/>"));
        String urlFoto = utenteTarget.getImmagineProfilo();
        if (urlFoto != null && !urlFoto.trim().isEmpty()) {
            Image imgProfilo = new Image(urlFoto);
            imgProfilo.addStyleName("imgProfilo-personalizzaProfilo");
            profiloPanel.add(imgProfilo);
        } else {
            Label lblNoFoto = new Label("Nessuna foto disponibile");
            profiloPanel.add(lblNoFoto);
        }

        String dataNascitaStr = null;
        if (utenteTarget.getData() != null) {
            // Usa il formattatore di GWT per renderla leggibile 
            com.google.gwt.i18n.client.DateTimeFormat dtf = com.google.gwt.i18n.client.DateTimeFormat.getFormat("dd/MM/yyyy");
            dataNascitaStr = dtf.format(utenteTarget.getData());
        }

        // Nome e Cognome
        HTML info = new HTML("<h2 class='Utente'>" + utenteTarget.getNome() + " " + utenteTarget.getCognome() + "</h2>" + 
                             "<p class='Username'>Username: " + utenteTarget.getUsername() + "</p>" +
                             "<p class='Data'> Data di Nascita: " + dataNascitaStr + "</p>" +
                             "<p class='Email'>Email: " + utenteTarget.getMail() + "</p>");
        info.addStyleName("info-personalizzaProfilo");
        profiloPanel.add(info);

        // Bio
        String bio = utenteTarget.getBio() != null ? utenteTarget.getBio() : "Nessuna bio disponibile";
        HTML htmlBio = new HTML("<p>" + bio + "</p>");
        htmlBio.addStyleName("bio-personalizzaProfilo");
        profiloPanel.add(htmlBio);

        // Tag Competenze
        String tagCompetenze = utenteTarget.getTagCompetenze() != null ? utenteTarget.getTagCompetenze() : "Nessuna competenza specificata";
        HTML htmlTagCompetenze = new HTML("<p>" + tagCompetenze + "</p>");
        htmlTagCompetenze.addStyleName("tagCompetenze-personalizzaProfilo");
        profiloPanel.add(htmlTagCompetenze);

        // Sezione Tag di livello e recensione
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
                HTML statistiche = new HTML("<p class='livello'>" + stats.getLivello() + "</p>" +
                                            "<p class='statiticheP'>Scambi completati: " + stats.getScambiCompletati() + "</p>" +
                                            "<p class='statiticheP'>Media recensioni: " + stats.getMediaRecensioni() + " / 5</p>");
                statistiche.addStyleName("tagCompetenze-personalizzaProfilo");
                profiloPanel.add(statistiche);

                // Tag assegnati dai Decorator
                if (!stats.getTag().isEmpty()) {
                    HorizontalPanel tagPanel = new HorizontalPanel();
                    tagPanel.setSpacing(5);
                    for (String tag : stats.getTag()) {
                        HTML htmlTag = new HTML("<span>" + tag + "</span>");
                        htmlTag.addStyleName("tagPanel-personalizzaProfilo");
                        tagPanel.add(htmlTag);
                    }
                    profiloPanel.add(tagPanel);
                }

                popup.center(); // ricalcola dimensioni popup
            }
        });


        // Storico scambi
        HTML lineaOrizzontale = new HTML("<hr/>");
        lineaOrizzontale.addStyleName("lineaOrizzontale-personalizzaProfilo");
        profiloPanel.add(lineaOrizzontale);

        // Bottone per mostrare lo storico degli scambi
        Button btnMostraStorico = new Button("Visualizza Storico Scambi");
        btnMostraStorico.getElement().setId("btn-mostra-storico");

        VerticalPanel storicoContainer = new VerticalPanel();
        //storicoContainer.setWidth("80%");
        storicoContainer.addStyleName("storicoContainer-personalizzaProfilo");
        storicoContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        profiloPanel.add(btnMostraStorico);
        profiloPanel.add(new HTML("<br/>"));
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
                    
                    // Storico vuoto
                    if (storico == null || storico.isEmpty()) {
                        Label lblVuoto = new Label("Nessuno scambio passato");
                        lblVuoto.getElement().setId("msg-storico-vuoto");
                        lblVuoto.addStyleName("lblVuoto-personalizzaProfilo");
                        storicoContainer.add(lblVuoto);
                        popup.center();
                        return; // Fine.
                    }

                    // Creazione della tabella per mostrare lo storico
                    FlexTable tabellaStorico = new FlexTable();
                    tabellaStorico.getElement().setId("tabella-storico");
                    tabellaStorico.setCellPadding(8);
                    tabellaStorico.addStyleName("tabellaStorico-personalizzaProfilo");
                    
                    // Intestazioni della tabella
                    tabellaStorico.setHTML(0, 0, "<b>ID Annuncio</b>");
                    tabellaStorico.setHTML(0, 1, "<b>Richiedente</b>");
                    tabellaStorico.setHTML(0, 2, "<b>Ricevente</b>");
                    tabellaStorico.setHTML(0, 3, "<b>Stato</b>");
                    tabellaStorico.getRowFormatter().addStyleName(0, "headTabellaStorico-personalizzaProfilo");

                    // Corpo della tabella con i dati dello storico
                    int riga = 1;
                    for (RichiestaScambio req : storico) {
                        tabellaStorico.setText(riga, 0, req.getIdAnnuncio());
                        tabellaStorico.setText(riga, 1, req.getRichiedente());
                        tabellaStorico.setText(riga, 2, req.getRicevente());
                        tabellaStorico.setText(riga, 3, req.getStato());
                        tabellaStorico.getRowFormatter().addStyleName(riga, "borderBottonCorpoTabella-personalizzaProfilo");
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
        bottoniPanel.addStyleName("bottoniContainer-personalizzaProfilo");

        // Se l'utente visualizzato è l'utente loggato, mostriamo anche il tasto per andare alla modifica
        if (utenteLoggato != null && utenteTarget != null && utenteLoggato.getMail().equals(utenteTarget.getMail())) {
            Button btnModifica = new Button("✏️ Modifica Profilo");
            btnModifica.getElement().setId("btn-vai-a-modifica-profilo");
            btnModifica.addStyleName("bottoniPanel-personalizzaProfilo");
            
            btnModifica.addClickHandler(event -> {
                popup.hide(); // Chiude il popup attuale
                new PersonalizzaProfiloGui().mostra(utenteLoggato); // Apre la schermata di personalizzazione
            });
            bottoniPanel.add(btnModifica);
        }

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.addClickHandler(event -> popup.hide());
        btnChiudi.getElement().setId("btn-chiudi-profilo");
        btnChiudi.addStyleName("btnChiudi-personalizzaProfilo");
        bottoniPanel.add(btnChiudi);

        panel.add(bottoniPanel);
        panel.setCellHorizontalAlignment(bottoniPanel, HasHorizontalAlignment.ALIGN_CENTER);
    }
    
}
