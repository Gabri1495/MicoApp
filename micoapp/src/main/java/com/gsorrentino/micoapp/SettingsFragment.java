package com.gsorrentino.micoapp;

import android.content.Context;
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
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;

import java.util.Objects;

/**
 * {@link androidx.fragment.app.Fragment} per la gestione
 * delle impostazioni dell'applicazione
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    public SettingsFragment() {}

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setSummaryAndListener(findPreference(getString(R.string.preference_nickname)));
        setSummaryAndListener(findPreference(getString(R.string.preference_name)));
        setSummaryAndListener(findPreference(getString(R.string.preference_surname)));
        setSummaryAndListener(findPreference(getString(R.string.preference_link_encyclopedia)));

        /*Configuro le Preference per le varie rimozioni*/
        Preference preferenceButton = findPreference(getString(R.string.preference_delete_finds));
        if (preferenceButton != null) {
            preferenceButton.setOnPreferenceClickListener(preference -> {
                final Context context = getContext();
                if(context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.dialog_deleting_finds);
                    builder.setPositiveButton(R.string.proceed, (dialog, which) -> new AsyncTasks.DeleteAllDbAsync(context).execute(Costanti.REMOVE_FINDS));
                    builder.setNegativeButton(R.string.undo, (dialog, which) -> dialog.cancel());
                    builder.create().show();
                }
                return true;
            });
        }

        preferenceButton = findPreference(getString(R.string.preference_delete_received));
        if (preferenceButton != null) {
            preferenceButton.setOnPreferenceClickListener(preference -> {
                final Context context = getContext();
                if(context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.dialog_deleting_received);
                    builder.setPositiveButton(R.string.proceed, (dialog, which) -> new AsyncTasks.DeleteAllDbAsync(context).execute(Costanti.REMOVE_RECEIVED));
                    builder.setNegativeButton(R.string.undo, (dialog, which) -> dialog.cancel());
                    builder.create().show();
                }
                return true;
            });
        }

        preferenceButton = findPreference(getString(R.string.preference_delete_db));
        if (preferenceButton != null) {
            preferenceButton.setOnPreferenceClickListener(preference -> {
                final Context context = getContext();
                if(context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.dialog_deleting_db);
                    builder.setPositiveButton(R.string.proceed, (dialog, which) -> {
                        context.deleteDatabase(Costanti.DB_NAME);
                        MicoAppDatabase.invalidateInstance();
                        Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
                    });
                    builder.setNegativeButton(R.string.undo, (dialog, which) -> dialog.cancel());
                    builder.create().show();
                }
                return true;
            });
        }
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
            tmp = newValue + " " + ((EditTextPreference) Objects
                    .requireNonNull(findPreference(getString(R.string.preference_surname)))).getText();
            ((TextView) headerView.findViewById(R.id.header_name_surname)).setText(tmp);
        }
        if(key.equals(getString(R.string.preference_surname))){
            preference.setSummary((String) newValue);
            tmp = ((EditTextPreference) Objects
                    .requireNonNull(findPreference(getString(R.string.preference_name)))).getText()
                    + " " + newValue;
            ((TextView) headerView.findViewById(R.id.header_name_surname)).setText(tmp);
        }
        if(key.equals(getString(R.string.preference_link_encyclopedia))){
            preference.setSummary((String) newValue);
        }

        return true;
    }

    /**
     * Imposta Listener e come Summary il valore dell'EditText
     * @param etp EditTextPreference da settare
     */
    private void setSummaryAndListener(EditTextPreference etp){
        if (etp != null) {
            etp.setOnPreferenceChangeListener(this);
            etp.setSummary(etp.getText());
        }
    }
}
