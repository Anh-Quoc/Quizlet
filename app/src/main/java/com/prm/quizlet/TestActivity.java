package com.prm.quizlet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.prm.quizlet.entity.Flashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestActivity extends AppCompatActivity {
    private LinearLayout questionContainer;
    private Button btnFinish;
    private TextView tvProgress;
    private List<Flashcards> flashcards = new ArrayList<>();
    private Map<Integer, Integer> selectedAnswers = new HashMap<>(); // Key: position, Value: selectedIndex
    private List<View> questionViews = new ArrayList<>(); // Để xử lý kết quả sau

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        questionContainer = findViewById(R.id.questionContainer);
        btnFinish = findViewById(R.id.btnFinish);
        tvProgress = findViewById(R.id.tvProgress);

        btnFinish.setVisibility(View.GONE);

        loadQuestions();     // Tạo danh sách câu hỏi
        renderQuestions();   // Gán ra giao diện

        btnFinish.setOnClickListener(v -> submitTest());
    }

    private void renderQuestions() {
        LayoutInflater inflater = LayoutInflater.from(this);
        questionViews.clear();

        for (int i = 0; i < flashcards.size(); i++) {
            Flashcards current = flashcards.get(i);
            View qView = inflater.inflate(R.layout.item_question, questionContainer, false);

            TextView tvQ = qView.findViewById(R.id.tvQuestion);
            RadioGroup rg = qView.findViewById(R.id.radioGroupOptions);
            tvQ.setText("Câu " + (i + 1) + ": " + current.front_text);

            // Chuẩn bị 4 đáp án
            List<String> options = new ArrayList<>();
            options.add(current.back_text); // đáp án đúng

            // Chọn 3 đáp án sai ngẫu nhiên
            List<Flashcards> incorrect = new ArrayList<>(flashcards);
            incorrect.remove(i); // bỏ flashcard hiện tại
            Collections.shuffle(incorrect);

            for (int j = 0; j < 3 && j < incorrect.size(); j++) {
                options.add(incorrect.get(j).back_text);
            }

            // Nếu flashcard ít hơn 4, thêm placeholder
            while (options.size() < 4) {
                options.add("Đáp án khác");
            }

            Collections.shuffle(options); // xáo trộn

            for (int j = 0; j < rg.getChildCount(); j++) {
                RadioButton rb = (RadioButton) rg.getChildAt(j);
                rb.setText(options.get(j));

                int finalI = i;
                int finalJ = j;
                rb.setOnClickListener(v -> {
                    selectedAnswers.put(finalI, finalJ);
                    updateProgress();
                });
            }

            // Lưu view
            questionContainer.addView(qView);
            questionViews.add(qView);

            // Lưu đúng đáp án index để đối chiếu khi submit
            current.back_text = current.back_text.trim(); // tránh lỗi khoảng trắng
            current.back_text = current.back_text.replaceAll("\\s+", " ");
        }
    }

    private void submitTest() {
        int correctCount = 0;

        for (int i = 0; i < flashcards.size(); i++) {
            Flashcards flashcard = flashcards.get(i);
            View qView = questionViews.get(i);
            RadioGroup rg = qView.findViewById(R.id.radioGroupOptions);

            for (int j = 0; j < rg.getChildCount(); j++) {
                RadioButton rb = (RadioButton) rg.getChildAt(j);
                rb.setEnabled(false); // disable sau khi nộp

                String answerText = rb.getText().toString().trim();
                String correctAnswer = flashcard.back_text;

                boolean isSelected = selectedAnswers.get(i) == j;
                boolean isCorrect = answerText.equalsIgnoreCase(correctAnswer);

                if (isSelected && isCorrect) {
                    rb.setBackgroundColor(Color.parseColor("#A5D6A7"));
                    correctCount++;
                } else if (isSelected && !isCorrect) {
                    rb.setBackgroundColor(Color.parseColor("#EF9A9A")); // đỏ
                } else if (!isSelected && isCorrect) {
                    rb.setBackgroundColor(Color.parseColor("#C5E1A5")); // đúng nhưng không chọn
                }
            }
        }

        showResult(correctCount);
    }
    private void showResult(int correctCount) {
        new AlertDialog.Builder(this)
                .setTitle("Kết quả")
                .setMessage("Bạn đã đúng " + correctCount + "/" + flashcards.size() +
                        " câu\nTỉ lệ: " + (correctCount * 100 / flashcards.size()) + "%")
                .setPositiveButton("Làm lại", (dialog, which) -> recreate())
                .setNegativeButton("Thoát về Set", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void updateProgress() {
        tvProgress.setText(selectedAnswers.size() + "/" + flashcards.size() + " câu đã trả lời");
        if (selectedAnswers.size() == flashcards.size()) {
            btnFinish.setVisibility(View.VISIBLE);
        }
    }
    private void loadQuestions() {
        int setId = getIntent().getIntExtra("setId", -1);
        if (setId == -1) {
            finish();
            return;
        }

        // Lấy dữ liệu flashcard từ database
        new Thread(() -> {
            QuizletDatabase db = QuizletDatabase.getInstance(this);
            List<Flashcards> loadedFlashcards = db.flashcardDao().getBySetId(setId);

            runOnUiThread(() -> {
                flashcards.clear();
                flashcards.addAll(loadedFlashcards);

                if (flashcards.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Không có dữ liệu")
                            .setMessage("Bộ thẻ này không có flashcard nào để làm bài test.")
                            .setPositiveButton("Thoát", (dialog, which) -> finish())
                            .show();
                } else {
                    renderQuestions();
                }
            });
        }).start();
    }

}

