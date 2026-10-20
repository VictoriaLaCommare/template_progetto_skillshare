package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.*;

public interface GestoreProfiloAsync{
    void aggiornaProfilo(Utente utenteAggiornato, AsyncCallback<Utente> callback);

    void ottieniProfiloUtente(String utente, AsyncCallback<Utente> callback);

    void ottieniStoricoScambi(String utente, AsyncCallback<List<RichiestaScambio>> callback);

    void ottieniStatistiche(String nomeutente, AsyncCallback<StatisticheUtente> callback);
}
