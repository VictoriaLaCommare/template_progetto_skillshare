package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.IndexTreeList;
import org.mapdb.Serializer;

import jakarta.servlet.ServletException;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatServiceImpl extends RemoteServiceServlet implements ChatService {

    private DB db;
    private IndexTreeList<Messaggio> messaggiDB;
    private static final String DB_FILE = "progetto_sweng.db";

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void init() throws ServletException {
        super.init();
        // Inizializza la connessione a MapDB quando parte il server
        db = DBMaker.fileDB(new File(DB_FILE))
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
        
        // Cast esplicito per risolvere l'errore di tipizzazione di MapDB
        messaggiDB = (IndexTreeList<Messaggio>) db.indexTreeList("chat_messaggi", (Serializer) Serializer.JAVA).createOrOpen();
    }

    @Override
    public void destroy() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        super.destroy();
    }

    @Override
    public Messaggio inviaMessaggio(Messaggio messaggio) throws Exception {
        try {
            messaggiDB.add(messaggio);
            db.commit(); // Salva permanentemente il messaggio
            return messaggio;
        } catch (Exception e) {
            db.rollback();
            throw new Exception("Errore di connessione durante l'invio del messaggio", e);
        }
    }

    @Override
    public List<Messaggio> getStoricoMessaggi(String idRichiesta) throws Exception {
        List<Messaggio> storico = new ArrayList<>();
        
        // Filtra i messaggi associati a questa specifica richiesta di scambio
        for (Messaggio m : messaggiDB) {
            if (m.getIdRichiesta() != null && m.getIdRichiesta().equals(idRichiesta)) {
                storico.add(m);
            }
        }
        
        // Ordina i messaggi dal più vecchio al più recente
        storico.sort(Comparator.comparing(Messaggio::getTimestamp));
        
        return storico;
    }
}