package com.prm.quizlet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.prm.quizlet.DAO.FlashcardDAO;
import com.prm.quizlet.DAO.SetDAO;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Sets;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class EditSetActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private ImageView btnClose, btnDone;
    private Button btnAddCard;
    private LinearLayout flashcardContainer;
    private QuizletDatabase db;
    private int setId;
    private Sets existingSet;
    private List<Flashcards> existingFlashcards;
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            importFlashcardsFromJson(uri);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to import JSON", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_set);

        setId = getIntent().getIntExtra("set_id", -1);
        if (setId == -1) {
            finish();
            return;
        }

        db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnClose = findViewById(R.id.btn_close);
        btnDone = findViewById(R.id.btn_done);
        btnAddCard = findViewById(R.id.btn_add_card);
        flashcardContainer = findViewById(R.id.flashcard_container);

        btnClose.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> updateSetAndFlashcards());
        btnAddCard.setOnClickListener(v -> addFlashcardView(null));
        Button btnImportJson = findViewById(R.id.btn_import_flashcards);
        btnImportJson.setOnClickListener(v -> openFilePicker());

        loadSetDetails();
    }

    private void loadSetDetails() {
        new Thread(() -> {
            SetDAO setDAO = db.setDao();
            FlashcardDAO flashcardDAO = db.flashcardDao();

            existingSet = setDAO.getById(setId);
            existingFlashcards = flashcardDAO.getBySetId(setId);

            runOnUiThread(() -> {
                etTitle.setText(existingSet.title);
                etDescription.setText(existingSet.description);

                for (Flashcards card : existingFlashcards) {
                    addFlashcardView(card);
                }
            });
        }).start();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Choose a JSON file"));
    }

    private void importFlashcardsFromJson(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();

        JSONArray jsonArray = new JSONArray(builder.toString());
        runOnUiThread(() -> {
//            flashcardContainer.removeAllViews();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                if (obj != null) {
                    String front = obj.optString("front_text", "");
                    String back = obj.optString("back_text", "");
                    if (!front.isEmpty() && !back.isEmpty()) {
                        Flashcards flashcard = new Flashcards();
                        flashcard.front_text = front;
                        flashcard.back_text = back;
                        addFlashcardView(flashcard);
                    }
                }
            }
        });
    }


    private void addFlashcardView(Flashcards flashcard) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View flashcardView = inflater.inflate(R.layout.item_flashcards_create, flashcardContainer, false);

        EditText etTerm = flashcardView.findViewById(R.id.et_term);
        EditText etDefinition = flashcardView.findViewById(R.id.et_definition);
        ImageView btnDelete = flashcardView.findViewById(R.id.btn_delete);

        if (flashcard != null) {
            etTerm.setText(flashcard.front_text);
            etDefinition.setText(flashcard.back_text);
            flashcardView.setTag(flashcard);
        }

        btnDelete.setOnClickListener(v -> flashcardContainer.removeView(flashcardView));
        flashcardContainer.addView(flashcardView);
    }

    private void updateSetAndFlashcards() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            SetDAO setDAO = db.setDao();
            FlashcardDAO flashcardDAO = db.flashcardDao();

            existingSet.title = title;
            existingSet.description = description;
            existingSet.updated_at = String.valueOf(System.currentTimeMillis());

            setDAO.update(existingSet);
            flashcardDAO.deleteBySetId(setId);

            List<Flashcards> newFlashcards = new ArrayList<>();
            for (int i = 0; i < flashcardContainer.getChildCount(); i++) {
                View cardView = flashcardContainer.getChildAt(i);
                EditText etTerm = cardView.findViewById(R.id.et_term);
                EditText etDefinition = cardView.findViewById(R.id.et_definition);

                String term = etTerm.getText().toString().trim();
                String definition = etDefinition.getText().toString().trim();

                if (!term.isEmpty() && !definition.isEmpty()) {
                    Flashcards card = new Flashcards();
                    card.set_id = setId;
                    card.front_text = term;
                    card.back_text = definition;
                    card.created_at = String.valueOf(System.currentTimeMillis());
                    newFlashcards.add(card);
                }
            }

            flashcardDAO.insertAll(newFlashcards);

            runOnUiThread(() -> {
                Toast.makeText(this, "Set updated!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}