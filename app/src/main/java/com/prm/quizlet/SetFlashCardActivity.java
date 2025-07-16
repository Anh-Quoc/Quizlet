package com.prm.quizlet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.entity.Flashcards;

import java.util.List;

public class SetFlashCardActivity extends AppCompatActivity {

    private TextView txtSetName;
    private RecyclerView recyclerView;
    private FlashcardAdapter adapter;
    private List<Flashcards> flashcardList;

    private FlashcardDAO flashcardDAO;
    private int setId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_flashcard);

        // Lấy setId từ Intent
        setId = getIntent().getIntExtra("setId", 1);
        if (setId == -1) {
            Toast.makeText(this, "Không tìm thấy Set!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo DAO và lấy dữ liệu
        flashcardDAO = QuizletDatabase.getInstance(this).flashcardDao();
        flashcardList = flashcardDAO.getBySetId(setId);


        // Gắn view
        txtSetName = findViewById(R.id.txtSetName);
        recyclerView = findViewById(R.id.recyclerFlashcards);
        Button btnLearn = findViewById(R.id.btnLearn);
        Button btnTest = findViewById(R.id.btnTest);
        Button btnFlashCard = findViewById(R.id.btnFlashCard);

        // (Có thể gán tên set sau nếu có thực thể Sets)
        txtSetName.setText("Danh sách thuật ngữ (" + flashcardList.size() + " thẻ)");


        // Hiển thị flashcards
        adapter = new FlashcardAdapter(flashcardList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnFlashCard.setOnClickListener(view -> {
            Intent intent = new Intent(SetFlashCardActivity.this, FlashcardStudyActivity.class);
            intent.putExtra("setId", setId);
            startActivity(intent);
        });


        // Sự kiện nút "Học"
        btnLearn.setOnClickListener(view -> {
            Intent intent = new Intent(SetFlashCardActivity.this, LearnActivity.class);
            intent.putExtra("setId", setId);
            startActivity(intent);
        });

        // Sự kiện nút "Kiểm tra"
        btnTest.setOnClickListener(view -> {
            btnTest.setVisibility(View.GONE);

            // Chưa có TestActivity nên tạm thời comment lại
            // Intent intent = new Intent(SetFlashCardActivity.this, TestActivity.class);
            // intent.putExtra("setId", setId);
            // startActivity(intent);
        });

    }
}
