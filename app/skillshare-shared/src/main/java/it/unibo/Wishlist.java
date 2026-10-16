package it.unibo;
import java.io.Serializable;

public class Wishlist implements Serializable {
    private String servizioRichiesto;
    private String tagCompetenza;
    private String descrizione;
    private boolean isPubblica; 
    
    public Wishlist(){}

    public Wishlist(String servizioRichiesto, String tagCompetenza, String descrizione, boolean isPubblica){
    this.servizioRichiesto = servizioRichiesto;
    this.tagCompetenza = tagCompetenza;
    this.descrizione = descrizione;
    this.isPubblica = isPubblica;
    }

    public String getServizioRichiesto() {
        return servizioRichiesto;
    }

    public void setServizioRichiesto(String servizioRichiesto) {
        this.servizioRichiesto = servizioRichiesto;
    }

    public String getTagCompetenza() {
        return tagCompetenza;
    }

    public void setTagCompetenza(String tagCompetenza) {
        this.tagCompetenza = tagCompetenza;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public boolean isPubblica() {
        return isPubblica;
    }

    public void setPubblica(boolean isPubblica) {
        this.isPubblica = isPubblica;
    }
}



