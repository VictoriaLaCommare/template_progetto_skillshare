package it.unibo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

@RemoteServiceRelativePath("chat")
public interface ChatService extends RemoteService {
    Messaggio inviaMessaggio(Messaggio messaggio) throws Exception;
    List<Messaggio> getStoricoMessaggi(String idRichiesta) throws Exception;
}
