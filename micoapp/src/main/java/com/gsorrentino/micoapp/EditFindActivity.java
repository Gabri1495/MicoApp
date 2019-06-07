package com.gsorrentino.micoapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;
import com.gsorrentino.micoapp.util.ManagePermissions;
import com.gsorrentino.micoapp.util.Metodi;
import com.gsorrentino.micoapp.util.OnClickCustomListeners;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.gsorrentino.micoapp.util.Costanti.CREATE_MODE;
import static com.gsorrentino.micoapp.util.Costanti.EDIT_MODE;

/**
 * Gestisce la modifica o creazione di un'entità di {@link Ritrovamento}
 */
public class EditFindActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences defaultSharedPreferences;
    SharedPreferences sharedPreferences;
    private Geocoder geocoder;
    private String nickname;
    private String nome;
    private String cognome;

    /*Capire se dobbiamo modificare o creare*/
    private String currentMode;
    private Ritrovamento ritrovamento;
    private String currentPhotoPath;
    /*Per capire se ho terminato salvando e quindi evito di salvare Preference*/
    private boolean terminated = false;
    /*Per capire se ho ripristinato le Preference*/
    private boolean recovered = false;
    /*Per vedere se ho i permessi di accesso allo storage*/
    private boolean permission;
    /*Usato per capire quale Ritrovamento stia modificando*/
    private int currentKey = 0;
    /*Lista dei path di cui è stata creata una view
    * (quindi per ognuno di essi esiste una foto salvata) e di
    * quelli caricati all'inizio*/
    private List<String> photoPathsAdded = new ArrayList<>();
    private List<String> photoPathsInitial = new ArrayList<>();

    private FloatingActionButton takeFab;
    private FloatingActionButton addFab;
    private EditText fungoEditText;
    private EditText quantityEditText;
    private EditText dayEditText;
    private EditText monthEditText;
    private EditText yearEditText;
    private EditText hourEditText;
    private EditText minuteEditText;
    private EditText secondEditText;
    private EditText noteEditText;
    private EditText latEditText;
    private EditText lngEditText;
    private ViewGroup imagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_find);
        permission = ManagePermissions.checkManageStoragePermissions(this);
        findViewById(R.id.edit_save_update_button).setOnClickListener(this);
        takeFab = findViewById(R.id.edit_take_photo_fab);
        takeFab.setOnClickListener(this);
        addFab = findViewById(R.id.edit_add_photo_fab);
        addFab.setOnClickListener(this);
        managePhotoButtons();

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = getSharedPreferences(Costanti.SHARED_PREFERENCES, 0);
        geocoder = new Geocoder(this, Locale.getDefault());
        nickname = defaultSharedPreferences.getString(getString(R.string.preference_nickname), "Nemo");
        nome = defaultSharedPreferences.getString(getString(R.string.preference_name), "Nessuno");
        cognome = defaultSharedPreferences.getString(getString(R.string.preference_surname), "Nessuno");

        fungoEditText = findViewById(R.id.edit_mushroom_editText);
        quantityEditText = findViewById(R.id.edit_quantity_editText);
        dayEditText = findViewById(R.id.edit_day_editText);
        monthEditText = findViewById(R.id.edit_month_editText);
        yearEditText = findViewById(R.id.edit_year_editText);
        hourEditText = findViewById(R.id.edit_hour_editText);
        minuteEditText = findViewById(R.id.edit_minute_editText);
        secondEditText = findViewById(R.id.edit_second_editText);
        noteEditText = findViewById(R.id.edit_note_editText);
        latEditText = findViewById(R.id.edit_lat_editText);
        lngEditText = findViewById(R.id.edit_lng_editText);
        imagesContainer = findViewById(R.id.edit_find_image_container);

        /*Subito pronto per l'immssione*/
        fungoEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);

        /*Recupero eventuali dati passati con l'Intent e le uso
        * per precompilare le relative TextViews*/
        LatLng latLng = getIntent().getParcelableExtra(Costanti.INTENT_LATLNG);
        if(latLng != null)
            createMode(latLng);

        Intent intent = getIntent();
        ritrovamento = intent.getParcelableExtra(Costanti.INTENT_FIND);
        if(ritrovamento != null)
            editMode(ritrovamento);

        String jsonPhotoPathsAdded = sharedPreferences.getString(Costanti.ADDED_PATHS, "[]");
        String jsonPhotoPathsCurrent = sharedPreferences.getString(Costanti.CURRENT_PATHS, "[]");
        String jsonPhotoPathsInitial = sharedPreferences.getString(Costanti.INITIAL_PATHS, "[]");
        int restoredKey = sharedPreferences.getInt(Costanti.CURRENT_KEY, 0);
        Gson gson = new Gson();
        List<String> tmpAdded;
        List<String> tmpInit;
        Type type = new TypeToken<List<String>>(){}.getType();
        tmpAdded = gson.fromJson(jsonPhotoPathsAdded == null ? "[]" : jsonPhotoPathsAdded, type);
        tmpInit = gson.fromJson(jsonPhotoPathsInitial == null ? "[]" : jsonPhotoPathsInitial, type);
        /*Erano presenti delle immagini, chiedo se si voglia recuperare il lavoro*/
        if(restoredKey == currentKey){
            List<String> tmpCurrent;
            tmpCurrent = gson.fromJson(jsonPhotoPathsCurrent == null ? "[]" : jsonPhotoPathsCurrent, type);
            /*Procedo solo se i dati temporanei salvati denotano una modifica effettuata*/
            if(!tmpInit.containsAll(tmpAdded) || !tmpInit.containsAll(tmpCurrent)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_recover_images);
                builder.setPositiveButton(R.string.proceed, (dialog, which) -> {
                    removeImageViews();
                    photoPathsInitial = tmpInit;
                    for (String s : tmpCurrent) {
                        currentPhotoPath = s;
                        addImageItem();
                    }
                    recovered = true;
                });
                builder.setNegativeButton(R.string.undo, (dialog, which) -> {
                    cleanRecovery(tmpAdded, tmpInit);
                    dialog.cancel();
                    recovered = true;
                });
                builder.create().show();
            }
        }
        else{
            /*L'utente ha aperto un Ritrovamento diverso, si presuppone
            * non sia interessato al ripristino, oppure non c'era nulla
            * da ripristinare*/
            cleanRecovery(tmpAdded, tmpInit);
            recovered = true;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        /*Nascondo la tastiera, senza essa rimarrebbe anche senza questa Activity*/
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(fungoEditText.getWindowToken(), 0);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(!terminated && recovered) {
            Gson gson = new Gson();
            String jsonPhotoPathsCurrent = gson.toJson(getCurrentPhotoPaths());
            String jsonPhotoPathsInitial = gson.toJson(photoPathsInitial);
            String jsonPhotoPathsAdded = gson.toJson(photoPathsAdded);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Costanti.ADDED_PATHS, jsonPhotoPathsAdded);
            editor.putString(Costanti.CURRENT_PATHS, jsonPhotoPathsCurrent);
            editor.putString(Costanti.INITIAL_PATHS, jsonPhotoPathsInitial);
            editor.putInt(Costanti.CURRENT_KEY, currentKey);
            editor.apply();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.edit_take_photo_fab:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    /* Create the File where the photo should go*/
                    File photoFile = Metodi.createImageFile(ritrovamento.fungo, nickname + "_" + nome + "_" + cognome);
                    /* Save a file path for use with ACTION_VIEW intents*/
                    if (photoFile != null) {
                        currentPhotoPath = photoFile.getAbsolutePath();
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.gsorrentino.micoapp",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, Costanti.REQUEST_IMAGE_CAPTURE);
                    }
                    else{
                        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.edit_add_photo_fab:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                startActivityForResult(intent, Costanti.REQUEST_OPEN_IMAGE);
                break;

            case R.id.edit_save_update_button:
                if (!checkFields()) {
                    Toast.makeText(this, R.string.error_missing_field, Toast.LENGTH_SHORT).show();
                    return;
                }

                /*Disattivo il pulsante per non rischiare di creare una doppia entità*/
                findViewById(R.id.edit_save_update_button).setEnabled(false);

                String fungo = fungoEditText.getText().toString();
                String nota = noteEditText.getText().toString();
                int quantita = Integer.valueOf(quantityEditText.getText().toString());
                double lat = Double.valueOf(latEditText.getText().toString());
                double lng = Double.valueOf(lngEditText.getText().toString());

                List<Address> indirizzo = null;
                try {
                    indirizzo = geocoder.getFromLocation(lat, lng, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }

                switch (currentMode) {
                    case CREATE_MODE:
                        Ritrovamento newRitrovamento = new Ritrovamento(lat, lng, fungo,
                                new Utente(Objects.requireNonNull(nickname), Objects.requireNonNull(nome), Objects.requireNonNull(cognome)));
                        if (indirizzo != null && indirizzo.size() > 0)
                            newRitrovamento.indirizzo = indirizzo.get(0).getAddressLine(0);
                        newRitrovamento.quantita = quantita;
                        newRitrovamento.note = nota;
                        newRitrovamento.setPathsImmagine(getCurrentPhotoPaths());

                        new AsyncTasks.ManageFindAsync(this, newRitrovamento).execute(Costanti.INSERT);
                        break;

                    case EDIT_MODE:
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
                        formatter.setLenient(false);
                        String date = "";
                        date += dayEditText.getText().toString()
                                + monthEditText.getText().toString()
                                + yearEditText.getText().toString()
                                + hourEditText.getText().toString()
                                + minuteEditText.getText().toString()
                                + secondEditText.getText().toString();
                        try {
                            ritrovamento.data.setTime(formatter.parse(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(this, R.string.error_date, Toast.LENGTH_SHORT).show();
                            findViewById(R.id.edit_save_update_button).setEnabled(true);
                            return;
                        }
                        ritrovamento.fungo = fungo;
                        /*Evito di invocare il geocoder se le coordinate non sono state toccate*/
                        if ((ritrovamento.latitudine != lat || ritrovamento.longitudine != lng)
                                && indirizzo != null && indirizzo.size() > 0)
                            ritrovamento.indirizzo = indirizzo.get(0).getAddressLine(0);
                        ritrovamento.latitudine = lat;
                        ritrovamento.longitudine = lng;
                        ritrovamento.quantita = quantita;
                        ritrovamento.note = nota;
                        ritrovamento.setPathsImmagine(getCurrentPhotoPaths());

                        new AsyncTasks.ManageFindAsync(this, ritrovamento).execute(Costanti.UPDATE);
                        /*Solo se sto modificando un Ritrovamento ritorno il valore modificato*/
                        manageReturnValue();
                        break;
                }
                terminated = true;
                deleteUnusedPhoto();
                resetPreferences();
                finish();
        }
    }

    /**
     * Aggiunge un layout nell'apposito container per una nuova
     * immagine da mostrare. Per mostrare l'immagine utilizza
     * {@link EditFindActivity#currentPhotoPath}.
     */
    private void addImageItem() {
        /*Aggiungo gli elementi grafici per la nuova immagine*/
        View viewToAdd = getLayoutInflater().inflate(
                R.layout.edit_find_image_item,
                imagesContainer,false);
        ImageView imageView = viewToAdd.findViewById(R.id.imageView);
        imageView.setTag(currentPhotoPath);
        viewToAdd.findViewById(R.id.image_fab).setOnClickListener(new OnClickCustomListeners.
                OnClickRemoveImageListener(viewToAdd));
        imagesContainer.addView(viewToAdd);

        /*Setto la Bitmap nell'ImageView appena creata*/
        BitmapFactory.Options options = new BitmapFactory.Options();
        //TODO migliorare riduzione immagine (per esempio con ThumbnailsUtils)
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, options);
        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }

        photoPathsAdded.add(currentPhotoPath);
    }

    /**
     * Ritorna l'elenco dei path delle foto mostrate in
     * {@link EditFindActivity#imagesContainer}
     *
     * @return Lista di stringhe con i path
     */
    private List<String> getCurrentPhotoPaths(){
        List<String> currentPhotoPaths = new ArrayList<>();
        int numberChild = imagesContainer.getChildCount();
        for(int index = 0; index < numberChild; index++) {
            currentPhotoPaths.add((String)imagesContainer.getChildAt(index)
                    .findViewById(R.id.imageView).getTag());
        }
        return currentPhotoPaths;
    }

    /**
     * Rimuove graficamente tutte le anteprime delle immagini
     */
    private void removeImageViews(){
        int numberChild = imagesContainer.getChildCount();
        for(int index = numberChild - 1; index >= 0; index--) {
            imagesContainer.removeViewAt(index);
        }
    }

    /**
     * Confrontando {@link EditFindActivity#getCurrentPhotoPaths()}
     * con {@link EditFindActivity#photoPathsAdded} cancella
     * dall'archivio tutte le foto non più utilizzate e svuota
     * {@link EditFindActivity#photoPathsAdded}.
     */
    private void deleteUnusedPhoto(){
        List<String> photoPathsCurrent = getCurrentPhotoPaths();
        File toDelete;
        boolean allDeleted = true;
        for(String s : photoPathsAdded){
            if(!photoPathsCurrent.contains(s)){
                toDelete = new File(s);
                if(toDelete.exists())
                    if(!toDelete.delete())
                        allDeleted = false;
            }
        }
        photoPathsAdded.clear();
        if(!allDeleted)
            Toast.makeText(this, R.string.error_photos_undeleted, Toast.LENGTH_SHORT).show();
    }

    /**
     * Rimuove le Preferences relative ai path usati per il ripristino
     */
    private void resetPreferences(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Costanti.ADDED_PATHS);
        editor.remove(Costanti.CURRENT_PATHS);
        editor.remove(Costanti.INITIAL_PATHS);
        editor.remove(Costanti.CURRENT_KEY);
        editor.apply();
    }

    /**
     * Nel caso non si sia interessati al ripristino rimuove tutti
     * i dati temporanei utilizzati
     *
     * @param list Lista di tutti i path utilizzati
     * @param subList Lista dei path da mantenere
     */
    private void cleanRecovery(List<String> list, List<String> subList){
        List<String> toRemove = new ArrayList<>();
        for(String s : list){
            if(!subList.contains(s)){
                toRemove.add(s);
            }
        }
        if(!Metodi.deletePhoto(toRemove))
            Toast.makeText(this, R.string.error_photos_undeleted, Toast.LENGTH_SHORT).show();
        resetPreferences();
    }

    /**
     * Gestisco i valori di ritorno dopo un StartActivityForResult
     */
    private void manageReturnValue(){
        Intent data = new Intent();
        data.putExtra(Costanti.INTENT_FIND, (Parcelable) ritrovamento);
        setResult(RESULT_OK, data);
    }

    /**
     * In base a {@link EditFindActivity#permission} abilita o meno
     * i due pulsanti per aggiungere e scattare foto
     */
    private void managePhotoButtons() {
        addFab.setEnabled(permission);
        takeFab.setEnabled(permission);
    }

    /**
     * Controlla che tutti i campi di testo cruciali siano stati compilati
     *
     * @return True se tutti i campi sono stati compilati
     */
    private boolean checkFields(){
        boolean base = !fungoEditText.getText().toString().isEmpty()
                && !quantityEditText.getText().toString().isEmpty()
                && !latEditText.getText().toString().isEmpty()
                && !lngEditText.getText().toString().isEmpty();
        boolean editMode = currentMode.equals(EDIT_MODE);
        if(editMode) {
                editMode = (!dayEditText.getText().toString().isEmpty()
                    && !monthEditText.getText().toString().isEmpty()
                    && !yearEditText.getText().toString().isEmpty()
                    && !hourEditText.getText().toString().isEmpty()
                    && !minuteEditText.getText().toString().isEmpty()
                    && !secondEditText.getText().toString().isEmpty());
            return base && editMode;
        }
        return base;
    }

    /**
     * Prepara l'activity per creare un {@link Ritrovamento}
     * partendo dalle coordinate ricevute tramite Intent
     *
     * @param latLng Coordinate ricevute
     */
    private void createMode(LatLng latLng){
        currentMode = CREATE_MODE;
        currentKey = 0;
        this.setTitle(R.string.create_find);
        latEditText.setText(String.valueOf(latLng.latitude));
        lngEditText.setText(String.valueOf(latLng.longitude));
        dayEditText.setEnabled(false);
        monthEditText.setEnabled(false);
        yearEditText.setEnabled(false);
        hourEditText.setEnabled(false);
        minuteEditText.setEnabled(false);
        secondEditText.setEnabled(false);
        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.preference_show_data_toast), true)) {
            Toast.makeText(this, R.string.date_autoSet, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Prepara l'activity per modificare un {@link Ritrovamento}
     * esistente e ricevuto tramite Intent
     *
     * @param ritrovamento Ritrovamento ricevuto
     */
    private void editMode(Ritrovamento ritrovamento){
        currentMode = EDIT_MODE;
        currentKey = ritrovamento.key;
        fungoEditText.setText(ritrovamento.fungo);
        quantityEditText.setText(String.valueOf(ritrovamento.quantita));
        noteEditText.setText(ritrovamento.note);
        latEditText.setText(String.valueOf(ritrovamento.latitudine));
        lngEditText.setText(String.valueOf(ritrovamento.longitudine));
        Date date = ritrovamento.data.getTime();
        dayEditText.setText(DateFormat.format("dd", date));
        monthEditText.setText(DateFormat.format("MM", date));
        yearEditText.setText(DateFormat.format("yyyy", date));
        hourEditText.setText(DateFormat.format("HH", date));
        minuteEditText.setText(DateFormat.format("mm", date));
        secondEditText.setText(DateFormat.format("ss", date));
        StringBuilder tmpPaths = new StringBuilder();
        boolean firstDone = false;
        for(String s : ritrovamento.getPathsImmagine()){
            if(firstDone) {
                tmpPaths.append("\n\n");
            }
            tmpPaths.append(s);
            firstDone = true;
            currentPhotoPath = s;
            addImageItem();
        }
        ((TextView)findViewById(R.id.edit_path_textView)).setText(tmpPaths.toString());
        photoPathsInitial = getCurrentPhotoPaths();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Costanti.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            addImageItem();
        }

        if (requestCode == Costanti.REQUEST_OPEN_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = null;
            if (data != null) {
                imageUri = data.getData();
            }
            if(imageUri != null) {
                File fileImage = Metodi.createImageFile(ritrovamento.fungo,nickname + "_" + nome + "_" + cognome);
                if(fileImage != null) {
                    Metodi.uriToFile(getContentResolver(), imageUri, fileImage);
                    currentPhotoPath = fileImage.getAbsolutePath();
                    addImageItem();
                } else{
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
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
                permission = true;
                managePhotoButtons();
            }
        }
    }
}
