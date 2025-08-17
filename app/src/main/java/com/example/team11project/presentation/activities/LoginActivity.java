package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.viewmodel.RegisterViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button regButton, logButton;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        logButton = findViewById(R.id.loginButton);

        logButton.setOnClickListener(v -> attemptLogin());

        regButton = findViewById(R.id.registerButton);
        regButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Unesite email i password", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterViewModel.Factory factory = new RegisterViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(RegisterViewModel.class);

        viewModel.getUser().observe(this, user -> {
            if(user != null){
                startActivity(new Intent(this, AddAndEditActivity.class));
                finish();
            }
        });

        viewModel.getError().observe(this, error -> {
            if(error != null){
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.login(email, password);
    }

}