package com.example.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.databinding.ActivityRegistrationBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private ActivityRegistrationBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.tvSignIn.setOnClickListener(v -> finish());

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String email = String.valueOf(binding.etRegEmail.getText()).trim();
        String password = String.valueOf(binding.etRegPassword.getText()).trim();
        String confirm = String.valueOf(binding.etRegConfirmPassword.getText()).trim();

        if (!isValidEmail(email)) {
            binding.tilRegEmail.setError("Enter a valid email");
            return;
        } else binding.tilRegEmail.setError(null);

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilRegPassword.setError("Min 6 characters");
            return;
        } else binding.tilRegPassword.setError(null);

        if (!password.equals(confirm)) {
            binding.tilRegConfirmPassword.setError("Passwords do not match");
            return;
        } else binding.tilRegConfirmPassword.setError(null);

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, (Task<AuthResult> task) -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        // Optional: send verification email
                        // auth.getCurrentUser().sendEmailVerification();

                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(this,
                                task.getException() != null ? task.getException().getMessage() : "Registration failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
        binding.tilRegEmail.setEnabled(!loading);
        binding.tilRegPassword.setEnabled(!loading);
        binding.tilRegConfirmPassword.setEnabled(!loading);
    }
}
