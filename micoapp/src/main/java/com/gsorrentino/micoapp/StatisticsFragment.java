package com.gsorrentino.micoapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RicevutoDao;
import com.gsorrentino.micoapp.persistence.RitrovamentoDao;
import com.gsorrentino.micoapp.util.AsyncTasks;

import java.util.Objects;


/**
 * Un {@link Fragment} usato per mostrare delle statistiche
 */
public class StatisticsFragment extends Fragment {

    private AsyncTasks.ShowStatAsync async;

    public StatisticsFragment() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
       Activity activity = Objects.requireNonNull(getActivity());
       async = new AsyncTasks.ShowStatAsync(activity);
       async.execute();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(async != null)
            async.cancel(true);
    }
}
