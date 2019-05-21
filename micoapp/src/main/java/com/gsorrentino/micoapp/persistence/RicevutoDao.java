package com.gsorrentino.micoapp.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gsorrentino.micoapp.model.Ricevuto;

import java.util.List;

@Dao
public interface RicevutoDao {
    @Insert
    void insertRicevuto (Ricevuto ricevuto);
    @Insert
    void insertAllRicevuti (Ricevuto[] ricevuto);

    @Update
    void updateRicevuto (Ricevuto ricevuto);

    @Delete
    void deleteRicevuto (Ricevuto ricevuto);

    @Query("SELECT * FROM ricevuto")
    LiveData<List<Ricevuto>> getAllRicevuti();

    @Query("SELECT * FROM ricevuto ORDER BY fungo ASC")
    LiveData<List<Ricevuto>> getAllRicevutiFungoAsc();

    @Query("SELECT * FROM ricevuto ORDER BY data DESC")
    LiveData<List<Ricevuto>> getAllRicevutiTimeDec();

    @Query("SELECT * FROM ricevuto ORDER BY dataRicezione DESC")
    LiveData<List<Ricevuto>> getAllRicevutiTimeRiceivedDec();

    @Query("SELECT * FROM ricevuto ORDER BY nickname ASC")
    LiveData<List<Ricevuto>> getAllRicevutiUserAsc();

    @Query("SELECT * FROM ricevuto WHERE fungo = :fungo")
    Ricevuto[] loadAllRicevutiFungo(String fungo);

    @Query("DELETE FROM ricevuto")
    void deleteAll();
}
