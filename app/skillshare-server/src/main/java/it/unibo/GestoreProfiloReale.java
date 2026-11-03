package it.unibo;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import java.util.*;

@SuppressWarnings("unchecked")
//Soggetto reale che si occupa di salvare effettivamente il profilo sul DB
public class GestoreProfiloReale implements GestoreProfilo {

    @Override
    public Utente aggiornaProfilo(Utente utenteAggiornato) throws Exception {
        DB db = DatabaseCore.getDB();

        HTreeMap<String, Utente> mappaUtenti = db.hashMap("utenti")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

        // Salvataggio basato sulla mail come chiave
        mappaUtenti.put(utenteAggiornato.getMail(), utenteAggiornato);
        //Salvataggio dell'indice secondario
        if (utenteAggiornato.getUsername() != null) {
            HTreeMap<String, String> indice = db.hashMap("indice_username", Serializer.STRING, Serializer.STRING).createOrOpen();
            indice.put(utenteAggiornato.getUsername(), utenteAggiornato.getMail());
        }
        DatabaseCore.commit();

        return utenteAggiornato;
    }

    @Override
    public Utente ottieniProfiloUtente(String username) throws Exception {
        // Recupero istantaneo tramite Indice Secondario
        Utente u = DatabaseUtente.getUtenteByUsername(username);

        if (u != null) {
            // Creazione dell'utente pubblico con solo i dati non sensibili
            Utente utentePubblico = new Utente(u.getNome(), u.getCognome());
            utentePubblico.setUsername(u.getUsername());
            utentePubblico.setMail(u.getMail());
            utentePubblico.setBio(u.getBio());
            utentePubblico.setTagCompetenze(u.getTagCompetenze());
            utentePubblico.setImmagineProfilo(u.getImmagineProfilo());

            return utentePubblico;
        }

        return null;
    }

    @Override
    public List<RichiestaScambio> ottieniStoricoScambi(String username) throws Exception {
        DB db = DatabaseCore.getDB();
        List<RichiestaScambio> storico = new ArrayList<>();

        HTreeMap<String, RichiestaScambio> mappaRichieste = db.hashMap("richieste")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

        for (RichiestaScambio ric : mappaRichieste.values()) {
            // Controllo se l'utente cercato è colui che ha fatto la richiesta oppure colui che la riceve
            if (ric.getRichiedente().equals(username) || ric.getRicevente().equals(username)) {
                storico.add(ric); // Se si l'aggiunge alla lista
            }
        }

        return storico;
    }

    @Override
    public StatisticheUtente ottieniStatistiche(String username) throws Exception {
        return null;
    }
}