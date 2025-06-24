package com.prm.quizlet.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(tableName = "flashcards",
        foreignKeys = @ForeignKey(entity = Sets.class,
                parentColumns = "id",
                childColumns = "set_id",
                onDelete = ForeignKey.CASCADE))
public class Flashcards {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int set_id;
    public String front_text;
    public String back_text;
    public String image_url;
    public String audio_url;
    public String created_at;
}