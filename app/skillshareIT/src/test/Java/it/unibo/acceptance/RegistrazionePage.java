package it.unibo.acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegistrazionePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ELEMENTI REGISTRAZIONE
    @FindBy(id = "reg-nome")           private WebElement campoNome;
    @FindBy(id = "reg-cognome")        private WebElement campoCognome;
    @FindBy(id = "reg-username")       private WebElement campoUsername;
    @FindBy(id = "reg-mail")           private WebElement campoMail;
    @FindBy(id = "reg-data")           private WebElement campoData;
    @FindBy(id = "reg-password")       private WebElement campoPassword;
    @FindBy(id = "reg-submit")         private WebElement bottoneRegistrati;
    @FindBy(id = "reg-message")        private WebElement messaggio;

    @FindBy(id = "reg-nome-error")     private WebElement erroreNome;
    @FindBy(id = "reg-cognome-error")  private WebElement erroreCognome;
    @FindBy(id = "reg-username-error") private WebElement erroreUsername;
    @FindBy(id = "reg-mail-error")     private WebElement erroreMail;
    @FindBy(id = "reg-data-error")     private WebElement erroreData;
    @FindBy(id = "reg-password-error") private WebElement errorePassword;

    // ELEMENTI LOGIN
    @FindBy(id = "login-mail")         private WebElement campoLoginMail;
    @FindBy(id = "login-password")     private WebElement campoLoginPassword;
    @FindBy(id = "login-submit")       private WebElement bottoneAccedi;
    @FindBy(id = "login-message")      private WebElement messaggioLogin;

    @FindBy(id = "login-mail-error")     private WebElement erroreLoginMail;
    @FindBy(id = "login-password-error") private WebElement erroreLoginPassword;

    @FindBy(id = "btn-vai-login")       private WebElement btnVaiLogin;
    @FindBy(id = "btn-vai-registra")    private WebElement btnVaiRegistra;


    public RegistrazionePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);
    }

    public RegistrazionePage apri(String baseUrl) {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.elementToBeClickable(btnVaiRegistra));
        return this;
    }

    public RegistrazionePage vaiAlFormRegistrazione() {
        btnVaiRegistra.click();
        wait.until(ExpectedConditions.elementToBeClickable(bottoneRegistrati));
        return this;
    }

    public RegistrazionePage vaiAlFormLogin() {
        btnVaiLogin.click();
        wait.until(ExpectedConditions.elementToBeClickable(bottoneAccedi));
        return this;
    }

    // AZIONI REGISTRAZIONE
    public RegistrazionePage compila(String nome, String cognome, String username,
                                     String mail, String dataNascitaGgMmAaaa, String password) {
        campoNome.clear();
        campoNome.sendKeys(nome);
        campoCognome.clear();
        campoCognome.sendKeys(cognome);
        campoUsername.clear();
        campoUsername.sendKeys(username);
        campoMail.clear();
        campoMail.sendKeys(mail);
        campoData.clear();
        campoData.sendKeys(dataNascitaGgMmAaaa);
        campoData.sendKeys(Keys.TAB);
        campoPassword.clear();
        campoPassword.sendKeys(password);
        return this;
    }

    public RegistrazionePage registrati() {
        bottoneRegistrati.click();
        wait.until(d -> !messaggio.getText().isEmpty());
        return this;
    }

    public String testoMessaggio() {
        return messaggio.getText();
    }

    // AZIONI LOGIN
    public RegistrazionePage compilaLogin(String mail, String password) {
        wait.until(ExpectedConditions.visibilityOf(campoLoginMail));
        campoLoginMail.clear();
        campoLoginMail.sendKeys(mail);
        campoLoginPassword.clear();
        campoLoginPassword.sendKeys(password);
        return this;
    }

    public RegistrazionePage accedi() {
        bottoneAccedi.click();
        //wait.until(d -> !messaggioLogin.getText().isEmpty());
        return this;
    }

    public String testoMessaggioLogin() {
        wait.until(d -> !messaggioLogin.getText().isEmpty());
        return messaggioLogin.getText();
    }

    //  ERRORE
    public boolean campoInErrore(WebElement campo) {
        String bordo = campo.getCssValue("border-top-color");
        return bordo.replace(" ", "").contains("229,57,53");
    }

    public boolean isFormLoginVisibile() {
        try {
            // Aspetta in automatico il timer di 1.5 secondi
            wait.until(ExpectedConditions.visibilityOf(campoLoginMail));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // GETTER REGISTRAZIONE
    public WebElement campoNome() { return campoNome; }
    public WebElement campoCognome() { return campoCognome; }
    public WebElement campoUsername() { return campoUsername; }
    public WebElement campoMail() { return campoMail; }
    public WebElement campoData() { return campoData; }
    public WebElement campoPassword() { return campoPassword; }

    // GETTER LOGIN
    public WebElement campoLoginMail() { return campoLoginMail; }
    public WebElement campoLoginPassword() { return campoLoginPassword; }
}