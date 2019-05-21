package com.gsorrentino.micoapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.gsorrentino.micoapp.util.Costanti.CREATE_MODE;
import static com.gsorrentino.micoapp.util.Costanti.EDIT_MODE;

/**
 * Gestisce la modifica o creazione di un'entità di {@link Ritrovamento}
 */
public class EditFindActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sharedPreferences;
    private Geocoder geocoder;

    /*Capire se dobbiamo modificare o creare*/
    private String currentMode;
    private Ritrovamento ritrovamento;

    private EditText fungoEditText;
    private EditText quantityEditText;
    private EditText dayEditText;
    private EditText monthEditText;
    private EditText yearEditText;
    private EditText hourEditText;
    private EditText minuteEditText;
    private EditText secondEditText;
    private EditText noteEditText;
    private EditText latEditText;
    private EditText lngEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_find);
        findViewById(R.id.edit_save_update_button).setOnClickListener(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        fungoEditText = findViewById(R.id.edit_mushroom_editText);
        quantityEditText = findViewById(R.id.edit_quantity_editText);
        dayEditText = findViewById(R.id.edit_day_editText);
        monthEditText = findViewById(R.id.edit_month_editText);
        yearEditText = findViewById(R.id.edit_year_editText);
        hourEditText = findViewById(R.id.edit_hour_editText);
        minuteEditText = findViewById(R.id.edit_minute_editText);
        secondEditText = findViewById(R.id.edit_second_editText);
        noteEditText = findViewById(R.id.edit_note_editText);
        latEditText = findViewById(R.id.edit_lat_editText);
        lngEditText = findViewById(R.id.edit_lng_editText);

        /*Subito pronto per l'immssione*/
        fungoEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);

        /*Recupero eventuali dati passati con l'Intent e le uso
        * per precompilare le relative TextViews*/
        LatLng latLng = getIntent().getParcelableExtra(Costanti.INTENT_LATLNG);
        if(latLng != null)
            createMode(latLng);

        ritrovamento = getIntent().getParcelableExtra(Costanti.INTENT_FIND);
        if(ritrovamento != null)
            editMode(ritrovamento);
    }

    @Override
    public void onPause(){
        super.onPause();
        /*Nascondo la tastiera, senza essa rimarrebbe anche senza questa Activity*/
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(fungoEditText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        if(!checkFields()){
            Toast.makeText(this, R.string.error_missing_field, Toast.LENGTH_SHORT).show();
            return;
        }

        /*Disattivo il pulsante per non rischiare di creare una doppia entità*/
        findViewById(R.id.edit_save_update_button).setEnabled(false);

        String fungo = fungoEditText.getText().toString();
        String nota = noteEditText.getText().toString();
        int quantita = Integer.valueOf(quantityEditText.getText().toString());
        double lat = Double.valueOf(latEditText.getText().toString());
        double lng = Double.valueOf(lngEditText.getText().toString());

        List<Address> indirizzo = null;
        try {
            indirizzo = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        }

        switch (currentMode){
            case CREATE_MODE:
                String nickname = sharedPreferences.getString(getString(R.string.preference_nickname), "Nemo");
                String nome = sharedPreferences.getString(getString(R.string.preference_name), "Nessuno");
                String cognome = sharedPreferences.getString(getString(R.string.preference_surname), "Nessuno");

                Ritrovamento newRitrovamento = new Ritrovamento(lat, lng, fungo,
                        new Utente(Objects.requireNonNull(nickname), Objects.requireNonNull(nome), Objects.requireNonNull(cognome)));
                if(indirizzo != null && indirizzo.size() > 0)
                    newRitrovamento.indirizzo = indirizzo.get(0).getAddressLine(0);
                newRitrovamento.quantita = quantita;
                newRitrovamento.note = nota;

                new AsyncTasks.ManageFindAsync(this, newRitrovamento).execute(Costanti.INSERT);
                break;

            case EDIT_MODE:
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
                formatter.setLenient(false);
                String date = "";
                date += dayEditText.getText().toString()
                        + monthEditText.getText().toString()
                        + yearEditText.getText().toString()
                        + hourEditText.getText().toString()
                        + minuteEditText.getText().toString()
                        + secondEditText.getText().toString();
                try {
                    ritrovamento.data.setTime(formatter.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error_date, Toast.LENGTH_SHORT).show();
                    findViewById(R.id.edit_save_update_button).setEnabled(true);
                    return;
                }
                ritrovamento.fungo = fungo;
                /*Evito di invocare il geocoder se le coordinate non sono state toccate*/
                if((ritrovamento.latitudine != lat || ritrovamento.longitudine != lng)
                        && indirizzo != null && indirizzo.size() > 0)
                    ritrovamento.indirizzo = indirizzo.get(0).getAddressLine(0);
                ritrovamento.latitudine = lat;
                ritrovamento.longitudine = lng;
                ritrovamento.quantita = quantita;
                ritrovamento.note = nota;

                new AsyncTasks.ManageFindAsync(this, ritrovamento).execute(Costanti.UPDATE);
                break;
        }
        finish();
    }

    /**
     * Controlla che tutti i campi di testo cruciali siano stati compilati
     *
     * @return True se tutti i campi sono stati compilati
     */
    private boolean checkFields(){
        boolean base = !fungoEditText.getText().toString().isEmpty()
                && !quantityEditText.getText().toString().isEmpty()
                && !latEditText.getText().toString().isEmpty()
                && !lngEditText.getText().toString().isEmpty();
        boolean editMode = currentMode.equals(EDIT_MODE);
        if(editMode) {
                editMode = (!dayEditText.getText().toString().isEmpty()
                    && !monthEditText.getText().toString().isEmpty()
                    && !yearEditText.getText().toString().isEmpty()
                    && !hourEditText.getText().toString().isEmpty()
                    && !minuteEditText.getText().toString().isEmpty()
                    && !secondEditText.getText().toString().isEmpty());
            return base && editMode;
        }
        return base;
    }

    /**
     * Prepara l'activity per creare un {@link Ritrovamento}
     * partendo dalle coordinate ricevute tramite Intent
     *
     * @param latLng Coordinate ricevute
     */
    private void createMode(LatLng latLng){
        currentMode = CREATE_MODE;
        this.setTitle(R.string.create_find);
        latEditText.setText(String.valueOf(latLng.latitude));
        lngEditText.setText(String.valueOf(latLng.longitude));
        dayEditText.setEnabled(false);
        monthEditText.setEnabled(false);
        yearEditText.setEnabled(false);
        hourEditText.setEnabled(false);
        minuteEditText.setEnabled(false);
        secondEditText.setEnabled(false);
        Toast.makeText(this, R.string.date_autoSet, Toast.LENGTH_LONG).show();
    }

    /**
     * Prepara l'activity per modificare un {@link Ritrovamento}
     * esistente e ricevuto tramite Intent
     *
     * @param ritrovamento Ritrovamento ricevuto
     */
    private void editMode(Ritrovamento ritrovamento){
        currentMode = EDIT_MODE;
        fungoEditText.setText(ritrovamento.fungo);
        quantityEditText.setText(String.valueOf(ritrovamento.quantita));
        noteEditText.setText(ritrovamento.note);
        latEditText.setText(String.valueOf(ritrovamento.latitudine));
        lngEditText.setText(String.valueOf(ritrovamento.longitudine));
        Date date = ritrovamento.data.getTime();
        dayEditText.setText(DateFormat.format("dd", date));
        monthEditText.setText(DateFormat.format("MM", date));
        yearEditText.setText(DateFormat.format("yyyy", date));
        hourEditText.setText(DateFormat.format("HH", date));
        minuteEditText.setText(DateFormat.format("mm", date));
        secondEditText.setText(DateFormat.format("ss", date));
    }
}
