package com.example.shealert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    private ImageView imgProfileLarge;
    private TextInputEditText inputName, inputEmail;
    private Button btnSaveProfile;

    private Uri tempImageUri = null;
    private boolean removePhotoSelected = false;

    private String uid;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences("profile_" + uid, MODE_PRIVATE);

        imgProfileLarge = findViewById(R.id.imgProfileLarge);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        loadUserData();
        loadSavedImage();

        imgProfileLarge.setOnClickListener(v -> showImageOptionsDialog());
        btnSaveProfile.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {

        inputEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        inputName.setText(snapshot.getValue(String.class));
                    }
                });
    }

    private void loadSavedImage() {

        String savedImagePath = prefs.getString("image", null);

        if (savedImagePath != null) {
            File imgFile = new File(savedImagePath);
            if (imgFile.exists()) {
                imgProfileLarge.setImageURI(Uri.fromFile(imgFile));
                return;
            }
        }

        imgProfileLarge.setImageResource(R.drawable.ic_profile);
    }

    private void showImageOptionsDialog() {

        String[] options = {"Change Photo", "Remove Photo", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, (dialog, which) -> {

            if (which == 0) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);

            } else if (which == 1) {
                removePhotoSelected = true;
                tempImageUri = null;
                imgProfileLarge.setImageResource(R.drawable.ic_profile);

            } else {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            tempImageUri = data.getData();
            removePhotoSelected = false;
            imgProfileLarge.setImageURI(tempImageUri);
        }
    }

    private void saveChanges() {

        String newName = inputName.getText().toString().trim();
        String newEmail = inputEmail.getText().toString().trim();

        if (!newName.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("name")
                    .setValue(newName);
        }

        if (!newEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {

            FirebaseAuth.getInstance().getCurrentUser()
                    .updateEmail(newEmail)
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Email update failed. Please re-login.",
                                    Toast.LENGTH_LONG).show());
        }

        SharedPreferences.Editor editor = prefs.edit();

        if (tempImageUri != null) {

            String path = saveImageToInternalStorage(tempImageUri);

            if (path != null) {
                editor.putString("image", path);
            }

        } else if (removePhotoSelected) {

            editor.remove("image");
        }

        editor.apply();

        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String saveImageToInternalStorage(Uri uri) {

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            File file = new File(getFilesDir(), "profile_" + uid + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}