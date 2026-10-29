package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface GestoreAnnunciAsync {
    
    void pubblicaAnnuncio(Annuncio annuncio, AsyncCallback<Boolean> callback);
    
    void ottieniTuttiGliAnnunci(AsyncCallback<List<Annuncio>> callback);

    void ottieniAnnunciUtente(String username, AsyncCallback<List<Annuncio>> callback);
    
    void modificaAnnuncio(Annuncio annuncio, AsyncCallback<Boolean> callback);
    
    void eliminaAnnuncio(String id, String username, AsyncCallback<Boolean> callback);

    void cercaAnnunci(String competenzaOfferta, String competenzaRichiesta, String keyword, String ordinamento, AsyncCallback<List<Annuncio>> callback);

    void ottieniAnnunciPreferiti(String username, AsyncCallback<List<Annuncio>> callback);
}