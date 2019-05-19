package com.gsorrentino.micoapp.model;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Rappresenta il ritrovamento di un fungo da parte dell'utente
 */
@Entity(indices={@Index(value={"nickname", "nome", "cognome", "data"}, unique=true)})
public class Ritrovamento {
    @PrimaryKey(autoGenerate = true)
    public int key;
    public double latitudine;
    public double longitudine;
    @Ignore
    private LatLng coordinate;
    public String indirizzo;
    // TODO Ricordarsi in futuro di usare Time (supportato solo da API 24)
    @NonNull
    public Calendar data;
    @NonNull
    public String fungo;
    public int quantita;
    public String note;
    @Embedded
    @NonNull
    public Utente autore;
    public String pathImmagine;
    @Ignore
    public boolean expanded;    /*Utilizzato per gestire l'ampliamento e riduzione nella RecyclerView*/


    private Ritrovamento(@NonNull String fungo, @NonNull Utente autore) {
        this.data = new GregorianCalendar();
        this.fungo = fungo;
        this.autore = autore;
        this.quantita = 1;
        this.note = "";
        this.expanded = false;
    }

    /**
     * I campi sono pubblici e la data del ritrovamento viene
     * inizialmente settata dal costruttore
     *
     * @param latitudine Lat del ritrovamento
     * @param longitudine Lng del ritrovamento
     * @param fungo Fungo trovato
     * @param autore Chi ha trovato il fungo
     */
    public Ritrovamento(double latitudine, double longitudine, @NonNull String fungo, @NonNull Utente autore) {
        this(fungo, autore);
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }

    /**
     * I campi sono pubblici e la data del ritrovamento viene
     * inizialmente settata dal costruttore. In questo
     * costruttore verranno automaticamente inizializzati anche
     * i campi {@link Ritrovamento#latitudine} e {@link Ritrovamento#longitudine}
     *
     * @param latLng LatLng del ritrovamento
     * @param fungo Fungo trovato
     * @param autore Chi ha trovato il fungo
     */
    public Ritrovamento(LatLng latLng, @NonNull String fungo, @NonNull Utente autore) {
        this(fungo, autore);
        setCoordinate(latLng);
    }

    /**
     * Se LatLng non ancora istanziato lo fa ora partendo da lat e lng
     *
     * @return Coordinate del ritrovamento
     */
    public LatLng getCoordinate() {
        if (coordinate == null)
            coordinate = new LatLng(latitudine, longitudine);
        return coordinate;
    }

    /**
     * Contestualmente imposta anche lat e lng
     *
     * @param coordinate Coordinate del ritrovamento; se null verranno ignorate
     */
    public void setCoordinate(LatLng coordinate) {
        if (coordinate != null) {
            this.coordinate = coordinate;
            this.latitudine = coordinate.latitude;
            this.longitudine = coordinate.longitude;
        }
    }
}
