package com.gsorrentino.micoapp.model;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Ritrovamento {
    private LatLng coordinate;
    private Address indirizzo;
//  Ricordarsi in futuro di usare Time (supportato solo da API 24)
    private Calendar data;
    private String fungo;
    private int quantita;
    private String note;
    private Utente autore;

    public Ritrovamento(LatLng coordinate, String fungo, Utente autore) {
        this.coordinate = coordinate;
        this.fungo = fungo;
        this.autore = autore;
        this.data = new GregorianCalendar();
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

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
}
