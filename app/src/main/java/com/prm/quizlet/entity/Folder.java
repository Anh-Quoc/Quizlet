package com.prm.quizlet.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "folder")
public class Folder {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String description;
    public String created_at;
}