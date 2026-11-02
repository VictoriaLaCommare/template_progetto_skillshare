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
        mainContainer.addStyleName("mainContainer-wishlist");

        HTML titolo = new HTML(
            "<h4>💡 Profili suggeriti per questa competenza:</h4>"
        );
        titolo.addStyleName("titoloCardSuggeriti-wishlist");

        mainContainer.add(titolo);

        Label lblStato = new Label("Ricerca profili compatibili...");
        lblStato.addStyleName("lblStato-wishlist");
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
                lblStato.addStyleName("testo-errore");
            }

            @Override
            public void onSuccess(List<Utente> result) {
                mainContainer.remove(lblStato);

                if (result == null || result.isEmpty()) {
                    HTML nessunRisultato = new HTML("<p><i>Nessun profilo trovato al momento. Prova ad aggiungere un servizio con un tag di competenza diverso.</i></p>");
                    nessunRisultato.addStyleName("txtNessunRisultato-wishlist");
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
        cardMatch.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        cardMatch.addStyleName("cardMatch-wishlist");

        String nome = SafeHtmlUtils.htmlEscape(profilo.getNome() + " " + profilo.getCognome());
        String competenze = SafeHtmlUtils.htmlEscape(profilo.getTagCompetenze() != null ? profilo.getTagCompetenze() : "Nessuna");

        HTML infoUtente = new HTML(
            "<div class='contenitore'>" +
            "<h4 class='titolo'>" + nome + "</h4>" +
            "<p class='competenze'>Competenze: <b>" + competenze + "</b></p>" +
            "</div>"
        );
        infoUtente.addStyleName("txtinfoUtente-wishlist");
        infoUtente.addStyleName("larghezzaMax");
        Button btnSalva = new Button("🤍");
        btnSalva.addStyleName("btnCuoreBianco-wishlist");

        // Profilo già salvato in precedenza: lo mostro subito come tale
        if (giaSalvato) {
            btnSalva.setText("❤️");
            btnSalva.addStyleName("btnSfondoTrasparente-wishlist");
        }

        btnSalva.addClickHandler(event -> gestisciToggleProfilo(profilo.getMail(), btnSalva));

        cardMatch.add(infoUtente);
        cardMatch.add(btnSalva);
        cardMatch.setCellHorizontalAlignment(btnSalva, HasHorizontalAlignment.ALIGN_RIGHT);

        return cardMatch;
    }

    private void gestisciToggleProfilo(String mailProfiloTarget, Button bottone) {
        bottone.setEnabled(false);
        boolean isGiaSalvato = bottone.getText().contains("❤️");

        if (isGiaSalvato) {
            // L'utente è già salvato -> Dobbiamo rimuoverlo
            
            matchmakingService.rimuoviProfilo(mailUtenteLoggato, mailProfiloTarget, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    bottone.setEnabled(true);
                    bottone.setText("❤️");
                    Window.alert("Errore durante la rimozione: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Boolean result) {
                    bottone.setEnabled(true);
                    if (result) {
                        // Torna allo stato iniziale
                        bottone.setText("🤍");
                        bottone.addStyleName("btnSfondoTrasparente-wishlist");
                    }
                }
            });
        } else {
            // L'utente non è salvato -> Dobbiamo salvarlo
            
            matchmakingService.salvaProfilo(mailUtenteLoggato, mailProfiloTarget, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    bottone.setEnabled(true);
                    bottone.setText("🤍");
                    Window.alert("Errore durante il salvataggio: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Boolean result) {
                    bottone.setEnabled(true);
                    if (result) {
                        // Cambia in stato salvato
                        bottone.setText("❤️");
                        bottone.addStyleName("btnSfondoTrasparente-wishlist");
                    }
                }
            });
        }
    }
}