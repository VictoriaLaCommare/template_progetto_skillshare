package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import java.util.List;

public class BachecaProposteGUI {
    private final GestoreAnnunciAsync rpcService = GWT.create(GestoreAnnunci.class);
    private final GestoreRichiesteServiceAsync richiesteService = GWT.create(GestoreRichiesteService.class);

    public void mostra(Utente utenteLoggato) {
        VerticalPanel mainContainer = new VerticalPanel();
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainContainer.addStyleName("mainContainer");

        VerticalPanel pannelloPrincipale = new VerticalPanel();
        pannelloPrincipale.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        pannelloPrincipale.addStyleName("pannelloPrincipale");

        // Aggiungiamo la Navbar centralizzata in cima
        if (utenteLoggato != null) {
            pannelloPrincipale.add(new AppNavbar(utenteLoggato, "ricerca"));
        }

        HTML bannerDashboard = new HTML(
            "<div>" +
            "<h1>Lista Proposte Disponibili</h1>" +
            "<p>Visualizza tutte le proposte disponibili</p>" +
            "</div>"
        );

        bannerDashboard.addStyleName("stylePannello");

        pannelloPrincipale.add(bannerDashboard);

        Label lblStato = new Label("Caricamento delle proposte in corso...");
        pannelloPrincipale.add(lblStato);

        VerticalPanel pannelloAnnunci = new VerticalPanel();
        pannelloAnnunci.addStyleName("larghezzaMax");

        Button btnFiltri = new Button("🔍 Filtri Avanzati");
        btnFiltri.getElement().setId("btn-filtri");
        btnFiltri.addStyleName("bottone-filtri-bacheca");
        btnFiltri.addClickHandler(event -> mostraPopupFiltriAvanzati(pannelloAnnunci, utenteLoggato, lblStato));

        pannelloPrincipale.add(btnFiltri);

        pannelloPrincipale.add(pannelloAnnunci);

        rpcService.ottieniTuttiGliAnnunci(new AsyncCallback<List<Annuncio>>() {

            @Override
            public void onFailure(Throwable caught){
                lblStato.setText("Errore di comunicazione col server: " + caught.getMessage());
                lblStato.addStyleName("testo-errore");
            }

            @Override
            public void onSuccess(List<Annuncio> listaProposte) {
                lblStato.setText("");
                popolaTabella(listaProposte, pannelloAnnunci, utenteLoggato);
            }
        });

        mainContainer.add(pannelloPrincipale);

        RootPanel.get().clear();
        RootPanel.get().add(mainContainer);
    }

