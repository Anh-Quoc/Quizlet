package com.prm.quizlet.ui.folder;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.prm.quizlet.library.LibraryAdapter;
import com.prm.quizlet.library.SetItem;
import com.prm.quizlet.library.LibraryItem;
import com.prm.quizlet.entity.Sets;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.util.Log;

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
        RecyclerView recyclerView = findViewById(R.id.recyclerViewLibrary);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<LibraryItem> displayList = new ArrayList<>();
        View emptyFolderView = findViewById(R.id.emptyFolderView);

        // Menu button for folder actions
        findViewById(R.id.btn_menu).setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FolderActivity.this);
            View sheetView = LayoutInflater.from(FolderActivity.this).inflate(R.layout.layout_bottom_sheet_folder_actions, null);
            bottomSheetDialog.setContentView(sheetView);

            // Add flashcard set to folder
            sheetView.findViewById(R.id.btn_add_flashcard_set).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                showAddSetDialog(folderId);
            });
            // Rename folder
            sheetView.findViewById(R.id.btn_rename_folder).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                // Show dialog to rename folder
                showRenameFolderDialog(folderId, folderTitle);
            });
            // Delete folder but keep flashcard sets
            sheetView.findViewById(R.id.btn_delete_folder).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                // TODO: Implement delete folder but keep flashcard sets
                showDeleteFolderDialog(folderId);
            });
            // Add existing flashcard set to folder
            sheetView.findViewById(R.id.btn_add_existing_flashcard_set).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                showAddExistingSetDialog(folderId);
            });

            bottomSheetDialog.show();
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Load folder and sets from database in background
        new Thread(() -> {
            QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();
            Folder folder = db.folderDao().getById((int) folderId);
            List<Sets> setsInFolder = db.setDao().getByFolderId(folderId);
            for (Sets set : setsInFolder) {
                int flashcardCount = db.flashcardDao().countBySetId(set.id);
                displayList.add(new SetItem(set, flashcardCount));
            }
            runOnUiThread(() -> {
                if (folder != null) {
                    folderTitle.setText(folder.name != null ? folder.name : "Untitled folder");
                } else {
                    folderTitle.setText("Folder not found");
                    Toast.makeText(FolderActivity.this, "Folder not found (ID: " + folderId + ")", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (displayList.isEmpty()) {
                    emptyFolderView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyFolderView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                LibraryAdapter adapter = new LibraryAdapter(displayList, set -> {
                    Intent intent = new Intent(FolderActivity.this, com.prm.quizlet.SetDetailsActivity.class);
                    intent.putExtra("set_id", set.id);
                    startActivity(intent);
                }, s -> showRemoveSetFromFolderDialog(s, folderId));
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void showRenameFolderDialog(int folderId, TextView folderTitle) {
        // Show a dialog to enter new folder name
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Rename Folder");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                new Thread(() -> {
                    QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                        .fallbackToDestructiveMigration()
                        .build();
                    Folder folder = db.folderDao().getById(folderId);
                    if (folder != null) {
                        folder.name = newName;
                        db.folderDao().update(folder);
                        runOnUiThread(() -> folderTitle.setText(newName));
                    }
                }).start();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteFolderDialog(int folderId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Folder");
        builder.setMessage("Are you sure you want to delete this folder? Flashcard sets will be kept.");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            new Thread(() -> {
                QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                    .fallbackToDestructiveMigration()
                    .build();
                db.folderDao().deleteById(folderId);
                runOnUiThread(this::finish);
            }).start();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddSetDialog(int folderId) {
        // Use a custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_set, null);
        TextInputLayout titleLayout = dialogView.findViewById(R.id.inputLayoutSetTitle);
        TextInputEditText titleInput = dialogView.findViewById(R.id.inputSetTitle);
        TextInputLayout descLayout = dialogView.findViewById(R.id.inputLayoutSetDescription);
        TextInputEditText descInput = dialogView.findViewById(R.id.inputSetDescription);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Add Flashcard Set");
        builder.setView(dialogView);
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String setTitle = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
                String setDesc = descInput.getText() != null ? descInput.getText().toString().trim() : "";
                if (setTitle.isEmpty()) {
                    titleLayout.setError("Title is required");
                    return;
                } else {
                    titleLayout.setError(null);
                }
                new Thread(() -> {
                    QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                        .fallbackToDestructiveMigration()
                        .build();
                    Sets newSet = new Sets();
                    newSet.folder_id = folderId;
                    newSet.title = setTitle;
                    newSet.description = setDesc;
                    newSet.is_public = 0;
                    String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
                    newSet.created_at = now;
                    newSet.updated_at = now;
                    db.setDao().insert(newSet);
                    // Refresh list
                    List<Sets> setsInFolder = db.setDao().getByFolderId(folderId);
                    List<LibraryItem> displayList = new ArrayList<>();
                    for (Sets set : setsInFolder) {
                        int flashcardCount = db.flashcardDao().countBySetId(set.id);
                        displayList.add(new SetItem(set, flashcardCount));
                    }
                    runOnUiThread(() -> {
                        RecyclerView recyclerView = findViewById(R.id.recyclerViewLibrary);
                        View emptyFolderView = findViewById(R.id.emptyFolderView);
                        if (displayList.isEmpty()) {
                            emptyFolderView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyFolderView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        LibraryAdapter adapter = new LibraryAdapter(displayList, set -> {
                            Intent intent = new Intent(FolderActivity.this, com.prm.quizlet.SetDetailsActivity.class);
                            intent.putExtra("set_id", set.id);
                            startActivity(intent);
                        }, s -> showRemoveSetFromFolderDialog(s, folderId));
                        recyclerView.setAdapter(adapter);
                        Toast.makeText(this, "Set added", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }).start();
            });
        });
        dialog.show();
    }

    private void showAddExistingSetDialog(int folderId) {
        new Thread(() -> {
            QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();
            List<Sets> availableSets = db.setDao().getAll();
            // Filter sets not in this folder
            List<Sets> filteredSets = new ArrayList<>();
            for (Sets set : availableSets) {
                if (set.folder_id == null || set.folder_id != folderId) {
                    filteredSets.add(set);
                }
            }
            runOnUiThread(() -> {
                if (filteredSets.isEmpty()) {
                    Toast.makeText(this, "No available sets to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] setTitles = new String[filteredSets.size()];
                for (int i = 0; i < filteredSets.size(); i++) {
                    setTitles[i] = filteredSets.get(i).title;
                }
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Select a set to add");
                builder.setItems(setTitles, (dialog, which) -> {
                    Sets selectedSet = filteredSets.get(which);
                    new Thread(() -> {
                        selectedSet.folder_id = folderId;
                        db.setDao().update(selectedSet);
                        // Refresh list
                        List<Sets> setsInFolder = db.setDao().getByFolderId(folderId);
                        List<LibraryItem> displayList = new ArrayList<>();
                        for (Sets set : setsInFolder) {
                            int flashcardCount = db.flashcardDao().countBySetId(set.id);
                            displayList.add(new SetItem(set, flashcardCount));
                        }
                        runOnUiThread(() -> {
                            RecyclerView recyclerView = findViewById(R.id.recyclerViewLibrary);
                            View emptyFolderView = findViewById(R.id.emptyFolderView);
                            if (displayList.isEmpty()) {
                                emptyFolderView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                emptyFolderView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                            LibraryAdapter adapter = new LibraryAdapter(displayList, set -> {
                                Intent intent = new Intent(FolderActivity.this, com.prm.quizlet.SetDetailsActivity.class);
                                intent.putExtra("set_id", set.id);
                                startActivity(intent);
                            });
                            recyclerView.setAdapter(adapter);
                            Toast.makeText(this, "Set added to folder", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            });
        }).start();
    }

    private void showRemoveSetFromFolderDialog(Sets set, int folderId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Remove from Folder");
        builder.setMessage("Are you sure you want to remove this set from the folder?");
        builder.setPositiveButton("Remove", (dialog, which) -> {
            new Thread(() -> {
                QuizletDatabase db = Room.databaseBuilder(getApplicationContext(), QuizletDatabase.class, "quizlet.db")
                    .fallbackToDestructiveMigration()
                    .build();
                set.folder_id = null;
                db.setDao().update(set);
                // Refresh list
                List<Sets> setsInFolder = db.setDao().getByFolderId(folderId);
                List<LibraryItem> displayList = new ArrayList<>();
                for (Sets s : setsInFolder) {
                    int flashcardCount = db.flashcardDao().countBySetId(s.id);
                    displayList.add(new SetItem(s, flashcardCount));
                }
                runOnUiThread(() -> {
                    RecyclerView recyclerView = findViewById(R.id.recyclerViewLibrary);
                    View emptyFolderView = findViewById(R.id.emptyFolderView);
                    if (displayList.isEmpty()) {
                        emptyFolderView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyFolderView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    LibraryAdapter adapter = new LibraryAdapter(displayList, s -> {
                        Intent intent = new Intent(FolderActivity.this, com.prm.quizlet.SetDetailsActivity.class);
                        intent.putExtra("set_id", s.id);
                        startActivity(intent);
                    }, s -> showRemoveSetFromFolderDialog(s, folderId));
                    recyclerView.setAdapter(adapter);
                    Toast.makeText(this, "Set removed from folder", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}