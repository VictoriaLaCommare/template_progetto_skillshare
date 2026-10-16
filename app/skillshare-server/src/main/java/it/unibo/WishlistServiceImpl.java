package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import jakarta.servlet.ServletException;

public class WishlistServiceImpl extends RemoteServiceServlet implements WishlistService {

    private DB db;
    // Usiamo HTreeMap perché Utente.java sfrutta un hashmap che ha come chiave la mail
    private HTreeMap<String, Utente> utentiDB; 

    @Override
    public void init() throws ServletException {
        super.init();
        initDb();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initDb() {
        if (db == null || db.isClosed()) {
            db = DatabaseCore.getDB();
            
            utentiDB = (HTreeMap<String, Utente>) db.hashMap("utenti", Serializer.STRING, Serializer.JAVA).createOrOpen();
        }
    }

    @Override
    public Boolean salvaWishlist(String mailUtente, Wishlist nuovaWishlist) throws Exception {
        initDb(); 
        try {
            //Recupera l'utente usando la mail
            Utente utente = utentiDB.get(mailUtente);
            
            if (utente == null) {
                throw new Exception("Utente non trovato nel database");
            }

            //Aggiungi la nuova wishlist alla sua lista
            utente.getMieWishlist().add(nuovaWishlist);
            
            //Aggiorna l'utente nel MapDB e fai il commit
            utentiDB.put(mailUtente, utente);
            DatabaseCore.commit();        
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Errore durante il salvataggio della wishlist", e);
        }
    }
}