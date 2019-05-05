package com.gsorrentino.micoapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.navigation.NavigationView;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RitrovamentoDao;

import java.lang.ref.WeakReference;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    public SettingsFragment() {}

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

         /*Imposto i riassunti delle varie impostazioni
         Riutilizzo sempre lo stesso oggetto, ma imposto anche i listener per i vari campi*/
        EditTextPreference etp = (EditTextPreference) findPreference(getString(R.string.preference_nickname));
        etp.setOnPreferenceChangeListener(this);
        etp.setSummary(etp.getText());
        etp = (EditTextPreference) findPreference(getString(R.string.preference_name));
        etp.setOnPreferenceChangeListener(this);
        etp.setSummary(etp.getText());
        etp = (EditTextPreference) findPreference(getString(R.string.preference_surname));
        etp.setOnPreferenceChangeListener(this);
        etp.setSummary(etp.getText());

        Preference preferenceButton = findPreference(getString(R.string.preference_delete_finds));
        preferenceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.dialog_deleting_finds);
                builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteAllDbAsync(MicoAppDatabase.getInstance(getContext(), false)).execute();
                    }
                });
                builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
            }
        });

        preferenceButton = findPreference(getString(R.string.preference_delete_db));
        preferenceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.dialog_deleting_db);
                builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContext().deleteDatabase(MicoAppDatabase.DB_NAME);
                        MicoAppDatabase.invalidateInstance();
                    }
                });
                builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        String tmp;

        if(key.equals(getString(R.string.preference_nickname))){
            preference.setSummary((String) newValue);
            ((TextView) headerView.findViewById(R.id.header_nickname)).setText((String) newValue);
        }
        if(key.equals(getString(R.string.preference_name))){
            preference.setSummary((String) newValue);
            tmp = newValue + " " + ((EditTextPreference)findPreference(getString(R.string.preference_surname))).getText();
            ((TextView) headerView.findViewById(R.id.header_name_surname)).setText(tmp);
        }
        if(key.equals(getString(R.string.preference_surname))){
            preference.setSummary((String) newValue);
            tmp = ((EditTextPreference)findPreference(getString(R.string.preference_name))).getText()
                    + " " + newValue;
            ((TextView) headerView.findViewById(R.id.header_name_surname)).setText(tmp);
        }


        return true;
    }



    private static class DeleteAllDbAsync extends AsyncTask<Void, Void, Void> {

        private final RitrovamentoDao dao;

        DeleteAllDbAsync(MicoAppDatabase db) {
            dao = db.ritrovamentoDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            dao.deleteAll();
            return null;
        }

    }
}
