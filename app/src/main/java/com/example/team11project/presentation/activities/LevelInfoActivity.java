package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.BossBattleRepositoryImpl;
import com.example.team11project.data.repository.BossRepositoryImpl;
import com.example.team11project.data.repository.BossRewardRepositoryImpl;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.LevelInfoRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.BossUseCase;
import com.example.team11project.presentation.viewmodel.LevelInfoViewModel;
import com.example.team11project.presentation.viewmodel.RegisterViewModel;

public class LevelInfoActivity extends BaseActivity {

    private TextView textTitle, textPP, textCurrentXP, textNextLevelXP, textLevel;
    private ProgressBar progressXp;

    private Button btnBossFight;
    private LevelInfoViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_info);
        setupNavbar();

        textTitle = findViewById(R.id.tvTitle);
        textPP = findViewById(R.id.tvPP);
        textCurrentXP = findViewById(R.id.tvXp);
        textNextLevelXP = findViewById(R.id.tvNextXp);
        progressXp = findViewById(R.id.progressXp);
        textLevel = findViewById(R.id.tvLevel);
        btnBossFight = findViewById(R.id.btnBossFight);

        String userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        LevelInfoRepository repository = new LevelInfoRepositoryImpl(getApplicationContext());
        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        BossRepositoryImpl bossRepository = new BossRepositoryImpl(getApplicationContext());
        BossBattleRepositoryImpl battleRepository = new BossBattleRepositoryImpl(getApplicationContext());
        BossRewardRepositoryImpl rewardRepository = new BossRewardRepositoryImpl(getApplicationContext());
        EquipmentRepository equipmentRepository = new EquipmentRepositoryImpl(getApplicationContext());
        BossUseCase bossUseCase = new BossUseCase(bossRepository, battleRepository, rewardRepository, equipmentRepository, userRepository);
        LevelInfoViewModel.Factory factory = new LevelInfoViewModel.Factory(repository, userRepository, bossUseCase, userId);
        viewModel = new ViewModelProvider(this, factory).get(LevelInfoViewModel.class);

        viewModel.getProgress().observe(this, progress -> {
            if (progress != null) {
                progressXp.setProgress(progress);
            }
        });

        viewModel.getLevelInfo().observe(this, levelInfo -> {
            if (levelInfo == null) return;

            textTitle.setText("Titula: " + levelInfo.getTitle().name());
            textPP.setText("PP: " + levelInfo.getPp());
            textCurrentXP.setText("XP: " + levelInfo.getXp());
            textNextLevelXP.setText(" / " + levelInfo.getXpForNextLevel());
            textLevel.setText("Nivo: " + levelInfo.getLevel());

            int progress = (int) ((levelInfo.getXp() * 100.0) / levelInfo.getXpForNextLevel());
            progressXp.setProgress(progress);
        });

        viewModel.getShowBossButton().observe(this, showButton -> {
            if (showButton != null && showButton) {
                btnBossFight.setVisibility(android.view.View.VISIBLE);
            } else {
                btnBossFight.setVisibility(android.view.View.GONE);
            }
        });

        viewModel.getAvailableBoss().observe(this, boss -> {
            if (boss != null) {
                btnBossFight.setText("Borba sa Bos-om (Nivo " + boss.getLevel() + ")");
            }
        });

        btnBossFight.setOnClickListener(v -> {
            Boss availableBoss = viewModel.getAvailableBoss().getValue();
            if (availableBoss != null) {
                Intent intent = new Intent(this, EquipmentActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

    }

}
