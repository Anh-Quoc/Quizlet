package com.prm.quizlet;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.prm.quizlet.entity.Sets;
import com.prm.quizlet.fragment.BottomNavFragment;
import com.prm.quizlet.library.LibraryAdapter;
import com.prm.quizlet.library.LibraryItem;
import com.prm.quizlet.library.MonthHeader;
import com.prm.quizlet.library.SetItem;
import com.prm.quizlet.FolderAdapter;
import com.prm.quizlet.entity.Folder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LibraryActivity extends AppCompatActivity implements BottomNavFragment.OnBottomNavClickListener {
    private QuizletDatabase db;
    private LibraryAdapter adapter;

    private List<Sets> allSets = new ArrayList<>();
    private List<LibraryItem> displayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        int selectedNavId = getIntent().getIntExtra("selected_nav_id", R.id.btn_home_nav);
        BottomNavFragment bottomNavFragment = (BottomNavFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentBottomNav); // đảm bảo id đúng

        if (bottomNavFragment != null) {
            bottomNavFragment.setSelectedNavId(selectedNavId);
        }

        db = Room.databaseBuilder(getApplicationContext(),
                        QuizletDatabase.class, "quizlet.db")
                .fallbackToDestructiveMigration()
                .build();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLibrary);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TabLayout setup (nếu muốn chuyển tab)
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Flashcard sets"));
        tabLayout.addTab(tabLayout.newTab().setText("Practice tests"));
        tabLayout.addTab(tabLayout.newTab().setText("Folders"));
        tabLayout.addTab(tabLayout.newTab().setText("Class"));

        EditText edtFilter = findViewById(R.id.edtFilter);

        new Thread(() -> {
            allSets = db.setDao().getAllOrderByCreatedAtDesc();
            runOnUiThread(() -> updateDisplayList(""));
        }).start();

        edtFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateDisplayList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) { // Flashcard sets
                    updateDisplayList(edtFilter.getText().toString());
                } else if (position == 2) { // Folders
                    // Show folders
                    new Thread(() -> {
                        List<Folder> folders = db.folderDao().getAll();
                        runOnUiThread(() -> {
                            FolderAdapter folderAdapter = new FolderAdapter(folders, folder -> {
                                Intent intent = new Intent(LibraryActivity.this, com.prm.quizlet.ui.folder.FolderActivity.class);
                                intent.putExtra("folder_id", folder.id);
                                startActivity(intent);
                            });
                            recyclerView.setAdapter(folderAdapter);
                        });
                    }).start();
                } else {
                    // You can add logic for other tabs here
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateDisplayList(String query) {
        new Thread(() -> {
            Map<String, List<Sets>> monthMap = new LinkedHashMap<>();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

            for (Sets set : allSets) {
                if (!set.title.toLowerCase().contains(query.toLowerCase())) continue;
                Date date;
                try {
                    date = inputFormat.parse(set.created_at);
                } catch (Exception e) {
                    continue;
                }
                String monthText = "In " + monthFormat.format(date);
                if (!monthMap.containsKey(monthText)) monthMap.put(monthText, new ArrayList<>());
                monthMap.get(monthText).add(set);
            }

            List<LibraryItem> filteredList = new ArrayList<>();
            for (String month : monthMap.keySet()) {
                filteredList.add(new MonthHeader(month));
                for (Sets set : monthMap.get(month)) {
                    int flashcardCount = db.flashcardDao().countBySetId(set.id);
                    filteredList.add(new SetItem(set, flashcardCount));
                }
            }

            runOnUiThread(() -> {
                adapter = new LibraryAdapter(filteredList, set -> {
                    Intent intent = new Intent(LibraryActivity.this, SetDetailsActivity.class);
                    intent.putExtra("set_id", set.id);
                    startActivity(intent);
                });
                RecyclerView recyclerView = findViewById(R.id.recyclerViewLibrary);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
    @Override
    public void onHomeClick() {
        finish();
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
//        Intent intent = new Intent(this, LibraryActivity.class);
//        startActivity(intent);
    }

    @Override
    public void onFreeTrialClick() {
        // Mở trang Free trial
    }
}
