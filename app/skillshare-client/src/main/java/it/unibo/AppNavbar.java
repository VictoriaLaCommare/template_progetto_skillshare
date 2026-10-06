package it.unibo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class AppNavbar extends Composite {

    public AppNavbar(Utente utenteLoggato, String paginaAttiva) {
        HorizontalPanel navbar = new HorizontalPanel();
        navbar.setWidth("100%"); 
        navbar.setSpacing(10);
        navbar.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);        
        navbar.getElement().getStyle().setProperty("backgroundColor", "#1b263b");
        navbar.getElement().getStyle().setProperty("padding", "10px 20px");
        navbar.getElement().getStyle().setProperty("boxShadow", "0 4px 8px rgba(0,0,0,0.15)");
        navbar.getElement().getStyle().setProperty("borderRadius", "12px"); 
        navbar.getElement().getStyle().setProperty("margin", "0 auto 20px auto");
        navbar.getElement().getStyle().setProperty("boxSizing", "border-box");

        // Logo
        Label lblLogo = new Label("SkillShare");
        lblLogo.getElement().getStyle().setProperty("color", "#ffffff");
        lblLogo.getElement().getStyle().setProperty("fontSize", "20px");
        lblLogo.getElement().getStyle().setProperty("fontWeight", "bold");
        lblLogo.getElement().getStyle().setProperty("cursor", "pointer");
        lblLogo.addClickHandler(event -> new PubblicaAnnunciGui().mostra(utenteLoggato));
        
        navbar.add(lblLogo);
        navbar.setCellWidth(lblLogo, "30%");

        // Pannello Menu
        HorizontalPanel menuPanel = new HorizontalPanel();
        menuPanel.setSpacing(15);
        menuPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        menuPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        // 1. Bacheca
        Button btnBacheca = creaPannelloStileLink("📢 Bacheca");
        btnBacheca.getElement().setId("nav-btn-bacheca"); 
        if ("bacheca".equals(paginaAttiva)) evidenziaBottone(btnBacheca);
        btnBacheca.addClickHandler(event -> new PubblicaAnnunciGui().mostra(utenteLoggato));
        
        // 2. Cerca / Proposte
        Button btnProposte = creaPannelloStileLink("🔍 Cerca / Scambi");
        btnProposte.getElement().setId("nav-btn-proposte"); 
        if ("ricerca".equals(paginaAttiva)) evidenziaBottone(btnProposte);
        btnProposte.addClickHandler(event -> new BachecaProposteGUI().mostra(utenteLoggato));

        // 3. I Miei Annunci
        Button btnMieiAnnunci = creaPannelloStileLink("📋 I Miei Annunci");
        btnMieiAnnunci.getElement().setId("btn-miei-annunci"); 
        if ("miei_annunci".equals(paginaAttiva)) evidenziaBottone(btnMieiAnnunci);
        btnMieiAnnunci.addClickHandler(event -> {
            RootPanel.get().clear();
            RootPanel.get().add(new MieiAnnunciPanel(utenteLoggato, () -> new PubblicaAnnunciGui().mostra(utenteLoggato)));
        });

        // 4. Richieste Ricevute
        Button btnRichieste = creaPannelloStileLink("📥 Richieste Ricevute");
        btnRichieste.getElement().setId("nav-btn-richieste"); 
        if ("richieste".equals(paginaAttiva)) evidenziaBottone(btnRichieste);
        btnRichieste.addClickHandler(event -> new GestisciRichiesteGUI().mostra(utenteLoggato));

        // 5. Chat
        Button btnChat = creaPannelloStileLink("💬 Chat");
        btnChat.getElement().setId("nav-btn-chat"); 
        if ("chat".equals(paginaAttiva)) evidenziaBottone(btnChat);
        btnChat.addClickHandler(event -> new LeMieChatGUI().mostra(utenteLoggato));

        // 6. Profilo
        Button btnProfilo = creaPannelloStileLink("👤 Profilo");
        btnProfilo.getElement().setId("nav-btn-profilo"); 
        btnProfilo.addClickHandler(event -> Window.alert("Utente loggato: " + utenteLoggato.getNome() + " " + utenteLoggato.getCognome()));

        // 7. Logout
        Button btnLogout = creaPannelloStileLink("↩️ Logout");
        btnLogout.getElement().setId("nav-btn-logout"); 
        btnLogout.addClickHandler(event -> new HomePageGUI().mostra());

        menuPanel.add(btnBacheca);
        menuPanel.add(btnProposte);
        menuPanel.add(btnMieiAnnunci);
        menuPanel.add(btnRichieste);
        menuPanel.add(btnChat);
        menuPanel.add(btnProfilo);
        menuPanel.add(btnLogout);

        navbar.add(menuPanel);
        navbar.setCellHorizontalAlignment(menuPanel, HasHorizontalAlignment.ALIGN_RIGHT);

        // Associa l'intera struttura al componente
        initWidget(navbar);
    }

    private Button creaPannelloStileLink(String testo) {
        Button btn = new Button(testo);
        btn.getElement().getStyle().setProperty("backgroundColor", "#f0f2f5");
        btn.getElement().getStyle().setProperty("color", "#1b263b");
        btn.getElement().getStyle().setProperty("border", "none");
        btn.getElement().getStyle().setProperty("padding", "6px 12px");
        btn.getElement().getStyle().setProperty("borderRadius", "4px");
        btn.getElement().getStyle().setProperty("fontSize", "13px");
        btn.getElement().getStyle().setProperty("fontWeight", "bold");
        btn.getElement().getStyle().setProperty("cursor", "pointer");
        return btn;
    }

    private void evidenziaBottone(Button btn) {
        btn.setEnabled(false);
        btn.getElement().getStyle().setProperty("backgroundColor", "#1b263b");
        btn.getElement().getStyle().setProperty("color", "#6c757d");
        btn.getElement().getStyle().setProperty("boxShadow", "inset 0 4px 6px rgba(0,0,0,0.4)");
        btn.getElement().getStyle().setProperty("cursor", "default");
    }
}
