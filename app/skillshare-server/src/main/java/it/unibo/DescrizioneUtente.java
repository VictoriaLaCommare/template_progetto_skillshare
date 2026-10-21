package it.unibo;

/**
 * Component interface del Pattern Decorator (server-only).
 * La catena di Decorator produce una StatisticheUtente arricchita
 * con livello e tag, partendo dai dati grezzi del DB.
 */
public interface DescrizioneUtente {
    StatisticheUtente costruisci();
}
