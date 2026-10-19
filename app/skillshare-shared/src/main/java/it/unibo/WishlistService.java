package it.unibo; 

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.ArrayList;

//URL a cui il frontend manderà la richiesta
@RemoteServiceRelativePath("wishlist")
public interface WishlistService extends RemoteService {
    
    // Metodo che verrà eseguito sul server
    Boolean aggiungiServizio(String mailUtente, ServizioRichiesto nuovoServizio) throws Exception;
    ArrayList<ServizioRichiesto> getWishlistUtente(String mailUtente) throws Exception;
}