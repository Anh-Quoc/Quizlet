package com.prm.quizlet;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.DAO.FolderDAO;
import com.prm.quizlet.DAO.SetDAO;
import com.prm.quizlet.DAO.StudyingProgressDAO;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Folder;
import com.prm.quizlet.entity.Sets;
import com.prm.quizlet.entity.StudyingProgress;

@Database(
        entities = {
                Folder.class,
                Sets.class,
                Flashcards.class,
                StudyingProgress.class
        },
        version = 1
)
public abstract class QuizletDatabase extends RoomDatabase {

    public abstract FolderDAO folderDao();
    public abstract SetDAO setDao();
    public abstract FlashcardDAO flashcardDao();
    public abstract StudyingProgressDAO studyingProgressDao();
}