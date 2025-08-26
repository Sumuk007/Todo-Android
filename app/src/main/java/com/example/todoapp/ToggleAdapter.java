package com.example.todoapp;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ToggleAdapter extends RecyclerView.Adapter<ToggleAdapter.ToggleViewHolder> {

    private Context context;
    private int selectedPosition = 0;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ToggleAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
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

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Calculate width as half of screen
        int halfWidth = screenWidth / 2;

        // Set width programmatically
        holder.tvToggle1.getLayoutParams().width = halfWidth;
        holder.tvToggle1.requestLayout();

        holder.tvToggle2.getLayoutParams().width = halfWidth;
        holder.tvToggle2.requestLayout();

        // Set text for two options
        holder.tvToggle1.setText("In Progress");
        holder.tvToggle2.setText("Completed");

        // Highlight selected option
        if (selectedPosition == 0) {
            holder.tvToggle1.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
            holder.tvToggle1.setTextColor(ContextCompat.getColor(context, android.R.color.white));

            holder.tvToggle2.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.tvToggle2.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        } else {
            holder.tvToggle2.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
            holder.tvToggle2.setTextColor(ContextCompat.getColor(context, android.R.color.white));

            holder.tvToggle1.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.tvToggle1.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        // Click listeners
        holder.tvToggle1.setOnClickListener(v -> {
            if (selectedPosition != 0) {
                int previous = selectedPosition;
                selectedPosition = 0;
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
                if (listener != null) listener.onItemClick(0);
            }
        });

        holder.tvToggle2.setOnClickListener(v -> {
            if (selectedPosition != 1) {
                int previous = selectedPosition;
                selectedPosition = 1;
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
                if (listener != null) listener.onItemClick(1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1; // Only one row containing both toggles
    }

    static class ToggleViewHolder extends RecyclerView.ViewHolder {
        TextView tvToggle1, tvToggle2;

        public ToggleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvToggle1 = itemView.findViewById(R.id.tvToggle1);
            tvToggle2 = itemView.findViewById(R.id.tvToggle2);
        }
    }
}
