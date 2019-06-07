package com.gsorrentino.micoapp.persistence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;

import java.lang.ref.WeakReference;

import static com.gsorrentino.micoapp.util.Costanti.DB_NAME;

@Database(entities = {Ritrovamento.class, Ricevuto.class}, version = Costanti.DB_VERSION, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MicoAppDatabase extends RoomDatabase {

    public abstract  RitrovamentoDao ritrovamentoDao();
    public abstract  RicevutoDao ricevutoDao();

    /* Per avere un accesso atomico al campo usiamo volatile */
    private static volatile MicoAppDatabase INSTANCE = null;
    private static WeakReference<Context> contextRef;


    /**
     * Il database è trattato come Singleton ed il contesto tenuto come {@link WeakReference}
     *
     * @param context Contesto da utilizzare per operare sul database
     * @param memoryOnly Creare database usando {@link Room#inMemoryDatabaseBuilder(Context, Class)}
     *                   o {@link Room#databaseBuilder(Context, Class, String)}
     * @return Istanza del database
     */
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


    /**
     * Si limita a settare a null l'istanza Singleton,
     * in questa modo alla prossima richiesta verrà ricreato il database
     */
    /* Utilizzata dopo aver cancellato il db */
    public static void invalidateInstance(){
        INSTANCE = null;
    }


    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new AsyncTasks.PopulateDbAsync(INSTANCE, contextRef.get()).execute();
        }
    };
}
