package com.gsorrentino.micoapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.gsorrentino.micoapp.R;

import java.util.Objects;

public class ManagePermissions {


    /**
     * Controlla se il permesso di Scrittura sia concesso o meno.
     * Nel caso non lo sia provvede a richiederlo ed eventualmente mostra notifica
     * informativa sull'utilizzo che viene fatto del permesso.
     *
     * @param fragment Il frammento che dovrà ricevere la callback
     * @return true solo se il permesso è già stato concesso
     */
    public static boolean checkManageStoragePermissions(Fragment fragment) {
        Activity activity = Objects.requireNonNull(fragment.getActivity());
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            /*Permission is not granted. Should we show an explanation?*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, Costanti.PERMISSION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_menu_archive)
                        .setContentTitle(fragment.getString(R.string.permission_stor_title))
                        .setContentText(fragment.getString(R.string.permission_stor_explanation))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(fragment.getString(R.string.permission_stor_explanation)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat.from(activity).notify(Costanti.PERMISSION_STORAGE_NOTIFICATION_ID, builder.build());
            }
            /*No explanation needed; request the permission*/
            /*La risposta sarà inviata in callback a questo fragment*/
            fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Costanti.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS);
        } else {
            return true;
        }
        return false;
    }

    /**
     * Controlla se il permesso di Scrittura sia concesso o meno.
     * Nel caso non lo sia provvede a richiederlo ed eventualmente mostra notifica
     * informativa sull'utilizzo che viene fatto del permesso.
     *
     * @param activity Il frammento che dovrà ricevere la callback
     * @return true solo se il permesso è già stato concesso
     */
    public static boolean checkManageStoragePermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            /*Permission is not granted. Should we show an explanation?*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, Costanti.PERMISSION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_menu_archive)
                        .setContentTitle(activity.getString(R.string.permission_stor_title))
                        .setContentText(activity.getString(R.string.permission_stor_explanation))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(activity.getString(R.string.permission_stor_explanation)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat.from(activity).notify(Costanti.PERMISSION_STORAGE_NOTIFICATION_ID, builder.build());
            }
            /*No explanation needed; request the permission*/
            /*La risposta sarà inviata in callback a questo activity*/
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Costanti.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS);
        } else {
            return true;
        }
        return false;
    }



    /**
     * Controlla se il permesso di Localizzazione sia concesso o meno.
     * Nel caso non lo sia provvede a richiederlo ed eventualmente mostra notifica
     * informativa sull'utilizzo che viene fatto del permesso.
     * Nel caso sia invece concesso abilita il pulsante nella {@link GoogleMap} per
     * ottenere la posizione corrente
     *
     * @param fragment Il frammento che dovrà ricevere la callback
     * @param mMap La mappa nella quale abilitare eventualmente il pulsante per la
     *             localizzazione
     * @return true solo se il permesso è già stato concesso
     */
    public static boolean checkManageLocationPermissions(Fragment fragment, GoogleMap mMap) {
        Activity activity = Objects.requireNonNull(fragment.getActivity());
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            /*Permission is not granted. Should we show an explanation?*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, Costanti.PERMISSION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_menu_map)
                        .setContentTitle(fragment.getString(R.string.permission_loc_title))
                        .setContentText(fragment.getString(R.string.permission_loc_explanation))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(fragment.getString(R.string.permission_loc_explanation)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat.from(activity).notify(Costanti.PERMISSION_LOCALIZATION_NOTIFICATION_ID, builder.build());
            }
            /*No explanation needed; request the permission*/
            /*La risposta sarà inviata in callback a questo fragment*/
            fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Costanti.REQUEST_ACCESS_FINE_LOCATION_PERMISSIONS);
        } else {
            /*Permission has already been granted*/
            if(mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
            return true;
        }
        return false;
    }
}
