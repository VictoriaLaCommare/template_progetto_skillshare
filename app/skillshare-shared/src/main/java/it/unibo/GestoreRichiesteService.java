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

    // Metodo per aggiornare lo stato di una richiesta (es. Accettata / Rifiutata)
    Boolean aggiornaStatoRichiesta(String idRichiesta, String nuovoStato) throws Exception;

    List<RichiestaScambio> ottieniTutteLeRichieste(String username) throws Exception;
}

