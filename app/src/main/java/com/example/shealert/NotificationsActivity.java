package com.example.shealert;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<NotificationModel> list = new ArrayList<>();
    private LinearLayout emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.toolbarNotifications);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        recyclerView = findViewById(R.id.recyclerNotifications);
        emptyLayout = findViewById(R.id.emptyLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(list);
        recyclerView.setAdapter(adapter);

        loadNotifications();
        enableSwipeToDelete();
    }

    private void loadNotifications() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("notifications");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    NotificationModel model = ds.getValue(NotificationModel.class);

                    if (model != null) {
                        model.id = ds.getKey();
                        list.add(model);
                    }
                }

                Collections.reverse(list);
                adapter.notifyDataSetChanged();

                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateEmptyState() {

        if (list.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void enableSwipeToDelete() {

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {

                        int position = viewHolder.getAdapterPosition();
                        NotificationModel model = list.get(position);

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid)
                                .child("notifications")
                                .child(model.id)
                                .removeValue();

                        list.remove(position);
                        adapter.notifyItemRemoved(position);

                        updateEmptyState();
                    }
                };

        new ItemTouchHelper(callback)
                .attachToRecyclerView(recyclerView);
    }
}