package com.prm.quizlet.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(tableName = "sets",
        foreignKeys = @ForeignKey(entity = Folder.class,
                parentColumns = "id",
                childColumns = "folder_id",
                onDelete = ForeignKey.SET_NULL))
public class Sets{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public Integer folder_id; // Có thể null
    public String title;
    public String description;
    public int is_public;
    public String created_at;
    public String updated_at;
}