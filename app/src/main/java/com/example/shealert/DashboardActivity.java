package com.example.shealert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.Toast;
import java.io.File;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtUserName, txtUserEmail;
    private ImageView imgProfile;

    private String uid;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 🔹 Toolbar Setup
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);

        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // 🔹 Check Login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔹 Drawer Header Views
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        imgProfile = findViewById(R.id.imgProfile);

        loadUserData();
        loadProfileImage();

        // 🔹 Open Profile Screen
        findViewById(R.id.profileHeader).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // 🔹 Logout
        findViewById(R.id.menuLogout).setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {

                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });

        // 🔹 Dashboard Cards Navigation
        findViewById(R.id.cardTrustedContacts).setOnClickListener(v ->
                startActivity(new Intent(this, TrustedContactsActivity.class)));

        findViewById(R.id.cardSOS).setOnClickListener(v ->
                startActivity(new Intent(this, SOSActivity.class)));

        findViewById(R.id.cardCheckIn).setOnClickListener(v ->
                startActivity(new Intent(this, CheckInActivity.class)));

        findViewById(R.id.cardSafeRoute).setOnClickListener(v ->
                startActivity(new Intent(this, SafeRouteActivity.class)));

        findViewById(R.id.cardAlertHistory).setOnClickListener(v ->
                startActivity(new Intent(this, AlertHistoryActivity.class)));

        findViewById(R.id.menuSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.menuHelp).setOnClickListener(v ->
                startActivity(new Intent(this, HelpActivity.class)));

        findViewById(R.id.menuNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        findViewById(R.id.menuUpdates).setOnClickListener(v ->
                startActivity(new Intent(this, UpdatesActivity.class)));
    }

    private void loadUserData() {

        // 🔹 Load Name
        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        txtUserName.setText(snapshot.getValue(String.class));
                    } else {
                        txtUserName.setText("User");
                    }
                });

        // 🔹 Load Email
        String email = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail();

        txtUserEmail.setText(email);
    }

    private void loadProfileImage() {

        String savedImage = getSharedPreferences("profile_" + uid, MODE_PRIVATE)
                .getString("image", null);

        if (savedImage != null) {
            File imgFile = new File(savedImage);
            if (imgFile.exists()) {
                imgProfile.setImageURI(Uri.fromFile(imgFile));
                   return;
            }
        }

        // 🔹 If no image found
        imgProfile.setImageResource(R.drawable.ic_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 🔹 Refresh data after returning from ProfileActivity
        loadUserData();
        loadProfileImage();
    }
}