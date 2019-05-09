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
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static final String REMOVE_FINDS = "removeFinds";
    private static final String REMOVE_RECEIVED = "removeReceived";

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

        /*Configuro le Preference per le varie rimozioni*/
        Preference preferenceButton = findPreference(getString(R.string.preference_delete_finds));
        preferenceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Context context = getContext();
                if(context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.dialog_deleting_finds);
                    builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteAllDbAsync(context, SettingsFragment.REMOVE_FINDS).execute();
                        }
                    });
                    builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }
                return true;
            }
        });

        preferenceButton = findPreference(getString(R.string.preference_delete_received));
        preferenceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Context context = getContext();
                if(context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.dialog_deleting_received);
                    builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteAllDbAsync(context, SettingsFragment.REMOVE_RECEIVED).execute();
                        }
                    });
                    builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }
                return true;
            }
        });

        preferenceButton = findPreference(getString(R.string.preference_delete_db));
        preferenceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Context context = getContext();
                if(context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.dialog_deleting_db);
                    builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.deleteDatabase(MicoAppDatabase.DB_NAME);
                            MicoAppDatabase.invalidateInstance();
                            Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        NavigationView navigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.nav_view);
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

        private MicoAppDatabase db;
        private String removal;
        private final WeakReference<Context> contextRef;

        DeleteAllDbAsync(Context context, String rm) {
            removal = rm;
            contextRef = new WeakReference<>(context);
            db = MicoAppDatabase.getInstance(context, false);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            switch(removal){
                case SettingsFragment.REMOVE_FINDS:
                    db.ritrovamentoDao().deleteAll();
                    break;
                case SettingsFragment.REMOVE_RECEIVED:
                    db.ricevutoDao().deleteAll();
                    break;
            }
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
