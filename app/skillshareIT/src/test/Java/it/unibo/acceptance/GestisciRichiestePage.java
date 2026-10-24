package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 Page helper per GestisciRichiesteGUI.
 Fornisce metodi di alto livello usati nei test di accettazione
 */
public class GestisciRichiestePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locator della sezione recensione.
    // Le stelle e la TextArea non hanno un id nella GUI: si individuano dal testo / placeholder.
    private static final By BTN_RECENSIONE = By.xpath("//button[starts-with(@id, 'btn-recensione-')]");
    private static final By BTN_INVIA_RECENSIONE = By.xpath("//button[starts-with(@id, 'btn-invia-recensione-')]");
    private static final By TXT_COMMENTO = By.xpath("//textarea[@placeholder='Scrivi un commento...']");
    private static final String STELLA = "//div[normalize-space(.)='☆' or normalize-space(.)='★']";
    private static final By STELLE_PIENE = By.xpath("//div[normalize-space(.)='★']");

    public GestisciRichiestePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    /** Clicca il primo pulsante "Accetta" presente nella pagina. */
    public GestisciRichiestePage accettaPrimaRichiesta() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[starts-with(@id, 'btn-accetta-')]"))).click();
        return this;
    }

    /** Clicca il primo pulsante "Rifiuta" o "Annulla" presente nella pagina. */
    public GestisciRichiestePage rifiutaPrimaRichiesta() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[starts-with(@id, 'btn-rifiuta-')]"))).click();
        return this;
    }

    /** Clicca il primo pulsante "Dichiara Terminazione" presente nella pagina. */
    public GestisciRichiestePage dichiaraPrimaTerminazione() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[starts-with(@id, 'btn-termina-')]"))).click();
        return this;
    }

    /**
     Attende un alert JavaScript, ne legge il testo e lo accetta.
     return Il testo dell'alert.
     */
    public String getTestoAlertEAccetta() {
        wait.until(ExpectedConditions.alertIsPresent());
        org.openqa.selenium.Alert alert = driver.switchTo().alert();
        String testo = alert.getText();
        alert.accept();
        return testo;
    }

    /**
     Controlla se almeno un pulsante "Dichiara Terminazione" è visibile.
     Restituisce false se il timeout scade senza trovarlo.
     */
    public boolean isBottoneTerminaVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[starts-with(@id, 'btn-termina-')]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     Controlla se il messaggio "In attesa della conferma dell'altro utente" è visibile.
     Viene mostrato all'utente che ha già dichiarato la propria terminazione.
     */
    public boolean isMessaggioAttesaVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'In attesa della conferma')]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //  Recensione
    /** Attende il bottone "Lascia Recensione" (compare solo a scambio Completato). */
    public boolean isBottoneRecensioneVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BTN_RECENSIONE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     Controllo immediato, senza attese: usarlo solo dopo aver atteso un elemento
     che garantisce che la lista richieste sia stata caricata (es. isBottoneTerminaVisibile).
     */
    public boolean isBottoneRecensioneAssente() {
        return driver.findElements(BTN_RECENSIONE).isEmpty();
    }

    /** Apre o chiude il form (il bottone fa da toggle). */
    public GestisciRichiestePage cliccaLasciaRecensione() {
        wait.until(ExpectedConditions.elementToBeClickable(BTN_RECENSIONE)).click();
        return this;
    }

    /** True se il form di recensione è aperto (attende il tasto "Invia Recensione"). */
    public boolean isFormRecensioneVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BTN_INVIA_RECENSIONE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** True se il form è nascosto o non esiste (veloce quando il form è chiuso). */
    public boolean isFormRecensioneChiuso() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(BTN_INVIA_RECENSIONE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Clicca l'n-esima stella (1-5). Presuppone una sola richiesta completata nella pagina. */
    public GestisciRichiestePage selezionaStelle(int n) {
        if (n < 1 || n > 5) {
            throw new IllegalArgumentException("Le stelle vanno da 1 a 5, ricevuto: " + n);
        }
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(" + STELLA + ")[" + n + "]"))).click();
        return this;
    }

    /** Numero di stelle piene (★) nel form. Funziona anche a form chiuso: legge il DOM. */
    public int getStelleSelezionate() {
        return driver.findElements(STELLE_PIENE).size();
    }

    public GestisciRichiestePage scriviCommento(String testo) {
        WebElement area = wait.until(ExpectedConditions.visibilityOfElementLocated(TXT_COMMENTO));
        area.clear();
        area.sendKeys(testo);
        return this;
    }

    /** Valore corrente della TextArea (anche a form chiuso). */
    public String getCommento() {
        String valore = driver.findElement(TXT_COMMENTO).getDomProperty("value");
        return valore == null ? "" : valore;
    }

    public GestisciRichiestePage inviaRecensione() {
        wait.until(ExpectedConditions.elementToBeClickable(BTN_INVIA_RECENSIONE)).click();
        return this;
    }
}