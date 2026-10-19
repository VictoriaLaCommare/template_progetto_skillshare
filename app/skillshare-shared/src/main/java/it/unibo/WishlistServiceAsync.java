package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;

public interface WishlistServiceAsync {
    
    void aggiungiServizio(String mailUtente, ServizioRichiesto nuovaWishlist, AsyncCallback<Boolean> callback);
    void getWishlistUtente(String mailUtente, AsyncCallback<ArrayList<ServizioRichiesto>> callback);
}