package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.presentation.adapters.FriendsAdapter;
import com.example.team11project.presentation.viewmodel.UserViewModel;

public class HomeScreenActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        setupNavbar();
        String userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        Button btnAllProfiles = findViewById(R.id.btnAllProfiles);
        btnAllProfiles.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsersListActivity.class);
            startActivity(intent);
        });

        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        Button btnAddEdit = findViewById(R.id.btnAddEdit);
        btnAddEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskActivity.class);
            startActivity(intent);
        });

        Button btnCategories = findViewById(R.id.btnCategories);
        btnCategories.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
        });

        Button btnLevelInfo = findViewById(R.id.btnLevelInfo);
        btnLevelInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, LevelInfoActivity.class);
            startActivity(intent);
        });

        Button btnStore = findViewById(R.id.btnStore);
        btnStore.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoreActivity.class);
            startActivity(intent);
        });

        Button btnFriends = findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(v -> {
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);
        });

        Button btnAlliance = findViewById(R.id.btnAlliance);
        UserViewModel userViewModel = new ViewModelProvider(this,
                new UserViewModel.Factory(new UserRepositoryImpl(getApplicationContext())))
                .get(UserViewModel.class);

        userViewModel.loadUser(userId);
        userViewModel.getUser().observe(this, user -> {
            if (user != null && user.getCurrentAlliance() != null && user.getCurrentAlliance().getId() != null) {
                btnAlliance.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AllianceDetailsActivity.class);
                    intent.putExtra("allianceId", user.getCurrentAlliance().getId());
                    startActivity(intent);
                });
            } else {
                btnAlliance.setOnClickListener(v ->
                        Toast.makeText(this, "Nemate savez ili niste član nijednog saveza.", Toast.LENGTH_SHORT).show()
                );
            }
        });



    }
}