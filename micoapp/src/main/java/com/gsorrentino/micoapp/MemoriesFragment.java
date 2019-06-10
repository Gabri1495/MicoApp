package com.gsorrentino.micoapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gsorrentino.micoapp.adapter.MemorieListAdapter;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Un {@link Fragment} per mostrare l'elenco di memorie,
 * ovvero funghi precedentemente trovati
 */
public class MemoriesFragment extends Fragment {

    private List<Integer> memories;
    private List<Ritrovamento> finds = new ArrayList<>();
    private MemorieListAdapter adapter;
    private AsyncTasks.GetFindsAsync async;
    private Gson gson = new Gson();
    private SharedPreferences sharedPrefs;
    /*Usato per capire se serva o meno ricaricare i Ritrovamenti sulla base delle memorie.
    * Inizializzo a true per evitare un doppio popolamento all'avvio*/
    private boolean findsUpdated = true;

    public MemoriesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Costanti.SHARED_PREFERENCES_MEMORIES, 0);
        String jsonMemories = sharedPrefs.getString(Costanti.SAVED_MEMORIES, "[]");
        Type type = new TypeToken<List<Integer>>(){}.getType();
        memories = gson.fromJson(jsonMemories == null ? "[]" : jsonMemories, type);

        RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.memories_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);
        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        adapter = new MemorieListAdapter(this);
        recyclerView.setAdapter(adapter);

        updateFinds();
    }

    @Override
    public void onStart(){
        super.onStart();
        if(!findsUpdated)
            updateFinds();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        async.cancel(true);
    }

    /**
     * Basandosi sulle Memorie recupera i Ritrovamenti per mostrarli
     */
    private void updateFinds(){
        async = new AsyncTasks.GetFindsAsync(this, memories);
        async.execute();
    }

    /**
     * Fa sapere a {@link MemoriesFragment} che al prossimo {@link MemoriesFragment#onStart()}
     * sarà necessario aggiornare la lista dei Ritrovamenti mostrati
     */
    public void findsMustBeUpdated(){
        findsUpdated = false;
    }

    /**
     * Se {@link MemoriesFragment#adapter} è diverso da null
     * vi imposta la lista dei Ritrovamenti da mostrare
     *
     * @param ritrovamenti Ritrovamenti da visualizzare
     */
    public void setRitrovamenti(List<Ritrovamento> ritrovamenti){
        finds = ritrovamenti;
        if(adapter != null)
            adapter.setRitrovamenti(ritrovamenti);
        findsUpdated = true;
    }

    /**
     * Rimuove una memoria dall'elenco salvato nelle SharedPreferences
     * e aggiorna l'elenco dei Ritrovamenti mostrati
     *
     * @param find Ritrovamento tolto dalle memorie
     */
    public void removeMemory(Ritrovamento find){
        memories.remove((Integer)find.key);
        String jsonMemories = gson.toJson(memories);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(Costanti.SAVED_MEMORIES, jsonMemories);
        editor.apply();
        finds.remove(find);
        setRitrovamenti(finds);
    }
}
