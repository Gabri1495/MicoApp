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
    private LiveData<List<Ritrovamento>> allRitrovamentiTimeDec;
    private LiveData<List<Ritrovamento>> allRitrovamentiFungoAsc;
    private LiveData<List<Ritrovamento>> allRitrovamentiNicknameAsc;

    public RitrovamentoViewModel(Application application) {
        super(application);
        repository = new MicoAppRepository(application);
        allRitrovamenti = repository.getAllRitrovamenti();
        allRitrovamentiTimeDec = repository.getAllRitrovamentiTimeDec();
        allRitrovamentiFungoAsc = repository.getAllRitrovamentiFungoAsc();
        allRitrovamentiNicknameAsc = repository.getAllRitrovamentiNicknameAsc();
    }

    public LiveData<List<Ritrovamento>> getAllRitrovamenti() {
        return allRitrovamenti;
    }

    public LiveData<List<Ritrovamento>> getAllRitrovamentiTimeDec() {
        return allRitrovamentiTimeDec;
    }

    public LiveData<List<Ritrovamento>> getAllRitrovamentiFungoAsc() {
        return allRitrovamentiFungoAsc;
    }

    public LiveData<List<Ritrovamento>> getAllRitrovamentiNicknameAsc() {
        return allRitrovamentiNicknameAsc;
    }

    public LiveData<List<Ritrovamento>> getAllRitrovamentiLuogoSearch(String luogo) {
        return repository.getAllRitrovamentiLuogoSearch(luogo);
    }

    public void insert (Ritrovamento ritrovamento) {
        repository.insertRitrovamento(ritrovamento);
    }
}
