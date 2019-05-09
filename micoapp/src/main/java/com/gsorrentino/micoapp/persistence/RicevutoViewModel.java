package com.gsorrentino.micoapp.persistence;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gsorrentino.micoapp.model.Ricevuto;

import java.util.List;

public class RicevutoViewModel extends AndroidViewModel {
    private MicoAppRepository repository;
    private LiveData<List<Ricevuto>> allRicevuti;

    public RicevutoViewModel(Application application) {
        super(application);
        repository = new MicoAppRepository(application);
        allRicevuti = repository.getAllRicevuti();
    }

    public LiveData<List<Ricevuto>> getAllRicevuti() {
        return allRicevuti;
    }

    public void insert (Ricevuto ricevuto) {
        repository.insertRicevuto(ricevuto);
    }
}
