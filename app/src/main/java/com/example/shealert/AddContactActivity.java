package com.example.shealert;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.net.Uri;
import android.provider.ContactsContract;
import android.database.Cursor;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddContactActivity extends AppCompatActivity {

    EditText edtName, edtPhone;
    TextView txtPickContact;
    Button btnSave;
    private static final int PICK_CONTACT = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSaveContact);
        txtPickContact = findViewById(R.id.txtPickContact);
        txtPickContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("vnd.android.cursor.dir/phone_v2");
            startActivityForResult(intent, PICK_CONTACT);
        });
        btnSave.setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String phoneInput = edtPhone.getText().toString().trim();
            String phone = "+91" + phoneInput;

            if (name.isEmpty() || phoneInput.isEmpty()) {
                Toast.makeText(this, "Enter name & number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phoneInput.length() != 10) {
                edtPhone.setError("Enter valid 10-digit number");
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("contacts");

            String contactId = ref.push().getKey();

            ref.child(contactId).child("name").setValue(name);
            ref.child(contactId).child("phone").setValue(phone);

            Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
            finish();  // go back
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK && data != null) {

            Uri uri = data.getData();

            Cursor cursor = getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {

                // get name
                int nameIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = cursor.getString(nameIndex);
                edtName.setText(name);

                // get number
                int numberIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberIndex);
                // Remove spaces and dashes
                number = number.replaceAll("\\s+", "");
                number = number.replaceAll("-", "");

                // Remove +91 if exists
                if (number.startsWith("+91")) {
                    number = number.substring(3);
                }

                // Remove leading 0 if exists
                if (number.startsWith("0")) {
                    number = number.substring(1);
                }

                // Now set only 10-digit number
                edtPhone.setText(number);

                cursor.close();
            }
        }
    }

}