package com.example.shealert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;

public class TrustedContactsActivity extends AppCompatActivity {

    Button btnAddContact;
    RecyclerView recyclerContacts;

    ArrayList<String> contactsList;
    ArrayList<String> contactIds;

    ContactAdapter adapter;
    DatabaseReference ref;

    TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_contacts);

        btnAddContact = findViewById(R.id.btnAddContact);
        recyclerContacts = findViewById(R.id.recyclerContacts);
        txtEmpty = findViewById(R.id.txtEmpty);

        contactsList = new ArrayList<>();
        contactIds = new ArrayList<>();

        adapter = new ContactAdapter(this, contactsList, contactIds);
        recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerContacts.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("contacts");

        btnAddContact.setOnClickListener(v ->
                startActivity(new Intent(
                        TrustedContactsActivity.this,
                        AddContactActivity.class)));

        loadContacts();
    }

    // 🔁 Reload contacts when coming back from AddContact screen
    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {

        ref.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                contactsList.clear();
                contactIds.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    String contactId = data.getKey();
                    contactIds.add(contactId);

                    String name = data.child("name").getValue(String.class);
                    String phone = data.child("phone").getValue(String.class);

                    if (name != null && phone != null) {
                        contactsList.add(name + "," + phone);
                    }
                }

                adapter.notifyDataSetChanged();

                if (contactsList.isEmpty()) {
                    recyclerContacts.setVisibility(View.GONE);
                    txtEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerContacts.setVisibility(View.VISIBLE);
                    txtEmpty.setVisibility(View.GONE);
                }

            }


            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
            }
        });
    }
    }
