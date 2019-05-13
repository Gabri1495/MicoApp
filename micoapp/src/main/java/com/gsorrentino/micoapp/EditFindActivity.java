package com.gsorrentino.micoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class EditFindActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sharedPreferences;
    private MicoAppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_find);
        findViewById(R.id.edit_save_button).setOnClickListener(this);

        if (db == null)
            this.db = MicoAppDatabase.getInstance(this, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        Ritrovamento ritrovamento = new Ritrovamento(lat, lng, fungo,
                new Utente(Objects.requireNonNull(nickname), Objects.requireNonNull(nome), Objects.requireNonNull(cognome)));
        ritrovamento.quantita = quantita;
        ritrovamento.note = nota;

        new InsertFindAsync(this, ritrovamento).execute();
        finish();
    }


    private static class InsertFindAsync extends AsyncTask<Void, Void, Void> {

        private MicoAppDatabase db;
        private Ritrovamento find;
        private final WeakReference<Context> contextRef;

        InsertFindAsync(Context context, Ritrovamento find) {
            this.find = find;
            contextRef = new WeakReference<>(context);
            db = MicoAppDatabase.getInstance(context, false);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            db.ritrovamentoDao().insertRitrovamento(find);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextRef.get();
            if(context != null) {
                Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
