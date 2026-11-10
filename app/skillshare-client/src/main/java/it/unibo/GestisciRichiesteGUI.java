package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class GestisciRichiesteGUI {

    private VerticalPanel mainPanel;
    private Utente utenteLoggato;
    private final GestoreRichiesteServiceAsync richiesteService = GWT.create(GestoreRichiesteService.class);

    public void mostra(Utente utente) {
        this.utenteLoggato = utente;

        VerticalPanel mainContainer = new VerticalPanel();
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainContainer.addStyleName("mainContainer");

        mainPanel = new VerticalPanel();
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.addStyleName("pannelloPrincipale");

        // Aggiungiamo la navbar centralizzata in cima
        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "richieste"));
        }

        // Intestazione della pagina
        HTML titolo = new HTML(
            "<div>" +
            "<h1>Le Mie Richieste</h1>" +
            "<p>Gestisci le richieste di scambio in sospeso e le chat attive</p>" +
            "</div>"
        );
        titolo.addStyleName("stylePannello");

        mainPanel.add(titolo);

        // Contenitore per la lista delle richieste
        VerticalPanel listaRichiestePanel = new VerticalPanel();
        listaRichiestePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        listaRichiestePanel.addStyleName("contenitoreLista");
        mainPanel.add(listaRichiestePanel);

        // Caricamento asincrono dal DB
        caricaRichieste(listaRichiestePanel);

        mainContainer.add(mainPanel);

        RootPanel.get().clear();
        RootPanel.get().add(mainContainer);
    }

    // ottieniTutteLeRichieste fa in modo che anche il richiedente veda le sue richieste attive
    private void caricaRichieste(VerticalPanel container) {
        container.clear();
        container.add(new Label("Caricamento richieste in corso..."));

        richiesteService.ottieniTutteLeRichieste(utenteLoggato.getUsername(), new AsyncCallback<List<RichiestaScambio>>() {
            @Override
            public void onFailure(Throwable caught) {
                container.clear();
                HTML erroreDiComunicazione = new HTML("<div>Errore di comunicazione col server: " + caught.getMessage() + "</div>");
                erroreDiComunicazione.addStyleName("erroreDiComunicazione-richiesteRicevute");
                container.add(erroreDiComunicazione);

            }

            @Override
            public void onSuccess(List<RichiestaScambio> result) {
                container.clear();
                if (result == null || result.isEmpty()) {
                    container.add(new Label("Nessuna richiesta al momento."));
                    return;
                }
                for (RichiestaScambio req : result) {
                    container.add(creaCardRichiesta(req, container));
                }
            }
        });
    }

    private Widget creaCardRichiesta(RichiestaScambio req, VerticalPanel container) {
        VerticalPanel card = new VerticalPanel();
        card.addStyleName("cardRichieste-richiesteRicevute");

        // Controlli per il ruolo dell'utente loggato e della sua controparte
        boolean sonoRichiedente = utenteLoggato.getUsername().equals(req.getRichiedente());
        String controparte = sonoRichiedente ? req.getRicevente() : req.getRichiedente();
        String stato = req.getStato().toLowerCase();

        String testoIntestazione = sonoRichiedente 
            ? "Richiesta da: Te <br/> A: " + req.getRicevente() 
            : "Richiesta da: " + req.getRichiedente();

        HTML info = new HTML(
            "<b class='richiesta'>" + testoIntestazione + "</b><br/>" +
            "<span class='id'>Riferimento Annuncio ID: " + req.getIdAnnuncio() + "</span>"
        );
        info.addStyleName("infoCard-richiesteRicevute");

        card.add(info);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonPanel.addStyleName("buttonPanel-richiesteRicevute");

        // Utilizzo dello switch per la gestione dei diversi stati della richiesta
        switch (stato) {
            case "in sospeso": {
                if (!sonoRichiedente) {
                    // Solo il ricevente può accettare
                    Button btnAccetta = new Button("Accetta");
                    btnAccetta.getElement().setId("btn-accetta-" + req.getIdRichiesta());
                    estilsaPulsante(btnAccetta, "#28a745");
                    btnAccetta.addClickHandler(event ->
                            richiesteService.accettaRichiesta(req.getIdRichiesta(), utenteLoggato.getUsername(), new AsyncCallback<Boolean>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Errore: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    Window.alert(Boolean.TRUE.equals(result) ? "Richiesta accettata! Ora puoi accordarti con l'utente." : "Errore: impossibile accettare la richiesta.");
                                    caricaRichieste(container);
                                }
                            })
                    );
                    buttonPanel.add(btnAccetta);
                }

                // Entrambi i ruoli possono rifiutare/annullare cambia solo la presentazione a seconda del ruolo
                String labelBtn = sonoRichiedente ? "Annulla" : "Rifiuta";
                String coloreBtn = sonoRichiedente ? "#Grey" : "#Red";
                String msgOk = sonoRichiedente ? "Richiesta annullata." : "Richiesta rifiutata.";

                Button btnRifiuta = new Button(labelBtn);
                btnRifiuta.getElement().setId("btn-rifiuta-" + req.getIdRichiesta());
                estilsaPulsante(btnRifiuta, coloreBtn);
                btnRifiuta.addClickHandler(event ->
                        richiesteService.rifiutaRichiesta(req.getIdRichiesta(), utenteLoggato.getUsername(), new AsyncCallback<Boolean>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert("Errore: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Boolean result) {
                                Window.alert(Boolean.TRUE.equals(result) ? msgOk : "Errore: impossibile completare l'operazione.");
                                caricaRichieste(container);
                            }
                        })
                );
                buttonPanel.add(btnRifiuta);
                break;
            }
            case "rifiutata": {
                String testoRifiuto = sonoRichiedente ? "Richiesta Annullata" : "Richiesta Rifiutata";
                HTML rifiutoHTML = new HTML("<span>" + testoRifiuto + "</span>");
                rifiutoHTML.addStyleName("testoRifiutoHTML-richiesteRicevute");
                buttonPanel.add(rifiutoHTML);
                break;
            }
            case "accettata": // Stesso comportamento
            case "in attesa di conferma dall'altro utente": {
                if (stato.equals("accettata")) {
                    HTML textrichiestaAccettata = new HTML("<span>Richiesta Accettata!</span>");
                    textrichiestaAccettata.addStyleName("testoRichiestaAccettata-richiesteRicevute");
                    buttonPanel.add(textrichiestaAccettata);
                }
                
                //controllo per capire se l'utente corrente ha già impostato la terminazione della richiesta
                //sonoRichiedente e !sonoRichiedente sono mutualmente esclusivi quindi ci si ritrova sempre in uno solo dei due rami
                boolean UtenteCorrenteHaGiaConfermato = (sonoRichiedente && req.isConfermataTerminazioneRichiedente())
                        || (!sonoRichiedente && req.isConfermataTerminazioneRicevente());

                if (!UtenteCorrenteHaGiaConfermato) {
                    Button btnTermina = new Button("Dichiara Terminazione");
                    btnTermina.getElement().setId("btn-termina-" + req.getIdRichiesta());
                    estilsaPulsante(btnTermina, "DodgerBlue");
                    btnTermina.addClickHandler(event ->
                            richiesteService.dichiaraTerminazione(req.getIdRichiesta(), utenteLoggato.getUsername(), new AsyncCallback<Boolean>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Errore: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    Window.alert(Boolean.TRUE.equals(result) ? "Terminazione dichiarata con successo." : "Errore: impossibile dichiarare la terminazione.");
                                    caricaRichieste(container);
                                }
                            })
                    );
                    buttonPanel.add(btnTermina);
                } else {
                    HTML messaggioAttesa = new HTML("<span>In attesa della conferma dell'altro utente...</span>");
                    messaggioAttesa.addStyleName("messaggioAttesa-richiesteRicevute");
                    buttonPanel.add(messaggioAttesa);
                }
                break;
            }
            case "completato": {
                HTML richiestaCompletata = new HTML("<span>Richiesta Completata</span>");
                richiestaCompletata.addStyleName("textRichiestaCompletata-richiesteRicevute");
                buttonPanel.add(richiestaCompletata);
                if (req.isRecensioneInviata(utenteLoggato.getUsername())) {
                    HTML recensioneInviata =new HTML("<span>✓ Recensione inviata</span>");
                    recensioneInviata.addStyleName("textRecensioneInviata-richiesteRicevute");
                    buttonPanel.add(recensioneInviata);
                    break;
                }

                //Array di un elemento: una variabile locale normale non funzionerebbe nel metodo di gestione degli eventi
                //perché le lambda richiedono che le variabili catturate siano "effectively final".
                int[] valutazioneSelezionata = {0};

                // Bottone che mostra/nasconde il form
                Button btnRecensione = new Button("Lascia Recensione");
                btnRecensione.getElement().setId("btn-recensione-" + req.getIdRichiesta());
                estilsaPulsante(btnRecensione, "LightSalmon");
                buttonPanel.add(btnRecensione);

                // Creato subito ma nascosto. Si apre solo quando l'utente clicca il bottone
                VerticalPanel formRecensione = new VerticalPanel();
                formRecensione.setVisible(false);
                formRecensione.setSpacing(8);
                formRecensione.addStyleName("formRecensione-richiesteRicevute");

                HTML titoloValuta = new HTML("<b>Valuta " + controparte + ":</b>");
                titoloValuta.addStyleName("titoloValuta-richiesteRicevute");
                formRecensione.add(titoloValuta);

                // Cinque stelle cliccabili
                HorizontalPanel stellePanel = new HorizontalPanel();
                stellePanel.addStyleName("stellePanel-richiesteRicevute");
                //stellePanel.setSpacing(4);
                Label[] stelle = new Label[5];

                for (int i = 0; i < 5; i++) {
                    final int valore = i + 1; // valore 1-5 catturato dalla lambda
                    stelle[i] = new Label("☆");
                    stelle[i].addStyleName("stellaStyle-richiesteRicevute");

                    stelle[i].addClickHandler(event -> {
                        valutazioneSelezionata[0] = valore; // salvo il valore cliccato

                        //Aggiorna l'aspetto di tutte e 5 le stelle in base alla scelta
                        for (int j = 0; j < 5; j++) {
                            boolean evidenziata = j < valore;
                            stelle[j].setText(evidenziata ? "★" : "☆");
                            if (evidenziata) {
                                stelle[j].addStyleName("stella-evidenziata-richiesteRicevute");
                                } else {
                                    stelle[j].removeStyleName("stella-evidenziata-richiesteRicevute");
                                }
                        }
                    });

                    stellePanel.add(stelle[i]);
                }
                formRecensione.add(stellePanel);

                //Commento facoltativo
                formRecensione.add(new Label("Commento (opzionale):"));
                TextArea txtCommento = new TextArea();
                txtCommento.setVisibleLines(3);
                txtCommento.getElement().setPropertyString("placeholder", "Scrivi un commento...");
                txtCommento.addStyleName("txtCommento-richiesteRicevute");
                formRecensione.add(txtCommento);

                //Invio recensione
                Button btnInvia = new Button("Invia Recensione");
                btnInvia.getElement().setId("btn-invia-recensione-" + req.getIdRichiesta());
                estilsaPulsante(btnInvia, "MidnightBlue");

                btnInvia.addClickHandler(event -> {
                    // Blocco se l'utente non ha selezionato nessuna stella
                    if (valutazioneSelezionata[0] == 0) {
                        Window.alert("Seleziona almeno una stella prima di inviare.");
                        return;
                    }

                    btnInvia.setEnabled(false);

                    // Costruisco la recensione con i valori inseriti dall'utente
                    Recensione recensione = Recensione.creaRecensione();
                    recensione.setValutazione(valutazioneSelezionata[0]);
                    recensione.setCommento(txtCommento.getValue());

                    // Invio al server: usernameRecensito è la controparte
                    richiesteService.inviaRecensione(req.getIdRichiesta(), controparte, recensione,
                            new AsyncCallback<Boolean>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Errore nell'invio: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    Window.alert(Boolean.TRUE.equals(result)
                                            ? "Recensione inviata con successo!"
                                            : "Errore: impossibile inviare la recensione.");
                                    caricaRichieste(container); // ricarica le card per aggiornare la vista

                                }
                            }
                    );
                });
                formRecensione.add(btnInvia);

                // Il bottone "Abbandona Recensione" inverte la visibilità del form ad ogni click
                btnRecensione.addClickHandler(event -> {
                    boolean staAprendo = !formRecensione.isVisible();
                    formRecensione.setVisible(staAprendo);

                    // Se si sta chiudendo il form, resetta tutto
                    if (!staAprendo) {
                        valutazioneSelezionata[0] = 0;
                        txtCommento.setValue("");
                        for (Label stella : stelle) {
                            stella.setText("☆");
                            stella.removeStyleName("stella-evidenziata-richiesteRicevute");
                        }
                    }
                });

                card.add(formRecensione);
                break;
            }
            default:
                break;
        }

        card.add(buttonPanel);
        return card;
    }

    private void estilsaPulsante(Button btn, String coloreBase) {
        btn.getElement().getStyle().setProperty("backgroundColor", coloreBase);
        btn.addStyleName("btn-base-richieste-richiesteRicevute");
    }
}