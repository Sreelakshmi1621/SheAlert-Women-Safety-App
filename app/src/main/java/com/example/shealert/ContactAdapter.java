package com.example.shealert;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    Context context;
    ArrayList<String> contactsList;
    ArrayList<String> contactIds;

    public ContactAdapter(Context context,
                          ArrayList<String> contactsList,
                          ArrayList<String> contactIds) {
        this.context = context;
        this.contactsList = contactsList;
        this.contactIds = contactIds;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {

        String data = contactsList.get(position);
        String[] parts = data.split(",");

        String name = parts[0];
        String phone = parts[1];

        holder.txtName.setText(name);
        holder.txtPhone.setText(phone);

        // Set first letter avatar
        holder.txtAvatar.setText(name.substring(0, 1).toUpperCase());

        holder.imgDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Contact")
                    .setMessage("Are you sure you want to delete this contact?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String contactId = contactIds.get(position);

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid)
                                .child("contacts")
                                .child(contactId)
                                .removeValue();

                        Toast.makeText(context, "Contact Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView txtAvatar, txtName, txtPhone;
        ImageView imgDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            txtAvatar = itemView.findViewById(R.id.txtAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}