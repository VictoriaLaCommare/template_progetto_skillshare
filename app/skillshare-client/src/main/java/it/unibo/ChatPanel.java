package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.Date;
import java.util.List;

public class ChatPanel extends Composite {

    private VerticalPanel mainPanel = new VerticalPanel();
    private ScrollPanel scrollPanel = new ScrollPanel();
    private VerticalPanel messaggiContainer = new VerticalPanel();
    private TextBox inputTextBox = new TextBox();
    private Button inviaButton = new Button("Invia →");

    private ChatServiceAsync chatService = GWT.create(ChatService.class);
    
    private String idRichiesta;
    private String utenteCorrente;
    private Timer refreshTimer;

    public ChatPanel(String idRichiesta, String utenteCorrente) {
        this.idRichiesta = idRichiesta;
        this.utenteCorrente = utenteCorrente;

        initWidget(mainPanel);
        setupUI();
        caricaStorico();
        avviaAggiornamentoAutomatico();
    }

    private void setupUI() {
        mainPanel.addStyleName("larghezzaMax");

        scrollPanel.addStyleName("scrollPanel-chatpanel");
        
        scrollPanel.setWidget(messaggiContainer);
        messaggiContainer.addStyleName("messaggiContainer-chatpanel");
        messaggiContainer.getElement().setId("chat-messaggi-container");

        HorizontalPanel inputPanel = new HorizontalPanel();
        inputPanel.addStyleName("inputPanel-chatpanel");
        
        inputTextBox.addStyleName("inputTextBox-chatpanel");
        inputTextBox.getElement().setId("chat-input-messaggio");

        //inviaButton.setWidth("100%");
        inviaButton.addStyleName("larghezzaMax");
        inviaButton.getElement().setId("chat-btn-invia");
        inviaButton.addClickHandler(event -> inviaMessaggio());

        inputPanel.add(inputTextBox);
        inputPanel.add(inviaButton);
        
        mainPanel.add(scrollPanel);
        mainPanel.add(inputPanel);
    }

    private void inviaMessaggio() {
        String testo = inputTextBox.getText().trim();
        if (testo.isEmpty()) return;

        inviaButton.setEnabled(false);

        Messaggio nuovoMessaggio = new Messaggio(idRichiesta, utenteCorrente, testo, new Date());

        chatService.inviaMessaggio(nuovoMessaggio, new AsyncCallback<Messaggio>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Errore invio: " + caught.getMessage());                
                inviaButton.setEnabled(true);
            }
            @Override
            public void onSuccess(Messaggio result) {
                inputTextBox.setText(""); 
                inviaButton.setEnabled(true);
                caricaStorico(); 
            }
        });
    }

    private void caricaStorico() {
        chatService.getStoricoMessaggi(idRichiesta, new AsyncCallback<List<Messaggio>>() {
            @Override
            public void onFailure(Throwable caught) {}

            @Override
            public void onSuccess(List<Messaggio> storico) {
                messaggiContainer.clear(); 

                for (Messaggio m : storico) {
                    String colore = m.getMittente().equals(utenteCorrente) ? "chatmittente-chatpanel" : "chatdestinatario-chatpanel";
                    HTML riga = new HTML("<b class='" + colore + "'>" + m.getMittente() + ":</b> " + m.getTesto());
                    riga.addStyleName("riga-messaggio-chatpanel");
                    messaggiContainer.add(riga);
                }
                scrollPanel.scrollToBottom();
            }
        });
    }

    private void avviaAggiornamentoAutomatico() {
        refreshTimer = new Timer() {
            @Override
            public void run() { caricaStorico(); }
        };
        refreshTimer.scheduleRepeating(1000);
    }
    
    public void fermaAggiornamento() {
        if (refreshTimer != null) refreshTimer.cancel();
    }
}
