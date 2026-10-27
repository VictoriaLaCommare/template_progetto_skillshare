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
        mainContainer.setWidth("90%");
        mainContainer.getElement().getStyle().setProperty("margin", "20px auto");
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        mainContainer.setSpacing(0);

        mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.setSpacing(15);
        mainPanel.getElement().getStyle().setProperty("fontFamily", "Segoe UI, Tahoma, sans-serif");

        // Aggiungiamo la navbar centralizzata in cima
        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "richieste"));
        }

        // Intestazione allineata nello stile a "I Miei Annunci"
        HTML titolo = new HTML(
                "<div style=\"background-color: #1b263b; color: #ffffff; padding: 25px; border-radius: 12px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); font-family: 'Segoe UI', Tahoma, sans-serif; margin-bottom: 25px;\">" +
                        "<h1 style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 28px; font-weight: bold;\">Richieste Ricevute</h1>" +
                        "<p style=\"margin: 0; font-size: 16px; color: #e0f2f1;\">Gestisci le richieste di scambio in sospeso e le chat attive</p>" +
                        "</div>"
        );
        mainPanel.add(titolo);

        // Contenitore per la lista delle richieste
        VerticalPanel listaRichiestePanel = new VerticalPanel();
        listaRichiestePanel.setWidth("100%");
        listaRichiestePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        listaRichiestePanel.setSpacing(10);
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
                container.add(new HTML("<div style=\"color: red; text-align: center;\">Errore di comunicazione col server: " + caught.getMessage() + "</div>"));
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
        card.setWidth("100%");
        card.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
        card.getElement().getStyle().setProperty("borderLeft", "4px solid #1b263b");
        card.getElement().getStyle().setProperty("padding", "15px");
        card.getElement().getStyle().setProperty("marginBottom", "12px");
        card.getElement().getStyle().setProperty("borderRadius", "4px");
        card.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");

        // Controlli per il ruolo dell'utente loggato e della sua controparte
        boolean sonoRichiedente = utenteLoggato.getUsername().equals(req.getRichiedente());
        String controparte = sonoRichiedente ? req.getRicevente() : req.getRichiedente();
        String stato = req.getStato().toLowerCase();

        HTML info = new HTML(
                "<b style=\"font-size: 16px; color: #1b263b;\">Richiesta da: " + req.getRichiedente() + "</b><br/>" +
                        "<span style=\"color: #555; font-size: 14px;\">Riferimento Annuncio ID: " + req.getIdAnnuncio() + "</span>"
        );
        card.add(info);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(10);
        buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonPanel.getElement().getStyle().setProperty("marginTop", "10px");

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
                buttonPanel.add(new HTML(
                        "<span style=\"color: red; font-weight: bold;\">" + testoRifiuto + "</span>"
                ));
                break;
            }
            case "accettata": // Stesso comportamento
            case "in attesa di conferma dall'altro utente": {
                if (stato.equals("accettata")) {
                    buttonPanel.add(new HTML(
                        "<span style=\"color: #28a745; font-weight: bold; margin-right: 10px;\">Richiesta Accettata!</span>"
                    ));
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
                    buttonPanel.add(new HTML(
                            "<span style=\"color: LightSlateGray;\">In attesa della conferma dell'altro utente...</span>"
                    ));
                }
                break;
            }
            case "completato": {
                buttonPanel.add(new HTML(
                        "<span style=\"color: purple;\">Richiesta Completata</span>"
                ));
                if (req.isRecensioneInviata(utenteLoggato.getUsername())) {
                    buttonPanel.add(new HTML("<span style=\"color: SeaGreen;\">✓ Recensione inviata</span>"));
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
                formRecensione.getElement().getStyle().setProperty("marginTop", "10px");
                formRecensione.getElement().getStyle().setProperty("padding", "12px");
                formRecensione.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
                formRecensione.getElement().getStyle().setProperty("border", "1px solid #ddd");
                formRecensione.getElement().getStyle().setProperty("borderRadius", "4px");

                formRecensione.add(new HTML("<b style=\"color:MidnightBlue;\">Valuta " + controparte + ":</b>"));

                // Cinque stelle cliccabili
                HorizontalPanel stellePanel = new HorizontalPanel();
                stellePanel.setSpacing(4);
                Label[] stelle = new Label[5];

                for (int i = 0; i < 5; i++) {
                    final int valore = i + 1; // valore 1-5 catturato dalla lambda
                    stelle[i] = new Label("☆");
                    stelle[i].getElement().getStyle().setProperty("fontSize", "28px");
                    stelle[i].getElement().getStyle().setProperty("cursor", "pointer");
                    stelle[i].getElement().getStyle().setProperty("color", "Silver");

                    stelle[i].addClickHandler(event -> {
                        valutazioneSelezionata[0] = valore; // salvo il valore cliccato

                        //Aggiorna l'aspetto di tutte e 5 le stelle in base alla scelta
                        for (int j = 0; j < 5; j++) {
                            boolean evidenziata = j < valore;
                            stelle[j].setText(evidenziata ? "★" : "☆");
                            stelle[j].getElement().getStyle()
                                    .setProperty("color", evidenziata ? "Gold" : "Silver");
                        }
                    });

                    stellePanel.add(stelle[i]);
                }
                formRecensione.add(stellePanel);

                //Commento facoltativo
                formRecensione.add(new Label("Commento (opzionale):"));
                TextArea txtCommento = new TextArea();
                txtCommento.setWidth("100%");
                txtCommento.setVisibleLines(3);
                txtCommento.getElement().setPropertyString("placeholder", "Scrivi un commento...");
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
                            stella.getElement().getStyle().setProperty("color", "Silver");
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
        btn.getElement().getStyle().setProperty("backgroundColor", coloreBase + " !important");
        btn.getElement().getStyle().setProperty("color", "#ffffff !important");
        btn.getElement().getStyle().setProperty("border", "none !important");
        btn.getElement().getStyle().setProperty("padding", "8px 20px !important");
        btn.getElement().getStyle().setProperty("borderRadius", "4px !important");
        btn.getElement().getStyle().setProperty("cursor", "pointer !important");
        btn.getElement().getStyle().setProperty("fontWeight", "bold !important");
        btn.getElement().getStyle().setProperty("boxShadow", "none !important");
    }
}