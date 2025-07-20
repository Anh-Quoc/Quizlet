package com.prm.quizlet;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.DAO.StudyingProgressDAO;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.StudyingProgress;

import java.text.SimpleDateFormat;
import java.util.*;

public class FlashcardStudyActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FlashcardProgressPrefs"; // More descriptive name
    private static final String KEY_LAST_FLASHCARD_ID_PREFIX = "last_flashcard_id_for_set_"; // Key prefix

    private List<Flashcards> flashcardList;
    private Stack<Integer> history = new Stack<>();
    private int currentIndex = 0;
    private int setId;
    private boolean isFrontShowing = true;

    private TextView txtCard, txtProgress, btnBack;
    private ProgressBar progressBar;

    private FlashcardDAO flashcardDAO;
    private StudyingProgressDAO studyingProgressDAO;

    private AnimatorSet flipIn, flipOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_study);

        flashcardDAO = QuizletDatabase.getInstance(this).flashcardDao();
        studyingProgressDAO = QuizletDatabase.getInstance(this).studyingProgressDao();

        txtCard = findViewById(R.id.txtCard);
        txtProgress = findViewById(R.id.txtProgress);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressStudy);

        setId = getIntent().getIntExtra("setId", -1);
        if (setId == -1) {
            Toast.makeText(this, "Không tìm thấy Set!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        flashcardList = flashcardDAO.getBySetId(setId);

        if (flashcardList == null || flashcardList.isEmpty()) {
            Toast.makeText(this, "Không có flashcard!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadSavedProgress();

        setupAnimations();
        progressBar.setMax(flashcardList.size());
        showCurrentCard();

        txtCard.setOnClickListener(v -> flipCard());

        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        swipeCard(true); // đã biết
                    } else {
                        swipeCard(false); // chưa biết
                    }
                    return true;
                }
                return false;
            }
        });

        // Back button
        ImageView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        txtCard.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        btnBack.setOnClickListener(v -> {
            if (!history.isEmpty()) {
                currentIndex = history.pop();
                showCurrentCard();
                // When going back, we should also save the new current card as the last viewed
                saveCurrentFlashcardProgress();
            }
        });
    }

    private void setupAnimations() {
        flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_in);
        flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_out);
        flipIn.setTarget(txtCard);
        flipOut.setTarget(txtCard);
    }

    private void showCurrentCard() {
        if (currentIndex >= flashcardList.size()) {
            Toast.makeText(this, "Hoàn thành tất cả thẻ!", Toast.LENGTH_SHORT).show();
            clearSavedProgressForSet();
            finish();
            return;
        }

        isFrontShowing = true;
        Flashcards card = flashcardList.get(currentIndex);
        txtCard.setText(card.front_text);
        txtCard.setBackgroundResource(R.drawable.flashcard_bg); // default border
        txtProgress.setText((currentIndex + 1) + " / " + flashcardList.size());
        progressBar.setProgress(currentIndex + 1);
    }

    private void flipCard() {
        if (currentIndex >= flashcardList.size()) return; // Avoid issues if trying to flip completed card
        Flashcards card = flashcardList.get(currentIndex);
        flipOut.start();
        txtCard.setText(isFrontShowing ? card.back_text : card.front_text);
        flipIn.start();
        isFrontShowing = !isFrontShowing;
    }

    private void swipeCard(boolean remembered) {
        if (currentIndex >= flashcardList.size()) return; // Already completed

        updateStudyingProgress(remembered);
        history.push(currentIndex);

        // Set border color
        if (remembered) {
            txtCard.setBackground(ContextCompat.getDrawable(this, R.drawable.flashcard_border_green));
        } else {
            txtCard.setBackground(ContextCompat.getDrawable(this, R.drawable.flashcard_border_red));
        }

        txtCard.postDelayed(() -> {
            currentIndex++;
            showCurrentCard();
            // --- Save progress after swiping to the NEXT card (or completion) ---
            if (currentIndex < flashcardList.size()) {
                saveCurrentFlashcardProgress();
            } else {
                // If all cards are completed, the progress for this set is cleared by showCurrentCard
                // Or you can explicitly call clearSavedProgressForSet() here if showCurrentCard's logic changes
            }
            // --- End saving ---
        }, 300);
    }

    private void updateStudyingProgress(boolean correct) {
        if (currentIndex >= flashcardList.size()) return;
        Flashcards card = flashcardList.get(currentIndex);
        StudyingProgress progress = studyingProgressDAO.getByFlashcardId(card.id);

        if (progress == null) {
            progress = new StudyingProgress();
            progress.flashcard_id = card.id;
            progress.correct_count = correct ? 1 : 0;
            progress.wrong_count = correct ? 0 : 1;
            progress.ease_factor = 2.5f;
        } else {
            if (correct) progress.correct_count++;
            else progress.wrong_count++;
        }

        progress.last_studied = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        // progress.next_due = ... // Calculate next_due if implementing SM-2
        insertOrUpdateProgress(progress);
    }

    private void insertOrUpdateProgress(StudyingProgress progress) {
        StudyingProgress existing = studyingProgressDAO.getByFlashcardId(progress.flashcard_id);
        if (existing == null) {
            studyingProgressDAO.insert(progress);
        } else {
            progress.id = existing.id;
            studyingProgressDAO.update(progress);
        }
    }

    // --- SharedPreferences Methods ---

    private void saveCurrentFlashcardProgress() {
        if (flashcardList == null || flashcardList.isEmpty() || currentIndex >= flashcardList.size() || setId == -1) {
            Log.w("FlashcardStudy", "Cannot save progress: Invalid state.");
            return;
        }

        Flashcards currentCard = flashcardList.get(currentIndex);
        int flashcardIdToSave = currentCard.id;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LAST_FLASHCARD_ID_PREFIX + setId, flashcardIdToSave);
        editor.apply();
        Log.d("FlashcardStudy", "Saved progress: Set ID " + setId + ", Flashcard ID " + flashcardIdToSave);
    }

    private void loadSavedProgress() {
        if (setId == -1 || flashcardList == null || flashcardList.isEmpty()) {
            currentIndex = 0; // Default to first card if no set ID or no flashcards
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Default to -1 or any other invalid ID if no progress is saved for this set
        int savedFlashcardId = prefs.getInt(KEY_LAST_FLASHCARD_ID_PREFIX + setId, -1);

        if (savedFlashcardId != -1) {
            for (int i = 0; i < flashcardList.size(); i++) {
                if (flashcardList.get(i).id == savedFlashcardId) {
                    currentIndex = i;
                    Log.d("FlashcardStudy", "Loaded progress: Set ID " + setId + ", Found Flashcard ID " + savedFlashcardId + " at index " + i);
                    return; // Found the saved card, exit
                }
            }
            // If savedFlashcardId was found but not in current list (e.g., card deleted), start from beginning
            Log.w("FlashcardStudy", "Saved Flashcard ID " + savedFlashcardId + " not found in current list for Set ID " + setId + ". Starting from beginning.");
            currentIndex = 0;
        } else {
            // No saved progress for this set, start from the beginning
            Log.d("FlashcardStudy", "No saved progress found for Set ID " + setId + ". Starting from beginning.");
            currentIndex = 0;
        }
    }

    private void clearSavedProgressForSet() {
        if (setId == -1) return;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_LAST_FLASHCARD_ID_PREFIX + setId);
        editor.apply();
        Log.d("FlashcardStudy", "Cleared saved progress for Set ID: " + setId);
    }

    // --- End SharedPreferences Methods ---

    // Optional: Save progress when the activity is paused or stopped
    // This is a good fallback if the app is closed unexpectedly.
    @Override
    protected void onPause() {
        super.onPause();
        if (currentIndex < flashcardList.size()) { // Only save if not completed
            saveCurrentFlashcardProgress();
        }
        Log.d("FlashcardStudy", "onPause - progress potentially saved.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // You could also save here, but onPause is generally sufficient for this use case.
        // If you save in both, it's not harmful due to overwriting.
        Log.d("FlashcardStudy", "onStop called.");
    }
}
