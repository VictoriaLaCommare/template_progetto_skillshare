package it.unibo;

import java.io.Serializable;
import java.util.Date;

public class Utente implements Serializable {
    private String nome;
    private String cognome;
    private String username;
    private String mail;
    private String password;
    private Date data;

    //Campi per la personalizzazione del profilo
    private String bio;
    private String tagCompetenze;
    private String immagineProfilo;

    public Utente(){};


    public Utente(String nome, String cognome){
        this.nome=nome;
        this.cognome=cognome;
    }

    //GET e SET. Tutti i controlli vengono fatti da FieldVerifier
    // Nome
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    // Cognome
    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome){
        this.cognome=cognome;
    }

    // Username
    public String getUsername() {
        return username;
    }
    //
    public void setUsername(String Username) {
        this.username = Username;
    }

    // Mail !! mail è chiave dell'hashmap DB
    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }

    // Password
    public String getPassword() {
        return password;
    }
    //Usare con le registrazioni
    public void setPassword(String password) {
        this.password = password;
    }

    // Data
    public Date getData() {
        return data;
    }
    public void setData(Date data) {
        this.data = data;
    }

    //Metodi per il profilo
    public String getBio(){
        return bio;
    }

    public void setBio(String bio){
        this.bio = bio;
    }

    public String getTagCompetenze(){
        return tagCompetenze;
    }

    public void setTagCompetenze(String tagCompetenze){
        this.tagCompetenze = tagCompetenze;
    }

    public String getImmagineProfilo(){
        return immagineProfilo;
    }

    public void setImmagineProfilo(String immagineProfilo){
        this.immagineProfilo = immagineProfilo;
    }
}