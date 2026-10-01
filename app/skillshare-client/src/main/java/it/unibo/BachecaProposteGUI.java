package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import java.util.List;

public class BachecaProposteGUI {
    private final GestoreAnnunciAsync rpcService = GWT.create(GestoreAnnunci.class);
    private final GestoreRichiesteServiceAsync richiesteService = GWT.create(GestoreRichiesteService.class);

    // NAVBAR SUPERIORE FISSA
    private HorizontalPanel creaNavbar(Utente utenteLoggato) {
        HorizontalPanel navbar = new HorizontalPanel();
        
        navbar.setWidth("100%"); 
        
        navbar.setSpacing(10);
        navbar.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);        
        navbar.getElement().getStyle().setProperty("backgroundColor", "#1b263b");
        navbar.getElement().getStyle().setProperty("padding", "10px 20px");
        navbar.getElement().getStyle().setProperty("boxShadow", "0 4px 8px rgba(0,0,0,0.15)");
        
        navbar.getElement().getStyle().setProperty("borderRadius", "12px"); 
        navbar.getElement().getStyle().setProperty("margin", "15px auto 25px auto");
        navbar.getElement().getStyle().setProperty("boxSizing", "border-box");

        // Logo / Nome a sinistra
        Label lblLogo = new Label("SkillShare");
        lblLogo.getElement().getStyle().setProperty("color", "#ffffff");
        lblLogo.getElement().getStyle().setProperty("fontSize", "20px");
        lblLogo.getElement().getStyle().setProperty("fontWeight", "bold");
        lblLogo.getElement().getStyle().setProperty("cursor", "pointer");
        lblLogo.addClickHandler(event -> new PubblicaAnnunciGui().mostra(utenteLoggato));
        
        navbar.add(lblLogo);
        navbar.setCellWidth(lblLogo, "30%");

        // Pannello pulsanti a destra
        HorizontalPanel menuPanel = new HorizontalPanel();
        menuPanel.setSpacing(15);
        menuPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        menuPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        // 1. Pulsante Bacheca / Home loggato
        Button btnBacheca = creaPannelloStileLink("📢 Bacheca");
        btnBacheca.getElement().setId("nav-btn-bacheca");
        btnBacheca.addClickHandler(event -> new PubblicaAnnunciGui().mostra(utenteLoggato));
        
        // 2. Pulsante Cerca / Proposte
        Button btnProposte = creaPannelloStileLink("🔍 Cerca / Scambi");
        btnProposte.setEnabled(false);
        btnProposte.getElement().getStyle().setProperty("backgroundColor", "#1b263b");
        btnProposte.getElement().getStyle().setProperty("color", "#6c757d");
        btnProposte.getElement().getStyle().setProperty("boxShadow", "inset 0 4px 6px rgba(0,0,0,0.4)");
        btnProposte.getElement().getStyle().setProperty("cursor", "default");
        //btnProposte.addClickHandler(event -> new BachecaProposteGUI().mostra(utenteLoggato));

        // 3. Pulsante i Miei Annunci
        Button btnMieiAnnunci = creaPannelloStileLink("📋 I Miei Annunci");
        btnMieiAnnunci.getElement().setId("btn-miei-annunci");
        btnMieiAnnunci.addClickHandler(event -> {
            RootPanel.get().clear();
            // Istanziamo il pannello passando l'username e il callback per tornare alla bacheca annunci
            MieiAnnunciPanel mieiAnnunciPanel = new MieiAnnunciPanel(
                utenteLoggato,
                () -> new PubblicaAnnunciGui().mostra(utenteLoggato)
            );
            RootPanel.get().add(mieiAnnunciPanel);
        });

        // 4. Pulsante Richieste
        Button btnRichieste = creaPannelloStileLink("📥 Richieste Ricevute");
        btnRichieste.getElement().setId("nav-btn-richieste");
        btnRichieste.addClickHandler(event -> {
            // Apriamo la nuova schermata
            new GestisciRichiesteGUI().mostra(utenteLoggato);
        });

