package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team11project.R;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.repository.EquipmentRepository;

import java.util.List;

public class HomeScreenActivity extends BaseActivity {

    private RemoteDataSource remoteDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        setupNavbar();
        remoteDataSource = new RemoteDataSource();

        remoteDataSource.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> data) {
                for (Equipment eq : data) {
                    // Logujemo naziv i tip
                    Log.d("Equipment", eq.getName() + " - type: " + eq.getType());
                }
                Log.d("Equipment", "Ukupno opreme: " + data.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Equipment", "Greška pri dohvatu opreme", e);
            }
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

    }


}