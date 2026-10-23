package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import jakarta.servlet.ServletException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MatchmakingServiceImpl extends RemoteServiceServlet implements MatchmakingService {

    private DB db;
    private HTreeMap<String, Utente> utentiDB;
    private HTreeMap<String, ArrayList<String>> preferenzeDB; 

    @Override
    public void init() throws ServletException {
        super.init();
        initDb();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initDb() {
        if (db == null || db.isClosed()) {
            db = DatabaseCore.getDB();
            utentiDB = (HTreeMap<String, Utente>) db.hashMap("utenti", Serializer.STRING, Serializer.JAVA).createOrOpen();
            preferenzeDB = (HTreeMap<String, ArrayList<String>>) db.hashMap("preferenze_match", Serializer.STRING, Serializer.JAVA).createOrOpen();
        }
    }

    @Override
    public List<Utente> trovaProfiliCompatibili(String mailUtente, String tagRicercato) throws Exception {
        initDb();
        List<Utente> profiliCompatibili = new ArrayList<>();

        //Controllo diretto dei parametri passati
        if (mailUtente == null || mailUtente.trim().isEmpty() || tagRicercato == null || tagRicercato.trim().isEmpty()) {
            return profiliCompatibili; 
        }

        // Il tag della wishlist è testo libero: può contenere più tag separati da virgola
        Set<String> tagCercati = normalizzaTag(tagRicercato);
        if (tagCercati.isEmpty()) {
            return profiliCompatibili;
        }

        try {

            for (String mail : utentiDB.keySet()) {

                //Salto l'utente stesso per evitare che si auto-selezioni
                if (mail.equals(mailUtente)) {
                    continue;
                }

                Utente potenzialeMatch;
                try {
                    potenzialeMatch = utentiDB.get(mail);
                } catch (Exception ex) {
                    //Utente salvato con una versione vecchia di Utente: non è leggibile, lo salto
                    System.err.println("Utente non leggibile, saltato: " + mail);
                    continue;
                }
                if (potenzialeMatch == null) {
                    continue;
                }

                //Controllo le competenze (confronto esatto sui singoli tag)
                if (haCompetenza(potenzialeMatch.getTagCompetenze(), tagCercati)) {
                    //Il client non deve mai ricevere la password
                    profiliCompatibili.add(senzaPassword(potenzialeMatch));
                }
            }

            return profiliCompatibili;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Errore durante la ricerca dei profili compatibili", e);
        }
    }

    @Override
    public List<String> getProfiliSalvati(String mailUtente) throws Exception {
        initDb();
        if (mailUtente == null || mailUtente.isEmpty()) {
            throw new Exception("Mail utente non valida");
        }
        ArrayList<String> salvati = preferenzeDB.get(mailUtente);
        // Restituisco sempre una lista, vuota se l'utente non ha salvato nessuno
        return salvati != null ? new ArrayList<>(salvati) : new ArrayList<>();
    }

    @Override
    public Boolean salvaProfilo(String mailUtente, String mailProfiloSalvato) throws Exception {
        initDb(); 
        try {
            if (mailUtente == null || mailUtente.isEmpty()) {
                throw new Exception("Mail utente non valida");
            }

            if (mailProfiloSalvato == null || mailProfiloSalvato.isEmpty()) {
                throw new Exception("Mail del profilo da salvare non valida");
            }
            if (mailUtente.equals(mailProfiloSalvato)) {
                throw new Exception("Non puoi salvare il tuo stesso profilo");
            }
            if (!utentiDB.containsKey(mailProfiloSalvato)) {
                throw new Exception("Profilo non trovato");
            }

            //Recupero la lista dei profili salvati. Se è nulla la creo.
            ArrayList<String> salvati = preferenzeDB.get(mailUtente);
            if (salvati == null) {
                salvati = new ArrayList<>();
            }

            //Aggiungo il profilo solo se non è già presente
            if (!salvati.contains(mailProfiloSalvato)) {
                salvati.add(mailProfiloSalvato);
                preferenzeDB.put(mailUtente, salvati);
                DatabaseCore.commit();
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Errore durante il salvataggio del profilo", e);
        }
    }

    private Set<String> normalizzaTag(String tag) {
        Set<String> risultato = new LinkedHashSet<>();
        if (tag == null) {
            return risultato;
        }
        for (String parte : tag.split(",")) {
            String pulito = parte.trim().toLowerCase();
            if (!pulito.isEmpty()) {
                risultato.add(pulito);
            }
        }
        return risultato;
    }

    /** True se almeno una competenza del profilo è uguale a uno dei tag cercati ("java" non corrisponde a "javascript"). */
    private boolean haCompetenza(String tagCompetenze, Set<String> tagCercati) {
        for (String competenza : normalizzaTag(tagCompetenze)) {
            if (tagCercati.contains(competenza)) {
                return true;
            }
        }
        return false;
    }

    /** Copia dell'utente senza la password, da restituire al client. */
    private Utente senzaPassword(Utente u) {
        Utente copia = new Utente();
        copia.setNome(u.getNome());
        copia.setCognome(u.getCognome());
        copia.setUsername(u.getUsername());
        copia.setMail(u.getMail());
        copia.setData(u.getData());
        copia.setBio(u.getBio());
        copia.setTagCompetenze(u.getTagCompetenze());
        copia.setImmagineProfilo(u.getImmagineProfilo());
        return copia;
    }
}