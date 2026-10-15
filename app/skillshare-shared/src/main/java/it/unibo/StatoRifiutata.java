package it.unibo;
//Estendendo AbstracStato non devo riscrivere i metodi accetta rifiuta
public class StatoRifiutata extends AbstractStato{
    @Override
    public String getNomeStato() {
        return "Rifiutata";
    }



}
