package com.gsorrentino.micoapp.model;

class Utente {
    private String nickname;
    private String nome;
    private String cognome;

    public Utente(String nickname, String nome, String cognome) {
        this.nickname = nickname;
        this.nome = nome;
        this.cognome = cognome;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
}
