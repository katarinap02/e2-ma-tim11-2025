package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;

public class BossActivity extends BaseActivity {

    private ImageView ivBoss;
    private ProgressBar pbUserPP;
    private ProgressBar pbBossHP;
    private TextView tvUserPP;
    private TextView tvBossHP;
    private TextView tvHitChance;
    private TextView tvAttacksLeft;
    LinearLayout layoutEquipment;
    LinearLayout layoutActiveEquipment;
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
        pbUserPP = findViewById(R.id.pbUserPP);
        pbBossHP = findViewById(R.id.pbBossHP);
        tvUserPP = findViewById(R.id.tvUserPP);
        tvBossHP = findViewById(R.id.tvBossHP);
        tvHitChance = findViewById(R.id.tvHitChance);
        tvAttacksLeft = findViewById(R.id.tvAttacksLeft);
        btnAttack = findViewById(R.id.btnAttack);
        layoutActiveEquipment = findViewById(R.id.layoutActiveEquipment);

    }

    private void setupClickListener() {
        btnAttack.setOnClickListener(v -> {
            ivBoss.setImageResource(R.drawable.boss_hit2);
            ivBoss.postDelayed(() -> ivBoss.setImageResource(R.drawable.boss_idle2), 500);

            viewModel.performAttack();
        });
    }

    private void setupViewModel() {
        BossViewModel.Factory factory = new BossViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(BossViewModel.class);

        viewModel.bossBattle.observe(this, bossBattle -> {
            if (bossBattle != null) {
                Boss boss = viewModel.boss.getValue();
                if (boss != null) {
                    updateUI(bossBattle, boss);
                }
            }
        });

        viewModel.boss.observe(this, boss -> {
            if (boss != null) {
                BossBattle bossBattle = viewModel.bossBattle.getValue();
                if (bossBattle != null) {
                    updateUI(bossBattle, boss);
                }
            }
        });


        viewModel.attackResult.observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                showToast(result);
                viewModel.clearAttackResult();
            }
        });

        // Observiranje završetka borbe
        viewModel.battleFinished.observe(this, isFinished -> {
            if (isFinished != null && isFinished) {
                showBattleEndAnimation();
            }
        });

        // Observiranje grešaka
        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showToast(error);
                viewModel.clearError();
            }
        });

        // Observiranje loading stanja
        viewModel.isLoading.observe(this, isLoading -> {
            btnAttack.setEnabled(!isLoading);
            if (isLoading) {
                btnAttack.setText("Napad...");
            } else {
                Boolean battleFinished = viewModel.battleFinished.getValue();
                if (battleFinished == null || !battleFinished) {
                    btnAttack.setText("Napad");
                }
            }
        });

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

        layoutActiveEquipment.removeAllViews();

        if (battle.getActiveEquipment() != null && !battle.getActiveEquipment().isEmpty()) {
            for (String imageName : battle.getActiveEquipment()) {
                ImageView iv = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110, 110);
                params.setMargins(8, 0, 8, 0);
                iv.setLayoutParams(params);

                int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                if (resId != 0) {
                    iv.setImageResource(resId);
                } else {
                }

                layoutActiveEquipment.addView(iv);
            }
        }

    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }

    private void showBattleEndAnimation() {
        BossBattle battle = viewModel.bossBattle.getValue();
        Boss boss = viewModel.boss.getValue();

        if (battle == null || boss == null) return;

        // Sakrij battle UI elemente
        hideBattleUI();

        String battleEndMessage;
        if (battle.isBossDefeated()) {
            battleEndMessage = "POBEDA!\nBoss je poražen!";
        } else {
            double damagePercent = (double) battle.getDamageDealt() / boss.getMaxHP();
            if (damagePercent >= 0.5) {
                battleEndMessage = "DELIMIČNA POBEDA!\nNanešeno više od 50% štete";
            } else {
                battleEndMessage = "PORAZ!\nBoss nije dovoljno oslabljen";
            }
        }

        // Prikaži poruku
        TextView tvBossTitle = findViewById(R.id.tvBossTitle);
        tvBossTitle.setText(battleEndMessage);
        tvBossTitle.setTextSize(24);

        // Animacija sa kovčegom
        showChestAnimation();
    }

    private void hideBattleUI() {
        pbUserPP.setVisibility(View.GONE);
        pbBossHP.setVisibility(View.GONE);
        tvUserPP.setVisibility(View.GONE);
        tvBossHP.setVisibility(View.GONE);
        btnAttack.setVisibility(View.GONE);
        layoutActiveEquipment.setVisibility(View.GONE);
        tvHitChance.setVisibility(View.GONE);
        tvAttacksLeft.setVisibility(View.GONE);

        // Sakrij ceo layout battle info ako želiš
        LinearLayout layoutBattleInfo = findViewById(R.id.layoutBattleInfo);
        LinearLayout layoutEquipment = findViewById(R.id.layoutEquipment);
        if (layoutBattleInfo != null) {
            layoutBattleInfo.setVisibility(View.GONE);
        }
        if (layoutEquipment != null) {
            layoutEquipment.setVisibility(View.GONE);
        }

        // Sakrij boss sliku
        ivBoss.setVisibility(View.GONE);
    }

    private void showChestAnimation() {
        // Postavi kovčeg na poziciju boss-a
        ivBoss.setVisibility(android.view.View.VISIBLE);
        ivBoss.setImageResource(R.drawable.chest_closed);

        ivBoss.setTranslationX(-70f);
        ivBoss.setTranslationY(70f);

        // Animacija skale za dramatičnost
        ivBoss.setScaleX(0.5f);
        ivBoss.setScaleY(0.5f);
        ivBoss.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(500)
                .start();

        // Nakon 1.5 sekunde otvori kovčeg
        ivBoss.postDelayed(() -> {
            ivBoss.setImageResource(R.drawable.chest_open);

            // Animacija "bounce" efekta prilikom otvaranja
            ivBoss.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(200)
                    .withEndAction(() -> ivBoss.animate()
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .setDuration(200)
                            .start())
                    .start();

            // Nakon još 1 sekunde automatski pređi na RewardActivity
            ivBoss.postDelayed(() -> {
                Intent intent = new Intent(this, RewardActivity.class);

                // Prosledi iste parametre kao pre
                intent.putExtra("bossId", getIntent().getStringExtra("bossId"));
                intent.putExtra("userId", getIntent().getStringExtra("userId"));
                intent.putExtra("level", getIntent().getIntExtra("level", 1));

                startActivity(intent);
                finish();
            }, 1000); // 1 sekunda kasnije
        }, 1500); // otvaranje kovčega
    }




}
