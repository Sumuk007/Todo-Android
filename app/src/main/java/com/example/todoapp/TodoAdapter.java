package com.example.todoapp;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private final Context context;
    private final List<Todo> todoList;

    public TodoAdapter(Context context, List<Todo> todoList) {
        this.context = context;
        this.todoList = todoList;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(holder.getAdapterPosition());
        holder.tvTask.setText(todo.getTitle());

        // Detach old listener before setting checked state
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked("completed".equals(todo.getStatus()));

        // Strike-through text if completed
        if ("completed".equals(todo.getStatus())) {
            holder.tvTask.setPaintFlags(holder.tvTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTask.setPaintFlags(holder.tvTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Handle status toggle
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "completed" : "in_progress";
            FirebaseFirestore.getInstance()
                    .collection("todos")
                    .document(todo.getId())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> Log.d("TodoAdapter", "Status updated"))
                    .addOnFailureListener(e -> {
                        Log.e("TodoAdapter", "Error updating status", e);
                        Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
                    });
        });

        // Handle delete
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                FirebaseFirestore.getInstance()
                        .collection("todos")
                        .document(todo.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            todoList.remove(pos);
                            notifyItemRemoved(pos);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTask;
        CheckBox checkBox;
        ImageButton btnDelete;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTask = itemView.findViewById(R.id.tvTask);
            checkBox = itemView.findViewById(R.id.cbCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
