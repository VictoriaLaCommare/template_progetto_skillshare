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
        mainContainer.setWidth("90%");
        mainContainer.getElement().getStyle().setProperty("margin", "20px auto");
        mainContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP); 
        mainContainer.setSpacing(0);

        mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.setSpacing(15);
        mainPanel.getElement().getStyle().setProperty("fontFamily", "Segoe UI, Tahoma, sans-serif");

        if (utenteLoggato != null) {
            mainPanel.add(new AppNavbar(utenteLoggato, "chat"));
        }

        HTML titolo = new HTML(
            "<div style=\"background-color: #1b263b; color: #ffffff; padding: 25px; border-radius: 12px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); font-family: 'Segoe UI', Tahoma, sans-serif; margin-bottom: 25px;\">" +
            "<h1 style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 28px; font-weight: bold;\">Le Mie Chat Attive</h1>" +
            "<p style=\"margin: 0; font-size: 16px; color: #e0f2f1;\">Comunica con gli utenti per i tuoi scambi approvati</p>" +
            "</div>"
        );
        mainPanel.add(titolo);

        VerticalPanel listaChatPanel = new VerticalPanel();
        listaChatPanel.setWidth("100%");
        listaChatPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER); 
        listaChatPanel.setSpacing(10);
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
                container.add(new HTML("<div style=\"color: red;\">Errore di comunicazione: " + caught.getMessage() + "</div>"));
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
        card.setWidth("100%");
        card.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        card.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
        card.getElement().getStyle().setProperty("borderLeft", "4px solid #1b263b"); 
        card.getElement().getStyle().setProperty("padding", "15px");
        card.getElement().getStyle().setProperty("marginBottom", "12px");
        card.getElement().getStyle().setProperty("borderRadius", "4px");
        card.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");

        String interlocutore = utenteLoggato.getUsername().equals(req.getRichiedente()) ? req.getRicevente() : req.getRichiedente();

        HTML info = new HTML(
            "<b style=\"font-size: 16px; color: #1b263b;\">Scambio con: " + interlocutore + "</b><br/>" +
            "<span style=\"color: #555; font-size: 14px;\">Riferimento Annuncio #" + req.getIdAnnuncio() + "</span>"
        );
        
        Button btnApriChat = new Button("💬 Apri Chat");
        btnApriChat.addClickHandler(event -> apriPopupChat(req, interlocutore));

        card.add(info);
        card.add(btnApriChat);
        card.setCellWidth(info, "80%");
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
        dialogLayout.setWidth("100%");
        dialogLayout.setSpacing(10); 

        ChatPanel chatPanel = new ChatPanel(req.getIdRichiesta(), utenteLoggato.getUsername());
        chatPanel.setWidth("100%");

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
