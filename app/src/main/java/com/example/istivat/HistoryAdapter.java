package com.example.istivat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String[] entry);
    }

    private final List<String[]> entries;
    private final OnItemClickListener listener;

    public HistoryAdapter(List<String[]> entries, OnItemClickListener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] entry = entries.get(position);
        holder.textDate.setText(entry[0]);
        holder.textArea.setText(entry[1] + " m²");
        holder.textBoiler.setText(entry[2] + " kW");
        holder.itemView.setOnClickListener(v -> listener.onItemClick(entry));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void removeAt(int position) {
        entries.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        TextView textArea;
        TextView textBoiler;

        ViewHolder(View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textHistoryDate);
            textArea = itemView.findViewById(R.id.textHistoryArea);
            textBoiler = itemView.findViewById(R.id.textHistoryBoiler);
        }
    }
}
