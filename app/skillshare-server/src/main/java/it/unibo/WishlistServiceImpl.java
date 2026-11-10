package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import java.util.ArrayList;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import jakarta.servlet.ServletException;

public class WishlistServiceImpl extends RemoteServiceServlet implements WishlistService {

    private DB db;
    //Chiave=mail utente, valore= le sue wishlist
    private HTreeMap<String, ArrayList<ServizioRichiesto>> wishlistDB; 

    @Override
    public void init() throws ServletException {
        super.init();
        initDb();
    }

    @SuppressWarnings("unchecked")
    private void initDb() {
        if (db == null || db.isClosed()) {
            db = DatabaseCore.getDB();
            
            wishlistDB = (HTreeMap<String, ArrayList<ServizioRichiesto>>) db.hashMap("wishlist_servizi", Serializer.STRING, Serializer.JAVA).createOrOpen();
        }
    }

    @Override
    public Boolean aggiungiServizio(String mailUtente, ServizioRichiesto nuovaWishlist) throws Exception {
        initDb(); 
        try {
            if(mailUtente == null || mailUtente.isEmpty()){
                throw new Exception("mail utente non valida");
            }

            ArrayList<ServizioRichiesto> lista = wishlistDB.get(mailUtente);
            if (lista == null){
                lista = new ArrayList<>();
            }

            lista.add(nuovaWishlist);

            wishlistDB.put(mailUtente, lista);
            DatabaseCore.commit();        
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("errore durante il salvataggio della wishlist: "+ e,e);
        }
    }

    @Override
    public ArrayList<ServizioRichiesto> getWishlistUtente(String mailUtente) throws Exception {
        initDb();
        if (mailUtente == null || mailUtente.isEmpty()) {
            throw new Exception("mail utente non valida");
        }
        ArrayList<ServizioRichiesto> lista = wishlistDB.get(mailUtente);
        // Restituisco una lista vuota invece di null, così la GUI non deve gestire il caso
        return lista != null ? lista : new ArrayList<>();
    }
}