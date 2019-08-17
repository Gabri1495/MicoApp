package com.gsorrentino.micoapp.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.model.Utente;

import java.util.Calendar;
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

    @Query("SELECT COUNT() FROM ritrovamento")
    int countRitrovamenti();

    @Query("SELECT nickname, nome, cognome FROM ritrovamento GROUP BY nickname, nome, cognome")
    List<Utente> getUtentiRitrovamenti();

    @Query("SELECT * FROM ritrovamento")
    List<Ritrovamento> getAllRitrovamentiStatic();

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

    @Query("SELECT * FROM ritrovamento WHERE " +
            "(CASE WHEN :latDown < :latUp " +
            "   THEN latitudine BETWEEN :latDown AND :latUp " +
            "   ELSE latitudine BETWEEN :latUp AND :latDown " +
            "END) " +
            "AND " +
            "(CASE WHEN :lngDown < :lngUp " +
            "   THEN longitudine BETWEEN :lngDown AND :lngUp " +
            "   ELSE longitudine BETWEEN :lngUp AND :lngDown " +
            "END)")
    List<Ritrovamento> getAllRitrovamentiCoordsBounds(double latDown, double lngDown,
                                                                double latUp, double lngUp);

    @Query("SELECT * FROM ritrovamento WHERE nickname = :nickname AND nome = :nome " +
            "AND cognome = :cognome AND data = :data")
    Ritrovamento getRitrovamento(String nickname, String nome, String cognome, Calendar data);

    @Query("SELECT * FROM ritrovamento WHERE `key` = :key")
    Ritrovamento getRitrovamento(int key);

    @Query("DELETE FROM ritrovamento")
    void deleteAll();
}
