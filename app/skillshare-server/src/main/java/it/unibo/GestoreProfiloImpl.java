package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import java.util.*;

public class GestoreProfiloImpl extends RemoteServiceServlet implements GestoreProfilo {

    private final GestoreProfilo proxy = new GestoreProfiloProxy();

    @Override
    public Utente aggiornaProfilo(Utente utenteAggiornato) throws Exception {
        return proxy.aggiornaProfilo(utenteAggiornato);
    }

    @Override
    public Utente ottieniProfiloUtente(String username) throws Exception{
        return proxy.ottieniProfiloUtente(username);
    }

    @Override
    public List<RichiestaScambio> ottieniStoricoScambi(String username) throws Exception {
        return proxy.ottieniStoricoScambi(username);
    }
}