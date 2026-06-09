package com.example.shealert;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FAQAdapter adapter;
    private List<FAQModel> faqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // 🔹 Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarHelp);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Help Center");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // 🔹 Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerFAQ);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        faqList = new ArrayList<>();

        // 🔹 Add FAQ Items
        faqList.add(new FAQModel(
                "What is SheAlert?",
                "SheAlert is a women safety application that allows users to send emergency alerts, share live location, and use smart safety check-ins."
        ));

        faqList.add(new FAQModel(
                "How does Silent SOS work?",
                "When activated, SheAlert sends your live location and battery level instantly to your trusted contacts."
        ));

        faqList.add(new FAQModel(
                "What is Check-In Safety?",
                "You can set an arrival time while travelling. If you don’t confirm safety before the timer expires, the app can automatically trigger an SOS."
        ));

        faqList.add(new FAQModel(
                "What happens if I forget to mark myself safe?",
                "If Auto Silent SOS is enabled, an emergency alert will be sent automatically."
        ));

        faqList.add(new FAQModel(
                "Is my data secure?",
                "Your alerts, contacts, and location data are securely stored in Firebase and used strictly for emergency safety purposes."
        ));

        faqList.add(new FAQModel(
                "How can I contact support?",
                "You can email us at: SheAlert.Support@gmail.com"
        ));

        adapter = new FAQAdapter(faqList);
        recyclerView.setAdapter(adapter);
    }
}