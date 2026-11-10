package it.unibo;

/*
Decorator astratto: delega a decorato e lascia ai figli
il compito di arricchire il risultato con livello e tag.
*/
public abstract class DescrizioneUtenteDecorator implements DescrizioneUtente {

    protected final DescrizioneUtente decorato;

    protected DescrizioneUtenteDecorator(DescrizioneUtente decorato) {
        this.decorato = decorato;
    }

    @Override
    public StatisticheUtente costruisci() {
        return decorato.costruisci();
    }
}
