package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class HomePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public HomePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    public HomePage apri(String baseUrl) {
        driver.get(baseUrl);
        // Attende che la sezione degli annunci pubblici sia caricata
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Ultimi Annunci Pubblici')]")));
        return this;
    }

    // Verifica se è presente almeno un pulsante profilo autore nelle card degli annunci
    public boolean isPulsanteAutorePresente() {
        try {
            WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[starts-with(@id, 'link-profilo-')]")
            ));
            return btn.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Clicca sul primo bottone dell'autore disponibile in home page
    public HomePage cliccaPrimoProfiloAutore() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[starts-with(@id, 'link-profilo-')]")
        ));
        btn.click();
        return this;
    }
}
