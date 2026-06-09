package com.example.shealert;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class AlertHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerAlerts;
    List<AlertModel> alertList;
    AlertAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_history);

        recyclerAlerts = findViewById(R.id.recyclerAlerts);
        recyclerAlerts.setLayoutManager(new LinearLayoutManager(this));

        alertList = new ArrayList<>();
        adapter = new AlertAdapter(this, alertList);
        recyclerAlerts.setAdapter(adapter);

        loadAlertsFromFirebase();
    }

    private void loadAlertsFromFirebase() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("alerts");

        ref.get().addOnSuccessListener(snapshot -> {

            alertList.clear();

            for (DataSnapshot ds : snapshot.getChildren()) {

                AlertModel alert = ds.getValue(AlertModel.class);

                if (alert != null) {
                    alertList.add(alert);
                }
            }

            Collections.reverse(alertList);

            adapter.notifyDataSetChanged();
        });
    }
}