package it.unibo; 

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

//URL a cui il frontend manderà la richiesta
@RemoteServiceRelativePath("wishlist")
public interface WishlistService extends RemoteService {
    
    // Metodo che verrà eseguito sul server
    Boolean salvaWishlist(String idUtente, Wishlist nuovaWishlist) throws Exception;
}