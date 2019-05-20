package com.gsorrentino.micoapp.persistence;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;

import java.lang.ref.WeakReference;
import java.util.List;

class MicoAppRepository {

    private WeakReference<Application> application;
    private RicevutoDao daoReceived;
    private RitrovamentoDao daoFind;

    MicoAppRepository(Application application) {
        MicoAppDatabase db = MicoAppDatabase.getInstance(application, false);
        daoFind = db.ritrovamentoDao();
        daoReceived = db.ricevutoDao();
        this.application = new WeakReference<>(application);
    }

    LiveData<List<Ritrovamento>> getAllRitrovamenti() {
        return daoFind.getAllRitrovamenti();
    }

    LiveData<List<Ritrovamento>> getAllRitrovamentiTimeDec() {
        return daoFind.getAllRitrovamentiTimeDec();
    }

    LiveData<List<Ritrovamento>> getAllRitrovamentiFungoAsc() {
        return daoFind.getAllRitrovamentiFungoAsc();
    }

     /*You must call this on a non-UI thread or your app will crash.
     Like this, Room ensures that you're not doing any long running operations on the main
     thread, blocking the UI.*/
    void insertRitrovamento(Ritrovamento ritrovamento) {
        Application context = application.get();
        if(context != null) {
            new AsyncTasks.ManageFindAsync(context, ritrovamento).execute(Costanti.INSERT);
        }
    }

    LiveData<List<Ricevuto>> getAllRicevuti() {
        return daoReceived.getAllRicevuti();
    }

    void insertRicevuto(Ricevuto ricevuto) {
        Application context = application.get();
        if(context != null) {
            new AsyncTasks.ManageReceivedAsync(context, ricevuto).execute(Costanti.INSERT);
        }
    }
}
