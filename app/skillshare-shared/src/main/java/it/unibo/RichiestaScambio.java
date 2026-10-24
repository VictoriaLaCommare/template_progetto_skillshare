package it.unibo;

import java.io.Serial;
import java.io.Serializable;

public class RichiestaScambio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String idRichiesta;
    private String idAnnuncio;
    private String richiedente;
    private String ricevente;
    private StatoRichiesta stato;
    private boolean confermataTerminazioneRichiedente = false;
    private boolean confermataTerminazioneRicevente = false;

    private Recensione recensione;
    private boolean recensioneInviataRichiedente = false;
    private boolean recensioneInviataRicevente = false;

    // true se questo utente ha già lasciato la sua recensione
    public boolean isRecensioneInviata(String nomeutente) {
        return nomeutente.equals(richiedente) ? recensioneInviataRichiedente : recensioneInviataRicevente;
    }

    public void setRecensioneInviata(String nomeutente) {
        if (nomeutente.equals(richiedente)) recensioneInviataRichiedente = true;
        else recensioneInviataRicevente = true;
    }

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
        this.stato = new StatoInSospeso(); // Stato iniziale della richiesta

    }

    public boolean accetta(String nomeutente) {
        return stato.accetta(this, nomeutente);
    }

    public boolean rifiuta(String nomeutente) {
        return stato.rifiuta(this, nomeutente);
    }

    public boolean dichiaraTerminazione(String nomeutente) {
        return stato.dichiaraTerminazione(this, nomeutente);
    }

    public void aggiungiRecensione(Recensione recensione) {
        this.recensione = recensione;
    }

    // Getter e Setter di stato e flag
    public String getStato() {
        return stato.getNomeStato();
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public boolean isConfermataTerminazioneRichiedente() {
        return confermataTerminazioneRichiedente;
    }

    public void setConfermataTerminazioneRichiedente(boolean val) {
        this.confermataTerminazioneRichiedente = val;
    }

    public boolean isConfermataTerminazioneRicevente() {
        return confermataTerminazioneRicevente;
    }

    public void setConfermataTerminazioneRicevente(boolean val) {
        this.confermataTerminazioneRicevente = val;
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


}
