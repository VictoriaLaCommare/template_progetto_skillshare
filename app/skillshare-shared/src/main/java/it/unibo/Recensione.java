package it.unibo;
import java.io.Serial;
import java.io.Serializable;
public class Recensione implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int valutazione;
    private String commento;
    private Recensione(){}
    
    //factory
    public static Recensione creaRecensione() {
        return new Recensione();
    }

    //A front end la valutazione verrà visualizzata come stelline ma imponiamo lo stesso un check
    public void setValutazione(int valutazione) {
        if(valutazione<=5 && valutazione>=1)
            this.valutazione = valutazione;
        else
            throw new IllegalArgumentException();
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public int getValutazione() {
        return valutazione;
    }

    public String getCommento() {
        return commento;
    }
}
