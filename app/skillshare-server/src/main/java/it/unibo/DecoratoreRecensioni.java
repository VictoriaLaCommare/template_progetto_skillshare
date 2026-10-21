package it.unibo;

/*Aggiunge tag basati sulla media delle recensioni ricevute.
 Livello 1: BenValutato: media >= 3.0
 Livello 2: Eccellente: media >= 4.0
 Livello 3: Leggendario: media >= 4.5 ma con almeno 5 recensioni ottenute
 Può essere aggiunto sopra qualsiasi combinazione dei Decorator
 di scambio poiché non dipendere da essi */
public class DecoratoreRecensioni extends DescrizioneUtenteDecorator {

    public DecoratoreRecensioni(DescrizioneUtente wrapped) {
        super(wrapped);
    }

    @Override
    public StatisticheUtente costruisci() {
        StatisticheUtente s = decorato.costruisci();
        double media = s.getMediaRecensioni();
        int    num   = s.getNumeroRecensioni();

        if (num == 0) return s;

        if (media >= 4.5 && num >= 5) {
            s.getTag().add("🌟 Leggendario");
        } else if (media >= 4.0) {
            s.getTag().add("✨ Eccellente");
        } else if (media >= 3.0) {
            s.getTag().add("👍 Ben Valutato");
        }
        return s;
    }
}
