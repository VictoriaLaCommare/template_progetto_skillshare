package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class GestoreRichiesteServiceImpl extends RemoteServiceServlet implements GestoreRichiesteService, SoggettoOsservabile, AnnuncioObserver {

    //Lista osservatori
    private final List<OsservatoreScambio> osservatori;

    private final HTreeMap<String, RichiestaScambio> getMappaRichieste() {
        DB db = DatabaseCore.getDB();
        return (HTreeMap<String, RichiestaScambio>) db.hashMap("richieste")
            .keySerializer(Serializer.STRING) //id richiesta
            .valueSerializer(Serializer.JAVA)//oggetto richiesta
            .createOrOpen();
    }

    private boolean ReqInDb(String idRichiesta) {
        return getMappaRichieste().containsKey(idRichiesta);
    }

    public GestoreRichiesteServiceImpl() {
        //Registriamo subito il gestore della gamification come osservatore
        osservatori=new ArrayList<>();
        aggiungiOsservatore(new GestoreLeveling(DatabaseCore.getDB()));

        // Registrazione come osservatore delle cancellazioni degli annunci
        GestoreAnnunciImpl.aggiungiOsservatore(this);
    }

    // Reazione automatica alla cancellazione di un annuncio
    @Override
    public void dopoAnnuncioEliminato(String idAnnuncio) {
        try {
            DB db = DatabaseCore.getDB();
            if (db == null || db.isClosed()) {
                return;
            }
            for (RichiestaScambio req : getMappaRichieste().values()) {
                if (idAnnuncio.equals(req.getIdAnnuncio())) {
                    req.setStato(new StatoRifiutata());
                    getMappaRichieste().put(req.getIdRichiesta(), req);
                }
            }
            DatabaseCore.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Override di SoggettoOsservabile
    @Override
    public void aggiungiOsservatore(OsservatoreScambio o) {
        osservatori.add(o);
    }

    @Override
    public void notificaCompletamento(RichiestaScambio richiesta) {
        for (OsservatoreScambio o : osservatori) {
            o.DopoScambioCompletato(richiesta);
        }
    }

    @Override
    public void notificaRecensione(String usernameRecensito, int valutazione) {
        for (OsservatoreScambio o : osservatori) {
            o.DopoRecensioneAggiunta(usernameRecensito, valutazione);
        }
    }


    // Metodi legati allo scambio di richieste
    @Override
    public Boolean inviaRichiesta(RichiestaScambio richiesta) throws Exception {
        if (richiesta.getRichiedente().equals(richiesta.getRicevente())) {
            return false;
        }
        if (richiesta.getIdRichiesta() == null || richiesta.getIdRichiesta().isEmpty()) {
            richiesta.setIdRichiesta(UUID.randomUUID().toString());
        }

        try {
            getMappaRichieste().put(richiesta.getIdRichiesta(), richiesta);
            DatabaseCore.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RichiestaScambio> ottieniRichiesteRicevute(String nomeutente) throws Exception {
        List<RichiestaScambio> richiesteRicevute = new ArrayList<>();
        try {
            for (RichiestaScambio req : getMappaRichieste().values()) {
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
            if (!ReqInDb(idRichiesta)) return false;

            RichiestaScambio richiesta = getMappaRichieste().get(idRichiesta);
            boolean esito = richiesta.dichiaraTerminazione(nomeutente);

            if (!esito) return false;

            getMappaRichieste().put(idRichiesta, richiesta);
            DatabaseCore.commit();

            // La recensione e la notifica si creano al completamento definitivo
            if ("Completato".equals(richiesta.getStato())) {
                notificaCompletamento(richiesta);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean accettaRichiesta(String idRichiesta, String nomeutente) throws Exception {
        try {
            if (!ReqInDb(idRichiesta)) return false;
            RichiestaScambio req = getMappaRichieste().get(idRichiesta);
            if (!req.getRicevente().equals(nomeutente)) return false;
            boolean esito = req.accetta(nomeutente);
            if (!esito) return false;
            getMappaRichieste().put(idRichiesta, req);
            DatabaseCore.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean rifiutaRichiesta(String idRichiesta, String nomeutente) throws Exception {
        try {
            if (!ReqInDb(idRichiesta)) return false;
            RichiestaScambio req = getMappaRichieste().get(idRichiesta);
            boolean esito = req.rifiuta(nomeutente);
            if (!esito) return false;
            getMappaRichieste().put(idRichiesta, req);
            DatabaseCore.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RichiestaScambio> ottieniTutteLeRichieste(String nomeutente) throws Exception {
        List<RichiestaScambio> tutteLeRichieste = new ArrayList<>();
        try {
            for (RichiestaScambio req : getMappaRichieste().values()) {
                if (req.getRicevente().equals(nomeutente) || req.getRichiedente().equals(nomeutente)) {
                    tutteLeRichieste.add(req);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tutteLeRichieste;
    }

    @Override
    public Boolean inviaRecensione(String idRichiesta, String usernameRecensito, Recensione recensione) throws Exception {
        try {
            if (!ReqInDb(idRichiesta)) return false;

            // Associa la recensione alla richiesta nel DB
            RichiestaScambio richiesta = getMappaRichieste().get(idRichiesta);
            if(richiesta.getStato().equalsIgnoreCase("completato")){
                // il recensito deve far parte dello scambio; il recensore è l'altra parte
                if (!usernameRecensito.equals(richiesta.getRichiedente()) && !usernameRecensito.equals(richiesta.getRicevente())) {
                    return false;
                }
                String recensore = usernameRecensito.equals(richiesta.getRicevente())
                        ? richiesta.getRichiedente() : richiesta.getRicevente();

                // una sola recensione per utente
                if (richiesta.isRecensioneInviata(recensore)) return false;

                richiesta.setRecensioneInviata(recensore);
                richiesta.aggiungiRecensione(recensione);
                getMappaRichieste().put(idRichiesta, richiesta);
                DatabaseCore.commit();

                // Notifica l'Observer: aggiorna punti e media recensioni dell'utente recensito
                notificaRecensione(usernameRecensito, recensione.getValutazione());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}