package com.example.team11project.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.repository.EquipmentRepository;

import java.util.List;

public class EquipmentRepositoryImpl implements EquipmentRepository {

    private RemoteDataSource remoteDataSource;
    private final Context context;


    public EquipmentRepositoryImpl(Context context) {
        this.context = context;
        this.remoteDataSource = new RemoteDataSource();
    }

    @Override
    public void getAllEquipment(RemoteDataSource.DataSourceCallback<List<Equipment>> callback) {
        remoteDataSource.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


}
