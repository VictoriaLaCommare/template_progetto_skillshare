package it.unibo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class ProfiliSuggeritiWidget extends Composite {

    private final MatchmakingServiceAsync matchmakingService = GWT.create(MatchmakingService.class);
    private VerticalPanel mainContainer;
    private String mailUtenteLoggato;
    private String tagRicercato;

    public ProfiliSuggeritiWidget(String mailUtenteLoggato, String tagRicercato) {
        this.mailUtenteLoggato = mailUtenteLoggato;
        this.tagRicercato = tagRicercato;

        mainContainer = new VerticalPanel();
        mainContainer.setWidth("100%");
        mainContainer.setSpacing(5);
        mainContainer.getElement().getStyle().setProperty("marginTop", "10px");
        mainContainer.getElement().getStyle().setProperty("marginBottom", "25px");
        mainContainer.getElement().getStyle().setProperty("paddingLeft", "20px");
        mainContainer.getElement().getStyle().setProperty("borderLeft", "4px solid #1b263b");

        HTML titolo = new HTML(
            "<h4 style=\"margin: 0 0 10px 0; color: #1b263b; font-size: 16px;\">💡 Profili suggeriti per questa competenza:</h4>"
        );
        mainContainer.add(titolo);

        Label lblStato = new Label("Ricerca profili compatibili...");
        lblStato.getElement().getStyle().setProperty("fontStyle", "italic");
        lblStato.getElement().getStyle().setProperty("color", "#757575");
        mainContainer.add(lblStato);

        initWidget(mainContainer);

        // Avvia la ricerca al server
        cercaProfiliCompatibili(lblStato);
    }

    private void cercaProfiliCompatibili(Label lblStato) {
        matchmakingService.trovaProfiliCompatibili(mailUtenteLoggato, tagRicercato, new AsyncCallback<List<Utente>>() {
            @Override
            public void onFailure(Throwable caught) {
                lblStato.setText("Errore nella ricerca dei profili: " + caught.getMessage());
                lblStato.getElement().getStyle().setProperty("color", "red");
            }

            @Override
            public void onSuccess(List<Utente> result) {
                mainContainer.remove(lblStato);

                if (result == null || result.isEmpty()) {
                    HTML nessunRisultato = new HTML("<p style=\"margin: 0; font-size: 14px; color: #757575;\"><i>Nessun profilo trovato al momento. Prova ad aggiungere un servizio con un tag di competenza diverso.</i></p>");
                    mainContainer.add(nessunRisultato);
                    return;
                }

                // Recupero i profili già salvati, così quelli già scelti risultano "Salvato"
                matchmakingService.getProfiliSalvati(mailUtenteLoggato, new AsyncCallback<List<String>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // Se non riesco a leggerli mostro comunque i suggerimenti
                        mostraCard(result, new HashSet<String>());
                    }

                    @Override
                    public void onSuccess(List<String> salvati) {
                        mostraCard(result, new HashSet<String>(salvati));
                    }
                });
            }
        });
    }

    private void mostraCard(List<Utente> profili, Set<String> mailSalvate) {
        for (Utente u : profili) {
            mainContainer.add(creaCardProfilo(u, mailSalvate.contains(u.getMail())));
        }
    }

    private HorizontalPanel creaCardProfilo(Utente profilo, boolean giaSalvato) {
        HorizontalPanel cardMatch = new HorizontalPanel();
        cardMatch.setWidth("100%");
        cardMatch.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        cardMatch.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
        cardMatch.getElement().getStyle().setProperty("padding", "15px");
        cardMatch.getElement().getStyle().setProperty("borderRadius", "8px");
        cardMatch.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.1)");
        cardMatch.getElement().getStyle().setProperty("marginBottom", "8px");
        cardMatch.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");

        String nome = SafeHtmlUtils.htmlEscape(profilo.getNome() + " " + profilo.getCognome());
        String competenze = SafeHtmlUtils.htmlEscape(profilo.getTagCompetenze() != null ? profilo.getTagCompetenze() : "Nessuna");

        HTML infoUtente = new HTML(
            "<div style=\"flex-grow: 1;\">" +
            "<h4 style=\"margin: 0 0 4px 0; color: #1b263b; font-size: 15px;\">" + nome + "</h4>" +
            "<p style=\"margin: 0; font-size: 13px; color: #555555;\">Competenze: <b>" + competenze + "</b></p>" +
            "</div>"
        );
        infoUtente.setWidth("100%");

        Button btnSalva = new Button("⭐ Salva Profilo");
        btnSalva.getElement().getStyle().setProperty("background", "#1b263b");
        btnSalva.getElement().getStyle().setProperty("color", "#ffffff");
        btnSalva.getElement().getStyle().setProperty("border", "none");
        btnSalva.getElement().getStyle().setProperty("padding", "8px 15px");
        btnSalva.getElement().getStyle().setProperty("borderRadius", "4px");
        btnSalva.getElement().getStyle().setProperty("cursor", "pointer");
        btnSalva.getElement().getStyle().setProperty("fontWeight", "bold");
        btnSalva.getElement().getStyle().setProperty("whiteSpace", "nowrap");

        // Profilo già salvato in precedenza: lo mostro subito come tale
        if (giaSalvato) {
            btnSalva.setText("✅ Salvato");
            btnSalva.getElement().getStyle().setProperty("background", "#2e7d32");
        }

        btnSalva.addClickHandler(event -> gestisciToggleProfilo(profilo.getMail(), btnSalva));

        cardMatch.add(infoUtente);
        cardMatch.setCellWidth(infoUtente, "80%");
        cardMatch.add(btnSalva);
        cardMatch.setCellHorizontalAlignment(btnSalva, HasHorizontalAlignment.ALIGN_RIGHT);

        return cardMatch;
    }

    private void gestisciToggleProfilo(String mailProfiloTarget, Button bottone) {
        bottone.setEnabled(false);
        boolean isGiaSalvato = bottone.getText().contains("Salvato");

        if (isGiaSalvato) {
            // L'utente è già salvato -> Dobbiamo rimuoverlo
            bottone.setText("⏳ Rimozione...");
            
            matchmakingService.rimuoviProfilo(mailUtenteLoggato, mailProfiloTarget, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    bottone.setEnabled(true);
                    bottone.setText("✅ Salvato");
                    Window.alert("Errore durante la rimozione: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Boolean result) {
                    bottone.setEnabled(true);
                    if (result) {
                        // Torna allo stato iniziale
                        bottone.setText("⭐ Salva Profilo");
                        bottone.getElement().getStyle().setProperty("background", "#1b263b");
                    }
                }
            });
        } else {
            // L'utente non è salvato -> Dobbiamo salvarlo
            bottone.setText("⏳ Salvataggio...");
            
            matchmakingService.salvaProfilo(mailUtenteLoggato, mailProfiloTarget, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    bottone.setEnabled(true);
                    bottone.setText("⭐ Salva Profilo");
                    Window.alert("Errore durante il salvataggio: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Boolean result) {
                    bottone.setEnabled(true);
                    if (result) {
                        // Cambia in stato salvato
                        bottone.setText("✅ Salvato");
                        bottone.getElement().getStyle().setProperty("background", "#2e7d32");
                    }
                }
            });
        }
    }
}