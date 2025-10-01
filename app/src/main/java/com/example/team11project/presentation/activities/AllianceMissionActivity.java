package com.example.team11project.presentation.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team11project.R;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.MemberProgress;
import com.example.team11project.presentation.adapters.MemberProgressAdapter;
import com.example.team11project.presentation.viewmodel.AllianceMissionViewModel;

public class AllianceMissionActivity extends AppCompatActivity {

    private AllianceMissionViewModel viewModel;
    private RecyclerView rvMembers;
    private MemberProgressAdapter adapter;

    private ProgressBar pbAllianceProgress, pbBossHP;
    private TextView tvAllianceProgress, tvBossHP, tvSpecialMissionTitle, tvBossName;
    private ImageView ivBoss;

    private String allianceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_mission);

        // Dobijamo allianceId iz prethodne stranice
        allianceId = getIntent().getStringExtra("allianceId");
        if (allianceId == null) {
            Toast.makeText(this, "Alliance ID nije prosleđen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        initializeViews();
        initializeViewModel();

        adapter = new MemberProgressAdapter(userId);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(adapter);

        observeViewModel();

        // Poziv repository-ja za učitavanje aktivne misije
        viewModel.getActiveMissionByAllianceId(allianceId);
    }

    private void initializeViews() {
        pbAllianceProgress = findViewById(R.id.pbAllianceProgress);
        pbBossHP = findViewById(R.id.pbBossHP);

        tvAllianceProgress = findViewById(R.id.tvAllianceProgress);
        tvBossHP = findViewById(R.id.tvBossHP);
        tvSpecialMissionTitle = findViewById(R.id.tvSpecialMissionTitle);
        tvBossName = findViewById(R.id.tvBossName);
        ivBoss = findViewById(R.id.ivBoss);

        rvMembers = findViewById(R.id.rvMembersProgress);
    }

    private void initializeViewModel() {
        AllianceMissionViewModel.Factory factory = new AllianceMissionViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(AllianceMissionViewModel.class);
    }

    private void observeViewModel() {
        viewModel.getActiveMission().observe(this, mission -> {
            if (mission != null) {
                updateUI(mission);
            } else {
                Toast.makeText(this, "Nema aktivne misije", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(AllianceMission mission) {
        // Boss informacije
        tvBossName.setText("Boss specijalne misije");

        // Boss HP
        int bossCurrentHp = mission.getBoss().getCurrentHp();
        int bossMaxHp = mission.getBoss().getMaxHp();

        pbBossHP.setMax(bossMaxHp);
        pbBossHP.setProgress(bossCurrentHp);
        tvBossHP.setText(bossCurrentHp + " / " + bossMaxHp + " HP");

        // Savez napredak (total damage svih članova)
        int allianceTotal = 0;
        for (MemberProgress mp : mission.getMemberProgressList()) {
            allianceTotal += mp.getTotalDamageDealt();
        }

        pbAllianceProgress.setMax(bossMaxHp);
        pbAllianceProgress.setProgress(allianceTotal);
        tvAllianceProgress.setText(allianceTotal + " / " + bossMaxHp);

        // Postavi max HP za adapter (da zna kako da prikaže progress bar)
        adapter.setBossMaxHp(bossMaxHp);

        // Adapter RecyclerView-a - prikaži sve članove i njihov damage
        adapter.setMemberProgressList(mission.getMemberProgressList());
    }
}