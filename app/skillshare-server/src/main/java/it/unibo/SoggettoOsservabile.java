package it.unibo;

//Interfaccia per l'oggetto osservabile ovvero GestisciRichiesteServiceImp
public interface SoggettoOsservabile {
    void aggiungiOsservatore(OsservatoreScambio o);
    void notificaCompletamento(RichiestaScambio richiesta);
    void notificaRecensione(String usernameRecensito, int valutazione);
}
