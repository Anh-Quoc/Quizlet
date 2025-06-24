package com.prm.quizlet.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm.quizlet.entity.Flashcards;

import java.util.List;

@Dao
public interface FlashcardDAO {
    @Insert
    long insert(Flashcards Flashcards);

    @Insert
    List<Long> insertAll(List<Flashcards> Flashcards);

    @Update
    int update(Flashcards Flashcards);

    @Delete
    int delete(Flashcards Flashcards);

    @Query("SELECT * FROM Flashcards")
    List<Flashcards> getAll();

    @Query("SELECT * FROM Flashcards WHERE id = :id LIMIT 1")
    Flashcards getById(int id);

    @Query("SELECT * FROM Flashcards WHERE set_id = :setId")
    List<Flashcards> getBySetId(int setId);

    @Query("DELETE FROM Flashcards")
    void deleteAll();
}