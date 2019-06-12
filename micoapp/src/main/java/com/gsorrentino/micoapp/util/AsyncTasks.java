package com.gsorrentino.micoapp.util;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.gsorrentino.micoapp.MemoriesFragment;
import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RicevutoDao;
import com.gsorrentino.micoapp.persistence.RitrovamentoDao;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
        private boolean allPhotosDeleted = true;

        /**
         * {@link AsyncTask} per gestire un {@link Ritrovamento}.
         * I parametri passati durante {@link AsyncTask#execute(Object[])} permettono
         * di effettuare un {@link Costanti#INSERT},
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
                        allPhotosDeleted= Metodi.deletePhoto(find.getPathsImmagine());
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
                if(!allPhotosDeleted)
                    Toast.makeText(context, R.string.error_photos_undeleted, Toast.LENGTH_SHORT).show();
                else if(!error)
                    Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, R.string.error_find_already_saved, Toast.LENGTH_LONG).show();
            }
        }
    }



    public static class GetFindsAsync extends AsyncTask<Void, Void, List<Ritrovamento>> {

        private MemoriesFragment fragment;
        private List<Integer> memories;

        /**
         * {@link AsyncTask} per recuperare una lista di {@link Ritrovamento}.
         *
         * @param fragment Utilizzato per sfruttarne il contesto e fornire la
         *                 lista di ritorno
         * @param memories Elenco delle key dei {@link Ritrovamento}
         *                 da recuperare
         */
        public GetFindsAsync(MemoriesFragment fragment, List<Integer> memories) {
            this.fragment = fragment;
            this.memories = memories;
        }

        @Override
        protected List<Ritrovamento> doInBackground(Void... params) {
            MicoAppDatabase db = MicoAppDatabase.getInstance(fragment.getActivity(), false);
            RitrovamentoDao dao = db.ritrovamentoDao();
            List<Ritrovamento> memFinds = new ArrayList<>();
            Ritrovamento tmp;

            for(Integer i : memories) {
                try {
                    tmp = dao.getRitrovamento(i);
                    if(tmp != null)
                        memFinds.add(tmp);
                    if(isCancelled())
                        return null;
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            return memFinds;
        }

        @Override
        protected void onPostExecute(List<Ritrovamento> memFinds) {
            if(fragment != null)
                fragment.setRitrovamenti(memFinds);
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



    public static class ImportFindAsync extends AsyncTask<Void, Void, Void> {

        private MicoAppDatabase db;
        private final WeakReference<Context> contextRef;
        private Ritrovamento find;
        /* 1 indica il successo dell'operazione
        *  2 indica l'aggiunta del Ritrovamento e l'aggiornamento del Ricevuto
         * 3 indica il fallimento dell'operazione */
        private int resultCode;

        /**
         * {@link AsyncTask} per importare un {@link Ritrovamento}
         *  dopo aver adeguatamente controllato il database.
         *
         * @param context Usato per recuperare istanza DB e mostrare Toast
         * @param find Elemento da importare
         */
        ImportFindAsync(Context context, Ritrovamento find) {
            this.find = find;
            contextRef = new WeakReference<>(context);
            db = MicoAppDatabase.getInstance(context, false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            resultCode = 0;
            Ricevuto received = new Ricevuto(find.data, find.fungo, find.autore);

            Ritrovamento findInDB = db.ritrovamentoDao().getRitrovamento(find.autore.nickname, find.autore.nome,
                    find.autore.cognome, find.data);
            Ricevuto receivedInDB = db.ricevutoDao().getRicevuto(received.autore.nickname, received.autore.nome,
                    received.autore.cognome, received.data);

            try {
                /*Nessun elemento nel db, aggiungo normalmente*/
                if (findInDB == null && receivedInDB == null) {
                    db.ricevutoDao().insertRicevuto(received);
                    db.ritrovamentoDao().insertRitrovamento(find);
                    resultCode = 1;
                }
                /*Ritrovamento non presente, ma Ricevuto sì,
                 * aggiorno Ricevuto e aggiungo Ritrovamento*/
                else if (findInDB == null) {
                    received.key = receivedInDB.key;
                    db.ricevutoDao().updateRicevuto(received);
                    db.ritrovamentoDao().insertRitrovamento(find);
                    resultCode = 2;
                }
                /*In qualsiasi altro caso non faccio nulla*/
                else {
                    resultCode = 3;
                }
            }catch(SQLiteConstraintException ignored){}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextRef.get();
            if(context != null) {
                switch(resultCode){
                    case 1:
                        Toast.makeText(context, R.string.success_operation, Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(context, R.string.update_import, Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(context, R.string.error_import_fail, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    /**
     * {@link AsyncTask} per generare le statistiche dal database e mostrarle nella ui
     */
    public static class ShowStatAsync extends AsyncTask<Void, Void, List<String>>{

        private final WeakReference<Activity> activityRef;
        private MicoAppDatabase db;

        public ShowStatAsync(Activity activity){
            activityRef = new WeakReference<>(activity);
            db = MicoAppDatabase.getInstance(activity, false);
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> results = new ArrayList<>();
            RitrovamentoDao findDao = db.ritrovamentoDao();
            RicevutoDao receivedDao = db.ricevutoDao();

            /*Conto Ritrovamenti*/
            results.add(String.valueOf(findDao.countRitrovamenti()));
            /*Conto foto*/
            List<Ritrovamento> finds = findDao.getAllRitrovamentiStatic();
            int numberPhoto = 0;
            int maxPhoto = 0;
            int maxQuantity = 0;
            String morePhoto = "";
            String moreQuantity = "";
            int tmpPhoto;
            int tmpQuantity;
            for(Ritrovamento r : finds) {
                tmpPhoto = r.getPathsImmagine().size();
                numberPhoto += tmpPhoto;
                if(tmpPhoto > maxPhoto) {
                    morePhoto = r.fungo + " (" + tmpPhoto + ")";
                    maxPhoto = tmpPhoto;
                }
                tmpQuantity = r.quantita;
                if(tmpQuantity > maxQuantity){
                    moreQuantity = r.fungo + " (" + tmpQuantity + ")";
                    maxQuantity = tmpQuantity;
                }
            }
            results.add(String.valueOf(numberPhoto));
            /*Ritrovamento con maggior foto*/
            results.add(morePhoto);
            /*Ritrovamento con maggior quantità*/
            results.add(moreQuantity);
            /*Conto di quanti diversi utenti ho Ritrovamenti*/
            results.add(String.valueOf(findDao.getUtentiRitrovamenti().size()));
            /*Conto Ricevuti*/
            results.add(String.valueOf(receivedDao.countRicevuti()));
            /*Conto da quanti diversi utenti ho Ricevuti*/
            results.add(String.valueOf(receivedDao.getUtentiRicevuti().size()));

            return results;
        }

        @Override
        protected void onPostExecute(List<String> results){
            Activity activity = activityRef.get();
            if(activity!=null){
                int i = 0;
                ((TextView) activity.findViewById(R.id.stat_finds_textView_result))
                        .setText(results.get(i++));
                ((TextView) activity.findViewById(R.id.stat_photo_textView_result))
                        .setText(results.get(i++));
                ((TextView) activity.findViewById(R.id.stat_more_photo_textView_result))
                        .setText(results.get(i++));
                ((TextView) activity.findViewById(R.id.stat_more_quantity_textView_result))
                        .setText(results.get(i++));
                ((TextView) activity.findViewById(R.id.stat_users_find_textView_result))
                        .setText(results.get(i++));
                ((TextView) activity.findViewById(R.id.stat_received_textView_result))
                        .setText(results.get(i++));
                ((TextView) activity.findViewById(R.id.stat_users_received_textView_result))
                        .setText(results.get(i));
            }
        }
    }
}