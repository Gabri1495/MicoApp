package com.gsorrentino.micoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.persistence.RitrovamentoListAdapter;
import com.gsorrentino.micoapp.persistence.RitrovamentoViewModel;

import java.util.List;
import java.util.Objects;

/**
 * Un {@link Fragment} che gestisce la visualizzazione dei
 * {@link Ritrovamento} salvati nel database locale
 */
public class ArchiveFragment extends Fragment {

    private RitrovamentoViewModel ritrovamentoViewModel;
    private RitrovamentoListAdapter adapter;


    public ArchiveFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        RadioGroup radioGroup = Objects.requireNonNull(getActivity()).findViewById(R.id.archive_radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> manageRadioGroup(checkedId));
    
        RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.archive_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        adapter = new RitrovamentoListAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        ritrovamentoViewModel = ViewModelProviders.of(this).get(RitrovamentoViewModel.class);
        manageRadioGroup(radioGroup.getCheckedRadioButtonId());
        ritrovamentoViewModel.getAllRitrovamenti().observe(this, adapter::setRitrovamenti);
    }

    private void manageRadioGroup(int checkedId){
        switch (checkedId) {
            case R.id.archive_insert_radioButton :
                ritrovamentoViewModel.getAllRitrovamenti().observe(this, adapter::setRitrovamenti);;
                break;
            case R.id.archive_date_radioButton :
                ritrovamentoViewModel.getAllRitrovamentiTimeDec().observe(this, adapter::setRitrovamenti);
                break;
            case R.id.archive_mushroom_radioButton :
                ritrovamentoViewModel.getAllRitrovamentiFungoAsc().observe(this, adapter::setRitrovamenti);;
                break;
        }
    }
}
