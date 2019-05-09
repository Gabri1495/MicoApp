package com.gsorrentino.micoapp.persistence;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Database(entities = {Ritrovamento.class, Ricevuto.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MicoAppDatabase extends RoomDatabase {

    public abstract  RitrovamentoDao ritrovamentoDao();
    public abstract  RicevutoDao ricevutoDao();

    /* Per avere un accesso atomico al campo usiamo volatile */
    public static final String DB_NAME = "mico_app.db";
    private static volatile MicoAppDatabase INSTANCE = null;
    private static WeakReference<Context> contextRef;

    /* Il lock viene applicato alla classe e NON all'istanza (come avverrebbe senza static) */
    public static synchronized MicoAppDatabase getInstance (final Context context, boolean memoryOnly) {
        if (INSTANCE == null) {
            contextRef = new WeakReference<>(context);
            INSTANCE = create (context, memoryOnly);
        }
        return INSTANCE;
    }

    /* Utilizzata dopo aver cancellato il db */
    public static void invalidateInstance(){
        INSTANCE = null;
    }

    private static MicoAppDatabase create (final Context context, boolean memoryOnly) {
        RoomDatabase.Builder<MicoAppDatabase> db;
        if (memoryOnly) {
            db = Room.inMemoryDatabaseBuilder(context.getApplicationContext(), MicoAppDatabase.class);
        } else {
            db = Room.databaseBuilder(context.getApplicationContext(),
                    MicoAppDatabase.class, DB_NAME);
        }
        db.addCallback(roomDatabaseCallback);
        return (db.build());
    }

    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsync(INSTANCE, contextRef.get()).execute();
        }
    };


    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final RitrovamentoDao daoF;
        private final RicevutoDao daoR;
        private final WeakReference<Context> contextRef;
        private boolean error = false;

        private Utente robby = new Utente("Robby", "Roberto", "Cocchi");
        private Utente gabri = new Utente("Gabri", "Gabriele", "Sorrentino");

        PopulateDbAsync(MicoAppDatabase db, Context context) {
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
                // TODO Trasporta questo catch dove inserirai cose
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

}
