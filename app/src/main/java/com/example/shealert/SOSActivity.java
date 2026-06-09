package com.example.shealert;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import android.view.WindowManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.NotificationManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SOSActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private boolean graceMode = false;

    private View countdownOverlay;
    private TextView txtCountdown;
    private TextView txtGraceMessage;
    private CountDownTimer countdownTimer;

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d(
                "SOS_DEBUG",
                "SOSActivity created. graceMode="
                        + getIntent().getBooleanExtra("GRACE_MODE", false)
        );
        setContentView(R.layout.activity_sosactivity);

        // Remove Check-In notification if user opened it
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.cancel(101);
        }

// 🔴 Cancel AUTO SOS alarm if user opened the notification
        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent autoIntent = new Intent(this, CheckInReceiver.class);
        autoIntent.putExtra("AUTO_SEND", true);

        PendingIntent autoPendingIntent = PendingIntent.getBroadcast(
                this,
                2001,
                autoIntent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (autoPendingIntent != null && alarmManager != null) {
            alarmManager.cancel(autoPendingIntent);
            autoPendingIntent.cancel();
        }

        boolean autoSend = getIntent().getBooleanExtra("AUTO_SEND", false);

        if (autoSend) {
            sendSOS(true);

            Intent intent = new Intent(SOSActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
            return;
        }


        //setShowWhenLocked(true);
        //setTurnScreenOn(true);

        getWindow().addFlags(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        );


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        graceMode = getIntent().getBooleanExtra("GRACE_MODE", false);

        countdownOverlay = findViewById(R.id.countdownOverlay);
        txtCountdown = findViewById(R.id.txtCountdown);
        txtGraceMessage = findViewById(R.id.txtGraceMessage);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        findViewById(R.id.sosContainer).startAnimation(pulse);

        findViewById(R.id.btnSOS).setOnClickListener(v -> startManualCountdown());

        findViewById(R.id.btnCancel).setOnClickListener(v -> {

            if (countdownTimer != null) countdownTimer.cancel();
            stopBeepSound();

            // Cancel AUTO SOS alarm
            AlarmManager alarmManagerCancel =
                    (AlarmManager) getSystemService(ALARM_SERVICE);

            Intent cancelIntent = new Intent(this, CheckInReceiver.class);
            cancelIntent.putExtra("AUTO_SEND", true);

            PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
                    this,
                    2001,
                    cancelIntent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (cancelPendingIntent != null && alarmManagerCancel != null) {
                alarmManagerCancel.cancel(cancelPendingIntent);
                cancelPendingIntent.cancel();
            }

            countdownOverlay.setVisibility(View.GONE);
            finish();
        });

        if (graceMode) {
            startGraceCountdown();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.d("SOS_DEBUG", "SOSActivity destroyed");

    }
    // =========================
    // MANUAL SOS (5 seconds)
    // =========================
    private void startManualCountdown() {

        countdownOverlay.setVisibility(View.VISIBLE);
        txtGraceMessage.setText("Sending Emergency Alert...");

        countdownTimer = new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                txtCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                countdownOverlay.setVisibility(View.GONE);
                vibrateShort();
                sendSOS(false);

                Toast.makeText(
                        SOSActivity.this,
                        "Emergency alert sent successfully",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }

        }.start();
    }

    // =========================
    // GRACE MODE (30 seconds)
    // =========================
    private void startGraceCountdown() {

        countdownOverlay.setVisibility(View.VISIBLE);
        txtGraceMessage.setText("No response detected.\nSOS will be sent in");

        startBeepSound();

        countdownTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                txtCountdown.setText(String.valueOf(millisUntilFinished / 1000));
                vibrateCallStyle();
            }

            @Override
            public void onFinish() {

                stopBeepSound();
                countdownOverlay.setVisibility(View.GONE);

                sendSOS(true);

                Toast.makeText(SOSActivity.this,
                        "Emergency alert sent",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SOSActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }

        }.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopBeepSound();
    }
    // =========================
    // SOS LOGIC
    // =========================
    private void sendSOS(boolean isAuto) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("contacts");

        ref.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                Toast.makeText(this,
                        "No trusted contacts added",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                return;
            }

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {

                        double lat = 0;
                        double lng = 0;
                        boolean locationAvailable = false;

                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            locationAvailable = true;
                        }

                        int battery = getBatteryLevel();

                        String message;

                        if (locationAvailable) {
                            message = "🚨 EMERGENCY SOS ALERT 🚨\n\n"
                                    + "User may be in danger.\n\n"
                                    + "📍 Location:\nhttps://maps.google.com/?q="
                                    + lat + "," + lng + "\n\n"
                                    + "🔋 Battery Level: " + battery + "%";
                        } else {
                            message = "🚨 EMERGENCY SOS ALERT 🚨\n\n"
                                    + "Location unavailable.\n"
                                    + "🔋 Battery Level: " + battery + "%";
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String phone = ds.child("phone").getValue(String.class);

                            if (phone != null && !phone.isEmpty()) {

                                SmsManager smsManager = SmsManager.getDefault();
                                ArrayList<String> parts =
                                        smsManager.divideMessage(message);

                                smsManager.sendMultipartTextMessage(
                                        phone,
                                        null,
                                        parts,
                                        null,
                                        null
                                );
                            }
                        }

                        String triggerType = isAuto ? "Auto" : "Manual";

                        saveAlertAndNotification(
                                triggerType,
                                locationAvailable ? String.valueOf(lat) : null,
                                locationAvailable ? String.valueOf(lng) : null,
                                String.valueOf(battery)
                        );
                    });
        });
    }

    // =========================
    // SAVE ALERT + NOTIFICATION
    // =========================
    private void saveAlertAndNotification(String triggerType,
                                          String latitude,
                                          String longitude,
                                          String battery) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String timestamp = new SimpleDateFormat(
                "dd MMM yyyy - hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        DatabaseReference alertRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("alerts")
                .push();

        AlertModel alert = new AlertModel(
                triggerType,
                timestamp,
                latitude,
                longitude,
                battery
        );

        alertRef.setValue(alert);

        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("notifications")
                .push();

        notifRef.child("title").setValue(triggerType + " SOS Triggered");
        notifRef.child("message").setValue("Emergency alert sent to trusted contacts.");
        notifRef.child("timestamp").setValue(timestamp);
        notifRef.child("type").setValue("danger");
    }

    // =========================
    // BEEP METHODS
    // =========================
    private void startBeepSound() {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_beep);
            mediaPlayer.setLooping(true);
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopBeepSound() {

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // =========================
    // VIBRATION METHODS
    // =========================
    private void vibrateCallStyle() {

        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            800,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    )
            );
        } else {
            vibrator.vibrate(800);
        }
    }

    private void vibrateShort() {

        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            500,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    )
            );
        } else {
            vibrator.vibrate(500);
        }
    }

    private int getBatteryLevel() {
        BatteryManager bm =
                (BatteryManager) getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}





