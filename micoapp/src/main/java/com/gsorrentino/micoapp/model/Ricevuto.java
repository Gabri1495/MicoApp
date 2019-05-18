package com.gsorrentino.micoapp.model;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Rappresenta un'entrata del registro di {@link Ritrovamento} ricevuti
 */
@Entity(indices={@Index(value={"nickname", "nome", "cognome", "data"}, unique=true)})
public class Ricevuto {
    @PrimaryKey(autoGenerate = true)
    public int key;
    // TODO Ricordarsi in futuro di usare Time (supportato solo da API 24)
    @NonNull
    public Calendar data;
    @NonNull
    public Calendar dataRicezione;
    @NonNull
    public String fungo;
    @Embedded
    @NonNull
    public Utente autore;

    /**
     * I campi sono pubblici e la data di ricezione è
     * automaticamente settata nel costruttore
     *
     * @param data La data del Ritrovamento ricevuto
     * @param fungo Fungo del Ritrovamento ricevuto
     * @param autore Chi ha creato l'originale Ritrovamento
     */
    public Ricevuto(@NonNull Calendar data, @NonNull String fungo, @NonNull Utente autore) {
        this.dataRicezione = new GregorianCalendar();
        this.data = data;
        this.fungo = fungo;
        this.autore = autore;
    }
}
