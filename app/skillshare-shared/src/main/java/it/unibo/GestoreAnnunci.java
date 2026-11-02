package it.unibo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

@RemoteServiceRelativePath("annunci")
public interface GestoreAnnunci extends RemoteService {

    // Metodo per salvare un nuovo annuncio
    Boolean pubblicaAnnuncio(Annuncio annuncio) throws Exception;
    
    // Metodo per recuperare tutti gli annunci pubblicati sulla piattaforma
    List<Annuncio> ottieniTuttiGliAnnunci() throws Exception;

    List<Annuncio> ottieniAnnunciUtente(String username) throws Exception;
    
    Boolean modificaAnnuncio(Annuncio annuncio) throws Exception;
    
    Boolean eliminaAnnuncio(String id, String username) throws Exception;

    List<Annuncio> cercaAnnunci(String competenzaOfferta, String competenzaRichiesta, String keyword, String ordinamento) throws Exception;

    // metodo per visualizzare annunci di utenti preferiti
    List<Annuncio> ottieniAnnunciPreferiti(String username) throws Exception;
}