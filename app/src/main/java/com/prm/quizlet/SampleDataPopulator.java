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
                // Ngày mẫu
                String now = "2025-07-01 09:00:00";
                String earlier = "2025-06-15 15:30:00";
                String evenEarlier = "2025-05-20 08:20:00";

                // 1. Folder
                Folder folder1 = new Folder();
                folder1.name = "Tiếng Anh";
                folder1.description = "Folder học tiếng Anh";
                folder1.created_at = now;

                Folder folder2 = new Folder();
                folder2.name = "Lịch Sử";
                folder2.description = "Các bộ thẻ về lịch sử";
                folder2.created_at = earlier;

                long folderId1 = db.folderDao().insert(folder1);
                long folderId2 = db.folderDao().insert(folder2);

                // 2. SetEntity
                Sets set1 = new Sets();
                set1.folder_id = (int) folderId1;
                set1.title = "Từ vựng cơ bản";
                set1.description = "Các từ tiếng Anh cơ bản";
                set1.is_public = 1;
                set1.created_at = now;
                set1.updated_at = now;

                Sets set2 = new Sets();
                set2.folder_id = (int) folderId2;
                set2.title = "Dòng thời gian lịch sử VN";
                set2.description = "Các sự kiện lịch sử nổi bật";
                set2.is_public = 1;
                set2.created_at = earlier;
                set2.updated_at = earlier;

                long setId1 = db.setDao().insert(set1);
                long setId2 = db.setDao().insert(set2);

                // 3. Flashcards
                Flashcards fc1 = new Flashcards();
                fc1.set_id = (int) setId1;
                fc1.front_text = "apple";
                fc1.back_text = "quả táo";
                fc1.image_url = null;
                fc1.audio_url = null;
                fc1.created_at = now;

                Flashcards fc2 = new Flashcards();
                fc2.set_id = (int) setId1;
                fc2.front_text = "book";
                fc2.back_text = "quyển sách";
                fc2.image_url = null;
                fc2.audio_url = null;
                fc2.created_at = now;

                Flashcards fc3 = new Flashcards();
                fc3.set_id = (int) setId1;
                fc3.front_text = "water";
                fc3.back_text = "nước";
                fc3.image_url = null;
                fc3.audio_url = null;
                fc3.created_at = earlier;

                Flashcards fc7 = new Flashcards();
                fc7.set_id = (int) setId1;
                fc7.front_text = "chair";
                fc7.back_text = "ghế";
                fc7.image_url = null;
                fc7.audio_url = null;
                fc7.created_at = null;

                Flashcards fc5 = new Flashcards();
                fc5.set_id = (int) setId1;
                fc5.front_text = "necessary";
                fc5.back_text = "cần thiết";
                fc5.image_url = null;
                fc5.audio_url = null;
                fc5.created_at = null;

                Flashcards fc6 = new Flashcards();
                fc6.set_id = (int) setId2;
                fc6.front_text = "1945";
                fc6.back_text = "Cách mạng tháng 8 thành công";
                fc6.image_url = null;
                fc6.audio_url = null;
                fc6.created_at = null;

                Flashcards fc4 = new Flashcards();
                fc4.set_id = (int) setId2;
                fc4.front_text = "1975";
                fc4.back_text = "Giải phóng miền Nam";
                fc4.image_url = null;
                fc4.audio_url = null;
                fc4.created_at = evenEarlier;

                Flashcards fc8 = new Flashcards();
                fc8.set_id = (int) setId2;
                fc8.front_text = "1972";
                fc8.back_text = "Chiến thắng Điện Biên Phủ trên không";
                fc8.image_url = null;
                fc8.audio_url = null;
                fc8.created_at = null;

                Flashcards fc9 = new Flashcards();
                fc9.set_id = (int) setId2;
                fc9.front_text = "1954";
                fc9.back_text = "Chiến thắng Điện Biên Phủ";
                fc9.image_url = null;
                fc9.audio_url = null;
                fc9.created_at = null;

                long fcId1 = db.flashcardDao().insert(fc1);
                long fcId2 = db.flashcardDao().insert(fc2);
                long fcId3 = db.flashcardDao().insert(fc3);
                long fcId4 = db.flashcardDao().insert(fc4);
                long fcId5 = db.flashcardDao().insert(fc5);
                long fcId6 = db.flashcardDao().insert(fc6);
                long fcId7 = db.flashcardDao().insert(fc7);
                long fcId8 = db.flashcardDao().insert(fc8);
                long fcId9 = db.flashcardDao().insert(fc9);

                // 4. StudyingProgress
                StudyingProgress sp1 = new StudyingProgress();
                sp1.flashcard_id = (int) fcId1;
                sp1.last_studied = "2025-07-02 10:00:00";
                sp1.correct_count = 2;
                sp1.wrong_count = 1;
                sp1.ease_factor = 2.5f;
                sp1.next_due = "2025-07-03 10:00:00";

                StudyingProgress sp2 = new StudyingProgress();
                sp2.flashcard_id = (int) fcId2;
                sp2.last_studied = "2025-07-02 11:00:00";
                sp2.correct_count = 1;
                sp2.wrong_count = 0;
                sp2.ease_factor = 2.5f;
                sp2.next_due = "2025-07-03 11:00:00";

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

                StudyingProgress sp5 = new StudyingProgress();
                sp5.flashcard_id = (int) fcId5;
                sp5.last_studied = null;
                sp5.correct_count = 0;
                sp5.wrong_count = 0;
                sp5.ease_factor = 2.5f;
                sp5.next_due = null;

                StudyingProgress sp6 = new StudyingProgress();
                sp6.flashcard_id = (int) fcId6;
                sp6.last_studied = null;
                sp6.correct_count = 0;
                sp6.wrong_count = 0;
                sp6.ease_factor = 2.5f;
                sp6.next_due = null;

                StudyingProgress sp7 = new StudyingProgress();
                sp7.flashcard_id = (int) fcId7;
                sp7.last_studied = null;
                sp7.correct_count = 0;
                sp7.wrong_count = 0;
                sp7.ease_factor = 2.5f;
                sp7.next_due = null;

                StudyingProgress sp8 = new StudyingProgress();
                sp8.flashcard_id = (int) fcId8;
                sp8.last_studied = null;
                sp8.correct_count = 0;
                sp8.wrong_count = 0;
                sp8.ease_factor = 2.5f;
                sp8.next_due = null;

                StudyingProgress sp9 = new StudyingProgress();
                sp9.flashcard_id = (int) fcId9;
                sp9.last_studied = null;
                sp9.correct_count = 0;
                sp9.wrong_count = 0;
                sp9.ease_factor = 2.5f;
                sp9.next_due = null;

                db.studyingProgressDao().insert(sp1);
                db.studyingProgressDao().insert(sp2);
                db.studyingProgressDao().insert(sp3);
                db.studyingProgressDao().insert(sp4);
                db.studyingProgressDao().insert(sp5);
                db.studyingProgressDao().insert(sp6);
                db.studyingProgressDao().insert(sp7);
                db.studyingProgressDao().insert(sp8);
                db.studyingProgressDao().insert(sp9);
            }
        });
    }
    public static void deleteAllData(final QuizletDatabase db) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                db.studyingProgressDao().deleteAll();
                db.flashcardDao().deleteAll();
                db.setDao().deleteAll();
                db.folderDao().deleteAll();
            }
        });
    }
}