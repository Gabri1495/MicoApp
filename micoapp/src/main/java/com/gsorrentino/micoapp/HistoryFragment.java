package com.gsorrentino.micoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RicevutoListAdapter;
import com.gsorrentino.micoapp.persistence.RicevutoViewModel;

import java.util.Objects;

/**
 * Un {@link Fragment} che gestisce la visualizzazione dei
 * {@link Ricevuto} salvati nel database locale
 */
public class HistoryFragment extends Fragment {

    private MicoAppDatabase db;


    public HistoryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null)
            this.db = MicoAppDatabase.getInstance(getActivity(), false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.history_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        final RicevutoListAdapter adapter = new RicevutoListAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        RicevutoViewModel ricevutoViewModel = ViewModelProviders.of(this).get(RicevutoViewModel.class);
        ricevutoViewModel.getAllRicevuti().observe(this, adapter::setRicevuti);
    }
}
