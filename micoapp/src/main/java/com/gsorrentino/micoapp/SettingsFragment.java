package com.gsorrentino.micoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.navigation.NavigationView;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Imposto i riassunti delle varie impostazioni
        // Riutilizzo sempre lo stesso oggetto, ma imposto anche i listener per i vari campi
        EditTextPreference etp = (EditTextPreference) findPreference(getString(R.string.preference_nickname));
        etp.setOnPreferenceChangeListener(this);
        etp.setSummary(etp.getText());
        etp = (EditTextPreference) findPreference(getString(R.string.preference_name));
        etp.setOnPreferenceChangeListener(this);
        etp.setSummary(etp.getText());
        etp = (EditTextPreference) findPreference(getString(R.string.preference_surname));
        etp.setOnPreferenceChangeListener(this);
        etp.setSummary(etp.getText());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        String finalValue = "";
        if(newValue instanceof String){
            finalValue = (String)newValue;
            int index = (finalValue.indexOf(System.getProperty("line.separator")));
            if(index != -1) {
                finalValue = (finalValue.substring(0, index));
                Toast.makeText(getActivity(), R.string.error_carriage_return, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        String tmp;

        if(key.equals(getString(R.string.preference_nickname))){
            preference.setSummary(finalValue);
            ((TextView) headerView.findViewById(R.id.header_nickname)).setText(finalValue);
        }
        if(key.equals(getString(R.string.preference_name))){
            preference.setSummary(finalValue);
            tmp = finalValue + " " + ((EditTextPreference)findPreference(getString(R.string.preference_surname))).getText();
            ((TextView) headerView.findViewById(R.id.header_name_surname)).setText(tmp);
        }
        if(key.equals(getString(R.string.preference_surname))){
            preference.setSummary(finalValue);
            tmp = ((EditTextPreference)findPreference(getString(R.string.preference_name))).getText()
                    + " " + finalValue;
            ((TextView) headerView.findViewById(R.id.header_name_surname)).setText(tmp);
        }

        return true;
    }
}
