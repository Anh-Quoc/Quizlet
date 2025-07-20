package com.prm.quizlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.DAO.SetDAO;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Sets;

public class CreateSetActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private ImageView btnClose, btnDone;
    private Button btnAddCard;
    private LinearLayout flashcardContainer;

    private QuizletDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_set);

        db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnClose = findViewById(R.id.btn_close);
        btnDone = findViewById(R.id.btn_done);
        btnAddCard = findViewById(R.id.btn_add_card);
        flashcardContainer = findViewById(R.id.flashcard_container);

        // Set listeners
        btnClose.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> saveSetWithFlashcards());
        btnAddCard.setOnClickListener(v -> addFlashcardView());

        // Initial card
        addFlashcardView();
    }

    private void addFlashcardView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View flashcardView = inflater.inflate(R.layout.item_flashcards_create, flashcardContainer, false);

        // Handle delete button
        ImageView btnDelete = flashcardView.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> flashcardContainer.removeView(flashcardView));

        flashcardContainer.addView(flashcardView);
    }

    private void saveSetWithFlashcards() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            SetDAO setDAO = db.setDao();
            FlashcardDAO flashcardDAO = db.flashcardDao();

            Sets existing = setDAO.findByTitle(title);
            if (existing != null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "A set with this title already exists", Toast.LENGTH_SHORT).show());
                return;
            }

            Sets newSet = new Sets();
            newSet.title = title;
            newSet.description = description;
            newSet.folder_id = null;
            newSet.is_public = 0;
            newSet.created_at = String.valueOf(System.currentTimeMillis());
            newSet.updated_at = newSet.created_at;

            long setId = setDAO.insert(newSet);

            if (setId > 0) {
                for (int i = 0; i < flashcardContainer.getChildCount(); i++) {
                    View cardView = flashcardContainer.getChildAt(i);
                    EditText etTerm = cardView.findViewById(R.id.et_term);
                    EditText etDefinition = cardView.findViewById(R.id.et_definition);

                    String term = etTerm.getText().toString().trim();
                    String definition = etDefinition.getText().toString().trim();

                    if (!term.isEmpty() && !definition.isEmpty()) {
                        Flashcards flashcard = new Flashcards();
                        flashcard.set_id = (int) setId;
                        flashcard.front_text = term;
                        flashcard.back_text = definition;
                        flashcard.created_at = String.valueOf(System.currentTimeMillis());
                        flashcardDAO.insert(flashcard);
                    }
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Set and flashcards saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to create set.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}


