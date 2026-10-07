package it.unibo;

import java.io.Serializable;

public class RichiestaScambio implements Serializable {
    private String idRichiesta;
    private String idAnnuncio;
    private String richiedente;
    private String ricevente;
    private String stato;

    @SuppressWarnings("unused")
    private RichiestaScambio() {
        // Costruttore privato per la serializzazione
    }

    // Costruttore pubblico per creare una nuova richiesta di scambio
    public RichiestaScambio(String idRichiesta, String idAnnuncio, String richiedente, String ricevente) {
        this.idRichiesta = idRichiesta;
        this.idAnnuncio = idAnnuncio;
        this.richiedente = richiedente;
        this.ricevente = ricevente;
        this.stato = "In sospeso"; // Stato iniziale della richiesta
    }

    public String getIdRichiesta() {
        return idRichiesta;
    }

    public void setIdRichiesta(String idRichiesta) {
        this.idRichiesta = idRichiesta;
    }

    public String getIdAnnuncio() {
        return idAnnuncio;
    }

    public void setIdAnnuncio(String idAnnuncio) {
        this.idAnnuncio = idAnnuncio;
    }

    public String getRichiedente() {
        return richiedente;
    }

    public void setRichiedente(String richiedente) {
        this.richiedente = richiedente;
    }

    public String getRicevente() {
        return ricevente;
    }

    public void setRicevente(String ricevente) {
        this.ricevente = ricevente;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

}
