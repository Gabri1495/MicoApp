package com.gsorrentino.micoapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.gsorrentino.micoapp.util.Costanti;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * Un {@link Fragment} che mostra lo stato del {@link androidx.work.Worker}
 * incaricato di generare i Ricordi
 */
public class HomeFragment extends Fragment {

    private SharedPreferences sharedPrefs;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        sharedPrefs = Objects.requireNonNull(getActivity())
                .getSharedPreferences(Costanti.SHARED_PREFERENCES_MEMORIES, 0);
        updateGraphics(null);

        Objects.requireNonNull(getActivity()).findViewById(R.id.home_delete_button).setOnClickListener(v -> {
            WorkManager.getInstance().cancelUniqueWork(MemoriesWorker.uniqueMemoriesWorkName);
            sharedPrefs.edit().remove(Costanti.LAST_MEMORIES).apply();
            updateGraphics(null);
        });

        WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(MemoriesWorker.uniqueMemoriesWorkName)
                .observe(this, this::updateGraphics);

    }

    private void updateGraphics(List<WorkInfo> workInfo){
        if(workInfo != null)
                ((TextView) Objects.requireNonNull(getActivity()).findViewById(R.id.home_state_textView))
                        .setText(workInfo.get(0).getState().toString());

        long time = sharedPrefs.getLong(Costanti.LAST_MEMORIES, 0);
        String lastRun;
        if(time == 0)
            lastRun = getString(R.string.never);
        else {
            lastRun = dateFormat.format(new Date(time));
        }
        ((TextView) Objects.requireNonNull(getActivity()).findViewById(R.id.home_last_run_textView))
                .setText(lastRun);
    }
}
