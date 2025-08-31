package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.LevelInfoRepositoryImpl;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.presentation.viewmodel.LevelInfoViewModel;
import com.example.team11project.presentation.viewmodel.RegisterViewModel;

public class LevelInfoActivity extends BaseActivity {

    private TextView textTitle, textPP, textCurrentXP, textNextLevelXP, textLevel;
    private ProgressBar progressXp;
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

        String userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        LevelInfoRepository repository = new LevelInfoRepositoryImpl(getApplicationContext());
        LevelInfoViewModel.Factory factory = new LevelInfoViewModel.Factory(repository, userId);
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

    }

}
