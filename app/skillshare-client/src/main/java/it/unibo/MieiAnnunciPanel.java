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

    // Costruttore: riceve l'username dell'utente che ha fatto il login
    public MieiAnnunciPanel(Utente usernameCorrente, Runnable onTornaIndietro) {
        this.usernameCorrente = usernameCorrente;

        mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainPanel.addStyleName("mainContainer");

        VerticalPanel panel = new VerticalPanel();
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        panel.addStyleName("pannelloPrincipale");
        
        // Aggiungiamo la Navbar centralizzata in cima se l'utente è loggato
        if (usernameCorrente != null) {
            panel.add(new AppNavbar(usernameCorrente, "miei_annunci"));
        }

        HTML bannerMieiAnnunci = new HTML(
            "<div>" +
            "<h1>I Miei Annunci</h1>" +
            "<p>Da qui puoi visualizzare, modificare o eliminare esclusivamente i post che hai pubblicato.</p>" +
            "</div>"
        );
        bannerMieiAnnunci.addStyleName("stylePannello");

        panel.add(bannerMieiAnnunci);

        listaContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        listaContainer.addStyleName("contenitoreLista");
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
        card.addStyleName("annuncio-card");
        card.addStyleName("card-mieiAnnunci ");

        HTML testoAnnuncio = new HTML(
            "<b class='annuncio-titolo'>" + annuncio.getTitolo() + "</b><br/>" +
            "<div class='annuncio-contenitore'><b>Offre:</b> <span class='annuncio-2riga'>" + annuncio.getCompetenzaOfferta() + "</span> | " +
            "<b>Richiede:</b> <span>" + annuncio.getCompetenzaRichiesta() + "</span></div>" +
            "<p class='annuncio-descrizione'><em>" + annuncio.getDescrizione() + "</em></p>"
        );
        testoAnnuncio.addStyleName("testoAnnuncio-mieiAnnunci");

        card.add(testoAnnuncio);

        HorizontalPanel btnPanel = new HorizontalPanel();
        btnPanel.addStyleName("distanzaBottoni-mieiAnnunci");

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
        dialogContents.addStyleName("panelElimina-Spacing-mieiAnnunci");
        dialogContents.add(new Label("Sei sicuro di voler eliminare questo annuncio?"));

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.addStyleName("distanzaBottoni-mieiAnnunci");

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

    // Invia la richiesta di eliminazione al server passando l'ID e l'utente richiedente
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
        dialogBox.setGlassEnabled(true); 

        VerticalPanel panel = new VerticalPanel();
        panel.addStyleName("panelModifica-Spacing-mieiAnnunci");

        final TextBox txtTitolo = new TextBox();
        txtTitolo.setText(annuncio.getTitolo());
        txtTitolo.addStyleName("txtbox-modifica-mieiAnnunci");
        
        final TextArea txtDescrizione = new TextArea();
        txtDescrizione.setText(annuncio.getDescrizione());
        txtDescrizione.setVisibleLines(3);
        txtDescrizione.addStyleName("textarea-modifica-mieiAnnunci");

        final TextBox txtOfferta = new TextBox();
        txtOfferta.setText(annuncio.getCompetenzaOfferta());
        txtOfferta.addStyleName("txtbox-modifica-mieiAnnunci");

        final TextBox txtRichiesta = new TextBox();
        txtRichiesta.setText(annuncio.getCompetenzaRichiesta());
        txtRichiesta.addStyleName("txtbox-modifica-mieiAnnunci");

        panel.add(new Label("Titolo:"));
        panel.add(txtTitolo);
        panel.add(new Label("Descrizione:"));
        panel.add(txtDescrizione);
        panel.add(new Label("Offre:"));
        panel.add(txtOfferta);
        panel.add(new Label("Richiede:"));
        panel.add(txtRichiesta);

        HorizontalPanel btnPanel = new HorizontalPanel();
        btnPanel.addStyleName("distanzaBottoni-mieiAnnunci");

        Button btnSalva = new Button("Salva Modifiche", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                annuncio.setTitolo(txtTitolo.getText());
                annuncio.setDescrizione(txtDescrizione.getText());
                annuncio.setCompetenzaOfferta(txtOfferta.getText());
                annuncio.setCompetenzaRichiesta(txtRichiesta.getText());

                dialogBox.hide();
                eseguiModificaServer(annuncio);
            }
        });

        Button btnAnnulla = new Button("Annulla", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        btnPanel.add(btnSalva);
        btnPanel.add(btnAnnulla);
        panel.add(btnPanel);

        // ID per Selenium
        txtTitolo.getElement().setId("modifica-titolo");
        txtDescrizione.getElement().setId("modifica-desc");
        txtOfferta.getElement().setId("modifica-offerta");
        txtRichiesta.getElement().setId("modifica-richiesta");
        btnSalva.getElement().setId("modifica-submit");

        dialogBox.setWidget(panel);
        dialogBox.center();
    }

    private void eseguiModificaServer(Annuncio annuncioAggiornato) {
        gestoreAnnunci.modificaAnnuncio(annuncioAggiornato, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Errore di rete durante la modifica.");
            }

            @Override
            public void onSuccess(Boolean risultato) {
                if (risultato != null && risultato) {
                    Window.alert("Annuncio modificato con successo!");
                    caricaMieiAnnunci();
                } else {
                    Window.alert("Modifica non riuscita.");
                }
            }
        });
    }
}