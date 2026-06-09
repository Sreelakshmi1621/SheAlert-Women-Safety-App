package com.example.shealert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CheckInActivity extends AppCompatActivity {

    TextView txtTimer;
    Button btnSafe;
    CountDownTimer timer;
    SharedPreferences prefs;

    private static final String PREF_NAME = "CheckInPrefs";
    private static final String KEY_END_TIME = "endTime";
    private static final String KEY_ACTIVE = "isActive";

    boolean warningGiven = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        Button btnPickTime = findViewById(R.id.btnPickTime);
        btnSafe = findViewById(R.id.btnSafe);
        txtTimer = findViewById(R.id.txtTimer);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        checkExistingTimer();

        btnPickTime.setOnClickListener(v -> {

            Calendar now = Calendar.getInstance();

            TimePickerDialog dialog = new TimePickerDialog(this,
                    (view, hour, minute) -> {

                        Calendar arrival = Calendar.getInstance();
                        arrival.set(Calendar.HOUR_OF_DAY, hour);
                        arrival.set(Calendar.MINUTE, minute);
                        arrival.set(Calendar.SECOND, 0);
                        arrival.set(Calendar.MILLISECOND, 0);

                        if (arrival.getTimeInMillis() <= System.currentTimeMillis()) {
                            arrival.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        long endTime = arrival.getTimeInMillis();
                        long diff = endTime - System.currentTimeMillis();
                        android.util.Log.d(
                                "CHECKIN",
                                "diff ms = " + diff +
                                        "  diff sec = " + (diff / 1000)
                        );

                        if (diff <= 0) {
                            diff = 60000; // minimum 1 minute safety
                        }

                        prefs.edit()
                                .putLong(KEY_END_TIME, endTime)
                                .putBoolean(KEY_ACTIVE, true)
                                .apply();

                        scheduleAlarm(endTime);

                        NotificationHelper.sendNotification(
                                "Check-In Started",
                                "You started a safety check-in.",
                                "warning"
                        );

                        startTimer(diff);

                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true);

            dialog.show();
        });

        btnSafe.setOnClickListener(v -> cancelCheckIn(true));
    }

    private void scheduleAlarm(long endTime) {

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, CheckInReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {

            alarmManager.cancel(pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            endTime,
                            pendingIntent
                    );

                } else {

                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            endTime,
                            pendingIntent
                    );
                }

            } else {

                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        endTime,
                        pendingIntent
                );
            }

        }
    }

    private void startTimer(long millis) {

        if (timer != null) timer.cancel();

        btnSafe.setEnabled(true);
        warningGiven = false;

        timer = new CountDownTimer(millis, 1000) {

            @Override
            public void onTick(long remaining) {

                long minutes = remaining / 60000;
                long seconds = (remaining % 60000) / 1000;

                txtTimer.setText(String.format("%02d : %02d", minutes, seconds));

                if (remaining <= 60000) {
                    txtTimer.setTextColor(Color.RED);
                } else if (remaining <= 120000) {
                    txtTimer.setTextColor(Color.parseColor("#FF9800"));
                } else {
                    txtTimer.setTextColor(Color.parseColor("#4A148C"));
                }

                if (remaining <= 120000 && !warningGiven) {
                    warningGiven = true;
                    vibrate(300);
                }
            }

            @Override
            public void onFinish() {

                txtTimer.setText("00 : 00");

                finish();
            }
        }.start();
    }

    private void cancelCheckIn(boolean isUserSafe) {

        if (timer != null) timer.cancel();

        prefs.edit()
                .putBoolean(KEY_ACTIVE, false)
                .remove(KEY_END_TIME)
                .apply();

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, CheckInReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        btnSafe.setEnabled(false);
        txtTimer.setText("SAFE ✅");

        if (isUserSafe) {
            NotificationHelper.sendNotification(
                    "Check-In Completed",
                    "You marked yourself safe successfully.",
                    "info"
            );
        }
    }

    private void checkExistingTimer() {

        boolean isActive = prefs.getBoolean(KEY_ACTIVE, false);
        if (!isActive) return;

        long endTime = prefs.getLong(KEY_END_TIME, 0);
        long remaining = endTime - System.currentTimeMillis();

        if (remaining > 0) {

            startTimer(remaining);

        } else {

            // Timer already expired
            // AlarmManager + CheckInReceiver already handled it

            prefs.edit()
                    .putBoolean(KEY_ACTIVE, false)
                    .remove(KEY_END_TIME)
                    .apply();

            txtTimer.setText("-- : --");
            btnSafe.setEnabled(false);
        }
    }

    private void vibrate(long duration) {

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (vibrator != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                duration,
                                VibrationEffect.DEFAULT_AMPLITUDE
                        )
                );

            } else {

                vibrator.vibrate(duration);
            }
        }
    }
}