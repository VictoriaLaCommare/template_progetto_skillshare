package it.unibo;

import com.google.gwt.user.client.ui.*;

public class AppNavbar extends Composite {

    public AppNavbar(Utente utenteLoggato, String paginaAttiva) {
        HorizontalPanel navbar = new HorizontalPanel();
        navbar.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        navbar.addStyleName("navbar-container");

        // Logo
        Label lblLogo = new Label("SkillShare");
        lblLogo.addStyleName("navbar-logo");
        lblLogo.addClickHandler(event -> new PubblicaAnnunciGui().mostra(utenteLoggato));
        
        navbar.add(lblLogo);
        navbar.setCellWidth(lblLogo, "30%");

        // Pannello Menu
        HorizontalPanel menuPanel = new HorizontalPanel();
        menuPanel.addStyleName("navbar-menu");
        menuPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        menuPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        // 1. Bacheca
        Button btnBacheca = creaPannelloStileLink("📢 Bacheca");
        btnBacheca.getElement().setId("nav-btn-bacheca"); 
        if ("bacheca".equals(paginaAttiva)) evidenziaBottone(btnBacheca);
        btnBacheca.addClickHandler(event -> new PubblicaAnnunciGui().mostra(utenteLoggato));
        
        // 2. Cerca / Proposte
        Button btnProposte = creaPannelloStileLink("🔍 Cerca");
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

        // 4. Richieste 
        Button btnRichieste = creaPannelloStileLink("📥 Le Mie Richieste");
        btnRichieste.getElement().setId("nav-btn-richieste"); 
        if ("richieste".equals(paginaAttiva)) evidenziaBottone(btnRichieste);
        btnRichieste.addClickHandler(event -> new GestisciRichiesteGUI().mostra(utenteLoggato));

        // 5. Chat
        Button btnChat = creaPannelloStileLink("💬 Chat");
        btnChat.getElement().setId("nav-btn-chat"); 
        if ("chat".equals(paginaAttiva)) evidenziaBottone(btnChat);
        btnChat.addClickHandler(event -> new LeMieChatGUI().mostra(utenteLoggato));

        // 6.Wishlist
        Button btnWishlist = creaPannelloStileLink("❤️ Wishlist");
        btnWishlist.getElement().setId("nav-btn-wishlist"); 
        if ("wishlist".equals(paginaAttiva)) evidenziaBottone(btnWishlist);
        btnWishlist.addClickHandler(event -> new MiaWishlistGUI().mostra(utenteLoggato));

        // 7. Profilo
        Button btnProfilo = creaPannelloStileLink("👤 Profilo");
        btnProfilo.getElement().setId("nav-btn-profilo"); 
        if ("profilo".equals(paginaAttiva)) evidenziaBottone(btnProfilo);
        btnProfilo.addClickHandler(event -> new ProfiloUtenteGUI().mostra(utenteLoggato, utenteLoggato.getUsername()));

        // 8. Logout
        Button btnLogout = creaPannelloStileLink("↩️ Logout");
        btnLogout.getElement().setId("nav-btn-logout"); 
        btnLogout.addClickHandler(event -> new HomePageGUI().mostra());

        menuPanel.add(btnBacheca);
        menuPanel.add(btnProposte);
        menuPanel.add(btnMieiAnnunci);
        menuPanel.add(btnRichieste);
        menuPanel.add(btnChat);
        menuPanel.add(btnWishlist);
        menuPanel.add(btnProfilo);
        menuPanel.add(btnLogout);

        navbar.add(menuPanel);
        navbar.setCellHorizontalAlignment(menuPanel, HasHorizontalAlignment.ALIGN_RIGHT);

        // Associa l'intera struttura al componente
        initWidget(navbar);
    }

    private Button creaPannelloStileLink(String testo) {
        Button btn = new Button(testo);
        btn.addStyleName("nav-link");
        return btn;
    }

    private void evidenziaBottone(Button btn) {
        btn.setEnabled(false);
        btn.addStyleName("nav-link-active");
    }
}
