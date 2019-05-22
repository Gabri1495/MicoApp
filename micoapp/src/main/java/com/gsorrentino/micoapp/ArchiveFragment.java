package com.gsorrentino.micoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.persistence.RitrovamentoListAdapter;
import com.gsorrentino.micoapp.persistence.RitrovamentoViewModel;
import com.gsorrentino.micoapp.util.Costanti;

import java.util.Objects;

/**
 * Un {@link Fragment} che gestisce la visualizzazione dei
 * {@link Ritrovamento} salvati nel database locale
 */
public class ArchiveFragment extends Fragment implements View.OnClickListener {

    private RitrovamentoViewModel ritrovamentoViewModel;
    private RitrovamentoListAdapter adapter;
    private SharedPreferences sharedPrefs;
    private RadioGroup radioGroup;

    /*Tengo traccia della ricerca visibile o meno*/
    private boolean searchOpen;


    public ArchiveFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchOpen = true;
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

        radioGroup = Objects.requireNonNull(getActivity()).findViewById(R.id.archive_radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> manageRadioGroup(checkedId));

        sharedPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Costanti.SHARED_PREFERENCES, 0);
        int restoredRadioSelection = sharedPrefs.getInt(Costanti.ARCHIVE_RADIO_SELECTION, R.id.archive_date_radioButton);
        boolean restoredSearchOpen = sharedPrefs.getBoolean(Costanti.ARCHIVE_SEARCH_OPEN, searchOpen);

        manageArrowButton(restoredSearchOpen);

        RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.archive_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        getActivity().findViewById(R.id.archive_arrow_button).setOnClickListener(this);
        getActivity().findViewById(R.id.archive_search_button).setOnClickListener(this);

        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        adapter = new RitrovamentoListAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        ritrovamentoViewModel = ViewModelProviders.of(this).get(RitrovamentoViewModel.class);
        radioGroup.check(restoredRadioSelection);
    }

    @Override
    public void onPause(){
        super.onPause();
        hideSearchKeyboard();

        SharedPreferences.Editor editor = sharedPrefs.edit();
        if(radioGroup != null && radioGroup.getCheckedRadioButtonId() != -1) {
            editor.putInt(Costanti.ARCHIVE_RADIO_SELECTION, radioGroup.getCheckedRadioButtonId());
        }
        editor.putBoolean(Costanti.ARCHIVE_SEARCH_OPEN, searchOpen);
        editor.apply();
    }

    /**
     * Modifica il LiveData e relativo observer in relazione
     * all'id ricevuto, riuscendo così ad avere diversi ordinamenti
     *
     * @param checkedId id del RadioButton selezionato
     */
    private void manageRadioGroup(int checkedId){
        hideSearchKeyboard();
        adapter.terminaActionMode();
        switch (checkedId) {
            case R.id.archive_nickname_radioButton :
                ritrovamentoViewModel.getAllRitrovamentiNicknameAsc().observe(this, adapter::setRitrovamenti);
                break;
            case R.id.archive_date_radioButton :
                ritrovamentoViewModel.getAllRitrovamentiTimeDec().observe(this, adapter::setRitrovamenti);
                break;
            case R.id.archive_mushroom_radioButton :
                ritrovamentoViewModel.getAllRitrovamentiFungoAsc().observe(this, adapter::setRitrovamenti);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        hideSearchKeyboard();
        adapter.terminaActionMode();
        switch (v.getId()){
            case R.id.archive_arrow_button :
                manageArrowButton(!searchOpen);
                break;

            case R.id.archive_search_button:
                radioGroup.check(-1);
                String textSearch = "%" +
                        ((EditText)Objects.requireNonNull(getActivity()).findViewById(R.id.archive_search_view))
                                .getText().toString()
                        + "%";
                ritrovamentoViewModel.getAllRitrovamentiLuogoSearch(textSearch).observe(this, adapter::setRitrovamenti);
                break;
        }
    }

    /**
     * Gestisce il Button incaricato di mostrare e nascondere
     * l'interfaccia di ricerca dei {@link Ritrovamento} in
     * {@link ArchiveFragment}
     *
     * @param open Interfaccia visibile
     */
    private void manageArrowButton(boolean open){
        ConstraintLayout constraintLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.archive_constraintLayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        if(open){
            Objects.requireNonNull(getActivity()).findViewById(R.id.archive_search_view).setVisibility(View.VISIBLE);
            Objects.requireNonNull(getActivity()).findViewById(R.id.archive_search_button).setVisibility(View.VISIBLE);
            ((ImageButton)Objects.requireNonNull(getActivity()).findViewById(R.id.archive_arrow_button))
                    .setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_arrow_drop_up));
            constraintSet.connect(R.id.archive_recycler,ConstraintSet.TOP,R.id.archive_search_view,ConstraintSet.BOTTOM,0);
        }
        else{
            Objects.requireNonNull(getActivity()).findViewById(R.id.archive_search_view).setVisibility(View.GONE);
            Objects.requireNonNull(getActivity()).findViewById(R.id.archive_search_button).setVisibility(View.GONE);
            ((ImageButton)Objects.requireNonNull(getActivity()).findViewById(R.id.archive_arrow_button))
                    .setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_arrow_drop_down));
            constraintSet.connect(R.id.archive_recycler,ConstraintSet.TOP,R.id.archive_horizontal_scrollView,ConstraintSet.BOTTOM,0);
        }
        constraintSet.applyTo(constraintLayout);
        searchOpen = open;
    }

    /**
     * Nasconde la tastiera che si apre dal campo di ricerca
     */
    private void hideSearchKeyboard(){
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity())
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.archive_search_view).getWindowToken(), 0);
    }
}
