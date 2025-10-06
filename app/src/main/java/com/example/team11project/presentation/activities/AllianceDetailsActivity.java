package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.BossRepositoryImpl;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.TaskInstanceRepositoryImpl;
import com.example.team11project.data.repository.TaskRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.presentation.adapters.FriendsAdapter;
import com.example.team11project.presentation.viewmodel.AllianceDetailsViewModel;
import com.example.team11project.presentation.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllianceDetailsActivity extends BaseActivity {

    private TextView tvAllianceName, tvLeaderName;
    private RecyclerView rvMembers;

    private AllianceDetailsViewModel allianceViewModel;
    private UserViewModel userViewModel;
    private FriendsAdapter adapter;

    private Button btnStartMission;
    private Button btnDisband;
    private Button btnChat;

    private Button bntRewards;
    private ProgressBar progressBar;

    private final List<User> members = new ArrayList<>();
    private final Set<String> loadedMemberIds = new HashSet<>();
    private String leaderId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_details);



        setupNavbar();
        initializeViews();
        initializeRepositories();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        // Učitaj trenutnog korisnika
        if (userId != null) {
            userViewModel.loadUser(userId);
        } else {
            tvLeaderName.setText("Trenutno niste prijavljeni");
        }
    }

    private void initializeViews() {
        tvAllianceName = findViewById(R.id.tvAllianceName);
        tvLeaderName = findViewById(R.id.tvLeaderName);
        rvMembers = findViewById(R.id.rvMembers);
        btnStartMission = findViewById(R.id.btnStartMission);
        btnDisband = findViewById(R.id.btnDisbandAlliance);
        btnChat = findViewById(R.id.btnChat);
        bntRewards = findViewById(R.id.btnRewards);
        progressBar = findViewById(R.id.progressBar);

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);
    }

    private void initializeRepositories() {
        AllianceRepository allianceRepository = new AllianceRepositoryImpl(getApplicationContext());
        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        AllianceMissionRepository allianceMissionRepository = new AllianceMissionRepositoryImpl(getApplicationContext());
        TaskRepository taskRepository = new TaskRepositoryImpl(getApplicationContext());
        TaskInstanceRepository taskInstanceRepository = new TaskInstanceRepositoryImpl(getApplicationContext());
        EquipmentRepository equipmentRepository = new EquipmentRepositoryImpl(getApplicationContext());
        BossRepository bossRepository = new BossRepositoryImpl(getApplicationContext());

        allianceViewModel = new ViewModelProvider(this,
                new AllianceDetailsViewModel.Factory(allianceRepository, userRepository,
                        allianceMissionRepository, taskRepository, taskInstanceRepository, equipmentRepository, bossRepository))
                .get(AllianceDetailsViewModel.class);

        userViewModel = new ViewModelProvider(this,
                new UserViewModel.Factory(userRepository, allianceMissionRepository))
                .get(UserViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new FriendsAdapter(members);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnDisband.setOnClickListener(v -> handleDisbandAlliance());
        btnChat.setOnClickListener(v -> handleOpenChat());
        btnStartMission.setOnClickListener(v -> handleStartMission());
        bntRewards.setOnClickListener(v-> handleRewardsOpen());
    }

    private void handleDisbandAlliance() {
        Alliance currentAlliance = getCurrentAlliance();
        if (currentAlliance == null) {
            Toast.makeText(this, "Niste član nijednog saveza", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentAlliance.getLeader().equals(userId)) {
            Toast.makeText(this, "Samo lider može da ukine savez", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ukini savez")
                .setMessage("Da li ste sigurni da želite da ukinete ovaj savez? Svi članovi će izgubiti članstvo.")
                .setPositiveButton("Da", (dialog, which) -> {
                    allianceViewModel.disbandAlliance(currentAlliance);
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void handleOpenChat() {
        Alliance currentAlliance = getCurrentAlliance();
        if (currentAlliance == null) {
            Toast.makeText(this, "Niste član nijednog saveza", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AllianceChatActivity.class);
        intent.putExtra("allianceId", currentAlliance.getId());
        intent.putExtra("allianceLeaderId", currentAlliance.getLeader());
        startActivity(intent);
    }

    private void handleRewardsOpen() {

        Intent intent = new Intent(this, AllianceRewardsActivity.class);
        startActivity(intent);
    }

    private void handleStartMission() {
        Alliance currentAlliance = getCurrentAlliance();
        if (currentAlliance == null) {
            Toast.makeText(this, "Niste član nijednog saveza", Toast.LENGTH_SHORT).show();
            return;
        }

            allianceViewModel.startSpecialMission(currentAlliance, userId);
            Intent intent = new Intent(this, AllianceMissionActivity.class);
            intent.putExtra("allianceId", currentAlliance.getId());
            startActivity(intent);
    }

    private void setupObservers() {
        // Observer za trenutnog korisnika
        userViewModel.getUser().observe(this, user -> {
            if (user == null) return;

            Alliance alliance = user.getCurrentAlliance();
            if (alliance != null) {
                displayAllianceInfo(alliance);
                // Automatski proveri da li ima istekla misija
                allianceViewModel.checkAndFinalizeExpiredMission(alliance, userId);
            } else {
                displayNoAlliance();
            }
        });

        // Observer za promene u savezu
        allianceViewModel.getAlliance().observe(this, alliance -> {
            if (alliance == null) {
                displayNoAlliance();
                Toast.makeText(this, "Savez je ukinut", Toast.LENGTH_SHORT).show();
            }
        });

        // Ukloni duplikate i ostavi samo jedan observer
        allianceViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                // Prikaži/sakrij progress bar
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

                // Onemogući/omogući dugmad
                btnStartMission.setEnabled(!isLoading);
                btnDisband.setEnabled(!isLoading);

                // Promeni tekst dugmeta ako je vidljivo
                if (isLoading && btnStartMission.getVisibility() == View.VISIBLE) {
                    btnStartMission.setText("Učitavanje...");
                }
            }
        });

// Observer za tekst dugmeta misije
        allianceViewModel.getMissionButtonText().observe(this, text -> {
            if (text == null || text.isEmpty()) {
                btnStartMission.setVisibility(View.GONE);
            } else {
                btnStartMission.setVisibility(View.VISIBLE);
                // Postavi tekst samo ako nije loading
                Boolean isLoading = allianceViewModel.getIsLoading().getValue();
                if (isLoading == null || !isLoading) {
                    btnStartMission.setText(text);
                }
            }
        });

        // Observer za pokretanje misije
        allianceViewModel.getMissionStarted().observe(this, allianceId -> {
            if (allianceId != null) {
                Intent intent = new Intent(this, AllianceMissionActivity.class);
                intent.putExtra("allianceId", allianceId);
                startActivity(intent);
            }
        });

        // Observer za success poruke
        allianceViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                // Osveži prikaz nakon završetka misije
                User currentUser = userViewModel.getUser().getValue();
                if (currentUser != null) {
                    userViewModel.loadUser(currentUser.getId());
                }
            }
        });

        // Observer za greške
        allianceViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Greška: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAllianceInfo(Alliance alliance) {
        tvAllianceName.setText(alliance.getName());
        members.clear();
        loadedMemberIds.clear();

        leaderId = alliance.getLeader();

        if (leaderId != null) {
            loadUserAndAddToMembers(leaderId);
        }

        if (alliance.getMembers() != null) {
            for (String memberId : alliance.getMembers()) {
                loadUserAndAddToMembers(memberId);
            }
        }

        // Proveri status misije i ažuriraj dugme
        allianceViewModel.checkActiveMission(alliance.getId(), userId, leaderId);
    }

    private void displayNoAlliance() {
        tvAllianceName.setText("");
        tvLeaderName.setText("Trenutno niste član nijednog saveza");
        members.clear();
        adapter.notifyDataSetChanged();
        btnStartMission.setVisibility(View.GONE);
    }

    private void loadUserAndAddToMembers(String id) {
        if (loadedMemberIds.contains(id)) return;
        loadedMemberIds.add(id);

        userViewModel.getUserById(id, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user.getId().equals(leaderId)) {
                        tvLeaderName.setText("Lider: " + user.getUsername());
                    }

                    if (!members.contains(user)) {
                        members.add(user);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(AllianceDetailsActivity.this,
                        "Greška pri učitavanju korisnika: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private Alliance getCurrentAlliance() {
        User currentUser = userViewModel.getUser().getValue();
        return currentUser != null ? currentUser.getCurrentAlliance() : null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Osvežavanje stanja kada se vraćamo na aktivnost
        User currentUser = userViewModel.getUser().getValue();
        if (currentUser != null && currentUser.getCurrentAlliance() != null) {
            Alliance alliance = currentUser.getCurrentAlliance();
            // Proveri da li ima istekla misija
            allianceViewModel.checkAndFinalizeExpiredMission(alliance, userId);
            // Osveži status misije
            allianceViewModel.refreshAllianceState(
                    alliance.getId(),
                    userId,
                    alliance.getLeader()
            );
        }
    }
}