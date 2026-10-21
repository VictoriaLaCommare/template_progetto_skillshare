package it.unibo;

import java.util.*;

// Classe proxy che si occupa del controllo dei valori del profilo prima di passarli al Reale
public class GestoreProfiloProxy implements GestoreProfilo {
    private final GestoreProfilo gestoreReale = new GestoreProfiloReale();

    @Override
    public Utente aggiornaProfilo(Utente utenteAggiornato) throws Exception {
        if (utenteAggiornato == null || utenteAggiornato.getMail() == null || utenteAggiornato.getMail().isEmpty()) {
            throw new IllegalArgumentException("Errore: email mancante o utente non valido.");
        }

        if(!FieldVerifier.isValidBio(utenteAggiornato.getBio()))
            throw new IllegalArgumentException("Limite di caratteri superato.");

        if(!FieldVerifier.isValidTag(utenteAggiornato.getTagCompetenze()))
            throw new IllegalArgumentException("Inserisci i tag.");

        // Se i controlli passano, delega al gestore reale
        return gestoreReale.aggiornaProfilo(utenteAggiornato);
    }

    @Override
    public Utente ottieniProfiloUtente(String username) throws Exception {
        // Controlla se l'username è null o da spazi bianchi
        if (username == null || username.trim().isEmpty()){
            throw new IllegalArgumentException("Username non valido per la ricerca");
        }

        return gestoreReale.ottieniProfiloUtente(username);
    }

    @Override
    public List<RichiestaScambio> ottieniStoricoScambi(String username) throws Exception {
        // Controlla se l'username è null o da spazi bianchi
        if (username == null || username.trim().isEmpty()){
            throw new IllegalArgumentException("Username non valido per lo storico scambi");
        }

        return gestoreReale.ottieniStoricoScambi(username);
    }

    @Override
    public StatisticheUtente ottieniStatistiche(String username) throws Exception {
        return gestoreReale.ottieniStatistiche(username);
    }
}