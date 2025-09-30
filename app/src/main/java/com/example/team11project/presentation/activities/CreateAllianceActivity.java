package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.FriendsUseCase;
import com.example.team11project.presentation.adapters.AllianceMemberAdapter;
import com.example.team11project.presentation.viewmodel.CreateAllianceViewModel;

import java.util.ArrayList;
import java.util.List;

public class CreateAllianceActivity extends BaseActivity {

    private EditText etAllianceName;
    private RecyclerView rvFriends;
    private Button btnCreateAlliance;

    private CreateAllianceViewModel viewModel;
    private AllianceMemberAdapter adapter;
    private FriendsUseCase friendsUseCase;

    private final List<User> friendsList = new ArrayList<>();
    private final List<User> selectedFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alliance);
        setupNavbar();

        etAllianceName = findViewById(R.id.etAllianceName);
        rvFriends = findViewById(R.id.rvFriends);
        btnCreateAlliance = findViewById(R.id.btnCreateAlliance);

        // Repozitorijumi i use case
        AllianceRepository allianceRepository = new AllianceRepositoryImpl(getApplicationContext());
        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        friendsUseCase = new FriendsUseCase(userRepository);

        // ViewModel sa Factory
        CreateAllianceViewModel.Factory factory =
                new CreateAllianceViewModel.Factory(allianceRepository, userRepository);
        viewModel = new ViewModelProvider(this, factory).get(CreateAllianceViewModel.class);

        setupFriendsRecyclerView();
        setupObservers();

        btnCreateAlliance.setOnClickListener(v -> createAlliance());

        loadFriends();
    }

    private void setupFriendsRecyclerView() {
        adapter = new AllianceMemberAdapter(friendsList, selectedFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCreatedAlliance().observe(this, alliance -> {
            Toast.makeText(this, "Savez \"" + alliance.getName() + "\" uspešno kreiran!", Toast.LENGTH_SHORT).show();
            finish();
        });

        viewModel.getErrorMessage().observe(this, message -> {
            Toast.makeText(this, "Greška: " + message, Toast.LENGTH_SHORT).show();
        });
    }

    private void createAlliance() {
        String allianceName = etAllianceName.getText().toString().trim();
        if (allianceName.isEmpty()) {
            Toast.makeText(this, "Unesite ime saveza", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        List<String> invitedUserIds = new ArrayList<>();
        for (User u : selectedFriends) {
            invitedUserIds.add(u.getId());
        }

        viewModel.createAlliance(currentUserId, allianceName, invitedUserIds);
    }

    private void loadFriends() {
        String currentUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        friendsUseCase.getFriends(currentUserId, new com.example.team11project.domain.repository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> friends) {
                runOnUiThread(() -> {
                    friendsList.clear();
                    friendsList.addAll(friends);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(CreateAllianceActivity.this, "Greška pri dohvatanju prijatelja", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}