package com.prm.quizlet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.prm.quizlet.entity.Flashcards;
import com.prm.quizlet.entity.Folder;
import com.prm.quizlet.fragment.BottomNavFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavFragment.OnBottomNavClickListener {
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
    }

    @Override
    public void onHomeClick() {
        // Chuyển về Home hoặc xử lý gì đó
    }

    @Override
    public void onCreateClick() {
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

    @Override
    public void onLibraryClick() {
        // Mở LibraryActivity
        Intent intent = new Intent(this, LibraryActivity.class);
        intent.putExtra("selected_nav_id", R.id.btn_library_nav);
        startActivity(intent);
    }

    @Override
    public void onFreeTrialClick() {
        // Mở trang Free trial
    }
}