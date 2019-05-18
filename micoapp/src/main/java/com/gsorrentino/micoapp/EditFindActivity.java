package com.gsorrentino.micoapp;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Gestisce la modifica o creazione di un'entità di {@link Ritrovamento}
 */
public class EditFindActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sharedPreferences;
    private MicoAppDatabase db;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_find);
        findViewById(R.id.edit_save_button).setOnClickListener(this);

        if (db == null)
            this.db = MicoAppDatabase.getInstance(this, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        /*Recupero eventuali coordinate passate con l'Intent e le uso
        * per precompilare le relative TextViews*/
        LatLng latLng = new LatLng(0f, 0f);
        LatLng tmp = getIntent().getParcelableExtra(getString(R.string.intent_latlng));
        if(tmp != null)
            latLng = tmp;

        ((EditText)findViewById(R.id.edit_lat_editText)).setText(String.valueOf(latLng.latitude));
        ((EditText)findViewById(R.id.edit_lng_editText)).setText(String.valueOf(latLng.longitude));
    }

    @Override
    public void onClick(View v) {
        String nickname = sharedPreferences.getString(getString(R.string.preference_nickname), "Nemo");
        String nome = sharedPreferences.getString(getString(R.string.preference_name), "Nessuno");
        String cognome = sharedPreferences.getString(getString(R.string.preference_surname), "Nessuno");

        String fungo = ((EditText)findViewById(R.id.edit_mushroom_editText)).getText().toString();
        String nota = ((EditText)findViewById(R.id.edit_note_editText)).getText().toString();
        int quantita =  Integer.valueOf(((EditText)findViewById(R.id.edit_quantity_editText)).getText().toString());
        double lat = Double.valueOf(((EditText)findViewById(R.id.edit_lat_editText)).getText().toString());
        double lng = Double.valueOf(((EditText)findViewById(R.id.edit_lng_editText)).getText().toString());

        List<Address> indirizzo = null;
        try {
            indirizzo = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Ritrovamento ritrovamento = new Ritrovamento(lat, lng, fungo,
                new Utente(Objects.requireNonNull(nickname), Objects.requireNonNull(nome), Objects.requireNonNull(cognome)));
        if(indirizzo != null && indirizzo.size() > 0)
            ritrovamento.indirizzo = indirizzo.get(0).getAddressLine(0);
        ritrovamento.quantita = quantita;
        ritrovamento.note = nota;

        new AsyncTasks.ManageFindAsync(this, ritrovamento).execute(Costanti.INSERT);
        finish();
    }
}
