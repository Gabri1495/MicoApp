package com.gsorrentino.micoapp.model;

import androidx.annotation.NonNull;

public class Utente {
    @NonNull
    public String nickname;
    @NonNull
    public String nome;
    @NonNull
    public String cognome;

    public Utente(@NonNull String nickname, @NonNull String nome, @NonNull String cognome) {
        this.nickname = nickname;
        this.nome = nome;
        this.cognome = cognome;
    }
}
