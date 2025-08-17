package com.example.team11project.presentation.activities;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.CategoryRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.CategoryRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.viewmodel.CategoryViewModel;
import com.example.team11project.presentation.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etUsername, etAvatar;
    private Button btnRegister;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        etAvatar = findViewById(R.id.etAvatar);
        btnRegister = findViewById(R.id.btnRegister);

        RegisterViewModel.Factory factory = new RegisterViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(RegisterViewModel.class);

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Greska: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUsers().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                Toast.makeText(this, "Registracija uspešna! Proveri email za aktivaciju.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, AddAndEditActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String avatar = etAvatar.getText().toString().trim();

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.registerUser(username, password, email, avatar);
        });
    }




}