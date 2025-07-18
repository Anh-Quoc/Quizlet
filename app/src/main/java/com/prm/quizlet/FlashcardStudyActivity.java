package com.prm.quizlet;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
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

    private List<Flashcards> flashcardList;
    private Stack<Integer> history = new Stack<>();
    private int currentIndex = 0;
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



        int setId = getIntent().getIntExtra("setId", -1);
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
        Flashcards card = flashcardList.get(currentIndex);
        flipOut.start();
        txtCard.setText(isFrontShowing ? card.back_text : card.front_text);
        flipIn.start();
        isFrontShowing = !isFrontShowing;
    }

    private void swipeCard(boolean remembered) {
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
        }, 300); // Delay to let user see border
    }

    private void updateStudyingProgress(boolean correct) {
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

        progress.last_studied = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
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
}
