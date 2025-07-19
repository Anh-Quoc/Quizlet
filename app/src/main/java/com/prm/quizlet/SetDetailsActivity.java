package com.prm.quizlet;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.prm.quizlet.details.FlashcardDetailsAdapter;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Sets;

import java.util.List;

public class SetDetailsActivity extends AppCompatActivity {
    private List<Flashcards> flashcards;

    private FlashcardDetailsAdapter adapter;
    private int currentIndex = 0;
    private boolean showingFront = true;
    private TextView flashcardText;
    private QuizletDatabase db;

    // In SetDetailsActivity.java, add these fields:
    private TextView tvSetTitle, tvTermCount, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_details);

        int setId = getIntent().getIntExtra("set_id", -1);
        if (setId == -1) {
            Toast.makeText(this, "Set not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        flashcardText = findViewById(R.id.flashcardText);
        LinearLayout flashcardArea = findViewById(R.id.flashcardArea);
        LinearLayout btnFlashCard = findViewById(R.id.btnFlashCard);
        LinearLayout btnLearn = findViewById(R.id.btnLearn);
        LinearLayout btnTest = findViewById(R.id.btnTest);

        db = Room.databaseBuilder(getApplicationContext(),
                        QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();

        RecyclerView recyclerView = findViewById(R.id.flashcardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // In onCreate(), after setContentView:
        tvSetTitle = findViewById(R.id.tvSetTitle);
        tvTermCount = findViewById(R.id.tvTermCount);
        tvDescription = findViewById(R.id.tvDescription);

        // Load flashcards in background
        new Thread(() -> {
            flashcards = db.flashcardDao().getBySetId(setId);
            Sets set = db.setDao().getById(setId);
            runOnUiThread(() -> {
                adapter = new FlashcardDetailsAdapter(flashcards);
                recyclerView.setAdapter(adapter);
                showCurrentFlashcard();
                if (set != null) {
                    tvSetTitle.setText(set.title); // Set title
                    tvDescription.setText(set.description); // Set description
                }
                tvTermCount.setText(flashcards.size() + " terms"); // Set count
            });
        }).start();



        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            showPreviousFlashcard();
                        } else {
                            showNextFlashcard();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // Set listeners on the flashcard area (not just kanjiText)
        flashcardArea.setOnClickListener(v -> flipCard());
        flashcardArea.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Back button
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());


        btnFlashCard.setOnClickListener(view -> {
            Intent intent = new Intent(SetDetailsActivity.this, FlashcardStudyActivity.class);
            intent.putExtra("setId", setId);
            startActivity(intent);
        });

        btnLearn.setOnClickListener(view -> {
            Intent intent = new Intent(SetDetailsActivity.this, LearnActivity.class);
            intent.putExtra("setId", setId);
            startActivity(intent);
        });

        btnTest.setOnClickListener(view -> {
            Intent intent = new Intent(SetDetailsActivity.this, TestActivity.class);
            intent.putExtra("setId", setId);
            startActivity(intent);
        });
    }

    private void showCurrentFlashcard() {
        if (flashcards == null || flashcards.isEmpty()) {
            flashcardText.setText("No flashcards");
            return;
        }
        showingFront = true;
        flashcardText.setText(flashcards.get(currentIndex).front_text);
    }

    private void flipCard() {
        if (flashcards == null || flashcards.isEmpty()) return;
        Flashcards card = flashcards.get(currentIndex);
        if (showingFront) {
            flashcardText.setText(card.back_text);
        } else {
            flashcardText.setText(card.front_text);
        }
        showingFront = !showingFront;
    }

    private void showNextFlashcard() {
        if (flashcards == null || currentIndex >= flashcards.size() - 1) return;
        currentIndex++;
        showCurrentFlashcard();
    }

    private void showPreviousFlashcard() {
        if (flashcards == null || currentIndex <= 0) return;
        currentIndex--;
        showCurrentFlashcard();
    }
}