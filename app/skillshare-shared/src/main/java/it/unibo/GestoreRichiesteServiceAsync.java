package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface GestoreRichiesteServiceAsync {
    void inviaRichiesta(RichiestaScambio richiesta, AsyncCallback<Boolean> callback);
    
    void ottieniRichiesteRicevute(String username, AsyncCallback<List<RichiestaScambio>> callback);
    
    void aggiornaStatoRichiesta(String idRichiesta, String nuovoStato, AsyncCallback<Boolean> callback);

    void ottieniTutteLeRichieste(String username, AsyncCallback<List<RichiestaScambio>> callback);
}