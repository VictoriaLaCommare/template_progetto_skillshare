package it.unibo.acceptance;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MatchmakingPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public MatchmakingPage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    public MatchmakingPage attendiSuggerimentiVisibili() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Profili suggeriti per questa competenza')]")
        ));
        return this;
    }

    public boolean isMessaggioNessunProfiloVisibile() {
        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Nessun profilo trovato al momento')]")
            ));
            return msg != null && msg.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Trova tutti i bottoni di matchmaking indipendentemente dal loro stato testuale
    public List<WebElement> getBottoniProfilo() {
        return driver.findElements(By.xpath("//button[contains(., 'Salva Profilo') or contains(., 'Salvato')]"));
    }

    // Clicca il primo bottone disponibile (esegue il toggle)
    public MatchmakingPage cliccaPrimoBottoneProfilo() {
        List<WebElement> bottoni = getBottoniProfilo();
        if (!bottoni.isEmpty()) {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(bottoni.get(0)));
            btn.click();
        }
        return this;
    }

    // Verifica se il profilo è nello stato "Salvato"
    public boolean isPrimoProfiloSalvato() {
        try {
            WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(., 'Salvato')]")
            ));
            return btn != null;
        } catch (Exception e) {
            return false;
        }
    }

    // Verifica se il profilo è tornato nello stato iniziale "Salva Profilo" (dopo la deselezione)
    public boolean isPrimoProfiloDaSalvare() {
        try {
            WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(., 'Salva Profilo')]")
            ));
            return btn != null;
        } catch (Exception e) {
            return false;
        }
    }
}