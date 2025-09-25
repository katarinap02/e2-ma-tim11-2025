package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import android.widget.ProgressBar;

import com.example.team11project.R;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.presentation.viewmodel.BossViewModel;
import com.example.team11project.domain.model.BossBattle;

public class BossActivity extends BaseActivity {

    private ImageView ivBoss;
    private ImageView ivEquipment;
    private ProgressBar pbUserPP;
    private ProgressBar pbBossHP;
    private TextView tvUserPP;
    private TextView tvBossHP;
    private TextView tvHitChance;
    private TextView tvAttacksLeft;
    private Button btnAttack;

    private String bossId;
    private String userId;
    private int level;

    private BossViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_boss);

        bossId = getIntent().getStringExtra("bossId");
        userId = getIntent().getStringExtra("userId");
        level = getIntent().getIntExtra("level", 1);

        initializeViews();
        setupClickListener();
        setupViewModel();

    }

    private void initializeViews() {
        ivBoss = findViewById(R.id.ivBoss);
        ivEquipment = findViewById(R.id.ivEquipment);
        pbUserPP = findViewById(R.id.pbUserPP);
        pbBossHP = findViewById(R.id.pbBossHP);
        tvUserPP = findViewById(R.id.tvUserPP);
        tvBossHP = findViewById(R.id.tvBossHP);
        tvHitChance = findViewById(R.id.tvHitChance);
        tvAttacksLeft = findViewById(R.id.tvAttacksLeft);
        btnAttack = findViewById(R.id.btnAttack);
    }

    private void setupClickListener() {
        btnAttack.setOnClickListener(v -> {
            ivBoss.setImageResource(R.drawable.boss_hit2);
            ivBoss.postDelayed(() -> ivBoss.setImageResource(R.drawable.boss_idle2), 500);

            // Logika napada bi ovde išla (update BossBattle)
        });
    }

    private void setupViewModel() {
        BossViewModel.Factory factory = new BossViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(BossViewModel.class);

        viewModel.boss.observe(this, boss -> {
            if (boss != null) {
                viewModel.bossBattle.observe(this, bossBattle -> {
                    if (bossBattle != null) {
                        updateUI(bossBattle, boss); // ažurira napade, PP, status borbe itd.
                    }
                });
            }
        });



        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showToast(error);
                viewModel.clearError();
            }
        });

        viewModel.isLoading.observe(this, isLoading -> btnAttack.setEnabled(!isLoading));

        // Učitavanje BossBattle-a
        viewModel.loadBattleWithBoss(userId, bossId, level);
    }

    private void updateUI(BossBattle battle, Boss boss) {
        // PP korisnika
        pbUserPP.setMax(battle.getUserPP());
        pbUserPP.setProgress(battle.getUserPP());
        tvUserPP.setText(battle.getUserPP() + " PP");

        // HP bossa
        int bossHP = boss.getMaxHP();
        pbBossHP.setMax(bossHP);
        pbBossHP.setProgress(bossHP - battle.getDamageDealt());
        tvBossHP.setText((bossHP - battle.getDamageDealt()) + "/" + bossHP + " HP");

        tvHitChance.setText("Šansa za pogodak: " + (int) (battle.getHitChance() * 100) + "%");

        int maxAttacks = 5;
        int attacksLeft = maxAttacks - battle.getAttacksUsed();
        tvAttacksLeft.setText(attacksLeft + "/" + maxAttacks + " napada");

        if (battle.getActiveEquipment() != null && !battle.getActiveEquipment().isEmpty()) {
            String equipmentName = battle.getActiveEquipment().get(0);
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }
}
