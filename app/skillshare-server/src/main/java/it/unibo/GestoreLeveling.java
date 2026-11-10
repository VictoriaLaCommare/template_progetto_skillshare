package it.unibo;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

/*
L'oggetto Observer concreto: reagisce agli eventi di completamento e recensione
aggiornando le statistiche nel DB e ricostruendo la catena di Decorator.

Punti assegnati: Per ogni recensione ricevuta: +valutazione pt (1-5) all'utente recensito
*/

@SuppressWarnings("unchecked")
public class GestoreLeveling implements OsservatoreScambio {

    public GestoreLeveling(DB db) {
    }
    
    private HTreeMap<String, StatisticheUtente> getMappaStatistiche() {
        return DatabaseCore.getDB().hashMap("statistiche")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    private void aggiornaDopoScambio(String username) {
        StatisticheUtente s = getOrCreate(username);
        s.incrementaScambi();
        getMappaStatistiche().put(username, s);
    }

    private StatisticheUtente getOrCreate(String username) {
        return (StatisticheUtente) getMappaStatistiche().getOrDefault(username, new StatisticheUtente(username));
    }

    @Override
    public void DopoScambioCompletato(RichiestaScambio richiesta) {
        aggiornaDopoScambio(richiesta.getRichiedente());
        aggiornaDopoScambio(richiesta.getRicevente());
        DatabaseCore.commit();
    }

    @Override
    public void DopoRecensioneAggiunta(String usernameRecensito, int valutazione) {
        StatisticheUtente s = getOrCreate(usernameRecensito);
        s.aggiungiValutazione(valutazione);
        getMappaStatistiche().put(usernameRecensito, s);
        DatabaseCore.commit();
    }

    // Getter dell'intera catena di decorazioni
    /*
    Restituisce le statistiche arricchite di un utente applicando
    la catena di Decorator completa.
    Solo uno tra Novizio/Esperto/Maestro attiverà il proprio ramo;
    DecoratoreRecensioni può combinarsi con ognuno essendo legato alle recensioni.
    */
    public StatisticheUtente getStatistiche(String username) {
        StatisticheUtente statbase = getOrCreate(username);
        DescrizioneUtente catena =
            new DecoratoreRecensioni(
                new MaestroDecorator(
                    new EspertoDecorator(
                        new NovizioDecorator(
                            new DescrizioneUtenteBase(statbase)
                        )
                    )
                )
            );
        return catena.costruisci();
    }


}
