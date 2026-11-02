package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class LeMieChatGUI {

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

        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "chat"));
        }

        HTML titolo = new HTML(
            "<div>" +
            "<h1>Le Mie Chat Attive</h1>" +
            "<p>Comunica con gli utenti per i tuoi scambi approvati</p>" +
            "</div>"
        );
        titolo.addStyleName("stylePannello");

        mainPanel.add(titolo);

        VerticalPanel listaChatPanel = new VerticalPanel();
        listaChatPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER); 
        listaChatPanel.addStyleName("contenitoreLista");
        mainPanel.add(listaChatPanel);

        caricaChatAttive(listaChatPanel);
        mainContainer.add(mainPanel);

        RootPanel.get().clear();
        RootPanel.get().add(mainContainer);
    }

    private void caricaChatAttive(VerticalPanel container) {
        container.clear();
        
        // Chiediamo al server le richieste
        richiesteService.ottieniTutteLeRichieste(utenteLoggato.getUsername(), new AsyncCallback<List<RichiestaScambio>>() {
            @Override
            public void onFailure(Throwable caught) {
                HTML erroreDiComunicazione = new HTML("<div>Errore di comunicazione: " + caught.getMessage() + "</div>");
                erroreDiComunicazione.addStyleName("testo-errore");
                container.add(erroreDiComunicazione);
            }

            @Override
            public void onSuccess(List<RichiestaScambio> result) {
                container.clear();
                boolean trovataAlmenoUnaChat = false;

                if (result != null) {
                    for (RichiestaScambio req : result) {
                        if ("Accettata".equalsIgnoreCase(req.getStato())) {
                            trovataAlmenoUnaChat = true;
                            container.add(creaCardChat(req));
                        }
                    }
                }

                if (!trovataAlmenoUnaChat) {
                    container.add(new Label("Non hai ancora chat attive. Accetta una richiesta per iniziare a chattare!"));
                }
            }
        });
    }

    private Widget creaCardChat(RichiestaScambio req) {
        HorizontalPanel card = new HorizontalPanel();
        card.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        card.addStyleName("cardChat-lemiechat");

        String interlocutore = utenteLoggato.getUsername().equals(req.getRichiedente()) ? req.getRicevente() : req.getRichiedente();

        HTML info = new HTML(
            "<b class='titoloScambio'>Scambio con: " + interlocutore + "</b><br/>" +
            "<span class='riferimentoAnnuncio'>Riferimento Annuncio #" + req.getIdAnnuncio() + "</span>"
        );
        info.addStyleName("infoCard-lemiechat");
        
        Button btnApriChat = new Button("💬 Apri Chat");
        btnApriChat.addClickHandler(event -> apriPopupChat(req, interlocutore));

        card.add(info);
        card.add(btnApriChat);
        card.setCellHorizontalAlignment(btnApriChat, HasHorizontalAlignment.ALIGN_RIGHT);

        return card;
    }

    private void apriPopupChat(RichiestaScambio req, String interlocutore) {
        DialogBox chatDialog = new DialogBox();
        chatDialog.setText("Chat con " + interlocutore);
        chatDialog.setGlassEnabled(true);
        chatDialog.setAnimationEnabled(false);

        chatDialog.setWidth("100%");

        VerticalPanel dialogLayout = new VerticalPanel(); 
        dialogLayout.addStyleName("contenitoreLista");

        ChatPanel chatPanel = new ChatPanel(req.getIdRichiesta(), utenteLoggato.getUsername());
        chatPanel.addStyleName("larghezzaMax");

        Button btnChiudi = new Button("X");
        btnChiudi.getElement().setId("chat-btn-chiudi");
        btnChiudi.addClickHandler(event -> {
            chatPanel.fermaAggiornamento();
            chatDialog.hide();
        });

        dialogLayout.add(chatPanel);
        dialogLayout.add(btnChiudi);
        dialogLayout.setCellHorizontalAlignment(btnChiudi, HasHorizontalAlignment.ALIGN_RIGHT);

        chatDialog.setWidget(dialogLayout);
        chatDialog.center();
    }
}