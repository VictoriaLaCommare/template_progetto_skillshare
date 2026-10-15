package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Indirizzo relativo per il servizio remoto
@RemoteServiceRelativePath("richieste")
public interface GestoreRichiesteService extends RemoteService {

    // Metodo per inviare una richiesta di scambio
    Boolean inviaRichiesta(RichiestaScambio richiesta) throws Exception;

    // Metodo per recuperare le richieste ricevute da un utente
    List<RichiestaScambio> ottieniRichiesteRicevute(String username) throws Exception;

    //Metodo per segnalare al sistema il completamento dello scambio (deve essere effettuato da
    //entrambi gli utenti coinvolti)
    Boolean dichiaraTerminazione(String idRichiesta, String username) throws Exception;

    // Metodo per accettare una richiesta ricevuta (eseguita solo dal Ricevente)
    Boolean accettaRichiesta(String idRichiesta, String username) throws Exception;

    // Metodo per rifiutare una richiesta ricevuta (Può essere fatta da entrambe prima dell'accettazione, se fatta dal richiedente ha la funzione di annulla)
    Boolean rifiutaRichiesta(String idRichiesta, String username) throws Exception;

    List<RichiestaScambio> ottieniTutteLeRichieste(String username) throws Exception;
}

