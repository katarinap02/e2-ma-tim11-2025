package com.example.team11project.data.repository;

import android.util.Log;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Equipment;

import java.util.List;

public class EquipmentRepositoryImpl {

    private RemoteDataSource remoteDataSource;

    public EquipmentRepositoryImpl() {
        this.remoteDataSource = new RemoteDataSource();
    }

    public void getAllEquipment() {
        remoteDataSource.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> data) {
                Log.d("EquipmentRepository", "Učitano " + data.size() + " itema.");
                for (Equipment eq : data) {
                    Log.d("EquipmentRepository", eq.getName() + " - type: " + eq.getType());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EquipmentRepository", "Greška pri učitavanju opreme", e);
            }
        });
    }
}
