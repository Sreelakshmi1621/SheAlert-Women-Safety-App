package com.example.shealert;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    ArrayList<NotificationModel> list;

    public NotificationAdapter(ArrayList<NotificationModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        NotificationModel model = list.get(position);

        holder.txtTitle.setText(model.title);
        holder.txtMessage.setText(model.message);
        holder.txtTime.setText(model.timestamp);

        switch (model.type) {

            case "danger":
                holder.typeIndicator.setBackgroundColor(Color.parseColor("#D32F2F"));
                break;

            case "warning":
                holder.typeIndicator.setBackgroundColor(Color.parseColor("#F57C00"));
                break;

            case "info":
                holder.typeIndicator.setBackgroundColor(Color.parseColor("#4A148C"));
                break;

            default:
                holder.typeIndicator.setBackgroundColor(Color.GRAY);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtMessage, txtTime;
        View typeIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            typeIndicator = itemView.findViewById(R.id.typeIndicator);
        }
    }
}