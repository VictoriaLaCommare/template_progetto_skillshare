package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class GestoreRichiesteServiceImpl extends RemoteServiceServlet implements GestoreRichiesteService {

    @Override
    public Boolean inviaRichiesta(RichiestaScambio richiesta) throws Exception {
        // Blocco auto-scambio
        if (richiesta.getRichiedente().equals(richiesta.getRicevente())){
            return false; // Fallisce perché l'utente non può inviare una richiesta a se stesso
        }

        // Generare un ID univoco per la richiesta
        if (richiesta.getIdRichiesta() == null || richiesta.getIdRichiesta().isEmpty()) {
            richiesta.setIdRichiesta(UUID.randomUUID().toString());
        }
        
        DB db = DatabaseCore.getDB();
        try {
            // Apriamo (o creiamo) la mappa che contiene tutte le richieste
            HTreeMap<String, RichiestaScambio> mappaRichieste = db.hashMap("richieste")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
                
            // Salviamo la richiesta usando il suo ID univoco
            mappaRichieste.put(richiesta.getIdRichiesta(), richiesta);
            
            // Salvare fisicamente le modifiche sul file del database
            DatabaseCore.commit();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Fallisce in caso di errore
        }
    }

    @Override
    public List<RichiestaScambio> ottieniRichiesteRicevute(String username) throws Exception {
        DB db = DatabaseCore.getDB();
        List<RichiestaScambio> richiesteRicevute = new ArrayList<>();
        
        try {
            HTreeMap<String, RichiestaScambio> mappaRichieste = db.hashMap("richieste")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
                
            // Filtriamo solo le richieste in cui l'utente loggato è il "ricevente" e sono "In sospeso"
            for (RichiestaScambio req : mappaRichieste.values()) {
                if (req.getRicevente().equals(username) && "In sospeso".equals(req.getStato())) {
                    richiesteRicevute.add(req);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return richiesteRicevute;
    }

    @Override
    public Boolean aggiornaStatoRichiesta(String idRichiesta, String nuovoStato) throws Exception {
        DB db = DatabaseCore.getDB();
        try {
            HTreeMap<String, RichiestaScambio> mappaRichieste = db.hashMap("richieste")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
                
            // Se la richiesta non esiste, falliamo
            if (!mappaRichieste.containsKey(idRichiesta)) {
                return false;
            }
            
            // Recuperiamo, aggiorniamo e risalviamo
            RichiestaScambio richiesta = mappaRichieste.get(idRichiesta);
            richiesta.setStato(nuovoStato);
            
            mappaRichieste.put(idRichiesta, richiesta);
            DatabaseCore.commit();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}