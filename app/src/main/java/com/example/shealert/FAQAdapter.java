package com.example.shealert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private List<FAQModel> faqList;

    public FAQAdapter(List<FAQModel> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {

        FAQModel model = faqList.get(position);

        holder.txtQuestion.setText("▸ " + model.question);
        holder.txtAnswer.setText(model.answer);

        holder.txtAnswer.setVisibility(
                model.isExpanded ? View.VISIBLE : View.GONE
        );

        holder.itemView.setOnClickListener(v -> {
            model.isExpanded = !model.isExpanded;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {

        TextView txtQuestion, txtAnswer;

        public FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestion = itemView.findViewById(R.id.txtQuestion);
            txtAnswer = itemView.findViewById(R.id.txtAnswer);
        }
    }
}