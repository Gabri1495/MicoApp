package com.gsorrentino.micoapp.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gsorrentino.micoapp.model.Ritrovamento;

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
    Ritrovamento[] loadAllRitrovamenti();
    @Query("SELECT * FROM ritrovamento WHERE fungo = :fungo")
    Ritrovamento[] loadAllRitrovamentiFungo(String fungo);
}
