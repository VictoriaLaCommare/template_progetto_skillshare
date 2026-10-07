package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class BachecaPropostePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Mappiamo gli elementi della pagina usando gli ID definiti nella GUI
    @FindBy(id = "tabella-proposte")
    private WebElement tabellaProposte;

    @FindBy(id = "msg-nessuna-proposta")
    private WebElement messaggioVuoto;

    public BachecaPropostePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);
    }

    // Controlla se la tabella delle proposte è visibile
    public boolean isTabellaVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOf(tabellaProposte));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isMessaggioVuotoVisibile() {
        try {
            wait.until(ExpectedConditions.visibilityOf(messaggioVuoto));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public BachecaPropostePage cliccaRichiediScambio() {
        // Attesa che la tabella sia visibile prima di cercare i pulsanti
        wait.until(ExpectedConditions.visibilityOf(tabellaProposte));

        WebElement bottoneScambio = tabellaProposte.findElement(
            org.openqa.selenium.By.xpath(".//button[starts-with(@id, 'btn-richiedi-scambio-')]")
        );
        
        bottoneScambio.click();
        return this;
    }

    public String getTestoAlertEAccetta() {
        // Attesa che l'alert sia presente
        wait.until(ExpectedConditions.alertIsPresent());

        // Passaggio al contesto dell'alert
        org.openqa.selenium.Alert alert = driver.switchTo().alert();

        // Salvataggio del testo dell'alert
        String testoAlert = alert.getText();

        // Accettazione dell'alert
        alert.accept();

        return testoAlert;
    }

    // Clicca il bottone "Richiedi Scambio" associato a uno specifico titolo di annuncio
    public BachecaPropostePage cliccaRichiediScambioPerTitolo(String titoloAnnuncio) {
        wait.until(ExpectedConditions.visibilityOf(tabellaProposte));
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), '" + titoloAnnuncio + "')]")
        ));
        
        WebElement bottoneScambio = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), '" + titoloAnnuncio + "')]//following::button[starts-with(@id, 'btn-richiedi-scambio-')][1]")
        ));
        
        bottoneScambio.click();
        return this;
    }
}
