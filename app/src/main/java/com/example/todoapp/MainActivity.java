package com.example.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerTodos;
    private TodoAdapter todoAdapter;
    private List<Todo> todoList;
    private FloatingActionButton fabAdd;

    private FirebaseFirestore db;
    private String userId;
    private String currentStatus = "in_progress";

    private ListenerRegistration todoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("NexTask");

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerTodos = findViewById(R.id.recyclerTodos);
        fabAdd = findViewById(R.id.fabAddTodo);

        setupTodoRecyclerView();
        setupToggleLayout();
        setupAddFab();

        loadTodos();
    }

    private void setupTodoRecyclerView() {
        todoList = new ArrayList<>();
        todoAdapter = new TodoAdapter(this, todoList);
        recyclerTodos.setLayoutManager(new LinearLayoutManager(this));
        recyclerTodos.setAdapter(todoAdapter);
    }

    private void setupToggleLayout() {
        ToggleAdapter toggleAdapter = new ToggleAdapter(this, position -> {
            currentStatus = (position == 0) ? "in_progress" : "completed";

            if (todoListener != null) todoListener.remove();

            todoList.clear();
            todoAdapter.notifyDataSetChanged();

            loadTodos();
        });

        RecyclerView recyclerToggle = findViewById(R.id.recyclerToggle);
        recyclerToggle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerToggle.setAdapter(toggleAdapter);
    }

    private void setupAddFab() {
        fabAdd.setOnClickListener(v -> {
            AddTodoDialog dialog = new AddTodoDialog();
            dialog.show(getSupportFragmentManager(), "AddTodoDialog");
        });
    }

    private void loadTodos() {
        if (todoListener != null) {
            todoListener.remove();
            todoListener = null;
        }

        todoList.clear();
        todoAdapter.notifyDataSetChanged();

        todoListener = db.collection("todos")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", currentStatus)
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("MainActivity", "Error loading todos", e);
                        return;
                    }
                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Todo todo = dc.getDocument().toObject(Todo.class);
                        todo.setId(dc.getDocument().getId());

                        switch (dc.getType()) {
                            case ADDED:
                                if (!containsTodo(todo.getId())) {
                                    todoList.add(todo);
                                    todoAdapter.notifyItemInserted(todoList.size() - 1);
                                }
                                break;

                            case MODIFIED:
                                for (int i = 0; i < todoList.size(); i++) {
                                    if (todoList.get(i).getId().equals(todo.getId())) {
                                        todoList.set(i, todo);
                                        todoAdapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                break;

                            case REMOVED:
                                int removeIndex = -1;
                                for (int i = 0; i < todoList.size(); i++) {
                                    if (todoList.get(i).getId().equals(todo.getId())) {
                                        removeIndex = i;
                                        break;
                                    }
                                }
                                if (removeIndex != -1) {
                                    todoList.remove(removeIndex);
                                    todoAdapter.notifyItemRemoved(removeIndex);
                                }
                                break;
                        }
                    }
                });
    }

    private boolean containsTodo(String id) {
        for (Todo t : todoList) {
            if (t.getId().equals(id)) return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (todoListener != null) todoListener.remove();
    }

    public void addTodoToList(Todo todo) {
        if (currentStatus.equals(todo.getStatus())) {
            todoList.add(0, todo);
            todoAdapter.notifyItemInserted(0);
            recyclerTodos.scrollToPosition(0);
        }
    }

}
