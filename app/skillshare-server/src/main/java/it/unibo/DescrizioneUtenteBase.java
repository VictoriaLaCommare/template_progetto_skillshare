package it.unibo;

import java.util.ArrayList;

/**
 * ConcreteComponent: avvolge i dati grezzi dal DB e li copia
 * in un oggetto pulito con tag e livello vuoti.
 * È il punto di partenza della catena di Decorator.
 */
public class DescrizioneUtenteBase implements DescrizioneUtente {

    private final StatisticheUtente datiGrezzi;

    public DescrizioneUtenteBase(StatisticheUtente datiGrezzi) {
        this.datiGrezzi = datiGrezzi;
    }

    @Override
    public StatisticheUtente costruisci() {
        StatisticheUtente s = new StatisticheUtente(datiGrezzi.getUsername());
       for (int i = 0; i < datiGrezzi.getScambiCompletati(); i++) s.incrementaScambi();
       for (int i = 0; i < datiGrezzi.getNumeroRecensioni(); i++)
            s.aggiungiValutazione((int) Math.round(datiGrezzi.getMediaRecensioni()));
        s.setTag(new ArrayList<>());
        s.setLivello("");
        return s;
    }
}
