package com.gsorrentino.micoapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.persistence.RitrovamentoDao;
import com.gsorrentino.micoapp.util.Costanti;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemoriesWorker extends Worker {
    static final String uniqueMemoriesWorkName = "com.gsorrentino.micoapp.memories_worker";

    public MemoriesWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    private static PeriodicWorkRequest getOwnWorkRequest() {
        return new PeriodicWorkRequest.Builder(
                MemoriesWorker.class, 1, TimeUnit.DAYS).build();
    }

    static void enqueueSelf() {
        WorkManager.getInstance().enqueueUniquePeriodicWork(
                uniqueMemoriesWorkName, ExistingPeriodicWorkPolicy.KEEP, getOwnWorkRequest() );
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPrefs = getApplicationContext()
                .getSharedPreferences(Costanti.SHARED_PREFERENCES_MEMORIES, 0);
        Gson gson = new Gson();
        Calendar c = Calendar.getInstance();
        Calendar lastCheck = new GregorianCalendar();
        lastCheck.setTime(new Date(sharedPrefs.getLong(Costanti.LAST_MEMORIES, 0)));
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String jsonMemories = sharedPrefs.getString(Costanti.SAVED_MEMORIES, "[]");
        Type type = new TypeToken<List<Integer>>(){}.getType();
        List<Integer> memories = gson.fromJson(jsonMemories == null ? "[]" : jsonMemories, type);

        boolean newMemories = false;
        MicoAppDatabase db = MicoAppDatabase.getInstance(getApplicationContext(), false);
        RitrovamentoDao dao = db.ritrovamentoDao();
        List<Ritrovamento> ritrovamenti = dao.getAllRitrovamentiStatic();
        /*Controllo se oggi ho già controllato*/
        if(ritrovamenti != null && year != lastCheck.get(Calendar.YEAR)
                && month != lastCheck.get(Calendar.MONTH) && day != lastCheck.get(Calendar.DAY_OF_MONTH)){
            for(Ritrovamento r : ritrovamenti){
                if(r.data.get(Calendar.DAY_OF_MONTH) == day
                        && r.data.get(Calendar.MONTH) == month
                        && !memories.contains(r.key)){
                    memories.add(r.key);
                    newMemories = true;
                }
            }
        }

        sharedPrefs.edit()
                .putString(Costanti.SAVED_MEMORIES, gson.toJson(memories))
                .putLong(Costanti.LAST_MEMORIES, c.getTimeInMillis())
                .apply();

        if(newMemories) {
            Context context = getApplicationContext();
            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Costanti.PERMISSION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_menu_memories)
                    .setContentTitle(context.getString(R.string.notif_mem_title))
                    .setContentText(context.getString(R.string.notif_mem_desc))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notif_mem_desc)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);
            NotificationManagerCompat.from(context).notify(Costanti.MEMORIES_NOTIFICATION_ID, builder.build());
        }

        return Result.success();
    }
}
