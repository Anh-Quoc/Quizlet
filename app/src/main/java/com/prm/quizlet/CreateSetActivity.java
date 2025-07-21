package com.prm.quizlet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CreateSetActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_IMPORT_JSON = 1001;

    private EditText etTitle, etDescription;
    private ImageView btnClose, btnDone;
    private Button btnAddCard;
    private LinearLayout flashcardContainer;
    private Button btnImportFlashcards;
    private QuizletDatabase db;
    private final ActivityResultLauncher<Intent> someActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            importFlashcardsFromJson(uri);
                        } catch (IOException | JSONException e) {
                            Toast.makeText(this, "Failed to import JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
    );

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
        btnImportFlashcards = findViewById(R.id.btn_import_flashcards);

        // Set listeners
        btnClose.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> saveSetWithFlashcards());
        btnAddCard.setOnClickListener(v -> addFlashcardView("", ""));
        btnImportFlashcards.setOnClickListener(v -> openJsonFilePicker());

        // Initial card
        addFlashcardView("", "");
    }

    private void openJsonFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        someActivityLauncher.launch(Intent.createChooser(intent, "Select JSON File"));
    }

    private void importFlashcardsFromJson(Uri uri) throws IOException, JSONException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();

        JSONArray jsonArray = new JSONArray(builder.toString());
//        flashcardContainer.removeAllViews();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String frontText = obj.optString("front_text", "");
            String backText = obj.optString("back_text", "");
            addFlashcardView(frontText, backText);
        }
    }

    private void addFlashcardView(String term, String definition) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View flashcardView = inflater.inflate(R.layout.item_flashcards_create, flashcardContainer, false);

        EditText etTerm = flashcardView.findViewById(R.id.et_term);
        EditText etDefinition = flashcardView.findViewById(R.id.et_definition);

        etTerm.setText(term);
        etDefinition.setText(definition);

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


