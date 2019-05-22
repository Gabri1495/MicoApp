package com.gsorrentino.micoapp.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gsorrentino.micoapp.model.Ritrovamento;

import java.util.List;

@Dao
public interface RitrovamentoDao {
    @Insert
    void insertRitrovamento (Ritrovamento ritrovamento);
    @Insert
    void insertAllRitrovamenti (Ritrovamento[] ritrovamenti);

    @Update
    void updateRitrovamento (Ritrovamento ritrovamento);

    @Delete
    void deleteRitrovamento (Ritrovamento ritrovamento);

    @Query("SELECT * FROM ritrovamento")
    LiveData<List<Ritrovamento>> getAllRitrovamenti();

    @Query("SELECT * FROM ritrovamento ORDER BY data DESC")
    LiveData<List<Ritrovamento>> getAllRitrovamentiTimeDec();

    @Query("SELECT * FROM ritrovamento ORDER BY fungo ASC")
    LiveData<List<Ritrovamento>> getAllRitrovamentiFungoAsc();

    @Query("SELECT * FROM ritrovamento ORDER BY nickname ASC")
    LiveData<List<Ritrovamento>> getAllRitrovamentiNicknameAsc();

    @Query("SELECT * FROM ritrovamento WHERE indirizzo LIKE :luogo")
    LiveData<List<Ritrovamento>> getAllRitrovamentiLuogoSearch(String luogo);

    @Query("DELETE FROM ritrovamento")
    void deleteAll();
}
