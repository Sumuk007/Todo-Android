package com.example.todoapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.btnReset.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (!isValidEmail(email)) {
                binding.tilEmail.setError("Enter valid email");
                return;
            } else {
                binding.tilEmail.setError(null);
            }

            setLoading(true);
            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Password reset link sent!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this,
                            task.getException() != null ? task.getException().getMessage() : "Error",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnReset.setEnabled(!loading);
        binding.tilEmail.setEnabled(!loading);
    }
}