package it.unibo;

//Livello 2: Esperto (5–14 scambi completati).
//Sovrascrive il livello Novizio se le soglie sono superate.
public class EspertoDecorator extends DescrizioneUtenteDecorator {

    public EspertoDecorator(DescrizioneUtente decoratee) {
        super(decoratee);
    }

    @Override
    public StatisticheUtente costruisci() {
        StatisticheUtente s = decorato.costruisci();
        if (s.getScambiCompletati() >= 5 && s.getScambiCompletati() <= 14) {
            s.getTag().add("⭐ Esperto");
            s.setLivello("Esperto");
        }
        return s;
    }
}
