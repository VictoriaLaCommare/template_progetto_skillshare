package it.unibo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;
import java.util.List;

public class HomePageGUI {

    private final GestoreAutenticazioneAsync rpcService = GWT.create(GestoreAutenticazione.class);

    private VerticalPanel mainPanel;

    private void evidenzia(Widget w, boolean valido) {
        if (valido) {
            w.removeStyleName("input-errore");
        } else {
            w.addStyleName("input-errore");
        }
    }

    private void mostraErrore(Label lbl, String testo) {
        lbl.removeStyleName("testo-successo");
        lbl.addStyleName("testo-errore");
        lbl.setText(testo);
    }

    private void mostraSuccesso(Label lbl, String testo) {
        lbl.removeStyleName("testo-errore");
        lbl.addStyleName("testo-successo");
        lbl.setText(testo);
    }

    public void mostra() {
        mainPanel = new VerticalPanel();
        mainPanel.addStyleName("main-panel-home");
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        mostraLandingPage();

        RootPanel.get().clear();
        RootPanel.get().add(mainPanel);
    }

    // SCHERMATA INIZIALE (LANDING PAGE)
    private void mostraLandingPage() {
        mainPanel.clear();

        // 1. Banner di benvenuto
        HTML banner = new HTML(
            "<div>" +
            "<h1>Benvenuto in SkillShare!</h1>" +
            "<p>La piattaforma ideale per condividere le tue competenze e trovare aiuto nella community.</p>" +
            "</div>"
        );
        banner.addStyleName("banner-home");
        mainPanel.add(banner);

        // 2. Pannello con i pulsanti Accedi e Registrati
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.addStyleName("buttonPanel-home");

        // --- BLOCCO ACCEDI ---
        VerticalPanel panelAccedi = new VerticalPanel();
        panelAccedi.addStyleName("panelAccediRegistrati-home");
        panelAccedi.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        Button btnVaiLogin = new Button("Accedi");
        btnVaiLogin.getElement().setId("btn-vai-login");
        btnVaiLogin.addStyleName("btnPrincipale-home");
        btnVaiLogin.addClickHandler(event -> mostraFormLogin());

        HTML lblInfoAccedi = new HTML("<div>Se sei già registrato</div>");
        lblInfoAccedi.addStyleName("lblInfo-home");
        panelAccedi.add(lblInfoAccedi);
        panelAccedi.add(btnVaiLogin);
        
        // --- BLOCCO REGISTRATI ---
        VerticalPanel panelRegistra = new VerticalPanel();
        panelRegistra.addStyleName("panelAccediRegistrati-home");
        panelRegistra.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        Button btnVaiRegistra = new Button("Registrati");
        btnVaiRegistra.getElement().setId("btn-vai-registra");
        btnVaiRegistra.addStyleName("btnPrincipale-home");
        btnVaiRegistra.addClickHandler(event -> mostraFormRegistrazione());

        HTML lblInfoRegistra = new HTML("<div>Se sei un nuovo utente</div>");   
        lblInfoRegistra.addStyleName("lblInfo-home");
        panelRegistra.add(lblInfoRegistra);
        panelRegistra.add(btnVaiRegistra);

        buttonPanel.add(panelAccedi);
        buttonPanel.add(panelRegistra);
        mainPanel.add(buttonPanel);

        // 3. Testo esplicativo sotto i pulsanti
        HTML lblInvito = new HTML(
            "<div>" +
            "Per pubblicare, gestire o chiedere gli scambi, entra nella community registrandoti o accedendo." +
            "</div>"
        );
        lblInvito.addStyleName("lblInvito-home");
        mainPanel.add(lblInvito);

        // 4. Sezione Anteprima Annunci Pubblici con tasto "Vedi tutti"
        HTML titoloSezione = new HTML("<h3>Ultimi Annunci Pubblici</h3>");
        titoloSezione.addStyleName("titoloSezione-home");
        mainPanel.add(titoloSezione);

        VerticalPanel panelListaAnteprima = new VerticalPanel();
        panelListaAnteprima.addStyleName("panelListaAnteprima");
        panelListaAnteprima.add(new Label("Caricamento annunci in corso..."));
        
        mainPanel.add(panelListaAnteprima);

        caricaAnnunciAnteprima(panelListaAnteprima);
    }

