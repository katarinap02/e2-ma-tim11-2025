package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
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

    private final List<User> members = new ArrayList<>();
    private final Set<String> loadedMemberIds = new HashSet<>();
    private String leaderId;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_details);

        setupNavbar();

        tvAllianceName = findViewById(R.id.tvAllianceName);
        tvLeaderName = findViewById(R.id.tvLeaderName);
        rvMembers = findViewById(R.id.rvMembers);
        btnStartMission = findViewById(R.id.btnStartMission);

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        AllianceRepository allianceRepository = new AllianceRepositoryImpl(getApplicationContext());
        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        AllianceMissionRepository allianceMissionRepository = new AllianceMissionRepositoryImpl((getApplicationContext()));

        allianceViewModel = new ViewModelProvider(this,
                new AllianceDetailsViewModel.Factory(allianceRepository, userRepository, allianceMissionRepository))
                .get(AllianceDetailsViewModel.class);

        userViewModel = new ViewModelProvider(this,
                new UserViewModel.Factory(userRepository))
                .get(UserViewModel.class);

        setupRecyclerView();
        setupObservers();



        Button btnDisband = findViewById(R.id.btnDisbandAlliance);

        btnDisband.setOnClickListener(v -> {
            Alliance currentAlliance = userViewModel.getUser().getValue() != null
                    ? userViewModel.getUser().getValue().getCurrentAlliance()
                    : null;

            if (currentAlliance == null) {
                Toast.makeText(this, "You are not part of any alliance", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!currentAlliance.getLeader().equals(userId)) {
                Toast.makeText(this, "Only the leader can disband the alliance", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Disband Alliance")
                    .setMessage("Are you sure you want to disband this alliance? All members will lose their membership.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        allianceViewModel.disbandAlliance(currentAlliance);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        if (userId != null) {
            userViewModel.loadUser(userId);
        } else {
            tvLeaderName.setText("Trenutno niste prijavljeni");
        }

        Button btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> {
            Alliance currentAlliance = userViewModel.getUser().getValue() != null
                    ? userViewModel.getUser().getValue().getCurrentAlliance()
                    : null;

            if (currentAlliance == null) {
                Toast.makeText(this, "Niste član nijednog saveza", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, AllianceChatActivity.class);
            intent.putExtra("allianceId", currentAlliance.getId());
            intent.putExtra("allianceLeaderId", currentAlliance.getLeader());
            startActivity(intent);
        });

        btnStartMission.setOnClickListener(v -> {
            Alliance currentAlliance = userViewModel.getUser().getValue() != null
                    ? userViewModel.getUser().getValue().getCurrentAlliance()
                    : null;

            if (currentAlliance == null) {
                Toast.makeText(this, "Niste član nijednog saveza", Toast.LENGTH_SHORT).show();
                return;
            }

            allianceViewModel.startSpecialMission(currentAlliance, userId);
        });





    }

    private void setupRecyclerView() {
        adapter = new FriendsAdapter(members);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(adapter);
    }

    private void setupObservers() {
        userViewModel.getUser().observe(this, user -> {
            if (user == null) return;

            Alliance alliance = user.getCurrentAlliance();
            if (alliance != null) {
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
                allianceViewModel.checkActiveMission(alliance.getId(), user.getId(), leaderId);

            } else {
                tvLeaderName.setText("Trenutno niste član nijednog saveza");
                members.clear();
                adapter.notifyDataSetChanged();
            }
        });

        allianceViewModel.getAlliance().observe(this, alliance -> {
            if (alliance == null) {
                tvAllianceName.setText("");
                tvLeaderName.setText("No alliance");
                members.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Alliance has been disbanded", Toast.LENGTH_SHORT).show();
            }
        });
        allianceViewModel.getMissionButtonText().observe(this, text -> {
            if (text == null || text.isEmpty()) {
                btnStartMission.setVisibility(Button.GONE); // sakrij za članove
            } else {
                btnStartMission.setVisibility(Button.VISIBLE);
                btnStartMission.setText(text);
            }
        });

        allianceViewModel.getMissionStarted().observe(this, allianceId -> {
            if (allianceId != null) {
                Intent intent = new Intent(this, AllianceMissionActivity.class);
                intent.putExtra("allianceId", allianceId);
                startActivity(intent);
            }
        });


        allianceViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getError().observe(this, message ->
                Toast.makeText(this, "Greška: " + message, Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserAndAddToMembers(String id) {
        if (loadedMemberIds.contains(id)) return;
        loadedMemberIds.add(id);

        userViewModel.getUserById(id, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user.getId().equals(leaderId)) {
                        tvLeaderName.setText("Leader: " + user.getUsername());
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

}
