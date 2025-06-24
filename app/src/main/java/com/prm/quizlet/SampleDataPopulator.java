package com.prm.quizlet;

import android.os.AsyncTask;

import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Folder;
import com.prm.quizlet.entity.Sets;
import com.prm.quizlet.entity.StudyingProgress;

import java.util.ArrayList;
import java.util.List;

public class SampleDataPopulator {
    public static void populateIfEmpty(final QuizletDatabase db) {
        // Kiểm tra và insert dữ liệu trên background thread để tránh ANR
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Nếu bảng Folder đã có dữ liệu thì không insert nữa
                if (db.folderDao().getAll().size() > 0) return;

                // 1. Folder
                Folder folder1 = new Folder();
                folder1.name = "Tiếng Anh";
                folder1.description = "Folder học tiếng Anh";
                folder1.created_at = null;

                Folder folder2 = new Folder();
                folder2.name = "Lịch Sử";
                folder2.description = "Các bộ thẻ về lịch sử";
                folder2.created_at = null;

                long folderId1 = db.folderDao().insert(folder1);
                long folderId2 = db.folderDao().insert(folder2);

                // 2. SetEntity
                Sets set1 = new Sets();
                set1.folder_id = (int) folderId1;
                set1.title = "Từ vựng cơ bản";
                set1.description = "Các từ tiếng Anh cơ bản";
                set1.is_public = 1;
                set1.created_at = null;
                set1.updated_at = null;

                Sets set2 = new Sets();
                set2.folder_id = (int) folderId2;
                set2.title = "Dòng thời gian lịch sử VN";
                set2.description = "Các sự kiện lịch sử nổi bật";
                set2.is_public = 1;
                set2.created_at = null;
                set2.updated_at = null;

                long setId1 = db.setDao().insert(set1);
                long setId2 = db.setDao().insert(set2);

                Flashcards fc1 = new Flashcards();
                fc1.set_id = (int) setId1;
                fc1.front_text = "apple";
                fc1.back_text = "quả táo";
                fc1.image_url = null;
                fc1.audio_url = null;
                fc1.created_at = null;

                Flashcards fc2 = new Flashcards();
                fc2.set_id = (int) setId1;
                fc2.front_text = "book";
                fc2.back_text = "quyển sách";
                fc2.image_url = null;
                fc2.audio_url = null;
                fc2.created_at = null;

                Flashcards fc3 = new Flashcards();
                fc3.set_id = (int) setId2;
                fc3.front_text = "1945";
                fc3.back_text = "Cách mạng tháng 8 thành công";
                fc3.image_url = null;
                fc3.audio_url = null;
                fc3.created_at = null;

                Flashcards fc4 = new Flashcards();
                fc4.set_id = (int) setId2;
                fc4.front_text = "1975";
                fc4.back_text = "Giải phóng miền Nam";
                fc4.image_url = null;
                fc4.audio_url = null;
                fc4.created_at = null;

                long fcId1 = db.flashcardDao().insert(fc1);
                long fcId2 = db.flashcardDao().insert(fc2);
                long fcId3 = db.flashcardDao().insert(fc3);
                long fcId4 = db.flashcardDao().insert(fc4);

                // 4. StudyingProgress
                StudyingProgress sp1 = new StudyingProgress();
                sp1.flashcard_id = (int) fcId1;
                sp1.last_studied = "2025-06-24 10:00:00";
                sp1.correct_count = 2;
                sp1.wrong_count = 1;
                sp1.ease_factor = 2.5f;
                sp1.next_due = "2025-06-25 10:00:00";

                StudyingProgress sp2 = new StudyingProgress();
                sp2.flashcard_id = (int) fcId2;
                sp2.last_studied = "2025-06-24 11:00:00";
                sp2.correct_count = 1;
                sp2.wrong_count = 0;
                sp2.ease_factor = 2.5f;
                sp2.next_due = "2025-06-25 11:00:00";

                StudyingProgress sp3 = new StudyingProgress();
                sp3.flashcard_id = (int) fcId3;
                sp3.last_studied = null;
                sp3.correct_count = 0;
                sp3.wrong_count = 0;
                sp3.ease_factor = 2.5f;
                sp3.next_due = null;

                StudyingProgress sp4 = new StudyingProgress();
                sp4.flashcard_id = (int) fcId4;
                sp4.last_studied = null;
                sp4.correct_count = 0;
                sp4.wrong_count = 0;
                sp4.ease_factor = 2.5f;
                sp4.next_due = null;

                db.studyingProgressDao().insert(sp1);
                db.studyingProgressDao().insert(sp2);
                db.studyingProgressDao().insert(sp3);
                db.studyingProgressDao().insert(sp4);
            }
        });
    }
}