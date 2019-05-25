package com.gsorrentino.micoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.gsorrentino.micoapp.util.Costanti;
import com.gsorrentino.micoapp.util.ImportExportRitrovamentoManager;

import java.util.Objects;


/**
 * Un {@link Fragment} usato per importare dei
 * {@link com.gsorrentino.micoapp.model.Ritrovamento} da file.
 */
public class ReceiveFragment extends Fragment {

    public ReceiveFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkManageStoragePermissions();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).findViewById(R.id.receive_select_file)
                .setOnClickListener(v -> {
                    /*ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser*/
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                     /*Filter to only show results that can be "opened"*/
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                     /*Filter to show all files with .micoapp extension*/
                    intent.setType("*/*");

                    startActivityForResult(intent, Costanti.REQUEST_READ_FILE);
                });
    }

    /**
     * Controlla se il permesso di Scrittura sia concesso o meno.
     * Nel caso non lo sia provvede a richiederlo ed eventualmente mostra notifica
     * informativa sull'utilizzo che viene fatto del permesso.
     */
    private void checkManageStoragePermissions() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            /*Permission is not granted. Should we show an explanation?*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), Costanti.PERMISSION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_menu_archive)
                        .setContentTitle(getString(R.string.permission_stor_title))
                        .setContentText(getString(R.string.permission_stor_explanation))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.permission_stor_explanation)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat.from(getActivity()).notify(Costanti.PERMISSION_STORAGE_NOTIFICATION_ID, builder.build());
            }
            /*No explanation needed; request the permission*/
            /*La risposta sarà inviata in callback a questo fragment*/
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Costanti.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == Costanti.REQUEST_READ_FILE && resultCode == Activity.RESULT_OK) {
            /*The document selected by the user won't be returned in the intent.
             Instead, a URI to that document will be contained in the return intent
             provided to this method as a parameter.
             Pull that URI using resultData.getData().*/
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
            }
            if (uri != null) {
                ImportExportRitrovamentoManager loader = new ImportExportRitrovamentoManager();
                loader.importRitrovamento(getActivity(), uri);
            }
        }
    }
}
