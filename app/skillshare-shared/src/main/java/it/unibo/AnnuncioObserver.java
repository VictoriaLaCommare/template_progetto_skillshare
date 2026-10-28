package it.unibo;

public interface AnnuncioObserver {

     // Chiamato quando un annuncio viene eliminato dal sistema.

    void dopoAnnuncioEliminato(String idAnnuncio);
}