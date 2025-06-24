package com.prm.quizlet.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "studyingProgress",
        foreignKeys = @ForeignKey(entity = Flashcards.class,
                parentColumns = "id",
                childColumns = "flashcard_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "flashcard_id", unique = true)})
public class StudyingProgress {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int flashcard_id;
    public String last_studied;
    public int correct_count;
    public int wrong_count;
    public float ease_factor;
    public String next_due;
}