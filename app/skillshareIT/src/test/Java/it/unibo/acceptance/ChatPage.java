package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class ChatPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ChatPage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);
    }

    public ChatPage scriviMessaggio(String testo) {
        WebElement campoMessaggio = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@type='text']")
        ));
        campoMessaggio.clear();
        campoMessaggio.sendKeys(testo);
        return this;
    }

    public ChatPage invia() {
        WebElement bottoneInvia = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Invia')]")
        ));
        bottoneInvia.click();
        return this;
    }

    public boolean isMessaggioPresente(String testo) {
        try {
            // Il punto (.) unisce tutto il testo e ignora i tag HTML interni come <b>
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(., '" + testo + "')]")
            ));
            return msg != null && msg.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public ChatPage chiudiChat() {
        WebElement btnChiudi = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'X')]")
        ));
        btnChiudi.click();
        return this;
    }
}