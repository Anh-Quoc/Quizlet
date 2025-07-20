package com.prm.quizlet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.DAO.StudyingProgressDAO;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.StudyingProgress;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LearnActivity extends AppCompatActivity {

    private TextView answer1, answer2, answer3, answer4, txtQuestion, txtInstruction, txtProgressStart, txtProgressEnd;
    private ProgressBar progressBar;
    private Button btnNext, btnRetry;
    private LinearLayout answerContainer;
    private StudyingProgressDAO studyingProgressDAO;
    private FlashcardDAO flashcardDAO;
    private List<Flashcards> flashcardList;
    private Flashcards currentCard;
    private int currentIndex = 0;
    private int correctIndex = -1;
    private int setId;
    private List<TextView> answerViews = new ArrayList<>();
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        setId = getIntent().getIntExtra("setId", -1);
        if (setId == -1) {
            Toast.makeText(this, "Không tìm thấy Set!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        studyingProgressDAO = QuizletDatabase.getInstance(this).studyingProgressDao();
        flashcardDAO = QuizletDatabase.getInstance(this).flashcardDao();
        flashcardList = flashcardDAO.getBySetId(setId);
        Collections.shuffle(flashcardList);

        // Gán view
        txtQuestion = findViewById(R.id.txtQuestion);
        txtInstruction = findViewById(R.id.txtInstruction);
        txtProgressStart = findViewById(R.id.txtProgressStart);
        txtProgressEnd = findViewById(R.id.txtProgressEnd);
        progressBar = findViewById(R.id.progressBar);
        btnNext = findViewById(R.id.btnNext);
        btnRetry = findViewById(R.id.btnRetry);
        answerContainer = findViewById(R.id.answerContainer);
        answer1 = findViewById(R.id.answer1);
        answer2 = findViewById(R.id.answer2);
        answer3 = findViewById(R.id.answer3);
        answer4 = findViewById(R.id.answer4);

        answerViews.add(answer1);
        answerViews.add(answer2);
        answerViews.add(answer3);
        answerViews.add(answer4);

        progressBar.setMax(flashcardList.size());
        txtProgressEnd.setText(String.valueOf(flashcardList.size()));

        btnNext.setOnClickListener(view -> {
            currentIndex++;
            if (currentIndex < flashcardList.size()) {
                loadQuestion();
            } else {
                Toast.makeText(this, "Bạn đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Back button
        ImageView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        btnRetry.setVisibility(View.GONE);
        loadQuestion();
    }

    private void loadQuestion() {
        answered = false;
        btnNext.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        currentCard = flashcardList.get(currentIndex);
        txtQuestion.setText(currentCard.front_text);
        txtProgressStart.setText(String.valueOf(currentIndex + 1));
        progressBar.setProgress(currentIndex);

        txtInstruction.setText("Choose the answer");
        txtInstruction.setTextColor(Color.parseColor("#6A6A6A"));

        List<String> options = getShuffledOptions(currentCard);
        for (int i = 0; i < answerViews.size(); i++) {
            TextView option = answerViews.get(i);
            option.setText(options.get(i));
            option.setBackgroundResource(R.drawable.bg_answer_option);
            option.setEnabled(true); // Reset trạng thái nếu đã chọn trước đó
            final int index = i;

            option.setOnClickListener(v -> handleAnswerSelected(index, option));
        }
    }


    private List<String> getShuffledOptions(Flashcards card) {
        List<String> result = new ArrayList<>();
        result.add(card.back_text); // Đáp án đúng

        List<String> otherBackTexts = flashcardList.stream()
                .filter(c -> c.id != card.id && c.back_text != null)
                .map(c -> c.back_text)
                .distinct()
                .collect(Collectors.toList());

        Collections.shuffle(otherBackTexts);
        int needed = Math.min(3, otherBackTexts.size());
        for (int i = 0; i < needed; i++) {
            result.add(otherBackTexts.get(i));
        }

        while (result.size() < 4) {
            result.add("Đáp án phụ " + (result.size() + 1));
        }

        Collections.shuffle(result);
        correctIndex = result.indexOf(card.back_text);
        return result;
    }

    private void handleAnswerSelected(int selectedIndex, TextView selectedView) {
        if (answered) return;

        answered = true;

        boolean isCorrect = selectedIndex == correctIndex;

        if (isCorrect) {
            selectedView.setBackgroundResource(R.drawable.bg_answer_correct);
            txtInstruction.setText("The answer is correct");
            txtInstruction.setTextColor(Color.parseColor("#2D6A4F"));
        } else {
            selectedView.setBackgroundResource(R.drawable.bg_answer_wrong);
            txtInstruction.setText("Let's try again");
            txtInstruction.setTextColor(Color.parseColor("#D00000"));
            btnRetry.setVisibility(View.VISIBLE);
        }

        updateStudyingProgress(currentCard, isCorrect); // ✅ Lưu tiến độ

        btnNext.setVisibility(View.VISIBLE);

        for (TextView answerView : answerViews) {
            answerView.setEnabled(false);
        }

        btnRetry.setOnClickListener(v -> loadQuestion());
    }

    // ✅ Lưu tiến độ học
    private void updateStudyingProgress(Flashcards card, boolean isCorrect) {
        StudyingProgress progress = studyingProgressDAO.getByFlashcardId(card.id);
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (progress == null) {
            // Chưa có, tạo mới
            progress = new StudyingProgress();
            progress.flashcard_id = card.id;
            progress.last_studied = now;
            progress.correct_count = isCorrect ? 1 : 0;
            progress.wrong_count = isCorrect ? 0 : 1;
            progress.ease_factor = isCorrect ? 2.5f : 2.0f;
            progress.next_due = now;  // hoặc cộng thêm 1 ngày nếu muốn

            studyingProgressDAO.insert(progress);
        } else {
            // Đã có, cập nhật
            progress.last_studied = now;

            if (isCorrect) {
                progress.correct_count += 1;
                progress.ease_factor = Math.min(progress.ease_factor + 0.1f, 3.0f);
            } else {
                progress.wrong_count += 1;
                progress.ease_factor = Math.max(progress.ease_factor - 0.2f, 1.3f);
            }

            progress.next_due = now;  // hoặc dùng thuật toán spaced repetition ở đây
            studyingProgressDAO.update(progress);
        }
    }

}

