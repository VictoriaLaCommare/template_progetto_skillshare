package it.unibo;

//Livello 3: Maestro (15+ scambi completati).
//Livello massimo della progressione basata sugli scambi.

public class MaestroDecorator extends DescrizioneUtenteDecorator {

    public MaestroDecorator(DescrizioneUtente decoratee) {
        super(decoratee);
    }

    @Override
    public StatisticheUtente costruisci() {
        StatisticheUtente s = decorato.costruisci();
        if (s.getScambiCompletati() >= 15) {
            s.getTag().add("🏆 Maestro");
            s.setLivello("Maestro");
        }
        return s;
    }
}
