package com.gsorrentino.micoapp.util;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Objects;

public class ImportExportRitrovamentoManager {


    public ImportExportRitrovamentoManager(){

    }

    /**
     * Se non è già presente crea la cartella dentro a {@link Environment#DIRECTORY_DOWNLOADS}
     * nella memoria esterna pubblica e vi salva il file ottenuto dal {@link Ritrovamento}
     *
     * @param activity Utilizzata per modificare la UI e invocarne i metodi
     * @param ritrovamento Ritrovamento che deve essere esportato come file
     */
    public void exportRitrovamento (Activity activity, Ritrovamento ritrovamento) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), activity.getString(R.string.app_name));
            if (!directory.isDirectory()) {
                /*La directory ancora non esiste*/
                if (!directory.mkdirs()) {
                    /*Fallimento nella creazione della directory, aborto*/
                    Toast.makeText(activity, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            /*Sono pronto a creare il file dentro la directory*/
            String fileName = ritrovamento.fungo
                    + "_" + ritrovamento.autore.nickname
                    + ritrovamento.autore.nome + ritrovamento.autore.cognome
                    + "_" + ritrovamento.data.get(Calendar.YEAR)
                    + (ritrovamento.data.get(Calendar.MONTH) + 1)
                    + ritrovamento.data.get(Calendar.DAY_OF_MONTH)
                    + ritrovamento.data.get(Calendar.HOUR_OF_DAY)
                    + ritrovamento.data.get(Calendar.MINUTE)
                    + ritrovamento.data.get(Calendar.SECOND)
                    + ".micoapp";
            File file = new File(directory, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(ritrovamento);
                oos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.error_file_creation_open, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.error_io, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(activity, R.string.success_operation, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(activity, R.string.error_storage_not_writable, Toast.LENGTH_SHORT).show();
    }


    /**
     * Legge un file ricevuto come argomento, ne estrapola il {@link Ritrovamento}
     * precedentemente salvatovi e lo aggiunge al database
     *
     * @param activity Utilizzata per modificare la UI e invocarne i metodi
     * @param filePathName Il path completo del file da caricare
     */
    public void importRitrovamento (Activity activity, Uri filePathName) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {

            /*Sono pronto a caricare il file*/
            Ritrovamento ritrovamento;
            try {
                FileDescriptor fileDescriptor = Objects.requireNonNull(activity.getContentResolver().openFileDescriptor(filePathName, "r")).getFileDescriptor();
                FileInputStream fis = new FileInputStream(fileDescriptor);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object tmp = ois.readObject();
                ritrovamento = (Ritrovamento) tmp;
                ois.close();
                fis.close();
                if(ritrovamento != null) {
                    /*AutoGenerate tratta lo 0 come valore non settato.
                    * In questo modo lascio gestire la chiave al database locale
                    * ed evito incompatibilità con quello d'origine*/
                    ritrovamento.key = 0;
                    new AsyncTasks.ImportFindAsync(activity, ritrovamento).execute();
                }
                else
                    throw new Exception();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.error_file_creation_open, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.error_io, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        Toast.makeText(activity, R.string.error_storage_not_readable, Toast.LENGTH_SHORT).show();
    }

}
