package com.prm.quizlet.ui.folder;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.prm.quizlet.QuizletDatabase;
import com.prm.quizlet.entity.Folder;
import androidx.room.Room;

import com.prm.quizlet.R;

public class FolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_folder);

        int folderId = getIntent().getIntExtra("folder_id", -1);
        if (folderId == -1) {
            Toast.makeText(this, "No folder ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView folderTitle = findViewById(R.id.folder_title);
        // Load folder from database in background
        new Thread(() -> {
            QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();
            Folder folder = db.folderDao().getById((int) folderId);
            runOnUiThread(() -> {
                if (folder != null) {
                    folderTitle.setText(folder.name != null ? folder.name : "Untitled folder");
                } else {
                    folderTitle.setText("Folder not found");
                }
            });
        }).start();
    }
}