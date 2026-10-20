package it.unibo;


 //Livello 1: Novizio (1–4 scambi completati)

public class NovizioDecorator extends DescrizioneUtenteDecorator {

    public NovizioDecorator(DescrizioneUtente decoratee) {
        super(decoratee);
    }

    @Override
    public StatisticheUtente costruisci() {
        StatisticheUtente s = decorato.costruisci();
        if (s.getScambiCompletati() >= 1 && s.getScambiCompletati() <= 4) {
            s.getTag().add("🌱 Novizio");
            s.setLivello("Novizio");
            //la decorazione parte dal livello più in basso perciò le classi superiori
            //sovrascriveranno il livello precedente

        }
        return s;
    }
}
