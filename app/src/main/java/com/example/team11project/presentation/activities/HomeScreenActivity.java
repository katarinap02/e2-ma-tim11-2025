package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.AllianceInvite;
import com.example.team11project.presentation.viewmodel.AllianceInvitationViewModel;
import com.example.team11project.presentation.viewmodel.UserViewModel;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HomeScreenActivity extends BaseActivity {

    private UserViewModel userViewModel;
    private AllianceInvitationViewModel invitationViewModel;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        setupNavbar();

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        userViewModel = new ViewModelProvider(this,
                new UserViewModel.Factory(new UserRepositoryImpl(getApplicationContext())))
                .get(UserViewModel.class);

        Executor executor = Executors.newSingleThreadExecutor();

        invitationViewModel = new ViewModelProvider(this,
                new AllianceInvitationViewModel.Factory(
                        new AllianceRepositoryImpl(getApplicationContext()),
                        executor, new UserRepositoryImpl(getApplicationContext())
                )).get(AllianceInvitationViewModel.class);

        if (userId != null) {
            invitationViewModel.loadInvites(userId);
        }

        setupButtons(userId);

        invitationViewModel.getInvites().observe(this, invites -> {
            if (invites == null || invites.isEmpty()) return;

            for (AllianceInvite invite : invites) {
                if (!invite.isResponded()) {
                    showInviteNotification(invite);
                }
            }
        });

        invitationViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Greška: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons(String userId) {
        Button btnAllProfiles = findViewById(R.id.btnAllProfiles);
        btnAllProfiles.setOnClickListener(v -> {
            startActivity(new Intent(this, UsersListActivity.class));
        });

        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        Button btnAddEdit = findViewById(R.id.btnAddEdit);
        btnAddEdit.setOnClickListener(v -> startActivity(new Intent(this, TaskActivity.class)));

        Button btnCategories = findViewById(R.id.btnCategories);
        btnCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoryActivity.class)));

        Button btnLevelInfo = findViewById(R.id.btnLevelInfo);
        btnLevelInfo.setOnClickListener(v -> startActivity(new Intent(this, LevelInfoActivity.class)));

        Button btnStore = findViewById(R.id.btnStore);
        btnStore.setOnClickListener(v -> startActivity(new Intent(this, StoreActivity.class)));

        Button btnFriends = findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(v -> startActivity(new Intent(this, FriendsActivity.class)));

        Button btnAlliance = findViewById(R.id.btnAlliance);

        userViewModel.loadUser(userId);
        userViewModel.getUser().observe(this, user -> {
            if (user != null && user.getCurrentAlliance() != null && user.getCurrentAlliance().getId() != null) {
                btnAlliance.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AllianceDetailsActivity.class);
                    intent.putExtra("allianceId", user.getCurrentAlliance().getId());
                    startActivity(intent);
                });
            } else {
                btnAlliance.setOnClickListener(v ->
                        Toast.makeText(this, "Nemate savez ili niste član nijednog saveza.", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showInviteNotification(AllianceInvite invite) {
        userViewModel.getUser().observe(this, user -> {
            if (user == null) return;

            boolean hasCurrentAlliance = user.getCurrentAlliance() != null;
            boolean missionActive = hasCurrentAlliance && user.getCurrentAlliance().isMissionActive();

            String message = invite.getFromUser().getUsername() + " vas je pozvao u savez: " +
                    invite.getAlliance().getName();

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Poziv za savez")
                    .setCancelable(false);

            if (hasCurrentAlliance && !missionActive) {
                message += "\n\nPrihvatanjem ovog poziva napuštate trenutni savez: " +
                        user.getCurrentAlliance().getName() + ". Da li želite da nastavite?";
                builder.setMessage(message)
                        .setPositiveButton("Prihvati", (dialog, which) -> invitationViewModel.acceptInvite(invite.getId(), userId))
                        .setNegativeButton("Odbij", (dialog, which) -> invitationViewModel.rejectInvite(invite.getId(), userId));
            } else if (missionActive) {
                message += "\n\nNe možete napustiti trenutni savez jer je pokrenuta misija!";
                builder.setMessage(message)
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            } else {
                builder.setMessage(message)
                        .setPositiveButton("Prihvati", (dialog, which) -> invitationViewModel.acceptInvite(invite.getId(), userId))
                        .setNegativeButton("Odbij", (dialog, which) -> invitationViewModel.rejectInvite(invite.getId(), userId));
            }

            builder.show();
        });
    }

}
