package com.example.shealert;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchAutoSOS;
    private Switch switchReminder;

    private SharedPreferences prefs;

    private static final String PREF_NAME = "settings";
    private static final String KEY_AUTO_SOS = "auto_sos";
    private static final String KEY_REMINDER = "checkin_reminder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 🔹 Toolbar Setup
        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings & Privacy");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // 🔹 Initialize Views
        switchAutoSOS = findViewById(R.id.switchAutoSOS);
        switchReminder = findViewById(R.id.switchReminder);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {

        boolean autoSOS = prefs.getBoolean(KEY_AUTO_SOS, true);
        boolean reminder = prefs.getBoolean(KEY_REMINDER, true);

        switchAutoSOS.setChecked(autoSOS);
        switchReminder.setChecked(reminder);
    }

    private void setupListeners() {

        switchAutoSOS.setOnCheckedChangeListener((buttonView, isChecked) -> {

            prefs.edit()
                    .putBoolean(KEY_AUTO_SOS, isChecked)
                    .apply();

            Toast.makeText(this,
                    isChecked ? "Auto Silent SOS Enabled"
                            : "Auto Silent SOS Disabled",
                    Toast.LENGTH_SHORT).show();
        });

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {

            prefs.edit()
                    .putBoolean(KEY_REMINDER, isChecked)
                    .apply();

            Toast.makeText(this,
                    isChecked ? "Check-In Reminder Enabled"
                            : "Check-In Reminder Disabled",
                    Toast.LENGTH_SHORT).show();
        });
    }
}