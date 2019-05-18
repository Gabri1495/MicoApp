package com.gsorrentino.micoapp.persistence;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gsorrentino.micoapp.model.Ritrovamento;

import java.util.List;

/* Using LiveData and caching what getAllRitrovamenti returns has several benefits:
 - We can put an observer on the data (instead of polling for changes) and only update the
   the UI when the data actually changes.
 - Repository is completely separated from the UI through the ViewModel.*/

public class RitrovamentoViewModel extends AndroidViewModel {

    private MicoAppRepository repository;
    private LiveData<List<Ritrovamento>> allRitrovamenti;

    public RitrovamentoViewModel(Application application) {
        super(application);
        repository = new MicoAppRepository(application);
        allRitrovamenti = repository.getAllRitrovamenti();
    }

    public LiveData<List<Ritrovamento>> getAllRitrovamenti() {
        return allRitrovamenti;
    }

    public void insert (Ritrovamento ritrovamento) {
        repository.insertRitrovamento(ritrovamento);
    }
}
