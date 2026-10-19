package it.unibo;
import java.io.Serializable;

public class ServizioRichiesto implements Serializable {
    private String nomeServizio;
    private String tagCompetenza;
    private String descrizione;
    private boolean isPubblica; 
    
    public ServizioRichiesto(){}

    public ServizioRichiesto(String nomeServizio, String tagCompetenza, String descrizione, boolean isPubblica){
    this.nomeServizio = nomeServizio;
    this.tagCompetenza = tagCompetenza;
    this.descrizione = descrizione;
    this.isPubblica = isPubblica;
    }

    public String getNomeServizio() {
        return nomeServizio;
    }

    public void setNomeServizio(String nomeServizio) {
        this.nomeServizio = nomeServizio;
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



