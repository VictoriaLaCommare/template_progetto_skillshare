package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GestoreAnnunciPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Mappatura degli elementi basata sugli ID che hai inserito nella GUI
    @FindBy(id = "annuncio-titolo")    private WebElement campoTitolo;
    @FindBy(id = "annuncio-desc")      private WebElement campoDescrizione;
    @FindBy(id = "annuncio-offerta")   private WebElement campoOfferta;
    @FindBy(id = "annuncio-richiesta") private WebElement campoRichiesta;
    @FindBy(id = "annuncio-submit")    private WebElement bottonePubblica;
    @FindBy(id = "annuncio-message")   private WebElement messaggio;

    public GestoreAnnunciPage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);
    }

    public GestoreAnnunciPage apri(String baseUrl) {
        driver.get(baseUrl);
        // Attende che il bottone di pubblicazione sia cliccabile
        wait.until(ExpectedConditions.elementToBeClickable(By.id("annuncio-submit")));
        return this;
    }

    // Metodo per compilare tutto il form in un colpo solo
    public GestoreAnnunciPage compila(String titolo, String descrizione, String offerta, String richiesta) {
        campoTitolo.clear();
        campoTitolo.sendKeys(titolo);
        
        campoDescrizione.clear();
        campoDescrizione.sendKeys(descrizione);
        
        campoOfferta.clear();
        campoOfferta.sendKeys(offerta);
        
        campoRichiesta.clear();
        campoRichiesta.sendKeys(richiesta);
        
        return this;
    }

    public GestoreAnnunciPage pubblica() {
        bottonePubblica.click();
        // Attende che il messaggio a schermo non sia vuoto
        wait.until(d -> !messaggio.getText().isEmpty());
        return this;
    }

    // Metodo di navigazione per spostarsi nella vista "I Miei Annunci"
    public GestoreAnnunciPage apriMieiAnnunci() {
        WebElement btnMieiAnnunci = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@id='btn-miei-annunci' or normalize-space()='I Miei Annunci']")));
        if (btnMieiAnnunci.isEnabled()) {
            wait.until(ExpectedConditions.elementToBeClickable(btnMieiAnnunci)).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'I Miei Annunci')]")));
        }
        return this;
    }

    public boolean isAnnuncioPresente(String titolo) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), '" + titolo + "')]")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public GestoreAnnunciPage eliminaAnnuncio(String titolo) {
        apriMieiAnnunci();
        // 1. Individua direttamente la card tramite il testo contenuto ovunque al suo interno
        WebElement cardAnnuncio = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(@class, 'annuncio-card') and contains(., '" + titolo + "')]")
        ));

        // 2. Trova e clicca il bottone elimina associato
        WebElement bottoneElimina = cardAnnuncio.findElement(By.xpath(".//button[starts-with(@id, 'btn-elimina-')]"));
        bottoneElimina.click();

        // 3. Clicca sul tasto "Sì" del popup di conferma GWT
        WebElement btnConfermaSi = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-conferma-elimina-si")));
        btnConfermaSi.click();

        // 4. Gestione dell'alert nativo di successo
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}

        // 5. Attendiamo finché la card non scompare dal DOM
        wait.until(ExpectedConditions.invisibilityOf(cardAnnuncio));

        return this;
    }

    public GestoreAnnunciPage modificaAnnuncio(String titoloVecchio, String nuovoTitolo, String nuovaDescrizione) {
        apriMieiAnnunci();
        // 1. Individua la card tramite il testo contenuto ovunque al suo interno
        WebElement cardAnnuncio = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(@class, 'annuncio-card') and contains(., '" + titoloVecchio + "')]")
        ));

        // 2. Trova e clicca il bottone modifica associato
        WebElement bottoneModifica = cardAnnuncio.findElement(By.xpath(".//button[starts-with(@id, 'btn-modifica-')]"));
        bottoneModifica.click();

        // 3. Compila la DialogBox di modifica usando gli ID dedicati
        WebElement campoModificaTitolo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modifica-titolo")));
        campoModificaTitolo.clear();
        campoModificaTitolo.sendKeys(nuovoTitolo);

        WebElement campoModificaDesc = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modifica-desc")));
        campoModificaDesc.clear();
        campoModificaDesc.sendKeys(nuovaDescrizione);

        WebElement campoModificaOfferta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modifica-offerta")));
        campoModificaOfferta.clear();
        campoModificaOfferta.sendKeys("GWT");

        WebElement campoModificaRichiesta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modifica-richiesta")));
        campoModificaRichiesta.clear();
        campoModificaRichiesta.sendKeys("Spring Boot");

        // 4. Clicca sul tasto Salva della modale
        WebElement btnSalva = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modifica-submit")));
        btnSalva.click();

        // 5. Gestione dell'alert nativo "Annuncio modificato con successo!"
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}

        // 6. Attendiamo che la vecchia card sparisca dal DOM
        wait.until(ExpectedConditions.invisibilityOf(cardAnnuncio));

        return this;
    }
    
    public void apriFiltriAvanzati() {
        driver.findElement(By.id("btn-filtri")).click();
    }

    public void compilaFiltri(String competenzaOfferta, String competenzaRichiesta, String parolaChiave) {
        WebElement txtOfferta = driver.findElement(By.id("tB-Offerta"));
        WebElement txtRichiesta = driver.findElement(By.id("tB-Richiesta"));
        WebElement txtParola = driver.findElement(By.id("tB-Keyword"));

        txtOfferta.clear();
        txtOfferta.sendKeys(competenzaOfferta);
        
        txtRichiesta.clear();
        txtRichiesta.sendKeys(competenzaRichiesta);
        
        txtParola.clear();
        txtParola.sendKeys(parolaChiave);
    }

    public void cliccaCercaAvanzata() {
        WebElement btnCerca = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-esegui-ricerca")));
        btnCerca.click();    
    }

    public String testoMessaggio() {
        return messaggio.getText();
    }

    // Verifica la presenza del bordo rosso (#e53935)
    public boolean campoInErrore(WebElement campo) {
        String bordo = campo.getCssValue("border-top-color");
        return bordo.replace(" ", "").contains("229,57,53");
    }

    // Getter
    public WebElement campoTitolo() { return campoTitolo; }
    public WebElement campoDescrizione() { return campoDescrizione; }
    public WebElement campoOfferta() { return campoOfferta; }
    public WebElement campoRichiesta() { return campoRichiesta; }
}