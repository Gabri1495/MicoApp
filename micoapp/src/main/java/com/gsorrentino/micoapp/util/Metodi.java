package com.gsorrentino.micoapp.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class Metodi {


    /**
     * Genera un file per un'immagine della directory "MicoApp" a sua
     * volta dentro a quella pubblica delle immagini
     *
     * @param imageFileNamePrefix Prefisso da anteporre alla data nel nome del file
     * @return Un file salvato con il formato .jpg, null in case of error
     */
    @SuppressLint("SimpleDateFormat")
    public static File createImageFile(String imageFileNamePrefix) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File storageDir = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MicoApp");
            if (!storageDir.isDirectory()) {
                /*La directory ancora non esiste*/
                if (!storageDir.mkdirs()) {
                    /*Fallimento nella creazione della directory, aborto*/
                    return null;
                }
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = imageFileNamePrefix + "_" + timeStamp + ".jpg";
            return new File(storageDir, imageFileName);
        }
        return null;
    }


    /**
     * Copia il contenuto di un file
     *
     * @param uri Lo stream del file da copiare
     * @param file Dove copiare
     */
    public static void uriToFile(ContentResolver ctxResolver, Uri uri, File file) {
        final int chunkSize = 1024;
        byte[] imageData = new byte[chunkSize];

        try {
            InputStream in = ctxResolver.openInputStream(uri);
            OutputStream out = new FileOutputStream(file);
            if(in == null)
                throw new Exception();

            int bytesRead;
            while ((bytesRead = in.read(imageData)) > 0) {
                out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
            }

            in.close();
            out.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
