package com.example.todoapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ToggleAdapter extends RecyclerView.Adapter<ToggleAdapter.ToggleViewHolder> {

    private Context context;
    private List<ToggleItem> toggleList;
    private int selectedPosition = 0;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ToggleAdapter(Context context, List<ToggleItem> toggleList, OnItemClickListener listener) {
        this.context = context;
        this.toggleList = toggleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToggleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_toggle, parent, false);
        return new ToggleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToggleViewHolder holder, int position) {
        ToggleItem item = toggleList.get(position);
        holder.tvToggle.setText(item.getTitle());

        // Highlight selected
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) return;

        if (adapterPosition == selectedPosition) {
            holder.tvToggle.setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_primary));
            holder.tvToggle.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary));
        } else {
            holder.tvToggle.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.tvToggle.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurface));
        }

        holder.tvToggle.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            int previousPosition = selectedPosition;
            selectedPosition = currentPosition;

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onItemClick(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return toggleList.size();
    }

    static class ToggleViewHolder extends RecyclerView.ViewHolder {
        TextView tvToggle;

        public ToggleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvToggle = itemView.findViewById(R.id.tvToggle);
        }
    }
}
