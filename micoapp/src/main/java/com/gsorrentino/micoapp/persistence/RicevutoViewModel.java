package com.gsorrentino.micoapp.persistence;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gsorrentino.micoapp.model.Ricevuto;

import java.util.List;

public class RicevutoViewModel extends AndroidViewModel {
    private MicoAppRepository repository;
    private LiveData<List<Ricevuto>> allRicevuti;
    private LiveData<List<Ricevuto>> allRicevutiFungoAsc;
    private LiveData<List<Ricevuto>> allRicevutiTimeDec;
    private LiveData<List<Ricevuto>> allRicevutiTimeReceivedDec;
    private LiveData<List<Ricevuto>> allRicevutiUserAsc;

    public RicevutoViewModel(Application application) {
        super(application);
        repository = new MicoAppRepository(application);
        allRicevuti = repository.getAllRicevuti();
        allRicevutiFungoAsc = repository.getAllRicevutiFungoAsc();
        allRicevutiTimeDec = repository.getAllRicevutiTimeDec();
        allRicevutiTimeReceivedDec = repository.getAllRicevutiTimeReceivedDec();
        allRicevutiUserAsc = repository.getAllRicevutiUserAsc();
    }

    public LiveData<List<Ricevuto>> getAllRicevuti() {
        return allRicevuti;
    }

    public LiveData<List<Ricevuto>> getAllRicevutiFungoAsc() {
        return allRicevutiFungoAsc;
    }

    public LiveData<List<Ricevuto>> getAllRicevutiTimeDec() {
        return allRicevutiTimeDec;
    }

    public LiveData<List<Ricevuto>> getAllRicevutiTimeReceivedDec() {
        return allRicevutiTimeReceivedDec;
    }

    public LiveData<List<Ricevuto>> getAllRicevutiUserAsc() {
        return allRicevutiUserAsc;
    }

    public void insert (Ricevuto ricevuto) {
        repository.insertRicevuto(ricevuto);
    }
}
