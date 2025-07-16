package com.prm.quizlet;

import android.content.Intent;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.prm.quizlet.entity.Folder;
import com.prm.quizlet.entity.Sets;
import com.prm.quizlet.ui.folder.CreateFolderActivity;
import com.prm.quizlet.fragment.BottomNavFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BottomNavFragment.OnBottomNavClickListener {
    private static final String PREFS_NAME = "study_prefs";
    private static final String LAST_STUDY_DATE_KEY = "last_study_date";
    private static final String STREAK_COUNT_KEY = "streak_count";
    private QuizletDatabase db;
    private FolderAdapter folderAdapter;
    private SetsAdapter setsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showCurrentWeek();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        db = QuizletDatabase.getInstance(getApplicationContext());

        handleStudyStreak();
        setDailyReminder8AM();


        // Initialize view
        EditText searchBar = findViewById(R.id.search_bar);

        RecyclerView rvFolders = findViewById(R.id.rvFolders);
        rvFolders.setLayoutManager(new LinearLayoutManager(this));
        new Thread(() -> {
            List<Folder> folders = db.folderDao().getAll();

            runOnUiThread(() -> {
                folderAdapter = new FolderAdapter(folders, folder -> {
                    Intent intent = new Intent(MainActivity.this, com.prm.quizlet.ui.folder.FolderActivity.class);
                    intent.putExtra("folder_id", folder.id);
                    startActivity(intent);
                });
                rvFolders.setAdapter(folderAdapter);
            });
        }).start();

        RecyclerView rvSets = findViewById(R.id.rvSets);
        rvSets.setLayoutManager(new LinearLayoutManager(this));
        new Thread(() -> {
            List<Sets> sets = db.setDao().getAll();

            runOnUiThread(() -> {
                setsAdapter = new SetsAdapter(sets);
                rvSets.setAdapter(setsAdapter);
            });
        }).start();
    }

    @Override
    public void onHomeClick() {
        finish();
        startActivity(getIntent());
        overridePendingTransition(0, 0);
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
            startActivity(new android.content.Intent(this, CreateFolderActivity.class));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Đã cấp quyền, bạn có thể hiện thông báo sau này
                Toast.makeText(this, "Đã cấp quyền thông báo. Bạn sẽ nhận được nhắc nhở hàng ngày!", Toast.LENGTH_SHORT).show();
            } else {
                // Người dùng từ chối quyền, có thể hiện dialog giải thích hoặc disable chức năng
                new AlertDialog.Builder(this)
                        .setTitle("Quyền thông báo bị từ chối")
                        .setMessage("Bạn cần cấp quyền thông báo để nhận được nhắc nhở học bài mỗi ngày.\nBạn có thể cấp lại quyền trong phần Cài đặt ứng dụng!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }

    private void showCurrentWeek() {
        // Tìm chủ nhật tuần này
        Calendar calendar = Calendar.getInstance();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK); // Chủ nhật = 1, Thứ hai = 2
        calendar.add(Calendar.DATE, Calendar.SUNDAY - todayIndex);

        // Hiển thị số ngày và highlight ngày hiện tại
        Calendar todayCal = Calendar.getInstance();
        for (int i = 1; i <= 7; i++) {
            int dayNum = calendar.get(Calendar.DAY_OF_MONTH);

            int dayResId = getResources().getIdentifier("day" + i, "id", getPackageName());
            TextView dayView = findViewById(dayResId);

            dayView.setText(String.valueOf(dayNum));

            int dotResId = getResources().getIdentifier("dot" + i, "id", getPackageName());
            View dotView = findViewById(dotResId);

            // Check nếu là ngày hôm nay
            boolean isToday = (calendar.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)) &&
                    (calendar.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR));
            if (isToday) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dayView.setTextAppearance(R.style.CalendarNumActive);
                } else {
                    dayView.setTextColor(getResources().getColor(R.color.black)); // Thêm màu #FF9800 vào colors.xml nếu dùng cách này
                }
                dotView.setVisibility(View.VISIBLE);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dayView.setTextAppearance(R.style.CalendarNum);
                } else {
                    dayView.setTextColor(getResources().getColor(R.color.black));
                }
                dotView.setVisibility(View.GONE);
            }
            calendar.add(Calendar.DATE, 1);
        }
    }

    private void handleStudyStreak() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastDay = prefs.getString(LAST_STUDY_DATE_KEY, null);
        int streak = prefs.getInt(STREAK_COUNT_KEY, 0);
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        boolean isConsecutive = false;

        if (lastDay != null && !lastDay.equals(todayStr)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date lastDate = sdf.parse(lastDay);
                Date todayDate = sdf.parse(todayStr);

                long diff = todayDate.getTime() - lastDate.getTime();
                long days = diff / (1000 * 60 * 60 * 24);

                if (days == 1) {
                    isConsecutive = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (lastDay == null || !lastDay.equals(todayStr)) {
            prefs.edit().putString(LAST_STUDY_DATE_KEY, todayStr).apply();
        }

        if (isConsecutive) {
            streak++;
            prefs.edit().putInt(STREAK_COUNT_KEY, streak).apply();
            sendStreakNotification(streak);
        } else if (lastDay == null || !lastDay.equals(todayStr)) {
            // Reset streak nếu ngày không liên tiếp
            streak = 1;
            prefs.edit().putInt(STREAK_COUNT_KEY, streak).apply();
        }
    }

    private void sendStreakNotification(int streak) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "streak_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Streak Noti", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        String content = "Bạn đã học " + streak + " ngày liên tiếp! Giữ vững chuỗi ngày học nhé 💪";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Chúc mừng!")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);
        manager.notify(2001, builder.build());
    }

    private void setDailyReminder8AM() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Nếu đã quá 8h sáng hôm nay, đặt cho ngày mai
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                800,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }
}