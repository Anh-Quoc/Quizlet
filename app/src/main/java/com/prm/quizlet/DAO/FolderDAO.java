package com.prm.quizlet.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm.quizlet.entity.Folder;

import java.util.List;

@Dao
public interface FolderDAO {
    @Insert
    long insert(Folder folder);

    @Insert
    List<Long> insertAll(List<Folder> folders);

    @Update
    int update(Folder folder);

    @Delete
    int delete(Folder folder);

    @Query("SELECT * FROM folder")
    List<Folder> getAll();

    @Query("SELECT * FROM folder WHERE id = :id LIMIT 1")
    Folder getById(int id);

    @Query("DELETE FROM folder")
    void deleteAll();

    @Query("DELETE FROM folder WHERE id = :id")
    void deleteById(int id);
}