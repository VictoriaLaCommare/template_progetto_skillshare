package it.unibo.acceptance;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


//Page object per la pagina "La mia Wishlist" e per il form "Aggiungi servizio".
public class WishlistPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WishlistPage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    //Apre la lista dalla voce "Wishlist" della navbar.
    public WishlistPage apriDallaNavbar() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-btn-wishlist"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("lista-wishlist")));
        return this;
    }

    //Dalla lista passa al form con il pulsante "Aggiungi servizio".
    public WishlistPage vaiAlFormAggiunta() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-vai-aggiungi-servizio"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txt-servizio")));
        return this;
    }

    //Form

    public WishlistPage compila(String servizio, String tag, String descrizione) {
        scrivi(By.id("txt-servizio"), servizio);
        scrivi(By.id("txt-tag"), tag);
        scrivi(By.id("txt-descrizione"), descrizione);
        return this;
    }

    public WishlistPage compilaSoloServizio(String servizio) {
        scrivi(By.id("txt-servizio"), servizio);
        return this;
    }

    public WishlistPage completaCampiMancanti(String tag, String descrizione) {
        scrivi(By.id("txt-tag"), tag);
        scrivi(By.id("txt-descrizione"), descrizione);
        return this;
    }

    public WishlistPage aggiungi() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-aggiungi-servizio"))).click();
        return this;
    }

    //Aspetta che compaia il messaggio di esito e ne restituisce il testo
    public String getMessaggio() {
        return wait.until(d -> {
            String testo = d.findElement(By.id("msg-wishlist")).getText();
            return testo.isEmpty() ? null : testo;
        });
    }

    public boolean messaggioContiene(String frammento) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("msg-wishlist"), frammento));
    }

    // Lista

    //True se la lista, finito il caricamento, mostra il messaggio "Wishlist vuota"
    public boolean isWishlistVuota() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msg-wishlist-vuota")));
        return true;
    }

    //Aspetta che compaia almeno una card e le restituisce
    public List<WebElement> getCards() {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("card-servizio")));
    }

    // Supporto

    private void scrivi(By locator, String testo) {
        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        campo.clear();
        campo.sendKeys(testo);
    }
}