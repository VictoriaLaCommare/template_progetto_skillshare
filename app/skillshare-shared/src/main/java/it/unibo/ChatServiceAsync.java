package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface ChatServiceAsync {
    void inviaMessaggio(Messaggio messaggio, AsyncCallback<Messaggio> callback);
    void getStoricoMessaggi(String idRichiesta, AsyncCallback<List<Messaggio>> callback);
}