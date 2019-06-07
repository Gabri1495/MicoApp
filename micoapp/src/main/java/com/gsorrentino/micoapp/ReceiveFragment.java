package com.gsorrentino.micoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.gsorrentino.micoapp.util.Costanti;
import com.gsorrentino.micoapp.util.ImportExportRitrovamentoManager;
import com.gsorrentino.micoapp.util.ManagePermissions;

import java.util.Objects;


/**
 * Un {@link Fragment} usato per importare dei
 * {@link com.gsorrentino.micoapp.model.Ritrovamento} da file.
 */
public class ReceiveFragment extends Fragment {

    private Button receiveButton;

    public ReceiveFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        receiveButton = Objects.requireNonNull(getActivity()).findViewById(R.id.receive_select_file);
        receiveButton.setEnabled(ManagePermissions.checkManageStoragePermissions(this));
        receiveButton.setOnClickListener(v -> {
            /*ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser*/
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            /*Filter to only show results that can be "opened"*/
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            /*Filter to show all files with .micoapp extension*/
            intent.setType("*/*");

            startActivityForResult(intent, Costanti.REQUEST_READ_FILE);
        });
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


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*If request is cancelled, the result arrays are empty.*/
        if (requestCode == Costanti.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                receiveButton.setEnabled(true);
            }
        }
    }
}
