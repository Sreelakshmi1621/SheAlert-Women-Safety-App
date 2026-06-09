package com.example.shealert;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private Context context;
    private List<AlertModel> alertList;

    public AlertAdapter(Context context, List<AlertModel> alertList) {
        this.context = context;
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {

        AlertModel alert = alertList.get(position);

        // SAFE trigger handling
        String type = alert.triggerType != null ? alert.triggerType : "Unknown";

        // Set badge text (uppercase)
        holder.txtTrigger.setText(type.toUpperCase());

        // Set badge background color
        if (type.equalsIgnoreCase("manual")) {
            holder.txtTrigger.setBackgroundResource(R.drawable.bg_status_manual);
        } else {
            holder.txtTrigger.setBackgroundResource(R.drawable.bg_status_auto);
        }

        // Time
        holder.txtTime.setText("Time: " + (alert.timestamp != null ? alert.timestamp : "-"));

        // Battery
        holder.txtBattery.setText("Battery: " + alert.battery + "%");

        // Location
        if (alert.latitude != null && alert.longitude != null) {

            String mapUrl = "https://maps.google.com/?q=" +
                    alert.latitude + "," + alert.longitude;

            holder.txtLocation.setText("View Location");

            holder.txtLocation.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
                context.startActivity(intent);
            });

        } else {
            holder.txtLocation.setText("Location unavailable");
            holder.txtLocation.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {

        TextView txtTrigger, txtTime, txtBattery, txtLocation;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTrigger = itemView.findViewById(R.id.txtTrigger);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtBattery = itemView.findViewById(R.id.txtBattery);
            txtLocation = itemView.findViewById(R.id.txtLocation);
        }
    }
}