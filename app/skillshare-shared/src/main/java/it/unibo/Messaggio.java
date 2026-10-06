package it.unibo;

import java.io.Serializable;
import java.util.Date;

public class Messaggio implements Serializable {
    private String idRichiesta; // Collega la chat alla specifica richiesta di scambio
    private String mittente;
    private String testo;
    private Date timestamp;

    // Costruttore vuoto obbligatorio per GWT RPC
    public Messaggio() {}

    public Messaggio(String idRichiesta, String mittente, String testo, Date timestamp) {
        this.idRichiesta = idRichiesta;
        this.mittente = mittente;
        this.testo = testo;
        this.timestamp = timestamp;
    }

    public String getIdRichiesta() { return idRichiesta; }
    public void setIdRichiesta(String idRichiesta) { this.idRichiesta = idRichiesta; }

    public String getMittente() { return mittente; }
    public void setMittente(String mittente) { this.mittente = mittente; }

    public String getTesto() { return testo; }
    public void setTesto(String testo) { this.testo = testo; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}