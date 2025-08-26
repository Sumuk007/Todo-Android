package com.example.todoapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddTodoDialog extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_todo, null);
        dialog.setContentView(view);

        EditText etTask = view.findViewById(R.id.etTask);
        Button btnAdd = view.findViewById(R.id.btnAdd);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnAdd.setOnClickListener(v -> {
            String task = etTask.getText().toString().trim();
            if (!task.isEmpty()) {
                Map<String, Object> todoMap = new HashMap<>();
                todoMap.put("title", task);
                todoMap.put("status", "in_progress"); // default
                todoMap.put("userId", userId);
                todoMap.put("timestamp", FieldValue.serverTimestamp());

                db.collection("todos")
                        .add(todoMap)
                        .addOnSuccessListener(docRef -> {
                            Todo newTodo = new Todo();
                            newTodo.setId(docRef.getId());
                            newTodo.setTitle(task);
                            newTodo.setStatus("in_progress");
                            newTodo.setUserId(userId);

                            ((MainActivity)getActivity()).addTodoToList(newTodo); // call a method in MainActivity to update list
                            dismiss();
                        });

            }
        });

        return dialog;
    }
}
