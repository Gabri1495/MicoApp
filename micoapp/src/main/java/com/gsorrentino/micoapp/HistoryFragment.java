package com.gsorrentino.micoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RicevutoListAdapter;
import com.gsorrentino.micoapp.persistence.RicevutoViewModel;

import java.util.List;
import java.util.Objects;


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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final RicevutoListAdapter adapter = new RicevutoListAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        RicevutoViewModel ricevutoViewModel = ViewModelProviders.of(this).get(RicevutoViewModel.class);
        ricevutoViewModel.getAllRicevuti().observe(this, new Observer<List<Ricevuto>>() {
            @Override
            public void onChanged(List<Ricevuto> ricevuti) {
                adapter.setRicevuti(ricevuti);
            }
        });
    }
}
