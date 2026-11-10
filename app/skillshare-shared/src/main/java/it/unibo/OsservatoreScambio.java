package it.unibo;

import java.io.Serializable;

 //Interfaccia Observer
public interface OsservatoreScambio extends Serializable {

    //Chiamato quando entrambi gli utenti hanno confermato la terminazione.
    void DopoScambioCompletato(RichiestaScambio richiesta);

    //Chiamato quando un utente aggiunge una recensione all'altro
    void DopoRecensioneAggiunta(String usernameRecensito, int valutazione);
}
