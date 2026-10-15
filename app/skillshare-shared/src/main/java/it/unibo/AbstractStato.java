package it.unibo;
import java.io.Serializable;
//Classe cuscinetto per eliminare la necessità di che creare tutti i metodi dell'interfaccia nelle classi dove non è necessario.
//rendendo il codice più pulito e mantenendo meglio il principio SRP.
// Le future classi di stato estenderanno questa classe ma nella classe di contesto, ovvero le in RichiestaScambio verrà referen=
//ziata l'interfaccia StatoRichiesta perciò quetsa classe è una Pure Fabrication
public abstract class AbstractStato implements StatoRichiesta, Serializable {
    public AbstractStato(){super();}
    @Override
    public boolean accetta(RichiestaScambio richiesta, String nomeutente){
        return false;
    }
    @Override
    public boolean rifiuta(RichiestaScambio richiesta, String nomeutente) {
        return false;
    }

    @Override
    public boolean dichiaraTerminazione(RichiestaScambio richiesta, String nomeutente) {
        return false;
    }
}
