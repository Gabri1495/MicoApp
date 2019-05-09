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

    @Query("SELECT * FROM ricevuto WHERE fungo = :fungo")
    Ricevuto[] loadAllRicevutiFungo(String fungo);

    @Query("DELETE FROM ricevuto")
    void deleteAll();
}
