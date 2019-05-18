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

import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RitrovamentoListAdapter;
import com.gsorrentino.micoapp.persistence.RitrovamentoViewModel;

import java.util.List;
import java.util.Objects;

/**
 * Un {@link Fragment} che gestisce la visualizzazione dei
 * {@link Ritrovamento} salvati nel database locale
 */
public class ArchiveFragment extends Fragment {

    private MicoAppDatabase db;


    public ArchiveFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null)
            this.db = MicoAppDatabase.getInstance(getActivity(), false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_archive, container, false);
    }

    /*Una volta che la View è stata creata basta agganciare il ViewModel*/
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    
        RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.archive_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final RitrovamentoListAdapter adapter = new RitrovamentoListAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        RitrovamentoViewModel ritrovamentoViewModel = ViewModelProviders.of(this).get(RitrovamentoViewModel.class);
        ritrovamentoViewModel.getAllRitrovamenti().observe(this, new Observer<List<Ritrovamento>>() {
            @Override
            public void onChanged(List<Ritrovamento> ritrovamenti) {
                adapter.setRitrovamenti(ritrovamenti);
            }
        });
    }
}
