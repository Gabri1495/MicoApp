package com.gsorrentino.micoapp.model;

import android.graphics.Bitmap;
import android.location.Address;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.GregorianCalendar;

@Entity
public class Ritrovamento {
    @PrimaryKey(autoGenerate = true)
    private int key;
    private double latitudine;
    private double longitudine;
    @Ignore
    private Address indirizzo;
//  Ricordarsi in futuro di usare Time (supportato solo da API 24)
    private Calendar data;
    private String fungo;
    private int quantita;
    private String note;
    @Embedded
    private Utente autore;
    @Ignore
    private Bitmap immagine;
    private String pathImmagine;

    public Ritrovamento(){
        this.data = new GregorianCalendar();
    }

    @Ignore
    public Ritrovamento(double latitudine, double longitudine, String fungo, Utente autore) {
        super();
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fungo = fungo;
        this.autore = autore;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    //    Geocoder geocoder;
//    Address address = null;
//    geocoder = new Geocoder(context, Locale.getDefault());
//
//            try {
//        address = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1).get(0);
//    } catch (IOException e) {
//        e.printStackTrace();
//    }

    public Address getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(Address indirizzo) {
        this.indirizzo = indirizzo;
    }

    public Calendar getData() {
        return data;
    }

    public void setData(Calendar data) {
        this.data = data;
    }

    public String getFungo() {
        return fungo;
    }

    public void setFungo(String fungo) {
        this.fungo = fungo;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Utente getAutore() {
        return autore;
    }

    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    public Bitmap getImmagine() {
        return immagine;
    }

    public void setImmagine(Bitmap immagine) {
        this.immagine = immagine;
    }

    public String getPathImmagine() {
        return pathImmagine;
    }

    public void setPathImmagine(String pathImmagine) {
        this.pathImmagine = pathImmagine;
    }

    public static Ritrovamento[] populateData() {
        return new Ritrovamento[] {
                new Ritrovamento(44.326838, 11.402792,
                        "Porcino",
                        new Utente("Robby", "Roberto", "Cocchi")),
                new Ritrovamento(44.405556, 11.407175,
                        "Finferli",
                        new Utente("Gabri", "Gabriele", "Sorrentino")),
                new Ritrovamento(46.282246, 11.436602,
                        "Fungo dell'antipatia",
                        new Utente("Gabri", "Gabriele", "Sorrentino"))
        };
    }
}
