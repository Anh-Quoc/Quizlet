package com.prm.quizlet.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm.quizlet.entity.StudyingProgress;

import java.util.List;

@Dao
public interface StudyingProgressDAO {
    @Insert
    long insert(StudyingProgress progress);

    @Insert
    List<Long> insertAll(List<StudyingProgress> progresses);

    @Update
    int update(StudyingProgress progress);

    @Delete
    int delete(StudyingProgress progress);

    @Query("SELECT * FROM studyingProgress")
    List<StudyingProgress> getAll();

    @Query("SELECT * FROM studyingProgress WHERE id = :id LIMIT 1")
    StudyingProgress getById(int id);

    @Query("SELECT * FROM studyingProgress WHERE flashcard_id = :flashcardId LIMIT 1")
    StudyingProgress getByFlashcardId(int flashcardId);

    @Query("DELETE FROM studyingProgress")
    void deleteAll();
}