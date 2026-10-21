package it.unibo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 Contiene i dati grezzi (punti, scambi, recensioni) e i dati calcolati
 (livello, tag) che il server popola tramite la catena di Decorator
 prima di spedirla al client.*/
public class StatisticheUtente implements Serializable {

    private String  username;
    private int     scambiCompletati  = 0;
    private int     sommaValutazioni  = 0;   // usata per calcolare la media
    private int     numeroRecensioni  = 0;

    // Popolati dai Decorator server-side, non persistiti nel DB
    private String       livello = "";
    private List<String> tag     = new ArrayList<>();

    @SuppressWarnings("unused")
    private StatisticheUtente() {}

    public StatisticheUtente(String username) { this.username = username; }

    //GETTER dei decorator
    public String  getUsername()          { return username; }
    public int     getScambiCompletati()  { return scambiCompletati; }
    public int     getNumeroRecensioni()  { return numeroRecensioni; }
    public String  getLivello()           { return livello; }
    public List<String> getTag()          { return tag; }

    public double getMediaRecensioni() {
        return numeroRecensioni > 0 ? (double) sommaValutazioni / numeroRecensioni : 0.0;
    }

    // ── mutatori chiamati da GestoreGamification
    public void incrementaScambi()           { scambiCompletati++; }
    public void aggiungiValutazione(int val) { sommaValutazioni += val; numeroRecensioni++; }

    // SETTER usati dai Decorator
    public void setLivello(String livello)   { this.livello = livello; }
    public void setTag(List<String> tag)     { this.tag = tag; }
}
