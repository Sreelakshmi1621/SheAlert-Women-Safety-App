package com.example.shealert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnSignUp;
    private TextView txtLoginLink;

    private FirebaseAuth mAuth;
    EditText edtFirstName, edtLastName;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Views
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLoginLink = findViewById(R.id.txtLoginLink);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Sign up click
        btnSignUp.setOnClickListener(v -> registerUser());

        // Go back to login
        txtLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = firstName + " " + lastName;

        if (email.isEmpty()) {
            edtEmail.setError("Email required");
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Password required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String userId = mAuth.getCurrentUser().getUid();

                        databaseReference.child(userId).child("name").setValue(fullName);
                        databaseReference.child(userId).child("email").setValue(email);

                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Registration successful. Verification email sent.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Registered, but failed to send verification email.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });

                    } else {
                        Toast.makeText(
                                RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

    }
}
