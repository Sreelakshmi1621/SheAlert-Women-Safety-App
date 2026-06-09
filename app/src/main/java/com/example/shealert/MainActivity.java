package com.example.shealert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnCreateAccount;
    private TextView txtForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔔 Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        // 🔹 Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // 🔥 AUTO LOGIN CHECK
        if (mAuth.getCurrentUser() != null &&
                mAuth.getCurrentUser().isEmailVerified()) {

            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();   // Close login screen
            return;     // VERY IMPORTANT
        }

        // 🔹 Bind UI elements
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        txtForgot = findViewById(R.id.txtForgot);

        // 🔹 Login Button Click
        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Email is required");
                edtEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Password is required");
                edtPassword.requestFocus();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            if (mAuth.getCurrentUser() != null &&
                                    mAuth.getCurrentUser().isEmailVerified()) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Login successful",
                                        Toast.LENGTH_SHORT
                                ).show();

                                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                startActivity(intent);
                                finish();

                            } else {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Please verify your email before logging in",
                                        Toast.LENGTH_LONG
                                ).show();

                                mAuth.signOut();
                            }

                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        // 🔹 Create Account
        btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 🔹 Forgot Password
        txtForgot.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Enter your registered email");
                edtEmail.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Password reset email sent",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }
}