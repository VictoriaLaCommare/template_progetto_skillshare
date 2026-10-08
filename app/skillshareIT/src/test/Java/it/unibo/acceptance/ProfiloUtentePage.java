package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ProfiloUtentePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "btn-mostra-storico")
    private WebElement btnMostraStorico;

    @FindBy(id = "btn-chiudi-profilo")
    private WebElement btnChiudi;

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
}