package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class MieiAnnunciPanel extends Composite {
    
    // Pannelli principali per strutturare la grafica della pagina
    private final VerticalPanel mainPanel = new VerticalPanel();
    private final VerticalPanel listaContainer = new VerticalPanel();
    
    // Memorizziamo l'username dell'utente attualmente loggato
    private final Utente usernameCorrente;

    // Interfaccia asincrona RPC generata da GWT per comunicare con il backend
    private final GestoreAnnunciAsync gestoreAnnunci = GWT.create(GestoreAnnunci.class);

    // Azione da eseguire per tornare indietro
    //private final Runnable onTornaIndietro;

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
        btnBacheca.addClickHandler(event -> new PubblicaAnnunciGui().mostra(utenteLoggato));
        
        // 2. Pulsante Cerca / Proposte
        Button btnProposte = creaPannelloStileLink("🔍 Cerca / Scambi");
        btnProposte.getElement().setId("nav-btn-proposte");
        btnProposte.addClickHandler(event -> new BachecaProposteGUI().mostra(utenteLoggato));

        // 3. Pulsante i Miei Annunci
        Button btnMieiAnnunci = creaPannelloStileLink("📋 I Miei Annunci");
        btnMieiAnnunci.setEnabled(false);
        btnMieiAnnunci.getElement().getStyle().setProperty("backgroundColor", "#1b263b");
        btnMieiAnnunci.getElement().getStyle().setProperty("color", "#6c757d");
        btnMieiAnnunci.getElement().getStyle().setProperty("boxShadow", "inset 0 4px 6px rgba(0,0,0,0.4)");
        btnMieiAnnunci.getElement().getStyle().setProperty("cursor", "default");
        btnMieiAnnunci.getElement().setId("btn-miei-annunci");

        // 4. Profilo utente
        Button btnProfilo = creaPannelloStileLink("👤 Profilo");
        btnProfilo.getElement().setId("nav-btn-profilo");
        btnProfilo.addClickHandler(event -> {
            com.google.gwt.user.client.Window.alert("Utente loggato: " + utenteLoggato.getNome() + " " + utenteLoggato.getCognome() + " (" + utenteLoggato.getMail() + ")");
        });

        // 5. Tasto Logout (Torna alla landing page)
        Button btnLogout = creaPannelloStileLink("↩️ Logout");
        btnLogout.getElement().setId("nav-btn-logout");
        btnLogout.addClickHandler(event -> {
            new HomePageGUI().mostra();
        });

        menuPanel.add(btnBacheca);
        menuPanel.add(btnProposte);
        menuPanel.add(btnMieiAnnunci);
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
    
    // Costruttore: riceve l'username dell'utente che ha fatto il login
    public MieiAnnunciPanel(Utente usernameCorrente, Runnable onTornaIndietro) {
        this.usernameCorrente = usernameCorrente;
        //this.onTornaIndietro = onTornaIndietro;

        mainPanel.setWidth("90%");
        mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);  
        mainPanel.getElement().getStyle().setProperty("margin", "20px auto");
        mainPanel.getElement().getStyle().setProperty("fontFamily", "Segoe UI, Tahoma, sans-serif");
        mainPanel.setSpacing(0);

        /*// --- PULSANTE TORNA INDIETRO ---
        Button btnIndietro = new Button("← Torna alla Bacheca");
        btnIndietro.addClickHandler(event -> {
            if (this.onTornaIndietro != null) {
                this.onTornaIndietro.run(); // Esegue il ritorno alla schermata precedente
            }
        });
        mainPanel.add(btnIndietro);
        */

        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(15);
        panel.setWidth("100%");
        panel.getElement().getStyle().setProperty("margin", "20px auto");
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        // Aggiungiamo la Navbar in cima se l'utente è loggato
        if (usernameCorrente != null) {
            panel.add(creaNavbar(usernameCorrente));
        }

        HTML bannerMieiAnnunci = new HTML(
            "<div style=\"background-color: #1b263b; color: #ffffff; padding: 25px; border-radius: 12px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); font-family: 'Segoe UI', Tahoma, sans-serif; margin-bottom: 25px;\">" +
            "<h1 style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 28px;\">I Miei Annunci</h1>" +
            "<p style=\"margin: 0; font-size: 16px; color: #e0f2f1;\">Da qui puoi visualizzare, modificare o eliminare esclusivamente i post che hai pubblicato.</p>" +
            "</div>"
        );
        panel.add(bannerMieiAnnunci);

        listaContainer.setWidth("100%");
        listaContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        listaContainer.setSpacing(10);
        panel.add(listaContainer);

        mainPanel.add(panel);

        // Collega il pannello principale al widget GWT
        initWidget(mainPanel);

        // Appena apri la schermata, scarica la lista dei tuoi annunci dal server
        caricaMieiAnnunci();
    }

    // Metodo per chiamare il backend e recuperare solo gli annunci dell'utente
    private void caricaMieiAnnunci() {
        listaContainer.clear();
        listaContainer.add(new Label("Caricamento dei tuoi annunci in corso..."));
        

        // Chiamata asincrona al metodo che hai creato nel backend
        gestoreAnnunci.ottieniAnnunciUtente(usernameCorrente.getUsername(), new AsyncCallback<List<Annuncio>>() {
            @Override
            public void onFailure(Throwable caught) {
                listaContainer.clear();
                listaContainer.add(new Label("Errore di comunicazione con il server: " + caught.getMessage()));
            }

            @Override
            public void onSuccess(List<Annuncio> annunci) {
                listaContainer.clear();
                
                if (annunci == null || annunci.isEmpty()) {
                    listaContainer.add(new Label("Non hai ancora pubblicato nessun annuncio."));
                    return;
                }

                // Per ogni annuncio trovato, creiamo una "card" grafica con i bottoni
                for (final Annuncio annuncio : annunci) {
                    VerticalPanel card = creaCardAnnuncio(annuncio);
                    listaContainer.add(card);
                }
            }
        });
    }

    // Disegna visivamente la scheda (card) per ogni singolo annuncio
    private VerticalPanel creaCardAnnuncio(final Annuncio annuncio) {
        VerticalPanel card = new VerticalPanel();
        card.setStyleName("annuncio-card");
        
        
        card.setWidth("100%");
        card.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
        card.getElement().getStyle().setProperty("borderLeft", "4px solid #1b263b");
        card.getElement().getStyle().setProperty("padding", "15px");
        card.getElement().getStyle().setProperty("borderRadius", "6px");
        card.getElement().getStyle().setProperty("boxShadow", "0 2px 4px rgba(0,0,0,0.05)");
        card.getElement().getStyle().setProperty("boxSizing", "border-box");

        HTML testoAnnuncio = new HTML(
            "<b style=\"font-size: 18px; color: #1b263b;\">" + annuncio.getTitolo() + "</b><br/>" +
            "<div style=\"margin-top: 5px; color: #333; font-size: 14px;\"><b>Offre:</b> <span style=\\\"color: #333;\\\">" + annuncio.getCompetenzaOfferta() + "</span> | " +
            "<b>Richiede:</b> <span style=\\\"color: #333;\\\">" + annuncio.getCompetenzaRichiesta() + "</span></div>" +
            "<p style=\"margin: 10px 0 15px 0; color: #555; font-size: 15px;\"><em>" + annuncio.getDescrizione() + "</em></p>"
        );
        card.add(testoAnnuncio);

        HorizontalPanel btnPanel = new HorizontalPanel();
        btnPanel.setSpacing(10);

        // Bottone Modifica con ID univoco basato sull'ID dell'annuncio
        Button btnModifica = new Button("Modifica");
        btnModifica.getElement().setId("btn-modifica-" + annuncio.getId());
        btnModifica.addClickHandler(event -> mostraDialogoModifica(annuncio));

        // Pulsante Elimina con ID univoco basato sull'ID dell'annuncio
        Button btnElimina = new Button("Elimina");
        btnElimina.getElement().setId("btn-elimina-" + annuncio.getId());
        btnElimina.addClickHandler(event -> mostraDialogoConfermaEliminazione(annuncio.getId()));

        btnPanel.add(btnModifica);
        btnPanel.add(btnElimina);

        card.add(btnPanel);

        return card;
    }

    // Apre un popup modale (DialogBox) pulito e nativo GWT con i bottoni Sì / No
    private void mostraDialogoConfermaEliminazione(final String idAnnuncio) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText("Conferma operazione");
        dialogBox.setAnimationEnabled(true);
        dialogBox.setGlassEnabled(true); // Scura lo sfondo dietro la finestra

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(15);
        dialogContents.add(new Label("Sei sicuro di voler eliminare questo annuncio?"));

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);

        // Bottone Sì: procede con l'eliminazione
        Button btnSi = new Button("Sì", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                eseguiEliminazioneServer(idAnnuncio);
            }
        });
        // Assegniamo un ID chiaro al bottone "Sì" della modale di conferma
        btnSi.getElement().setId("btn-conferma-elimina-si");

        // Bottone No: chiude semplicemente il popup senza fare nulla
        Button btnNo = new Button("No", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        buttonPanel.add(btnSi);
        buttonPanel.add(btnNo);
        dialogContents.add(buttonPanel);
        
        dialogBox.setWidget(dialogContents);
        dialogBox.center(); // Centra perfettamente il popup nella schermata del browser
    }

    // Invia la richiesta di eliminazione al server passando l'ID e l'utente richiedente (Controllo Zero Trust)
    private void eseguiEliminazioneServer(String idAnnuncio) {
        gestoreAnnunci.eliminaAnnuncio(idAnnuncio, usernameCorrente.getUsername(), new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Errore di rete durante l'eliminazione.");
            }

            @Override
            public void onSuccess(Boolean risultato) {
                if (risultato != null && risultato) {
                    Window.alert("Annuncio eliminato con successo!");
                    // Ricarica la lista per aggiornare subito la schermata rimuovendo il post
                    caricaMieiAnnunci();
                } else {
                    Window.alert("Operazione negata: non sei il proprietario di questo annuncio.");
                }
            }
        });
    }

    private void mostraDialogoModifica(final Annuncio annuncio) {
        // Creazione di una DialogBox nativa GWT per contenere il form di modifica
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText("Modifica Annuncio");
        dialogBox.setAnimationEnabled(true);
        dialogBox.setGlassEnabled(true); // Scura lo sfondo della pagina per dare effetto modale

        // Pannello verticale principale dentro il popup
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(10);

        // Campo di testo per il titolo, precompilato con il valore attuale dell'annuncio
        final TextBox txtTitolo = new TextBox();
        txtTitolo.setText(annuncio.getTitolo());
        
        // Area di testo per la descrizione, precompilata con il valore attuale
        final TextArea txtDescrizione = new TextArea();
        txtDescrizione.setText(annuncio.getDescrizione());
        txtDescrizione.setVisibleLines(3);

        // Campo di testo per la competenza offerta, precompilato
        final TextBox txtOfferta = new TextBox();
        txtOfferta.setText(annuncio.getCompetenzaOfferta());

        // Campo di testo per la competenza richiesta, precompilato
        final TextBox txtRichiesta = new TextBox();
        txtRichiesta.setText(annuncio.getCompetenzaRichiesta());

        // Aggiungiamo etichette e campi di input al pannello del form
        panel.add(new Label("Titolo:"));
        panel.add(txtTitolo);
        panel.add(new Label("Descrizione:"));
        panel.add(txtDescrizione);
        panel.add(new Label("Offre:"));
        panel.add(txtOfferta);
        panel.add(new Label("Richiede:"));
        panel.add(txtRichiesta);

        // Pannello orizzontale per i pulsanti di azione in basso
        HorizontalPanel btnPanel = new HorizontalPanel();
        btnPanel.setSpacing(10);

        // Pulsante per confermare e salvare le modifiche apportate
        Button btnSalva = new Button("Salva Modifiche", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Aggiorniamo l'oggetto annuncio con i nuovi testi scritti dall'utente
                annuncio.setTitolo(txtTitolo.getText());
                annuncio.setDescrizione(txtDescrizione.getText());
                annuncio.setCompetenzaOfferta(txtOfferta.getText());
                annuncio.setCompetenzaRichiesta(txtRichiesta.getText());

                // Chiudiamo il popup di modifica e avviamo la chiamata al server per salvare i dati modificati
                dialogBox.hide();
                eseguiModificaServer(annuncio);
            }
        });

        // Pulsante per annullare l'operazione e chiudere il popup senza salvare
        Button btnAnnulla = new Button("Annulla", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        // Inseriamo i bottoni nel loro sotto-pannello e il sotto-pannello nel form principale
        btnPanel.add(btnSalva);
        btnPanel.add(btnAnnulla);
        panel.add(btnPanel);

        // Assegniamo gli ID ai campi del popup per renderli facilmente testabili con Selenium
        txtTitolo.getElement().setId("modifica-titolo");
        txtDescrizione.getElement().setId("modifica-desc");
        txtOfferta.getElement().setId("modifica-offerta");
        txtRichiesta.getElement().setId("modifica-richiesta");
        btnSalva.getElement().setId("modifica-submit");

        // Impostiamo il pannello come contenuto del DialogBox e lo centriamo a schermo
        dialogBox.setWidget(panel);
        dialogBox.center();
    }

    // Metodo per inviare l'annuncio modificato al backend tramite RPC asincrona
    private void eseguiModificaServer(Annuncio annuncioAggiornato) {
        gestoreAnnunci.modificaAnnuncio(annuncioAggiornato, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                // Gestione dell'errore di rete o di comunicazione
                Window.alert("Errore di rete durante la modifica.");
            }

            @Override
            public void onSuccess(Boolean risultato) {
                // Controllo se il server ha completato con successo l'operazione
                if (risultato != null && risultato) {
                    Window.alert("Annuncio modificato con successo!");
                    // Ricarichiamo la lista dei nostri annunci per mostrare i dati aggiornati a schermo
                    caricaMieiAnnunci();
                } else {
                    Window.alert("Modifica non riuscita.");
                }
            }
        });
    }
}
