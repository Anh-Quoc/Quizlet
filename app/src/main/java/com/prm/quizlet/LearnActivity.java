package com.prm.quizlet;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.entity.Flashcards;

import java.util.*;

public class LearnActivity extends AppCompatActivity {

    private List<Flashcards> flashcardList;
    private int currentIndex = 0;
    private int correctCount = 0;

    private TextView txtQuestion;
    private LinearLayout answerContainer;
    private ProgressBar progressBar;
    private Button btnNext;

    private Flashcards currentCard;
    private String correctAnswer = "";

    private FlashcardDAO flashcardDAO;
    private int setId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        txtQuestion = findViewById(R.id.txtQuestion);
        answerContainer = findViewById(R.id.answerContainer);
        progressBar = findViewById(R.id.progressBar);
        btnNext = findViewById(R.id.btnNext);

        setId = getIntent().getIntExtra("setId", 1);
        if (setId == -1) {
            Toast.makeText(this, "Thiếu Set ID!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        flashcardDAO = QuizletDatabase.getInstance(this).flashcardDao();
        flashcardList = flashcardDAO.getBySetId(setId);

        if (flashcardList == null || flashcardList.isEmpty()) {
            Toast.makeText(this, String.format("Không có flashcard nào trong set %d!", setId), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Collections.shuffle(flashcardList);
        progressBar.setMax(flashcardList.size());

        showNextQuestion();
    }


    private void showNextQuestion() {
        flashcardList = flashcardDAO.getBySetId(setId);

        if (flashcardList == null || flashcardList.isEmpty()) {
            Toast.makeText(this, "Không có flashcard nào trong set này!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (currentIndex >= flashcardList.size()) {
            Toast.makeText(this, "Hoàn thành! Đúng: " + correctCount + "/" + flashcardList.size(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("DEBUG", "Số flashcard: " + flashcardList.size());

        currentCard = flashcardList.get(currentIndex);
        correctAnswer = currentCard.back_text;

        txtQuestion.setText(currentCard.front_text);
        answerContainer.removeAllViews();
        btnNext.setVisibility(View.GONE);

        List<String> options = generateOptions(correctAnswer);
        Collections.shuffle(options);

        for (String option : options) {
            Button btn = new Button(this);
            btn.setText(option);
            btn.setAllCaps(false);
            btn.setBackgroundColor(Color.DKGRAY);
            btn.setTextColor(Color.WHITE);
            btn.setPadding(20, 10, 20, 10);

            btn.setOnClickListener(v -> checkAnswer((Button) v, option.equals(correctAnswer)));

            answerContainer.addView(btn);
        }

        progressBar.setProgress(currentIndex);
    }

    private void checkAnswer(Button selectedBtn, boolean isCorrect) {
        if (isCorrect) {
            selectedBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
            correctCount++;
        } else {
            selectedBtn.setBackgroundColor(Color.parseColor("#F44336"));
            // highlight đúng
            for (int i = 0; i < answerContainer.getChildCount(); i++) {
                Button btn = (Button) answerContainer.getChildAt(i);
                if (btn.getText().toString().equals(correctAnswer)) {
                    btn.setBackgroundColor(Color.parseColor("#4CAF50"));
                }
            }
        }

        // Disable tất cả lựa chọn
        for (int i = 0; i < answerContainer.getChildCount(); i++) {
            answerContainer.getChildAt(i).setEnabled(false);
        }

        btnNext.setVisibility(View.VISIBLE);
        btnNext.setOnClickListener(view -> {
            currentIndex++;
            showNextQuestion();
        });
    }

    private List<String> generateOptions(String correctAnswer) {
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);

        Random rand = new Random();
        while (options.size() < 4 && flashcardList.size() > 1) {
            String fakeAnswer = flashcardList.get(rand.nextInt(flashcardList.size())).back_text;
            if (!options.contains(fakeAnswer)) {
                options.add(fakeAnswer);
            }
        }

        return options;
    }
}
