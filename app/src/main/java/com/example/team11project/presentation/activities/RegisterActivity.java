package com.example.team11project.presentation.activities;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    private EditText etEmail, etPassword, etConfirmPassword, etUsername;
    private Button btnRegister;
    private RegisterViewModel viewModel;
    private ImageView avatar1, avatar2, avatar3, avatar4, avatar5;

    private String selectedAvatar = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        btnRegister = findViewById(R.id.btnRegister);

        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);
        avatar5 = findViewById(R.id.avatar5);

        avatar1.setOnClickListener(v -> selectAvatar("avatar1", avatar1));
        avatar2.setOnClickListener(v -> selectAvatar("avatar2", avatar2));
        avatar3.setOnClickListener(v -> selectAvatar("avatar3", avatar3));
        avatar4.setOnClickListener(v -> selectAvatar("avatar4", avatar4));
        avatar5.setOnClickListener(v -> selectAvatar("avatar5", avatar5));


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
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedAvatar == null) {
                Toast.makeText(this, "Izaberi avatar!", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.registerUser(username, password, email, selectedAvatar);
        });
    }


    private void selectAvatar(String avatarName, ImageView selectedView) {
        selectedAvatar = avatarName;

        avatar1.setBackground(null);
        avatar2.setBackground(null);
        avatar3.setBackground(null);
        avatar4.setBackground(null);
        avatar5.setBackground(null);

        selectedView.setBackgroundResource(R.drawable.avatar_border);
    }

}