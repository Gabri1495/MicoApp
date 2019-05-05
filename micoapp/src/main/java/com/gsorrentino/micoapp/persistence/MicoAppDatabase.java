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

import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;

import java.lang.ref.WeakReference;

@Database(entities = {Ritrovamento.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MicoAppDatabase extends RoomDatabase {

    public abstract  RitrovamentoDao ritrovamentoDao();

    /* Per avere un accesso atomico al campo usiamo volatile */
    private static final String DB_NAME = "mico_app.db";
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

        private final RitrovamentoDao dao;
        private final WeakReference<Context> contextRef;

        PopulateDbAsync(MicoAppDatabase db, Context context) {
            dao = db.ritrovamentoDao();
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            dao.insertAllRitrovamenti(populateData());
            dao.insertAllRitrovamenti(populateData());
            dao.insertAllRitrovamenti(populateData());
            dao.insertAllRitrovamenti(populateData());
            dao.insertAllRitrovamenti(populateData());
            dao.insertAllRitrovamenti(populateData());
            return null;
        }

        /*Questo metodo esegue già nel thread UI*/
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextRef.get();
            if(context != null) {
                Toast.makeText(context, "DB pre-popolato!\nToast italiani perché di testing.\nVerranno rimossi", Toast.LENGTH_LONG).show();
            }
        }

        private Ritrovamento[] populateData() {
            return new Ritrovamento[] {
                    new Ritrovamento(44.326838, 11.402792,
                            "Porcino",
                            new Utente("Robby", "Roberto", "Cocchi")),
                    new Ritrovamento(44.405556, 11.407175,
                            "Finferli",
                            new Utente("Gabri", "Gabriele", "Sorrentino")),
                    new Ritrovamento(46.282246, 11.436602,
                            "Fungo dell'antipatia",
                            new Utente("Gabri", "Gabriele", "Sorrentino"))
            };
        }
    }

}
