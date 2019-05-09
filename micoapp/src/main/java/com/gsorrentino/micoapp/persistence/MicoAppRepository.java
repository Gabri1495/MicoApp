package com.gsorrentino.micoapp.persistence;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.model.Ritrovamento;

import java.util.List;

class MicoAppRepository {

    private RitrovamentoDao ritrovamentoDao;
    private RicevutoDao ricevutoDao;
    private LiveData<List<Ritrovamento>> allRitrovamenti;
    private LiveData<List<Ricevuto>> allRicevuti;

    MicoAppRepository(Application application) {
        MicoAppDatabase db = MicoAppDatabase.getInstance(application, false);
        ritrovamentoDao = db.ritrovamentoDao();
        allRitrovamenti = ritrovamentoDao.getAllRitrovamenti();
        ricevutoDao = db.ricevutoDao();
        allRicevuti = ricevutoDao.getAllRicevuti();
    }

    LiveData<List<Ritrovamento>> getAllRitrovamenti() {
        return allRitrovamenti;
    }

     /*You must call this on a non-UI thread or your app will crash.
     Like this, Room ensures that you're not doing any long running operations on the main
     thread, blocking the UI.*/
    void insertRitrovamento(Ritrovamento ritrovamento) {
        new insertRitrovamentoAsyncTask(ritrovamentoDao).execute(ritrovamento);
    }

    LiveData<List<Ricevuto>> getAllRicevuti() {
        return allRicevuti;
    }

    void insertRicevuto(Ricevuto ricevuto) {
        new insertRicevutoAsyncTask(ricevutoDao).execute(ricevuto);
    }

    private static class insertRitrovamentoAsyncTask extends AsyncTask<Ritrovamento, Void, Void> {

        private RitrovamentoDao mAsyncTaskDao;

        insertRitrovamentoAsyncTask(RitrovamentoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Ritrovamento... params) {
            mAsyncTaskDao.insertRitrovamento(params[0]);
            return null;
        }
    }


    private static class insertRicevutoAsyncTask extends AsyncTask<Ricevuto, Void, Void> {

        private RicevutoDao mAsyncTaskDao;

        insertRicevutoAsyncTask(RicevutoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Ricevuto... params) {
            mAsyncTaskDao.insertRicevuto(params[0]);
            return null;
        }
    }
}