    private void caricaAnnunciAnteprima(VerticalPanel container) {
        GestoreAnnunciAsync annuncioService = GWT.create(GestoreAnnunci.class);
        annuncioService.ottieniTuttiGliAnnunci(new AsyncCallback<List<Annuncio>>() {
            @Override
            public void onFailure(Throwable caught) {
                container.clear();
                container.add(new Label("Errore RPC: " + (caught.getMessage() != null ? caught.getMessage() : caught.getClass().getName())));
                System.err.println("Errore caricamento annunci: ");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(List<Annuncio> annunci) {
                container.clear();
                if (annunci == null || annunci.isEmpty()) {
                    container.add(new Label("Nessun annuncio presente al momento. Sii il primo a pubblicarne uno!"));
                    return;
                }
                popolaListaAnnunciInPagina(container, annunci, 3); // Mostra inizialmente solo i primi 3
            }
        });
    }

    private void popolaListaAnnunciInPagina(VerticalPanel container, List<Annuncio> annunci, int limiteIniziale) {
        container.clear();

        int numeroDaMostrare = Math.min(limiteIniziale, annunci.size());

        for (int i = 0; i < numeroDaMostrare; i++) {
            Annuncio a = annunci.get(i);

            HorizontalPanel cardWrapper = new HorizontalPanel();
            cardWrapper.addStyleName("cardWrapper-home");
            cardWrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

            VerticalPanel textPanel = new VerticalPanel();
            textPanel.addStyleName("textPanel-home");

            HTML lblTitolo = new HTML("<b class='lbltitolo'>" + a.getTitolo() + "</b>");
            HTML lblCompetenze = new HTML("<span class='contenitore'><b>Offre:</b> " + a.getCompetenzaOfferta() + " | <b>Richiede:</b> " + a.getCompetenzaRichiesta() + "</span>");
            HTML lblDescrizione = new HTML("<p class='lbldescrizione'><em>" + a.getDescrizione() + "</em></p>");
            lblTitolo.addStyleName("lbl-home");
            lblCompetenze.addStyleName("lbl-home");
            lblDescrizione.addStyleName("lbl-home");

            textPanel.add(lblTitolo);
            textPanel.add(lblCompetenze);
            textPanel.add(lblDescrizione);

            Button btnAutore = new Button("👤 " + a.getAutoreUsername());
            btnAutore.addStyleName("btnAutore-home");

            btnAutore.addClickHandler(e -> {
                new ProfiloUtenteGUI().mostra(null, a.getAutoreUsername());
            });

            cardWrapper.add(textPanel);
            cardWrapper.setCellWidth(textPanel, "80%");
            
            cardWrapper.add(btnAutore);
            cardWrapper.setCellWidth(btnAutore, "20%");
            cardWrapper.setCellHorizontalAlignment(btnAutore, HasHorizontalAlignment.ALIGN_RIGHT);

            // Aggiungiamo alla lista della pagina SOLO il wrapper completo
            container.add(cardWrapper);
        }

        // Se ci sono più annunci di quelli mostrati, aggiungi il pulsante "Vedi tutti"
        HorizontalPanel panelBtnWrapper = new HorizontalPanel();
        panelBtnWrapper.addStyleName("panelBtnWrapper-home");
        panelBtnWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        if (annunci.size() > limiteIniziale) {
            Button btnVediTutti = new Button("↓ Vedi tutti gli annunci (" + annunci.size() + ")");
            btnVediTutti.getElement().setId("btn-vedi-tutti");
            btnVediTutti.addStyleName("btnVedi-home");

            btnVediTutti.addClickHandler(event -> popolaListaAnnunciInPagina(container, annunci, annunci.size()));
            
            panelBtnWrapper.add(btnVediTutti);
            
            container.add(panelBtnWrapper);
        } else if (annunci.size() > 3 && limiteIniziale >= annunci.size()){
            Button btnRiduci = new Button("↑ Riduci lista");
            btnRiduci.getElement().setId("btn-riduci");
            btnRiduci.addStyleName("btnVedi-home");

            btnRiduci.addClickHandler(event -> popolaListaAnnunciInPagina(container, annunci, 3));
            
            panelBtnWrapper.add(btnRiduci);
            
            container.add(panelBtnWrapper);
        }
    }

    // REGISTRAZIONE 
    private void mostraFormRegistrazione() {
        mainPanel.clear();

        Button btnIndietro = new Button("← Torna alla Home");
        btnIndietro.addClickHandler(event -> mostraLandingPage());
        mainPanel.add(btnIndietro);

        mainPanel.add(new HTML("<h2>Crea un nuovo account</h2>"));

        // NOME
        TextBox txtNome = new TextBox();
        txtNome.getElement().setPropertyString("placeholder", "Nome");
        txtNome.getElement().setId("reg-nome");
        Label errNome = new Label();
        errNome.addStyleName("testo-errore");
        HorizontalPanel rowNome = new HorizontalPanel();
        rowNome.addStyleName("form-row");
        rowNome.add(txtNome);
        rowNome.add(errNome);

        // COGNOME
        TextBox txtCognome = new TextBox();
        txtCognome.getElement().setPropertyString("placeholder", "Cognome");
        txtCognome.getElement().setId("reg-cognome");
        Label errCognome = new Label();
        errCognome.addStyleName("testo-errore");
        HorizontalPanel rowCognome = new HorizontalPanel();
        rowCognome.addStyleName("form-row");
        rowCognome.add(txtCognome);
        rowCognome.add(errCognome);

        // USERNAME
        TextBox txtUser = new TextBox();
        txtUser.getElement().setPropertyString("placeholder", "Username");
        txtUser.getElement().setId("reg-username");
        Label errUser = new Label();
        errUser.addStyleName("testo-errore");
        HorizontalPanel rowUser = new HorizontalPanel();
        rowUser.addStyleName("form-row");
        rowUser.add(txtUser);
        rowUser.add(errUser);

        // EMAIL
        TextBox txtMail = new TextBox();
        txtMail.getElement().setPropertyString("placeholder", "Email");
        txtMail.getElement().setId("reg-mail");
        Label errMail = new Label();
        errMail.addStyleName("testo-errore");
        HorizontalPanel rowMail = new HorizontalPanel();
        rowMail.addStyleName("form-row");
        rowMail.add(txtMail);
        rowMail.add(errMail);

        // DATA DI NASCITA
        DatePicker datePicker = new DatePicker();
        datePicker.setYearArrowsVisible(true);
        DateBox dateNascitaBox = new DateBox(
                datePicker,
                null,
                new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd/MM/yyyy"))
        );
        dateNascitaBox.getElement().setPropertyString("placeholder", "Data di nascita (gg/mm/aaaa)");
        dateNascitaBox.getElement().setId("reg-data");
        Label errData = new Label();
        errData.addStyleName("testo-errore");
        HorizontalPanel rowData = new HorizontalPanel();
        rowData.addStyleName("form-row");
        rowData.add(dateNascitaBox);
        rowData.add(errData);

        // PASSWORD
        PasswordTextBox txtPass = new PasswordTextBox();
        txtPass.getElement().setPropertyString("placeholder", "Password");
        txtPass.getElement().setId("reg-password");
        Label errPass = new Label();
        errPass.addStyleName("testo-errore");
        HorizontalPanel rowPass = new HorizontalPanel();
        rowPass.addStyleName("form-row");
        rowPass.add(txtPass);
        rowPass.add(errPass);

        Button btnRegistrati = new Button("Registrati");
        btnRegistrati.getElement().setId("reg-submit");

        Label lblMessaggio = new Label();
        lblMessaggio.getElement().setId("reg-message");

        btnRegistrati.addClickHandler(event -> {
            errNome.setText("");
            errCognome.setText("");
            errUser.setText("");
            errMail.setText("");
            errData.setText("");
            errPass.setText("");
            lblMessaggio.setText("");

            boolean nomeOk = FieldVerifier.isValidName(txtNome.getText());
            boolean cognomeOk = FieldVerifier.isValidName(txtCognome.getText());
            boolean userOk = FieldVerifier.isValidUsername(txtUser.getText());
            boolean mailOk = FieldVerifier.isValidEmail(txtMail.getText());
            boolean passOk = FieldVerifier.isValidPassword(txtPass.getText());
            Date dataNascita = dateNascitaBox.getValue();
            boolean dataOk = FieldVerifier.isValidDataNascita(dataNascita);

            evidenzia(txtNome, nomeOk);
            if (!nomeOk) errNome.setText("Nome non valido");

            evidenzia(txtCognome, cognomeOk);
            if (!cognomeOk) errCognome.setText("Cognome non valido");

            evidenzia(txtUser, userOk);
            if (!userOk) errUser.setText("Username non valido");

            evidenzia(txtMail, mailOk);
            if (!mailOk) errMail.setText("Email non valida");

            evidenzia(dateNascitaBox, dataOk);
            if (!dataOk) errData.setText("Data non valida");

            evidenzia(txtPass, passOk);
            if (!passOk) errPass.setText("Password non valida");

            if (!nomeOk || !cognomeOk || !userOk || !mailOk || !dataOk || !passOk) {
                mostraErrore(lblMessaggio, "Controlla gli errori segnalati accanto ai campi e riprova.");
                return;
            }

            Utente u = new Utente();
            u.setNome(txtNome.getText());
            u.setCognome(txtCognome.getText());
            u.setUsername(txtUser.getText());
            u.setMail(txtMail.getText());
            u.setPassword(txtPass.getText());
            u.setData(dataNascita);

            rpcService.registraUtente(u, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    String msg = caught.getMessage() == null ? "" : caught.getMessage();

                    if (msg.contains("già registrato")) {
                        evidenzia(txtMail, false);
                        errMail.setText("Email già registrata, effettua il login.");
                        mostraErrore(lblMessaggio, "Errore: questa email è già registrata.");
                        return;
                    }

                    if (caught instanceof IllegalArgumentException) {
                        boolean isNomeErr = msg.contains("Nome");
                        boolean isCognomeERr = msg.contains("Cognome");
                        boolean isUserErr = msg.contains("Username");
                        boolean isMailErr = msg.contains("mail");
                        boolean isDataErr = msg.contains("Data di nascita");
                        boolean isPassErr = msg.contains("Password");

                        evidenzia(txtNome, !isNomeErr);
                        if (isNomeErr) errNome.setText("Rifiutato dal server");

                        evidenzia(txtCognome, !isCognomeERr);
                        if (isCognomeERr) errCognome.setText("Rifiutato dal server");

                        evidenzia(txtUser, !isUserErr);
                        if (isUserErr) errUser.setText("Rifiutato dal server");

                        evidenzia(txtMail, !isMailErr);
                        if (isMailErr) errMail.setText("Rifiutato dal server");

                        evidenzia(dateNascitaBox, !isDataErr);
                        if (isDataErr) errData.setText("Rifiutata dal server");

                        evidenzia(txtPass, !isPassErr);
                        if (isPassErr) errPass.setText("Rifiutata dal server");

                        mostraErrore(lblMessaggio, "Errore di validazione dal server. " + msg);
                        return;
                    }

                    mostraErrore(lblMessaggio, "Errore di comunicazione col server: " + msg);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (Boolean.TRUE.equals(result)) {
                        mostraSuccesso(lblMessaggio, "Registrazione completata con successo! Reindirizzamento al login...");
                        
                        // Reindirizzamento automatico al form di login dopo 1.5 secondi
                        com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
                            @Override
                            public void run() {
                                mostraFormLogin();
                            }
                        };
                        timer.schedule(1500);
                    }
                }
            });
        });

        mainPanel.add(rowNome);
        mainPanel.add(rowCognome);
        mainPanel.add(rowUser);
        mainPanel.add(rowMail);
        mainPanel.add(rowData);
        mainPanel.add(rowPass);
        mainPanel.add(btnRegistrati);
        mainPanel.add(lblMessaggio);
    }

    // LOGIN
    private void mostraFormLogin() {
        mainPanel.clear();

        Button btnIndietro = new Button("← Torna alla Home");
        btnIndietro.addClickHandler(event -> mostraLandingPage());
        mainPanel.add(btnIndietro);

        mainPanel.add(new HTML("<h2>Accedi al tuo account</h2>"));

        TextBox txtLoginMail = new TextBox();
        txtLoginMail.getElement().setPropertyString("placeholder", "Email");
        txtLoginMail.getElement().setId("login-mail");
        Label errLoginMail = new Label();
        errLoginMail.getElement().getStyle().setColor("red");
        errLoginMail.getElement().setId("login-mail-error");
        HorizontalPanel rowLoginMail = new HorizontalPanel();
        rowLoginMail.addStyleName("form-row");
        rowLoginMail.add(txtLoginMail);
        rowLoginMail.add(errLoginMail);

        PasswordTextBox txtLoginPassword = new PasswordTextBox();
        txtLoginPassword.getElement().setPropertyString("placeholder", "Password");
        txtLoginPassword.getElement().setId("login-password");
        Label errLoginPassword = new Label();
        errLoginPassword.getElement().getStyle().setColor("red");
        errLoginPassword.getElement().setId("login-password-error");
        HorizontalPanel rowLoginPassword = new HorizontalPanel();
        rowLoginPassword.addStyleName("form-row");
        rowLoginPassword.add(txtLoginPassword);
        rowLoginPassword.add(errLoginPassword);

        Button btnLogin = new Button("Accedi");
        btnLogin.getElement().setId("login-submit");

        Label lblLoginMessaggio = new Label();
        lblLoginMessaggio.getElement().setId("login-message");

        btnLogin.addClickHandler(event -> {
            errLoginMail.setText("");
            errLoginPassword.setText("");
            lblLoginMessaggio.setText("");

            boolean mailOk = FieldVerifier.isValidEmail(txtLoginMail.getText());
            boolean passOk = !txtLoginPassword.getText().isEmpty();

            evidenzia(txtLoginMail, mailOk);
            if (!mailOk) errLoginMail.setText("Email non valida");

            evidenzia(txtLoginPassword, passOk);
            if (!passOk) errLoginPassword.setText("Password obbligatoria");

            if (!mailOk || !passOk) {
                mostraErrore(lblLoginMessaggio, "Controlla gli errori segnalati accanto ai campi e riprova.");
                return;
            }

            rpcService.effettuaLogin(txtLoginMail.getText(), txtLoginPassword.getText(), new AsyncCallback<Utente>() {
                @Override
                public void onFailure(Throwable caught) {
                    if (caught instanceof IllegalArgumentException) {
                        evidenzia(txtLoginMail, false);
                        errLoginMail.setText("Rifiutata dal server");
                        mostraErrore(lblLoginMessaggio, "Errore: " + caught.getMessage());
                        return;
                    }
                    mostraErrore(lblLoginMessaggio, "Errore di comunicazione col server: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Utente utente) {
                    if (utente != null) {
                        new PubblicaAnnunciGui().mostra(utente);
                    } else {
                        evidenzia(txtLoginMail, false);
                        evidenzia(txtLoginPassword, false);
                        mostraErrore(lblLoginMessaggio, "Credenziali errate, riprova.");
                    }
                }
            });
        });

        mainPanel.add(rowLoginMail);
        mainPanel.add(rowLoginPassword);
        mainPanel.add(btnLogin);
        mainPanel.add(lblLoginMessaggio);
    }
}