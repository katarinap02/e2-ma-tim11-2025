package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.presentation.adapters.ClothingActivateAdapter;
import com.example.team11project.presentation.adapters.PotionActivateAdapter;
import com.example.team11project.presentation.adapters.WeaponActivateAdapter;
import com.example.team11project.presentation.viewmodel.EquipmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class EquipmentActivity extends BaseActivity {

    private TextView tvSelectEquipment;
    private Button btnStartBossFight;

    private RecyclerView rvWeapons, rvPotions, rvClothing;
    private WeaponActivateAdapter weaponAdapter;
    private PotionActivateAdapter potionAdapter;
    private ClothingActivateAdapter clothingAdapter;

    private EquipmentViewModel viewModel;
    private String userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);

        setupNavbar();
        getUserData();

        EquipmentViewModel.Factory factory = new EquipmentViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(EquipmentViewModel.class);

        initializeViews();
        setupAdapters();
        setupClickListeners();
        observeViewModel();
        loadUserEquipment();
    }

    private void initializeViews() {
        tvSelectEquipment = findViewById(R.id.tvSelectEquipment);
        btnStartBossFight = findViewById(R.id.btnStartBossFight);

        rvWeapons = findViewById(R.id.rvWeapons);
        rvPotions = findViewById(R.id.rvPotions);
        rvClothing = findViewById(R.id.rvClothing);
    }

    private void setupAdapters() {
        weaponAdapter = new WeaponActivateAdapter();
        rvWeapons.setAdapter(weaponAdapter);
        rvWeapons.setLayoutManager(new LinearLayoutManager(this));

        potionAdapter = new PotionActivateAdapter();
        rvPotions.setAdapter(potionAdapter);
        rvPotions.setLayoutManager(new LinearLayoutManager(this));

        clothingAdapter = new ClothingActivateAdapter();
        rvClothing.setAdapter(clothingAdapter);
        rvClothing.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getUserData() {
        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);
    }

    private void loadUserEquipment() {
        if (userId != null) {
            viewModel.loadUserEquipment(userId);
        }
    }

    /*private void setupClickListeners() {
        btnStartBossFight.setOnClickListener(v -> {
            if (userId != null) {
                List<Weapon> selectedWeapons = weaponAdapter.getSelectedWeapons();
                List<Potion> selectedPotions = potionAdapter.getSelectedPotions();
                List<Clothing> selectedClothing = clothingAdapter.getSelectedClothing();

                viewModel.activateEquipment(selectedWeapons, selectedPotions, selectedClothing);

                viewModel.startBossFight(userId);
            } else {
                Toast.makeText(this, "Greška: Nedostaju podaci o korisniku", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void setupClickListeners() {
        btnStartBossFight.setOnClickListener(v -> {
            if (userId != null) {
                List<Weapon> selectedWeapons = weaponAdapter.getSelectedWeapons();
                List<Potion> selectedPotions = potionAdapter.getSelectedPotions();
                List<Clothing> selectedClothing = clothingAdapter.getSelectedClothing();

                viewModel.activateEquipment(selectedWeapons, selectedPotions, selectedClothing, () -> {
                    ArrayList<String> activeEquipmentImages = viewModel.getActiveEquipmentImages();
                    viewModel.startBossFight(userId, activeEquipmentImages);
                });


            } else {
                Log.e("BossFight", "Greška: userId je null");
                Toast.makeText(this, "Greška: Nedostaju podaci o korisniku", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void observeViewModel() {
        viewModel.bossBattle.observe(this, bossBattle -> {
            if (bossBattle != null) {
                navigateToBossActivity(bossBattle);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            btnStartBossFight.setEnabled(!isLoading);
            btnStartBossFight.setText(isLoading ? "UČITAVANJE..." : "ZAPOČNI BORBU SA BOSOM");
        });

        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        viewModel.weapon.observe(this, weapons -> {
            weaponAdapter.setWeapons(weapons);
        });

        viewModel.potion.observe(this, potions -> {
            potionAdapter.setPotions(potions);
        });

        viewModel.clothing.observe(this, clothing -> {
            clothingAdapter.setClothingList(clothing);
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
