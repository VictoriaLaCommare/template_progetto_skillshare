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
                String coloreBtn = sonoRichiedente ? "#6c757d" : "#dc3545";
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
            case "accettata"://stesso comportamento
            case "in attesa di conferma dall'altro utente" : {
                boolean haGiaConfermato = (sonoRichiedente && req.isConfermataTerminazioneRichiedente())
                        || (!sonoRichiedente && req.isConfermataTerminazioneRicevente());

                if (!haGiaConfermato) {
                    Button btnTermina = new Button("Dichiara Terminazione");
                    btnTermina.getElement().setId("btn-termina-" + req.getIdRichiesta());
                    estilsaPulsante(btnTermina, "#007bff");
                    btnTermina.addClickHandler(event ->
                            richiesteService.dichiaraTerminazione(req.getIdRichiesta(), utenteLoggato.getUsername(), new AsyncCallback<Boolean>() {
                                @Override public void onFailure(Throwable caught) { Window.alert("Errore: " + caught.getMessage()); }
                                @Override public void onSuccess(Boolean result) {
                                    Window.alert(Boolean.TRUE.equals(result) ? "Terminazione dichiarata con successo." : "Errore: impossibile dichiarare la terminazione.");
                                    caricaRichieste(container);
                                }
                            })
                    );
                    buttonPanel.add(btnTermina);
                } else {
                    buttonPanel.add(new HTML(
                            "<span style=\"color: #6c757d;\">In attesa della conferma dell'altro utente...</span>"
                    ));
                }
                break;
            }
            case "completato": {
                buttonPanel.add(new HTML(
                        "<span style=\"color: purple;\">Richiesta Completata</span>"));                break;
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