package com.example.shealert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SOSHelper {

    public static void sendSOS(Context context, boolean isAuto) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("contacts");

        ref.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) return;

            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.getFusedLocationProviderClient(context)
                    .getLastLocation()
                    .addOnSuccessListener(location -> {

                        double lat = 0;
                        double lng = 0;
                        boolean locationAvailable = false;

                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            locationAvailable = true;
                        }

                        BatteryManager bm =
                                (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

                        int battery = bm.getIntProperty(
                                BatteryManager.BATTERY_PROPERTY_CAPACITY);

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

                        saveAlert(context, isAuto, lat, lng, battery);
                    });
        });
    }

    private static void saveAlert(Context context,
                                  boolean isAuto,
                                  double lat,
                                  double lng,
                                  int battery) {

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
                isAuto ? "Auto" : "Manual",
                timestamp,
                String.valueOf(lat),
                String.valueOf(lng),
                String.valueOf(battery)
        );

        alertRef.setValue(alert);
    }
}