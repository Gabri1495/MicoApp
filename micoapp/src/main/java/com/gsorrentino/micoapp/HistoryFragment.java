package com.gsorrentino.micoapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.adapter.RicevutoListAdapter;
import com.gsorrentino.micoapp.persistence.RicevutoViewModel;
import com.gsorrentino.micoapp.util.Costanti;

import java.util.Objects;

/**
 * Un {@link Fragment} che gestisce la visualizzazione dei
 * {@link Ricevuto} salvati nel database locale
 */
public class HistoryFragment extends Fragment {

    private RicevutoViewModel ricevutoViewModel;
    private RicevutoListAdapter adapter;
    private SharedPreferences sharedPrefs;
    private RadioGroup radioGroup;


    public HistoryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = Objects.requireNonNull(getActivity()).findViewById(R.id.history_radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> manageRadioGroup(checkedId));

        sharedPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Costanti.SHARED_PREFERENCES, 0);
        int restored = sharedPrefs.getInt(Costanti.HISTORY_RADIO_SELECTION, R.id.history_date_radioButton);

        RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.history_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        adapter = new RicevutoListAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        ricevutoViewModel = ViewModelProviders.of(this).get(RicevutoViewModel.class);
        radioGroup.check(restored);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(radioGroup != null) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(Costanti.HISTORY_RADIO_SELECTION, radioGroup.getCheckedRadioButtonId());
            editor.apply();
        }
    }

    /**
     * Modifica il LiveData e relativo observer in relazione
     * all'id ricevuto, riuscendo così ad avere diversi ordinamenti
     *
     * @param checkedId id del RadioButton selezionato
     */
    private void manageRadioGroup(int checkedId){
        adapter.terminaActionMode();
        switch (checkedId) {
            case R.id.history_date_radioButton :
                ricevutoViewModel.getAllRicevutiTimeDec().observe(this, adapter::setRicevuti);
                break;
            case R.id.history_dateReceived_radioButton :
                ricevutoViewModel.getAllRicevutiTimeReceivedDec().observe(this, adapter::setRicevuti);
                break;
            case R.id.history_nickname_radioButton :
                ricevutoViewModel.getAllRicevutiUserAsc().observe(this, adapter::setRicevuti);
                break;
            case R.id.history_mushroom_radioButton :
                ricevutoViewModel.getAllRicevutiFungoAsc().observe(this, adapter::setRicevuti);
                break;
        }
    }
}
