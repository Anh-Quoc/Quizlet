package com.prm.quizlet;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.prm.quizlet.DAO.FolderDAO;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Folder;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private QuizletDatabase db;
    private FolderAdapter folderAdapter;
    private FlashcardAdapter flashcardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(),
                        QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();

        SampleDataPopulator.populateIfEmpty(db);

        EditText searchBar = findViewById(R.id.search_bar);

        RecyclerView rvFolders = findViewById(R.id.rvFolders);
        rvFolders.setLayoutManager(new LinearLayoutManager(this));
        new Thread(() -> {
            List<Folder> folders = db.folderDao().getAll();

            runOnUiThread(() -> {
                folderAdapter = new FolderAdapter(folders);
                rvFolders.setAdapter(folderAdapter);
            });
        }).start();

        RecyclerView rvFlashcards = findViewById(R.id.rvFlashcards);
        rvFlashcards.setLayoutManager(new LinearLayoutManager(this));
        new Thread(() -> {
            List<Flashcards> flashcards = db.flashcardDao().getAll();

            runOnUiThread(() -> {
                flashcardAdapter = new FlashcardAdapter(flashcards);
                rvFlashcards.setAdapter(flashcardAdapter);
            });
        }).start();

        LinearLayout btnCreate = findViewById(R.id.btn_create_nav);
        btnCreate.setOnClickListener(view -> showCreateBottomSheet());
    }

    private void showCreateBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_create, null);
        bottomSheetDialog.setContentView(sheetView);

        sheetView.findViewById(R.id.btn_flashcard_set).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
        sheetView.findViewById(R.id.btn_folder).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
        sheetView.findViewById(R.id.btn_class).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}