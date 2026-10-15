package it.unibo;

public class StatoAccettato extends AbstractStato{

    @Override
    public String getNomeStato() {
        return "Accettata";
    }

    @Override
    public boolean dichiaraTerminazione(RichiestaScambio richiesta, String nomeutente) {
        if (nomeutente.equals(richiesta.getRichiedente())) {
            richiesta.setConfermataTerminazioneRichiedente(true);
        } else if (nomeutente.equals(richiesta.getRicevente())) {
            richiesta.setConfermataTerminazioneRicevente(true);
        } else {
            return false;
        }

        // Se entrambi hanno confermato la richiesta è completata sennò è in attesa di conferma
        if (richiesta.isConfermataTerminazioneRichiedente() && richiesta.isConfermataTerminazioneRicevente()) {
            richiesta.setStato(new StatoCompletato());
        } else {
            richiesta.setStato(new StatoInAttesaConferma());
        }
        return true;
    }
}
