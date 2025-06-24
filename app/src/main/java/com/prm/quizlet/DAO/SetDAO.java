package com.prm.quizlet.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm.quizlet.entity.Sets;

import java.util.List;

@Dao
public interface SetDAO {
    @Insert
    long insert(Sets set);

    @Insert
    List<Long> insertAll(List<Sets> sets);

    @Update
    int update(Sets set);

    @Delete
    int delete(Sets set);

    @Query("SELECT * FROM sets")
    List<Sets> getAll();

    @Query("SELECT * FROM sets WHERE id = :id LIMIT 1")
    Sets getById(int id);

    @Query("SELECT * FROM sets WHERE folder_id = :folderId")
    List<Sets> getByFolderId(int folderId);

    @Query("DELETE FROM sets")
    void deleteAll();
}