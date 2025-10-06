package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.AllianceInvite;
import com.example.team11project.domain.model.User;
import com.example.team11project.presentation.viewmodel.AllianceInvitationViewModel;
import com.example.team11project.presentation.viewmodel.UserViewModel;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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


        String lastActiveDate = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("lastActiveDate", null);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String yesterday = java.time.LocalDate.now().minusDays(1).toString();


        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        userViewModel = new ViewModelProvider(this,
                new UserViewModel.Factory(new UserRepositoryImpl(getApplicationContext()), new AllianceMissionRepositoryImpl(getApplicationContext())))
                .get(UserViewModel.class);



        userViewModel.getUser().observe(this, user -> {
            if (user == null) return;

            Log.d("ACTIVE_DAYS", "Učitani user: " + new Gson().toJson(user));

            Log.d("ACTIVE_DAYS", "Učitani activeDays iz DB: " + user.getActiveDays());

            if (lastActiveDate == null) {
                // Prvi put, postavi danasnji datum i activeDays na 1
                user.setActiveDays(1);
                userViewModel.updateUser(user);
                getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("lastActiveDate", today)
                        .apply();
            } else {
                java.time.LocalDate lastDate = java.time.LocalDate.parse(lastActiveDate);
                java.time.LocalDate currentDate = java.time.LocalDate.parse(today);

                if (lastDate.plusDays(1).equals(currentDate)) {
                    // Novi dan, uzastopni
                    user.setActiveDays(user.getActiveDays() + 1);
                    userViewModel.updateUser(user);
                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("lastActiveDate", today)
                            .apply();
                } else if (lastDate.isBefore(currentDate)) {
                    // Prekinut niz, resetuj na 1
                    user.setActiveDays(1);
                    userViewModel.updateUser(user);
                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("lastActiveDate", today)
                            .apply();
                } else {
                    // Isto je dan, ništa ne radimo
                    Log.d("ACTIVE_DAYS", "Datum je isti, activeDays ostaje: " + user.getActiveDays());
                }
            }
            // Prikaz u TextView
        });

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