        // 5. Profilo utente
        Button btnProfilo = creaPannelloStileLink("👤 Profilo");
        btnBacheca.getElement().setId("nav-btn-profilo");
        btnProfilo.addClickHandler(event -> {
            com.google.gwt.user.client.Window.alert("Utente loggato: " + utenteLoggato.getNome() + " " + utenteLoggato.getCognome() + " (" + utenteLoggato.getMail() + ")");
        });

        // 6. Tasto Logout (Torna alla landing page)
        Button btnLogout = creaPannelloStileLink("↩️ Logout");
        btnBacheca.getElement().setId("nav-btn-logout");
        btnLogout.addClickHandler(event -> {
            new HomePageGUI().mostra();
        });

        menuPanel.add(btnBacheca);
        menuPanel.add(btnProposte);
        menuPanel.add(btnMieiAnnunci);
        menuPanel.add(btnRichieste);
        menuPanel.add(btnProfilo);
        menuPanel.add(btnLogout);

        navbar.add(menuPanel);
        navbar.setCellHorizontalAlignment(menuPanel, HasHorizontalAlignment.ALIGN_RIGHT);

        return navbar;
    }

    private Button creaPannelloStileLink(String testo) {
        Button btn = new Button(testo);
        btn.getElement().getStyle().setProperty("backgroundColor", "#f0f2f5");
        btn.getElement().getStyle().setProperty("color", "#1b263b");
        btn.getElement().getStyle().setProperty("border", "none");
        btn.getElement().getStyle().setProperty("padding", "6px 12px");
        btn.getElement().getStyle().setProperty("borderRadius", "4px");
        btn.getElement().getStyle().setProperty("fontSize", "13px");
        btn.getElement().getStyle().setProperty("fontWeight", "bold");
        btn.getElement().getStyle().setProperty("cursor", "pointer");
        return btn;
    }
    
    public void mostra(Utente utenteLoggato) {
        VerticalPanel mainContainer = new VerticalPanel();
        mainContainer.setWidth("90%");
        mainContainer.getElement().getStyle().setProperty("margin", "20px auto");
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);  
        mainContainer.setSpacing(0);

        VerticalPanel pannelloPrincipale = new VerticalPanel();
        pannelloPrincipale.setSpacing(15);
        pannelloPrincipale.setWidth("100%");
        pannelloPrincipale.getElement().getStyle().setProperty("margin", "20px auto");
        pannelloPrincipale.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        // Aggiungiamo la Navbar in cima se l'utente è loggato
        if (utenteLoggato != null) {
            pannelloPrincipale.add(creaNavbar(utenteLoggato));
        }

        HTML bannerDashboard = new HTML(
        "<div style=\"background-color: #1b263b; color: #ffffff; padding: 25px; border-radius: 12px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); font-family: 'Segoe UI', Tahoma, sans-serif; margin-bottom: 25px;\">" +
        "<h1 style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 28px;\">Lista Proposte Disponibili</h1>" +
        "<p style=\"margin: 0; font-size: 16px; color: #e0f2f1;\">Visualizza tutte le proposte disponibili</p>" +
        "</div>"
        );
        
        pannelloPrincipale.add(bannerDashboard);

        Label lblStato = new Label("Caricamento delle proposte in corso...");
        pannelloPrincipale.add(lblStato);

        VerticalPanel pannelloAnnunci = new VerticalPanel();
        pannelloAnnunci.setWidth("100%");

        Button btnFiltri = new Button("🔍 Filtri Avanzati");
        btnFiltri.getElement().setId("btn-filtri");
        btnFiltri.getElement().getStyle().setProperty("marginBottom", "10px");
        btnFiltri.addClickHandler(event -> mostraPopupFiltriAvanzati(pannelloAnnunci, utenteLoggato, lblStato));
        
        pannelloPrincipale.add(btnFiltri);

        pannelloPrincipale.add(pannelloAnnunci);

        rpcService.ottieniTuttiGliAnnunci(new AsyncCallback<List<Annuncio>>() {
            
            @Override 
            public void onFailure(Throwable caught){
                lblStato.setText("Errore di comunicazione col server: " + caught.getMessage());
                lblStato.getElement().getStyle().setColor("red");
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

        pannelloFiltri.getElement().getStyle().setBackgroundColor("#ffffff");
        pannelloFiltri.getElement().getStyle().setZIndex(9999);
        pannelloFiltri.getElement().getStyle().setProperty("border", "3px solid #1b263b");
        pannelloFiltri.getElement().getStyle().setProperty("padding", "15px");
        pannelloFiltri.getElement().getStyle().setProperty("borderRadius", "8px");

        VerticalPanel layoutFiltri = new VerticalPanel();
        layoutFiltri.setSpacing(10);
        layoutFiltri.getElement().getStyle().setBackgroundColor("#ffffff");
        
        layoutFiltri.add(new HTML("<h3 style='margin-top: 0; color: #1b263b;'>🔍 Ricerca Avanzata</h3>"));
        
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
        panelBottoni.setSpacing(5);
        
        Button btnEseguiRicerca = new Button("Cerca");
        btnEseguiRicerca.getElement().setId("btn-esegui-ricerca");
        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.getElement().setId("btn-annulla");
        
        panelBottoni.add(btnEseguiRicerca);
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
        
        btnAnnulla.addClickHandler(event -> pannelloFiltri.hide());
        
        pannelloFiltri.center();
        pannelloFiltri.show();
    }

    private void popolaTabella(List<Annuncio> listaProposte, VerticalPanel pannelloAnnunci, Utente utenteLoggato) {
        pannelloAnnunci.clear();
            
        if (listaProposte == null || listaProposte.isEmpty()){
            Label lblVuoto = new Label("Non ci sono proposte disponibili al momento.");
            lblVuoto.getElement().setId("msg-nessuna-proposta");
            pannelloAnnunci.add(lblVuoto);
            return;
        }

        FlexTable tabellaAnnunci = new FlexTable();
        tabellaAnnunci.getElement().setId("tabella-proposte");
        tabellaAnnunci.setWidth("100%");
        tabellaAnnunci.setCellPadding(10);
        tabellaAnnunci.setCellSpacing(0);
        tabellaAnnunci.getElement().getStyle().setProperty("borderCollapse", "collapse");
        tabellaAnnunci.getElement().getStyle().setProperty("fontFamily", "sans-serif");

        tabellaAnnunci.getElement().getStyle().setProperty("borderRadius", "12px");
        tabellaAnnunci.getElement().getStyle().setProperty("overflow", "hidden");

        tabellaAnnunci.setHTML(0, 0, "<b>Titolo</b>");
        tabellaAnnunci.setHTML(0, 1, "<b>Autore</b>");
        tabellaAnnunci.setHTML(0, 2, "<b>Descrizione</b>");
        tabellaAnnunci.setHTML(0, 3, "<b>Competenza Offerta</b>");
        tabellaAnnunci.setHTML(0, 4, "<b>Competenza Richiesta</b>");
        tabellaAnnunci.setHTML(0,5, "<b>Azione</b>");

        tabellaAnnunci.getRowFormatter().getElement(0).getStyle().setProperty("backgroundColor","#1b263b");
        tabellaAnnunci.getRowFormatter().getElement(0).getStyle().setProperty("color","white");
        tabellaAnnunci.getRowFormatter().getElement(0).getStyle().setProperty("textAlign","center");
        tabellaAnnunci.getRowFormatter().getElement(0).getStyle().setProperty("textTransform","uppercase");

        int riga = 1;
        for (Annuncio annuncio : listaProposte) {
            tabellaAnnunci.setText(riga, 0, annuncio.getTitolo());
            tabellaAnnunci.setText(riga, 1, annuncio.getAutoreUsername());
            tabellaAnnunci.setText(riga, 2, annuncio.getDescrizione());
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
                        } else {
                            Window.alert("Errore: impossibile inviare la richiesta. Azione bloccata dal server.");
                        }
                    }
                });
            });

            tabellaAnnunci.setWidget(riga, 5, btnRichiediScambio);
            tabellaAnnunci.getRowFormatter().getElement(riga).getStyle().setProperty("textAlign","center");
            tabellaAnnunci.getRowFormatter().getElement(riga).getStyle().setProperty("borderBottom", "1px solid #dddddd");

            if (riga % 2 == 0){
                tabellaAnnunci.getRowFormatter().getElement(riga).getStyle().setProperty("backgroundColor", "#f9f9f9");
            }

            riga++;
        }
        pannelloAnnunci.add(tabellaAnnunci);
    }
}