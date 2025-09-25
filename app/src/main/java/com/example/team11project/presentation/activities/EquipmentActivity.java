package com.example.team11project.presentation.activities;
import android.content.Intent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.team11project.R;

public class EquipmentActivity extends BaseActivity {

    private TextView tvSelectEquipment;
    private Button btnStartBossFight;

    private int bossId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        setupNavbar();

        initializeViews();
        getBossData();
        setupClickListeners();
    }

    private void initializeViews() {
        tvSelectEquipment = findViewById(R.id.tvSelectEquipment);
        btnStartBossFight = findViewById(R.id.btnStartBossFight);
    }

    private void getBossData() {
        Intent intent = getIntent();
        bossId = intent.getIntExtra("bossId", -1);

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);
    }

    private void setupClickListeners() {
        btnStartBossFight.setOnClickListener(v -> {
            Intent intent = new Intent(this, BossActivity.class);
            intent.putExtra("bossId", bossId);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }
}