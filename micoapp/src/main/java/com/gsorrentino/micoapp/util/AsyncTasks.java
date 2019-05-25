package com.gsorrentino.micoapp.util;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.widget.Toast;

import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RicevutoDao;
import com.gsorrentino.micoapp.persistence.RitrovamentoDao;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Classe container per classi statiche di {@link AsyncTask},
 * la maggior parte delle quali usate per gestire il database
 */
public class AsyncTasks {


    public static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final RitrovamentoDao daoF;
        private final RicevutoDao daoR;
        private final WeakReference<Context> contextRef;
        private boolean error = false;

        private Utente robby = new Utente("Robby", "Roberto", "Cocchi");
        private Utente gabri = new Utente("Gabri", "Gabriele", "Sorrentino");

        /**
         * {@link AsyncTask} per prepopolare il database {@link MicoAppDatabase}
         * con dei {@link Ritrovamento} e {@link Ricevuto}
         */
        public PopulateDbAsync(MicoAppDatabase db, Context context) {
            daoF = db.ritrovamentoDao();
            daoR = db.ricevutoDao();
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                daoF.insertAllRitrovamenti(populateDataFinds());
                daoR.insertAllRicevuti(populateDataReceived());
            }catch(Exception e){
                error = true;
            }
            return null;
        }

        /*Questo metodo esegue già nel thread UI*/
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextRef.get();
            if(context != null) {
                if(error)
                    Toast.makeText(context, R.string.error_insert, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(context, R.string.db_populated, Toast.LENGTH_LONG).show();
            }
        }

        private Ritrovamento[] populateDataFinds() {
            return new Ritrovamento[] {
                    new Ritrovamento(44.326838, 11.402792,
                            "Porcino", robby),
                    new Ritrovamento(44.405556, 11.407175,
                            "Finferli", gabri),
                    new Ritrovamento(46.282246, 11.436602,
                            "Fungo dell'antipatia", new Utente("Pino", "Pino", "Smeraldino"))
            };
        }

        private Ricevuto[] populateDataReceived() {
            Calendar date1 = new GregorianCalendar();
            date1.set(2000, 7, 15, 9, 24, 35);
            Calendar date2 = new GregorianCalendar();
            date2.set(2015, 6, 9, 12, 56, 54);
            Calendar date3 = new GregorianCalendar();
            date3.set(2018, 9, 22, 8, 1, 25);

            return new Ricevuto[] {
                    new Ricevuto(date1,"Porcino", robby),
                    new Ricevuto(date2, "Finferli", gabri),
                    new Ricevuto(date3, "Fungo della simpatia", robby)
            };
        }
    }



    public static class ManageFindAsync extends AsyncTask<String, Void, Void> {

        private MicoAppDatabase db;
        private Ritrovamento find;
        private final WeakReference<Context> contextRef;
        private boolean error = false;

        /**
         * {@link AsyncTask} per gestire un {@link Ritrovamento}.
         * I parametri passati durante {@link AsyncTask#execute(Object[])} permettono di effettuare un {@link Costanti#INSERT},
         * {@link Costanti#UPDATE}, oppure {@link Costanti#DELETE}
         *
         * @param context Usato per recuperare istanza DB e mostrare Toast
         * @param find Elemento da gestire
         */
        public ManageFindAsync(Context context, Ritrovamento find) {
            this.find = find;
            contextRef = new WeakReference<>(context);
            db = MicoAppDatabase.getInstance(context, false);
        }

        @Override
        protected Void doInBackground(final String... mode) {
            RitrovamentoDao dao = db.ritrovamentoDao();
            try {
                switch (mode[0]) {
                    case Costanti.INSERT:
                        dao.insertRitrovamento(find);
                        break;
                    case Costanti.UPDATE:
                        dao.updateRitrovamento(find);
                        break;
                    case Costanti.DELETE:
                        dao.deleteRitrovamento(find);
                        break;
                }
            }catch (SQLiteConstraintException e){
                error = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextRef.get();
            if(context != null) {
                if(!error)
                    Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, R.string.error_find_already_saved, Toast.LENGTH_LONG).show();
            }
        }
    }



    public static class ManageReceivedAsync extends AsyncTask<String, Void, Void> {

        private MicoAppDatabase db;
        private Ricevuto received;
        private final WeakReference<Context> contextRef;
        private boolean error = false;

        /**
         * {@link AsyncTask} per gestire un {@link Ricevuto}.
         * I parametri passati durante {@link AsyncTask#execute(Object[])}
         * permettono di effettuare un {@link Costanti#INSERT},
         * {@link Costanti#UPDATE}, oppure {@link Costanti#DELETE}
         *
         * @param context Usato per recuperare istanza DB e mostrare Toast
         * @param received Elemento da gestire
         */
        public ManageReceivedAsync(Context context, Ricevuto received) {
            this.received = received;
            contextRef = new WeakReference<>(context);
            db = MicoAppDatabase.getInstance(context, false);
        }

        @Override
        protected Void doInBackground(final String... mode) {
            RicevutoDao dao = db.ricevutoDao();
            try {
                switch (mode[0]) {
                    case Costanti.INSERT:
                        dao.insertRicevuto(received);
                        break;
                    case Costanti.UPDATE:
                        dao.updateRicevuto(received);
                        break;
                    case Costanti.DELETE:
                        dao.deleteRicevuto(received);
                        break;
                }
            }catch (SQLiteConstraintException e){
                error = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextRef.get();
            if(context != null) {
                if(!error)
                    Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, R.string.error_received_already_saved, Toast.LENGTH_LONG).show();
            }
        }
    }



    public static class DeleteAllDbAsync extends AsyncTask<String, Void, Void> {

        private MicoAppDatabase db;
        private final WeakReference<Context> contextRef;

        /**
         * {@link AsyncTask} usato per rimuovere dal database
         * tutti i {@link Ritrovamento} o {@link Ricevuto}
         * in base al parametro passato a {@link AsyncTask#execute(Object[])}
         * ({@link Costanti#REMOVE_FINDS} o {@link Costanti#REMOVE_RECEIVED})
         *
         * @param context Usato per recuperare istanza DB e mostrare Toast
         */
        public DeleteAllDbAsync(Context context) {
            contextRef = new WeakReference<>(context);
            db = MicoAppDatabase.getInstance(context, false);
        }

        @Override
        protected Void doInBackground(final String... mode) {
            switch(mode[0]){
                case Costanti.REMOVE_FINDS:
                    db.ritrovamentoDao().deleteAll();
                    break;
                case Costanti.REMOVE_RECEIVED:
                    db.ricevutoDao().deleteAll();
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextRef.get();
            if(context != null) {
                Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
            }
        }
    }
}