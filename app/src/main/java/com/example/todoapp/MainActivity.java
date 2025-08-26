package com.example.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerTodos, recyclerToggle;
    private TodoAdapter todoAdapter;
    private ToggleAdapter toggleAdapter;
    private List<Todo> todoList;
    private List<ToggleItem> toggleList;
    private FloatingActionButton fabAdd;

    private FirebaseFirestore db;
    private String userId;
    private String currentStatus = "in_progress";

    private ListenerRegistration todoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish(); // no user logged in
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerTodos = findViewById(R.id.recyclerTodos);
        recyclerToggle = findViewById(R.id.recyclerToggle);
        fabAdd = findViewById(R.id.fabAddTodo);

        setupTodoRecyclerView();
        setupToggleRecyclerView();
        setupAddFab();

        // Load todos initially
        loadTodos();
    }


    private void setupTodoRecyclerView() {
        todoList = new ArrayList<>();
        todoAdapter = new TodoAdapter(this, todoList);
        recyclerTodos.setLayoutManager(new LinearLayoutManager(this));
        recyclerTodos.setAdapter(todoAdapter);
    }

    private void setupToggleRecyclerView() {
        toggleList = new ArrayList<>();
        toggleList.add(new ToggleItem("In Progress"));
        toggleList.add(new ToggleItem("Completed"));

        toggleAdapter = new ToggleAdapter(this, toggleList, position -> {
            // Update currentStatus
            currentStatus = (position == 0) ? "in_progress" : "completed";

            // Remove previous listener
            if (todoListener != null) todoListener.remove();

            // Clear old todos
            todoList.clear();
            todoAdapter.notifyDataSetChanged();

            // Load todos for new status
            loadTodos();
        });

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
        // Remove previous listener if exists
        if (todoListener != null) {
            todoListener.remove();
            todoListener = null;
        }

        // Clear current list immediately
        todoList.clear();
        todoAdapter.notifyDataSetChanged();

        // Attach new listener for current status
        todoListener = db.collection("todos")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", currentStatus)
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        // Log error instead of showing repeated Toast
                        Log.e("MainActivity", "Error loading todos", e);
                        return;
                    }

                    if (snapshots == null) return;

                    // Process changes
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Todo todo = dc.getDocument().toObject(Todo.class);
                        todo.setId(dc.getDocument().getId());

                        switch (dc.getType()) {
                            case ADDED:
                                // Avoid duplicates
                                boolean exists = false;
                                for (Todo t : todoList) {
                                    if (t.getId().equals(todo.getId())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Handle logout
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

    // Method to be called from AddTodoDialog to immediately add new todo
    public void addTodoToList(Todo todo) {
        if (currentStatus.equals(todo.getStatus())) {
            todoList.add(0, todo);
            todoAdapter.notifyItemInserted(0);
            recyclerTodos.scrollToPosition(0);
        }
    }
}
