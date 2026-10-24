package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfiloUtentePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "btn-mostra-storico")
    private WebElement btnMostraStorico;

    @FindBy(id = "btn-chiudi-profilo")
    private WebElement btnChiudi;

    // Sezione statistiche (livello, scambi, media, tag): nella GUI non hanno id,
    // si individuano dal testo. Il livello è il <p> che precede "Scambi completati".
    private static final By TESTO_SCAMBI =By.xpath("//p[contains(text(), 'Scambi completati')]");
    private static final By TESTO_MEDIA =By.xpath("//p[contains(text(), 'Media recensioni')]");
    private static final By TESTO_LIVELLO =By.xpath("//p[contains(text(), 'Scambi completati')]/preceding-sibling::p[1]");

    // Costruttore
    public ProfiloUtentePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);

        // Aspettiamo che il popup sia effettivamente a schermo prima di usarlo
        wait.until(ExpectedConditions.visibilityOf(btnChiudi));
    }

    // Metodo per cliccare sul pulsante "Mostra Storico"
    public ProfiloUtentePage cliccaMostraStorico() {
        wait.until(ExpectedConditions.elementToBeClickable(btnMostraStorico));
        btnMostraStorico.click();
        return this;
    }

    // Metodo per verificare se il messaggio di storico vuoto è visibile
    public boolean isMessaggioVuotoVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("msg-storico-vuoto")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Metodo per verificare se la tabella dello storico è visibile
    public boolean isTabellaStoricoVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabella-storico")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Metodo per chiudere il popup del profilo utente
    public void chiudiProfilo() {
        wait.until(ExpectedConditions.elementToBeClickable(btnChiudi));
        btnChiudi.click();
        // Aspettiamo che il popup sparisca
        wait.until(ExpectedConditions.invisibilityOf(btnChiudi));
    }

    // Statistiche e gamification

    /**
     Le statistiche arrivano con una chiamata RPC separata dal profilo:
     va chiamato prima di leggere livello, scambi, media e tag.
     Se il server non risponde, la GUI mostra "Statistiche non disponibili." e qui scatta il timeout.
     */
    public ProfiloUtentePage attendiStatistiche() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(TESTO_SCAMBI));
        return this;
    }

    public int getScambiCompletati() {
        return (int) primoNumeroDi(TESTO_SCAMBI);
    }

    /**
     La media viene letta con una regex: GWT compilato stampa un double intero senza decimali
     ("4" e non "4.0"), quindi non si può confrontare una stringa fissa.
     */
    public double getMediaRecensioni() {
        return primoNumeroDi(TESTO_MEDIA);
    }

    public String getLivello() {
        return driver.findElement(TESTO_LIVELLO).getText().trim();
    }

    /** True se esiste un tag (badge) il cui testo contiene il frammento, es. "Eccellente". */
    public boolean haTag(String frammento) {
        return !driver.findElements(
                By.xpath("//span[contains(text(), '" + frammento + "')]")).isEmpty();
    }

    private double primoNumeroDi(By locator) {
        String testo = driver.findElement(locator).getText();
        Matcher m = Pattern.compile("[0-9]+(?:\\.[0-9]+)?").matcher(testo);
        if (!m.find()) {
            throw new AssertionError("Nessun numero trovato in: '" + testo + "'");
        }
        return Double.parseDouble(m.group());
    }
}