    private void mostraPopupFiltriAvanzati(VerticalPanel pannelloAnnunci, Utente utenteLoggato, Label lblStato) {
        final PopupPanel pannelloFiltri = new PopupPanel(true);
        pannelloFiltri.setGlassEnabled(true);
        
        pannelloFiltri.addStyleName("pannelloFiltri-bacheca");

        VerticalPanel layoutFiltri = new VerticalPanel();
        layoutFiltri.setSpacing(10);
        layoutFiltri.add(new HTML("<h3>🔍 Ricerca Avanzata</h3>"));
        layoutFiltri.addStyleName("layoutFiltri-bacheca");

        final TextBox txtOfferta = new TextBox();
        txtOfferta.getElement().setId("tB-Offerta");
        txtOfferta.getElement().setPropertyString("placeholder", "Competenza offerta");

        final TextBox txtRichiesta = new TextBox();
        txtRichiesta.getElement().setId("tB-Richiesta");
        txtRichiesta.getElement().setPropertyString("placeholder", "Competenza richiesta");

        final TextBox txtKeyword = new TextBox();
        txtKeyword.getElement().setId("tB-Keyword");
        txtKeyword.getElement().setPropertyString("placeholder", "Parola chiave");

        final ListBox listOrdinamento = new ListBox();
        listOrdinamento.addItem("Nessun ordinamento", "");
        listOrdinamento.addItem("Alfabetico per Titolo", "titolo");

        layoutFiltri.add(new Label("Competenza Offerta:"));
        layoutFiltri.add(txtOfferta);
        layoutFiltri.add(new Label("Competenza Richiesta:"));
        layoutFiltri.add(txtRichiesta);
        layoutFiltri.add(new Label("Parola chiave:"));
        layoutFiltri.add(txtKeyword);
        layoutFiltri.add(new Label("Ordinamento:"));
        layoutFiltri.add(listOrdinamento);

        HorizontalPanel panelBottoni = new HorizontalPanel();
        panelBottoni.addStyleName("form-row");

        Button btnEseguiRicerca = new Button("Cerca");
        btnEseguiRicerca.getElement().setId("btn-esegui-ricerca");
        //ricerca per preferiti
        Button btnAnnunciPerTe = new Button("❤️ Per Te");
        btnAnnunciPerTe.getElement().setId("btn-annunci-per-te");

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.getElement().setId("btn-annulla");

        panelBottoni.add(btnEseguiRicerca);
        panelBottoni.add(btnAnnunciPerTe);
        panelBottoni.add(btnAnnulla);
        layoutFiltri.add(panelBottoni);

        pannelloFiltri.setWidget(layoutFiltri);

        btnEseguiRicerca.addClickHandler(event -> {
            String offerta = txtOfferta.getValue();
            String richiesta = txtRichiesta.getValue();
            String keyword = txtKeyword.getValue();
            String ordinamento = listOrdinamento.getSelectedValue();

            lblStato.setText("Ricerca in corso...");

            rpcService.cercaAnnunci(offerta, richiesta, keyword, ordinamento, new AsyncCallback<List<Annuncio>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Errore durante la ricerca: " + caught.getMessage());
                }

                @Override
                public void onSuccess(List<Annuncio> risultati) {
                    pannelloFiltri.hide();
                    lblStato.setText("");
                    popolaTabella(risultati, pannelloAnnunci, utenteLoggato);
                }
            });
        });

        btnAnnunciPerTe.addClickHandler(event -> {
            pannelloFiltri.hide();
            lblStato.setText("Caricamento annunci consigliati...");

            rpcService.ottieniAnnunciPreferiti(utenteLoggato.getUsername(), new AsyncCallback<List<Annuncio>>() {
                @Override
                public void onFailure(Throwable caught) {
                    lblStato.setText("");
                    Window.alert("Errore nel recupero degli annunci personalizzati: " + caught.getMessage());
                }

                @Override
                public void onSuccess(List<Annuncio> risultati) {
                    lblStato.setText("");
                    popolaTabella(risultati, pannelloAnnunci, utenteLoggato);
                }
            });
        });

        btnAnnulla.addClickHandler(event -> pannelloFiltri.hide());

        pannelloFiltri.center();
        pannelloFiltri.show();
    }

    private void popolaTabella(List<Annuncio> listaProposte, VerticalPanel pannelloAnnunci, Utente utenteLoggato) {
        pannelloAnnunci.clear();

        if (listaProposte == null || listaProposte.isEmpty()){
            HTML spazio = new HTML("</br>");
            Label lblVuoto = new Label( "Non ci sono proposte disponibili al momento.");
            lblVuoto.getElement().setId("msg-nessuna-proposta");
            lblVuoto.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            pannelloAnnunci.add(spazio);
            pannelloAnnunci.add(lblVuoto);
            return;
        }

        FlexTable tabellaAnnunci = new FlexTable();
        tabellaAnnunci.getElement().setId("tabella-proposte");
        tabellaAnnunci.setCellPadding(10);
        tabellaAnnunci.setCellSpacing(0);

        tabellaAnnunci.addStyleName("tabellaAnnunci-bacheca");

        tabellaAnnunci.setHTML(0, 0, "<b>Titolo</b>");
        tabellaAnnunci.setHTML(0, 1, "<b>Autore</b>");
        tabellaAnnunci.setHTML(0, 2, "<b>Descrizione</b>");
        tabellaAnnunci.setHTML(0, 3, "<b>Competenza Offerta</b>");
        tabellaAnnunci.setHTML(0, 4, "<b>Competenza Richiesta</b>");
        tabellaAnnunci.setHTML(0, 5, "<b>Azione</b>");

        tabellaAnnunci.getRowFormatter().addStyleName(0, "tabella-header-bacheca");

        // Ricorda in quale riga della tabella si trova ogni annuncio: serve per sostituire
        // il bottone con la scritta "In attesa di accettazione" una volta noto lo stato della richiesta
        java.util.Map<String, Integer> rigaPerAnnuncio = new java.util.HashMap<>();

        int riga = 1;
        for (Annuncio annuncio : listaProposte) {
            final int rigaCorrente = riga;
            rigaPerAnnuncio.put(annuncio.getId(), riga);

            tabellaAnnunci.setText(riga, 0, annuncio.getTitolo());

            Button btnAutore = new Button(annuncio.getAutoreUsername());

            btnAutore.getElement().setId("link-profilo-" + annuncio.getAutoreUsername());

            btnAutore.addClickHandler(e -> {
                new ProfiloUtenteGUI().mostra(utenteLoggato, annuncio.getAutoreUsername());
            });

            tabellaAnnunci.setWidget(riga, 1, btnAutore);
            tabellaAnnunci.setText(riga, 2, annuncio.getDescrizione());
            tabellaAnnunci.getCellFormatter().addStyleName(riga, 2, "breakDescrizione-bacheca");
            tabellaAnnunci.setText(riga, 3, annuncio.getCompetenzaOfferta());
            tabellaAnnunci.setText(riga, 4, annuncio.getCompetenzaRichiesta());

            Button btnRichiediScambio = new Button("Richiedi Scambio");
            btnRichiediScambio.getElement().setId("btn-richiedi-scambio-" + annuncio.getId());

            if (annuncio.getAutoreUsername().equals(utenteLoggato.getUsername())) {
                btnRichiediScambio.setEnabled(false);
                btnRichiediScambio.setTitle("Non puoi richiedere uno scambio con il tuo annuncio.");
            }

            btnRichiediScambio.addClickHandler(event ->{
                RichiestaScambio richiesta = new RichiestaScambio("",annuncio.getId(), utenteLoggato.getUsername(), annuncio.getAutoreUsername());

                richiesteService.inviaRichiesta(richiesta, new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Errore di rete: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Boolean successo) {
                        if (successo) {
                            Window.alert("Richiesta di scambio inviata con successo a " + annuncio.getAutoreUsername() + " per l'annuncio '" + annuncio.getTitolo() + "'.");
                            // Il bottone sparisce e al suo posto compare la scritta di attesa
                            tabellaAnnunci.setWidget(rigaCorrente, 5, creaEtichettaAttesa());
                        } else {
                            Window.alert("Errore: impossibile inviare la richiesta. Azione bloccata dal server.");
                        }
                    }
                });
            });

            tabellaAnnunci.setWidget(riga, 5, btnRichiediScambio);
            tabellaAnnunci.getRowFormatter().addStyleName(riga, "tabella-riga-dispari-bacheca");

            if (riga % 2 == 0) {
                tabellaAnnunci.getRowFormatter().addStyleName(riga, "tabella-riga-pari-bacheca");
            }

            riga++;
        }
        pannelloAnnunci.add(tabellaAnnunci);

        // Le richieste già inviate dall'utente: "in sospeso" -> scritta di attesa di risposta
        // già accettata (o oltre) -> il bottone scompare cosicché non sia possibile inviare più richieste allo stesso annuncio
        // rifiutata -> resta il bottone per riprovare
        richiesteService.ottieniTutteLeRichieste(utenteLoggato.getUsername(), new AsyncCallback<List<RichiestaScambio>>() {
            @Override
            public void onFailure(Throwable caught) {
                //niente: restano i bottoni
            }

            @Override
            public void onSuccess(List<RichiestaScambio> richieste) {
                for (RichiestaScambio r : richieste) {
                    Integer rigaTabella = rigaPerAnnuncio.get(r.getIdAnnuncio());
                    if (rigaTabella == null || !r.getRichiedente().equals(utenteLoggato.getUsername())) continue;

                    if ("In sospeso".equalsIgnoreCase(r.getStato()) || "in sospeso".equalsIgnoreCase(r.getStato())) {
                        tabellaAnnunci.setWidget(rigaTabella, 5, creaEtichettaAttesa());
                    } else if (!"Rifiutata".equalsIgnoreCase(r.getStato())) {
                        HTML etichettaVuota = new HTML("<span></span>");
                        etichettaVuota.getElement().setId("gia-richiesto-" + r.getIdAnnuncio());
                        tabellaAnnunci.clearCell(rigaTabella, 5); // accettata, in attesa di conferma o completata
                    }
                }
            }
        });
    }

    private Widget creaEtichettaAttesa() {
        HTML messaggioAttesa = new HTML("<span>In attesa di accettazione</span>");
        messaggioAttesa.getElement().setId("msg-in-attesa");
        messaggioAttesa.addStyleName("messaggioDiAttesa");
        return messaggioAttesa;
    }
}