package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
    private Button inviaButton = new Button("➤");

    // Collegamento con il backend
    private ChatServiceAsync chatService = GWT.create(ChatService.class);
    
    private String idRichiesta;
    private String utenteCorrente;
    private Timer refreshTimer;

    // Il costruttore richiede l'ID della chat (idRichiesta) e chi sta scrivendo
    public ChatPanel(String idRichiesta, String utenteCorrente) {
        this.idRichiesta = idRichiesta;
        this.utenteCorrente = utenteCorrente;

        initWidget(mainPanel);
        setupUI();
        caricaStorico();
        avviaAggiornamentoAutomatico();
    }

    private void setupUI() {
        mainPanel.setWidth("100%");
        mainPanel.setSpacing(10);

        // Configurazione dell'area di scorrimento dei messaggi
        scrollPanel.setSize("100%", "300px");
        scrollPanel.setWidget(messaggiContainer);
        messaggiContainer.setWidth("100%");
        messaggiContainer.setSpacing(5);
        scrollPanel.getElement().setId("chat-scroll-panel"); 

        // Configurazione della barra di input
        HorizontalPanel inputPanel = new HorizontalPanel();
        inputPanel.setWidth("100%");
        inputPanel.setSpacing(5);
        
        inputTextBox.setWidth("100%");
        inputTextBox.getElement().setId("chat-input"); 

        inviaButton.getElement().setId("chat-btn-invia"); 
        inviaButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                inviaMessaggio();
            }
        });

        // La TextBox si prende tutto lo spazio rimasto, il bottone il necessario
        inputPanel.add(inputTextBox);
        inputPanel.setCellWidth(inputTextBox, "80%");
        inputPanel.add(inviaButton);
        inputPanel.setCellWidth(inviaButton, "20%");

        mainPanel.add(scrollPanel);
        mainPanel.add(inputPanel);
    }

    private void inviaMessaggio() {
        String testo = inputTextBox.getText().trim();
        if (testo.isEmpty()) {
            return; // Non invia messaggi vuoti
        }

        inviaButton.setEnabled(false); // Disabilita il bottone mentre invia per evitare doppi click

        Messaggio nuovoMessaggio = new Messaggio(idRichiesta, utenteCorrente, testo, new Date());

        chatService.inviaMessaggio(nuovoMessaggio, new AsyncCallback<Messaggio>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Errore di connessione: il messaggio non è stato inviato.");
                inviaButton.setEnabled(true);
            }

            @Override
            public void onSuccess(Messaggio result) {
                inputTextBox.setText(""); // Svuota la casella di testo
                inviaButton.setEnabled(true);
                caricaStorico(); // Ricarica la chat per mostrare il messaggio
            }
        });
    }

    private void caricaStorico() {
        chatService.getStoricoMessaggi(idRichiesta, new AsyncCallback<List<Messaggio>>() {
            @Override
            public void onFailure(Throwable caught) {
                // Silenzioso per non disturbare l'utente ad ogni aggiornamento fallito
            }

            @Override
            public void onSuccess(List<Messaggio> storico) {
                messaggiContainer.clear(); // Pulisce i vecchi messaggi

                for (Messaggio m : storico) {
                    HorizontalPanel rigaMessaggio = new HorizontalPanel();
                    rigaMessaggio.setWidth("100%");

                    Label labelMittente = new Label(m.getMittente() + ": ");
                    labelMittente.getElement().getStyle().setProperty("fontWeight", "bold");
                    
                    Label labelTesto = new Label(m.getTesto());

                    // Se il messaggio è mio lo coloro diversamente
                    if (m.getMittente().equals(utenteCorrente)) {
                        labelMittente.getElement().getStyle().setProperty("color", "blue");
                    } else {
                        labelMittente.getElement().getStyle().setProperty("color", "green");
                    }

                    rigaMessaggio.add(labelMittente);
                    rigaMessaggio.add(labelTesto);
                    
                    messaggiContainer.add(rigaMessaggio);
                }

                // Scorre la barra in basso per far vedere l'ultimo messaggio
                scrollPanel.scrollToBottom();
            }
        });
    }

    // GWT non ha WebSockets di default, quindi usiamo un Timer per chiedere al server i nuovi messaggi ogni 3 secondi
    private void avviaAggiornamentoAutomatico() {
        refreshTimer = new Timer() {
            @Override
            public void run() {
                caricaStorico();
            }
        };
        // 3000 millisecondi = 3 secondi
        refreshTimer.scheduleRepeating(1000);
    }
    
    // Metodo utile per fermare il timer se l'utente cambia schermata
    public void fermaAggiornamento() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }
}