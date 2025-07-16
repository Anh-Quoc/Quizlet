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
    long insert(Flashcards flashcards);

    @Insert
    List<Long> insertAll(List<Flashcards> flashcards);

    @Update
    int update(Flashcards flashcards);

    @Delete
    int delete(Flashcards flashcards);

    @Query("SELECT * FROM flashcards")
    List<Flashcards> getAll();

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    Flashcards getById(int id);

    @Query("SELECT * FROM flashcards WHERE set_id = :setId")
    List<Flashcards> getBySetId(int setId);

    @Query("DELETE FROM flashcards")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM flashcards WHERE set_id = :setId")
    int countBySetId(int setId);
}