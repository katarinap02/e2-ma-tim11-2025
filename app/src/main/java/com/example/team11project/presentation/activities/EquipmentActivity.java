package com.example.team11project.presentation.activities;
import android.content.Intent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.presentation.viewmodel.EquipmentViewModel;

public class EquipmentActivity extends BaseActivity {

    private TextView tvSelectEquipment;
    private Button btnStartBossFight;

    private EquipmentViewModel viewModel;
    private String bossId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        setupNavbar();
        getBossData();
        EquipmentViewModel.Factory factory = new EquipmentViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(EquipmentViewModel.class);

        initializeViews();
        setupClickListeners();
        observeViewModel();
    }

    private void initializeViews() {
        tvSelectEquipment = findViewById(R.id.tvSelectEquipment);
        btnStartBossFight = findViewById(R.id.btnStartBossFight);
    }

    private void getBossData() {
        Intent intent = getIntent();

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);
    }

    private void setupClickListeners() {
        btnStartBossFight.setOnClickListener(v -> {
            if (userId != null) {
                viewModel.startBossFight(userId);
            } else {
                Toast.makeText(this, "Greška: Nedostaju podaci o korisniku ili boss-u", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        // Osluškujemo kada se BossBattle kreira ili učita
        viewModel.bossBattle.observe(this, bossBattle -> {
            if (bossBattle != null) {
                navigateToBossActivity(bossBattle);
            }
        });

        // Osluškujemo loading stanje
        viewModel.isLoading.observe(this, isLoading -> {
            btnStartBossFight.setEnabled(!isLoading);
            if (isLoading) {
                btnStartBossFight.setText("UČITAVANJE...");
            } else {
                btnStartBossFight.setText("ZAPOČNI BORBU SA BOSOM");
            }
        });

        // Osluškujemo greške
        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                // Opcionalno: očisti grešku nakon što je prikazana
                viewModel.clearError();
            }
        });
    }

    private void navigateToBossActivity(BossBattle bossBattle) {
        Intent intent = new Intent(this, BossActivity.class);
        intent.putExtra("bossId", bossBattle.getBossId());
        intent.putExtra("userId", userId);
        intent.putExtra("level", bossBattle.getLevel());
        startActivity(intent);
    }
}