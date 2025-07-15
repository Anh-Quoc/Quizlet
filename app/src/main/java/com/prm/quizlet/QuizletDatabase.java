package com.prm.quizlet;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.DAO.FolderDAO;
import com.prm.quizlet.DAO.SetDAO;
import com.prm.quizlet.DAO.StudyingProgressDAO;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Folder;
import com.prm.quizlet.entity.Sets;
import com.prm.quizlet.entity.StudyingProgress;

@Database(entities = {
        Folder.class,
        Sets.class,
        Flashcards.class,
        StudyingProgress.class
}, version = 1)
public abstract class QuizletDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "quizlet.db";
    private static volatile QuizletDatabase INSTANCE;

    public static synchronized QuizletDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    QuizletDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract FolderDAO folderDao();

    public abstract SetDAO setDao();

    public abstract FlashcardDAO flashcardDao();

    public abstract StudyingProgressDAO studyingProgressDao();

    public static synchronized QuizletDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    QuizletDatabase.class,
                    "quizlet.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}