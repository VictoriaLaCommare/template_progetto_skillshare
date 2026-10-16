package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
}