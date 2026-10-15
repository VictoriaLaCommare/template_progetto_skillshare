package it.unibo;
/*Stato della richiesta in sospeso: qui sono presenti i metodi accetta e rifiuta attraverso le operazioni di
override.
Il metodo accetta può essere effettuato solo dall'utente Ricevente, a differenza del metodo rifiuta che può
essere effettuato da entrambi
* */
public class StatoInSospeso extends AbstractStato{

    @Override
    public String getNomeStato() {
        return "In sospeso";
    }

    @Override
    public boolean accetta(RichiestaScambio richiesta, String nomeutente){
        if(richiesta.getRicevente().equals(nomeutente)){
            richiesta.setStato(new StatoAccettato());
            return true;
        }
        return false;
    }

    @Override
    public boolean rifiuta(RichiestaScambio richiesta, String nomeutente){
        richiesta.setStato(new StatoRifiutata());
        return true;

    }

}
