package com.example.shealert;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;

public class CheckInReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 101;
    private static final int AUTO_SOS_REQUEST = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {


        android.util.Log.d("CHECKIN", "Receiver triggered at: " + System.currentTimeMillis());
        SharedPreferences prefs =
                context.getSharedPreferences("CheckInPrefs", Context.MODE_PRIVATE);

        if (!prefs.getBoolean("isActive", false)) {
            android.util.Log.d("CHECKIN", "Ignored - check-in already cancelled");
            return;
        }

        boolean autoSend = intent.getBooleanExtra("AUTO_SEND", false);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // =========================
        // AUTO SOS TRIGGER
        // =========================
        if (autoSend) {

            android.util.Log.d("CHECKIN", "AUTO SOS BRANCH EXECUTED");

            if (manager != null) {
                manager.cancel(NOTIFICATION_ID);
            }

            prefs.edit()
                    .putBoolean("isActive", false)
                    .remove("endTime")
                    .apply();

            SOSHelper.sendSOS(context, true);

            return;
        }

        // =========================
        // SHOW CHECK-IN EXPIRED NOTIFICATION
        // =========================

        Intent sosIntent = new Intent(context, SOSActivity.class);
        sosIntent.putExtra("GRACE_MODE", true);
        sosIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                sosIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = "checkin_alert";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Check-In Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Check-In Expired")
                        .setContentText("Tap to confirm your safety")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
        context.startActivity(sosIntent);

        // =========================
        // SCHEDULE AUTO SOS AFTER 30 SEC
        // =========================

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent autoIntent = new Intent(context, CheckInReceiver.class);
        autoIntent.putExtra("AUTO_SEND", true);

        PendingIntent autoPendingIntent = PendingIntent.getBroadcast(
                context,
                AUTO_SOS_REQUEST,
                autoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + 30000;

        if (alarmManager != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {

                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            autoPendingIntent
                    );

                } else {

                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            autoPendingIntent
                    );
                }

            } else {

                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        autoPendingIntent
                );
            }
        }
    }
}