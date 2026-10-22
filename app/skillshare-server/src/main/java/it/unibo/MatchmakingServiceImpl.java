package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import jakarta.servlet.ServletException;

import java.util.ArrayList;
import java.util.List;

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

        try {
            //Ciclo su tutti gli utenti del database
            for (Utente potenzialeMatch : utentiDB.getValues()) {
                
                //Salto l'utente stesso per evitare che si auto-selezioni
                if (potenzialeMatch.getMail() != null && potenzialeMatch.getMail().equals(mailUtente)) {
                    continue; 
                }
                
                //Controllo le competenze
                String competenze = potenzialeMatch.getTagCompetenze();
                if (competenze != null && !competenze.trim().isEmpty()) {
                    // Se la competenza contiene il tag ricercato, c'è un match
                    if (competenze.toLowerCase().contains(tagRicercato.toLowerCase().trim())) {
                        profiliCompatibili.add(potenzialeMatch);
                    }
                }
            }
            return profiliCompatibili;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Errore durante la ricerca dei profili compatibili", e);
        }
    }

    @Override
    public Boolean salvaProfilo(String mailUtente, String mailProfiloSalvato) throws Exception {
        initDb(); 
        try {
            if (mailUtente == null || mailUtente.isEmpty()) {
                throw new Exception("Mail utente non valida");
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
}