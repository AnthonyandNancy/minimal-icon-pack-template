package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;

import java.util.ArrayList;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder> {

    public static class FaqItem {
        public String question;
        public String answer;
        public boolean expanded;

        public FaqItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
            this.expanded = false;
        }
    }

    private final List<FaqItem> items;

    public FaqAdapter() {
        items = new ArrayList<>();
    }

    public void setItems(List<FaqItem> faqItems) {
        items.clear();
        items.addAll(faqItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaqItem item = items.get(position);
        holder.question.setText(item.question);
        holder.answer.setText(item.answer);
        holder.answer.setVisibility(item.expanded ? View.VISIBLE : View.GONE);
        holder.arrow.setText(item.expanded ? "v" : ">");

        holder.itemView.setOnClickListener(v -> {
            item.expanded = !item.expanded;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView question;
        TextView answer;
        TextView arrow;

        ViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.faq_question);
            answer = itemView.findViewById(R.id.faq_answer);
            arrow = itemView.findViewById(R.id.faq_arrow);
        }
    }
}
