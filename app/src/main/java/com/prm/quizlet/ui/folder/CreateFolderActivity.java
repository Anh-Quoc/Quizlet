package com.prm.quizlet.ui.folder;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.prm.quizlet.QuizletDatabase;
import com.prm.quizlet.entity.Folder;

import com.prm.quizlet.R;
import com.prm.quizlet.MainActivity;

public class CreateFolderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_folder);

        ImageView btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView btnDone = findViewById(R.id.btn_done);
        EditText etFolderName = findViewById(R.id.et_folder_name);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = etFolderName.getText().toString().trim();
                if (folderName.isEmpty()) {
                    Toast.makeText(CreateFolderActivity.this, "Please enter a folder name", Toast.LENGTH_SHORT).show();
                } else {
                    saveNewFolder(folderName);
                }
            }
        });
    }

    private void saveNewFolder(String folderName){
        // Insert folder in background thread
        new Thread(() -> {
            QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                    .fallbackToDestructiveMigration()
                    .build();
            Folder folder = new Folder();
            folder.name = folderName;
            folder.description = null;
            folder.created_at = null;
            long id = db.folderDao().insert(folder);
            runOnUiThread(() -> {
                if (id > 0) {
                    Toast.makeText(CreateFolderActivity.this, "Folder created!", Toast.LENGTH_SHORT).show();
                    // Navigate to FolderActivity with the new folder's ID
                    Intent intent = new Intent(CreateFolderActivity.this, com.prm.quizlet.ui.folder.FolderActivity.class);
                    intent.putExtra("folder_id", (int) id);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateFolderActivity.this, "Failed to create folder", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
} 