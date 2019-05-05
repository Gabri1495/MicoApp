package com.gsorrentino.micoapp.persistence;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.gsorrentino.micoapp.model.Ritrovamento;

import java.util.List;

public class MicoAppRepository {

    private RitrovamentoDao ritrovamentoDao;
    private LiveData<List<Ritrovamento>> allRitrovamenti;

    MicoAppRepository(Application application) {
        MicoAppDatabase db = MicoAppDatabase.getInstance(application, false);
        ritrovamentoDao = db.ritrovamentoDao();
        allRitrovamenti = ritrovamentoDao.getAllRitrovamenti();
    }

    public LiveData<List<Ritrovamento>> getAllRitrovamenti() {
        return allRitrovamenti;
    }

     /*You must call this on a non-UI thread or your app will crash.
     Like this, Room ensures that you're not doing any long running operations on the main
     thread, blocking the UI.*/
    public void insert (Ritrovamento ritrovamento) {
        new insertAsyncTask(ritrovamentoDao).execute(ritrovamento);
    }


    private static class insertAsyncTask extends AsyncTask<Ritrovamento, Void, Void> {

        private RitrovamentoDao mAsyncTaskDao;

        insertAsyncTask(RitrovamentoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Ritrovamento... params) {
            mAsyncTaskDao.insertRitrovamento(params[0]);
            return null;
        }
    }
}
