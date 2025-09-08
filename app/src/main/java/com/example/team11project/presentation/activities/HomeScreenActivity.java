package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team11project.R;

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

    }
}