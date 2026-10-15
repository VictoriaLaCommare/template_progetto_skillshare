package it.unibo;
import java.io.Serializable;
//Interfaccia per applicare il Pattern STATE
public interface StatoRichiesta extends Serializable{
    //tutti i metodi sfruttano la dependency injection 
    String stato="";
    boolean accetta(RichiestaScambio richiesta, String nomeutente);
    boolean rifiuta(RichiestaScambio richiesta, String nomeutente);
    boolean dichiaraTerminazione(RichiestaScambio richiesta, String nomeutente);
    String getNomeStato();
}

