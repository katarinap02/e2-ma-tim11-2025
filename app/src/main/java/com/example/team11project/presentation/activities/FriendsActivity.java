package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.FriendsUseCase;
import com.example.team11project.presentation.adapters.FriendsAdapter;
import com.example.team11project.presentation.viewmodel.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FriendsActivity extends BaseActivity {

    private UserViewModel viewModel;
    private FriendsAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setupNavbar();

        RecyclerView recyclerView = findViewById(R.id.friendsRecyclerView);
        FloatingActionButton fab = findViewById(R.id.addFriendFab);

        currentUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        adapter = new FriendsAdapter(new ArrayList<>(), friend -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", friend.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        AllianceMissionRepository allianceMissionRepository = new AllianceMissionRepositoryImpl(getApplicationContext());
        UserViewModel.Factory factory = new UserViewModel.Factory(userRepository, allianceMissionRepository);
        viewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        viewModel.getFriendsLiveData().observe(this, friends -> {
            if (friends != null) {
                adapter.setFriends(friends);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Greška: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadFriends(currentUserId);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsActivity.this, AddFriendsActivity.class);
            startActivity(intent);
        });

        Button btnCreateAlliance = findViewById(R.id.btnCreateAlliance);
        btnCreateAlliance.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAllianceActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadFriends(currentUserId);
    }
}
