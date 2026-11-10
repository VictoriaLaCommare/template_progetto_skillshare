package it.unibo;
public class PasswordManager implements GestorePassword {

    public PasswordManager() {}

    @Override
    public Utente effettuaLogin(String email, String passwordInserita) {
        Utente utenteinMemoria = DatabaseUtente.getUtente(email);

        //Controllo di sicurezza
        if (utenteinMemoria != null && utenteinMemoria.getPassword().equals(passwordInserita)) {
            return utenteinMemoria; // Password corretta -> accesso consentito all'account
        }

        return null; // Utente non trovato o password errata
    }

}
