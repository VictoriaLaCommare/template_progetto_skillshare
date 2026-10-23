package it.unibo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

@RemoteServiceRelativePath("matchmaking")
public interface MatchmakingService extends RemoteService {
    
    //Cerca i profili che offrono la competenza richiesta dal tag
    List<Utente> trovaProfiliCompatibili(String mailUtente, String tagRicercato) throws Exception;
    
    //Salva il profilo tra i preferiti dell'utente
    Boolean salvaProfilo(String mailUtente, String mailProfiloSalvato) throws Exception;

    //Restituisce le mail dei profili che l'utente ha già salvato   
    List<String> getProfiliSalvati(String mailUtente) throws Exception;  
}