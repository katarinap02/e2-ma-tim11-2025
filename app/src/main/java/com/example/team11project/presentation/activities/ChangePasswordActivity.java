package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.viewmodel.UserViewModel;

public class ChangePasswordActivity extends BaseActivity {

    private UserViewModel viewModel;
    private EditText etNewPassword, etConfirmPassword, etOldPassword;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        setupNavbar();

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        etConfirmPassword = findViewById(R.id.etRepeatPassword);



        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());

        UserViewModel.Factory factory = new UserViewModel.Factory(userRepository);
        viewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        String userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);
        Log.d("DEBUG", "ChangePasswordActivity: userId = " + userId);


        viewModel.getUpdateSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Lozinka je uspešno promenjena!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        viewModel.getError().observe(this, message -> {
            Toast.makeText(this, "Greška: " + message, Toast.LENGTH_SHORT).show();
        });


        btnChangePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String oldPassword = etOldPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Polja ne smeju biti prazna", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Lozinke se ne poklapaju", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.getUser().observe(this, user -> {

                if (user == null) {
                    Toast.makeText(this, "Korisnik ne postoji", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!user.getPassword().equals(oldPassword)) {
                    Toast.makeText(this, "Stara lozinka nije tačna", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sve je u redu → menja se lozinka
                viewModel.updatePassword(userId, newPassword);
            });
            viewModel.loadUser(userId);

        });
    }
}