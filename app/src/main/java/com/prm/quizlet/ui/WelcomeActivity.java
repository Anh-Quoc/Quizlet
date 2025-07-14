package com.prm.quizlet.ui;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.prm.quizlet.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean welcomeShown = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getBoolean("welcome_shown", false);

        if (welcomeShown) {
            startActivity(new Intent(this, com.prm.quizlet.MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button btn_get_started = findViewById(R.id.btn_get_started);

        btn_get_started.setOnClickListener(v -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("welcome_shown", true)
                .apply();
            Intent intent = new Intent(WelcomeActivity.this, com.prm.quizlet.MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
