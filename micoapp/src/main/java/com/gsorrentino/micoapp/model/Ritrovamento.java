package com.gsorrentino.micoapp.model;

import android.graphics.Bitmap;
import android.location.Address;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.GregorianCalendar;

@Entity
public class Ritrovamento {
    @PrimaryKey(autoGenerate = true)
    public int key;
    public double latitudine;
    public double longitudine;
    @Ignore
    private LatLng coordinate;
    @Ignore
    public Address indirizzo;
    // TODO Ricordarsi in futuro di usare Time (supportato solo da API 24)
    public Calendar data;
    @NonNull
    public String fungo;
    public int quantita;
    public String note;
    @Embedded
    @NonNull
    public Utente autore;
    @Ignore
    public Bitmap immagine;
    public String pathImmagine;

    public Ritrovamento(double latitudine, double longitudine, @NonNull String fungo, @NonNull Utente autore) {
        this.data = new GregorianCalendar();
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fungo = fungo;
        this.autore = autore;
    }

    public LatLng getCoordinate() {
        if (coordinate == null)
            coordinate = new LatLng(latitudine, longitudine);
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
        this.latitudine = coordinate.latitude;
        this.longitudine = coordinate.longitude;
    }

//    TODO Usare oppure cancellare
//    Geocoder geocoder;
//    Address address = null;
//    geocoder = new Geocoder(context, Locale.getDefault());
//
//            try {
//        address = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1).get(0);
//    } catch (IOException e) {
//        e.printStackTrace();
//    }


}
