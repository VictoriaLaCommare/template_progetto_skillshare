package it.unibo;

public class StatoInAttesaConferma extends AbstractStato{
    @Override
    public String getNomeStato() {
        return "In attesa di conferma dall'altro utente";
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

        // Se entrambi hanno confermato la richiesta è completata sennò rimane in attesa di conferma
        if (richiesta.isConfermataTerminazioneRichiedente() && richiesta.isConfermataTerminazioneRicevente()) {
            richiesta.setStato(new StatoCompletato());
        }
        return true;
    }
}
