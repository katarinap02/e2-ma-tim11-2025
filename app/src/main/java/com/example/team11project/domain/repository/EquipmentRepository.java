package com.example.team11project.domain.repository;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Equipment;

import java.util.List;

public interface EquipmentRepository {
//    void getAllEquipment(RepositoryCallback<List<Equipment>> callback);
    void getAllEquipment(RemoteDataSource.DataSourceCallback<List<Equipment>> callback);

}
