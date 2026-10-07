package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import org.mapdb.DB;
import org.mapdb.IndexTreeList;
import org.mapdb.Serializer;

import jakarta.servlet.ServletException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatServiceImpl extends RemoteServiceServlet implements ChatService {

    private DB db;
    private IndexTreeList<Messaggio> messaggiDB;

    public ChatServiceImpl() {
    }

    @Override
    public void init() throws ServletException {
        super.init();
        initDb();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initDb() {
        if (db == null || db.isClosed()) {
            db = DatabaseCore.getDB();
            messaggiDB = (IndexTreeList<Messaggio>) db.indexTreeList("chat_messaggi", (Serializer) Serializer.JAVA).createOrOpen();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public Messaggio inviaMessaggio(Messaggio messaggio) throws Exception {
        initDb(); 
        try {
            messaggiDB.add(messaggio);
            DatabaseCore.commit();        
            return messaggio;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Errore di connessione durante l'invio del messaggio", e);
        }
    }

    @Override
    public List<Messaggio> getStoricoMessaggi(String idRichiesta) throws Exception {
        initDb();
        List<Messaggio> storico = new ArrayList<>();
        
        for (Messaggio m : messaggiDB) {
            if (m.getIdRichiesta() != null && m.getIdRichiesta().equals(idRichiesta)) {
                storico.add(m);
            }
        }
        
        storico.sort(Comparator.comparing(Messaggio::getTimestamp));
        return storico;
    }
}