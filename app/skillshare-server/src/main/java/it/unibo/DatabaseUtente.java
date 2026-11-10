package it.unibo;

import java.util.concurrent.ConcurrentMap;
import org.mapdb.DB;
import org.mapdb.Serializer;

@SuppressWarnings("unchecked")
public class DatabaseUtente {
    private static DB dbCache;
    private static ConcurrentMap<String, Utente> utenti;
    private static ConcurrentMap<String, String> indiceUsername;
    
    private static synchronized ConcurrentMap<String, Utente> getUtenti() {
        DB attuale = DatabaseCore.getDB();
        if (utenti == null || dbCache != attuale || attuale.isClosed()) {
            dbCache = attuale;
            utenti = dbCache.hashMap(
                    "utenti",
                    Serializer.STRING,
                    Serializer.JAVA
            ).createOrOpen();
        }
        return utenti;
    }
    private static synchronized ConcurrentMap<String, String> getIndiceUsername() {
        DB attuale = DatabaseCore.getDB();
        if (indiceUsername == null || dbCache != attuale || attuale.isClosed()) {
            dbCache = attuale;
            indiceUsername = dbCache.hashMap(
                    "indice_username",
                    Serializer.STRING,
                    Serializer.STRING
            ).createOrOpen();
        }
        return indiceUsername;
    }
  
     //Forza il ricaricamento della mappa alla prossima chiamata a getUtenti().
    public static synchronized void resetPerTest() {
        dbCache = null;
        utenti = null;
        indiceUsername = null;
    }

    /*
    Registra un nuovo utente.
    Restituisce true se registrato, lancia ClassNotFoundException se l'username esiste già.
    */
    public static Boolean registraUtente(Utente utente) throws Exception {
        if (utente == null)
            throw new IllegalArgumentException("richiesta non valida");

        String mail = utente.getMail();
        String username = utente.getUsername();
        ConcurrentMap<String, Utente> mappa = getUtenti();
        ConcurrentMap<String, String> mappaIndice = getIndiceUsername();

        //username essendo la chiave deve essere univoco percui eseguo il controllo
        if (mappa.containsKey(mail))
            throw new Exception("utente già registrato, effettua login per autenticarti");

        if (username != null && mappaIndice.containsKey(username))
            throw new Exception("username già in uso da un altro utente");

        mappa.put(mail, utente);
        if (username != null) {
            mappaIndice.put(username, mail);
        }
        DatabaseCore.commit();
        return true;
    }

    /*
    Recupera il profilo di un utente dato il suo username.
    */
    public static Utente getUtente(String mail) {
        if (mail == null) {
            return null;
        }
        return getUtenti().get(mail);
    }

    //Recupera l'utente tramite lo username sfruttando l'indice secondario in O(1).
    public static Utente getUtenteByUsername(String username) {
        if (username == null) {
            return null;
        }
        String mail = getIndiceUsername().get(username);
        if (mail == null) {
            return null;
        }
        return getUtente(mail);
    }

    /*
    Verifica le credenziali per il Login.
    Restituisce l'oggetto Utente se corrette, altrimenti null.
    */
    public static Utente login(String mail, String password) {
        if (mail == null || password == null) {
            return null;
        }

        Utente u = getUtente(mail);
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }

        return null;
    }


    //Aggiorna i dati di un utente esistente
    public static Boolean aggiornaUtente(Utente utenteModificato) {
        if (utenteModificato == null || utenteModificato.getMail() == null) {
            return false;
        }

        String mail = utenteModificato.getMail();
        ConcurrentMap<String, Utente> mappa = getUtenti();

        if (!mappa.containsKey(mail)) {
            return false; // L'utente non esiste
        }

        mappa.put(mail, utenteModificato);
        DatabaseCore.commit();

        return true;
    }
}