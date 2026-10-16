package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WishlistServiceAsync {
    
    void salvaWishlist(String idUtente, Wishlist nuovaWishlist, AsyncCallback<Boolean> callback);
}