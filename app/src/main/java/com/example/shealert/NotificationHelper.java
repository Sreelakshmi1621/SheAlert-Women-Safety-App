package com.example.shealert;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    public static void sendNotification(String title,
                                        String message,
                                        String type) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("notifications");

        String notifId = ref.push().getKey();

        String currentTime = new SimpleDateFormat(
                "dd MMM yyyy - hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        NotificationModel notification = new NotificationModel(
                title,
                message,
                currentTime,
                type
        );

        ref.child(notifId).setValue(notification);
    }
}