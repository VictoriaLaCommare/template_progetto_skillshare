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

    private final DB db=DatabaseCore.getDB();
    // Apriamo (o creiamo) la mappa che contiene tutte le richieste in modo da non doverlo ripetere
    private final HTreeMap<String, RichiestaScambio> mappaRichieste = db.hashMap("richieste")
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.JAVA)
            .createOrOpen();
    private boolean ReqInDb(String idRichiesta){
        if(mappaRichieste.containsKey(idRichiesta))
            return true;
        return false;
    }


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


        try {

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
    public List<RichiestaScambio> ottieniRichiesteRicevute(String nomeutente) throws Exception {
        List<RichiestaScambio> richiesteRicevute = new ArrayList<>();

        try {
            for (RichiestaScambio req : mappaRichieste.values()) {
                if (req.getRicevente().equals(nomeutente)) {
                    String stato = req.getStato();
                    if ("In sospeso".equals(stato) || "Accettata".equals(stato)) {
                        richiesteRicevute.add(req);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return richiesteRicevute;
    }

    @Override
    public Boolean dichiaraTerminazione(String idRichiesta, String nomeutente) throws Exception {
        try {
            // Se la richiesta non esiste, falliamo
            if (!ReqInDb(idRichiesta)) {
                return false;
            }

            // Recuperiamo la richiesta e deleghiamo l'azione di terminazione
            RichiestaScambio richiesta = mappaRichieste.get(idRichiesta);
            boolean esito = richiesta.dichiaraTerminazione(nomeutente);

            if (!esito) {
                return false;
            }
            else {
                mappaRichieste.put(idRichiesta, richiesta);
                DatabaseCore.commit();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean accettaRichiesta(String idRichiesta, String nomeutente) throws Exception {
        try{
            if (!ReqInDb(idRichiesta)) {
            return false;
        }
            RichiestaScambio req=mappaRichieste.get(idRichiesta);
            if(req.getRicevente().equals(nomeutente)) {//solo il ricevente può accettare
                boolean esito = req.accetta(nomeutente);
                if (!esito) {
                    return false;
                } else {
                    mappaRichieste.put(idRichiesta, req);
                    DatabaseCore.commit();
                }

                return true;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean rifiutaRichiesta(String idRichiesta, String nomeutente) throws Exception {
        try{
            if (!ReqInDb(idRichiesta)) {
                return false;
            }

            RichiestaScambio req=mappaRichieste.get(idRichiesta);
            assert req != null;


            boolean esito = req.rifiuta(nomeutente);
            if (!esito) {
                return false;
            } else {
                mappaRichieste.put(idRichiesta, req);
                DatabaseCore.commit();
            }

            return true;

        }catch (Exception e) {
            e.printStackTrace();
        }



        return false;
    }

    @Override
    public List<RichiestaScambio> ottieniTutteLeRichieste(String nomeutente) throws Exception {
        List<RichiestaScambio> tutteLeRichieste = new ArrayList<>();

        try {
            for (RichiestaScambio req : mappaRichieste.values()) {
                // Se l'utente è coinvolto nella richiesta (sia che l'abbia inviata, sia che l'abbia ricevuta)
                if (req.getRicevente().equals(nomeutente) || req.getRichiedente().equals(nomeutente)) {
                    tutteLeRichieste.add(req);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tutteLeRichieste;
    }
}
