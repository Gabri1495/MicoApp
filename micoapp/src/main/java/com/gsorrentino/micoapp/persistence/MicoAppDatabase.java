package com.gsorrentino.micoapp.persistence;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;

import java.util.concurrent.Executors;

@Database(entities = {Ritrovamento.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MicoAppDatabase extends RoomDatabase {

    public abstract  RitrovamentoDao ritrovamentoDao();

    private static final String DB_NAME = "mico_app.db";
    private static volatile MicoAppDatabase INSTANCE = null;

    public synchronized static MicoAppDatabase getInstance (final Context context, boolean memoryOnly) {
        if (INSTANCE == null) {
            INSTANCE = create (context, memoryOnly);
        }
        return INSTANCE;
    }

    private static MicoAppDatabase create (final Context context, boolean memoryOnly) {
        RoomDatabase.Builder<MicoAppDatabase> db;
        if (memoryOnly) {
            db = Room.inMemoryDatabaseBuilder(context.getApplicationContext(), MicoAppDatabase.class);
        }
        else {
            db = Room.databaseBuilder(context.getApplicationContext(),
                    MicoAppDatabase.class, DB_NAME);
        }
        //TODO Controllare che venga invocata la Callback
        db.addCallback(new Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        getInstance(context, false).ritrovamentoDao().insertAllRitrovamenti(Ritrovamento.populateData());
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "DB pre-popolato", Toast.LENGTH_LONG).show();
                                Toast.makeText(context, "Toast italiani perché di testing. Verranno rimossi", Toast.LENGTH_LONG).show();
                            }
                        });
                        //TODO Iniziare a gestire le notifiche
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Creazione DB")
//                                        .setContentTitle("Creazione del Database")
//                                        .setContentText("Essendo stato avviato per la prima volta il DB è stato pre-popolato")
//                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                                NotificationManagerCompat.from(context).notify(1, builder.build());
//                            }
//                        });
                    }
                });
            }
        });
        return (db.build());
    }
}
