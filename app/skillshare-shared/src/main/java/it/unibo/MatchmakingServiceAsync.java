package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface MatchmakingServiceAsync {
    
    void trovaProfiliCompatibili(String mailUtente, String tagRicercato, AsyncCallback<List<Utente>> callback);
    
    void salvaProfilo(String mailUtente, String mailProfiloSalvato, AsyncCallback<Boolean> callback);

    void rimuoviProfilo(String mailUtente, String mailProfiloRimosso, AsyncCallback<Boolean> callback);

    void getProfiliSalvati(String mailUtente, AsyncCallback<List<String>> callback);
}