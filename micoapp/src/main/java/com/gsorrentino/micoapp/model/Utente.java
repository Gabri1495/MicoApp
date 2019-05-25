package com.gsorrentino.micoapp.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Un persona definita da nickname, nome e cognome che ha trovato un fungo.
 */
public class Utente implements Serializable {
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

    /**
     * @return Nome concatenato al cognome e separato da spazio
     */
    public String getNomeCompleto() {
        return this.nome + " " + this.cognome;
    }
}
