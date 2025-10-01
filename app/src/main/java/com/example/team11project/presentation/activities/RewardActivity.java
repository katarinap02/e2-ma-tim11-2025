package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.presentation.viewmodel.BossViewModel;
import com.example.team11project.presentation.viewmodel.RewardViewModel;

public class RewardActivity extends BaseActivity{

    private String bossId;
    private String userId;
    private int level;
    private RewardViewModel rewardViewModel;

    private ProgressBar progressBar;
    private TextView tvReward;
    private TextView tvCoins;
    private TextView tvEquipment;
    private TextView tvEquipmentName;
    private ImageView ivEquipment;
    private LinearLayout equipmentContainer;
    private Button btnClaimReward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reward);
        setupNavbar();

        bossId = getIntent().getStringExtra("bossId");
        userId = getIntent().getStringExtra("userId");
        level = getIntent().getIntExtra("level", 1);

        initializeViews();

        setupViewModel();
        rewardViewModel.fetchReward(userId, bossId, level);

    }

    private void initializeViews()
    {
        progressBar = findViewById(R.id.progressBarReward);
        tvReward = findViewById(R.id.tvReward);
        tvCoins = findViewById(R.id.tvCoins);
        tvEquipment = findViewById(R.id.tvEquipment);
        tvEquipmentName = findViewById(R.id.tvEquipmentName);
        ivEquipment = findViewById(R.id.ivEquipment);
        equipmentContainer = findViewById(R.id.equipmentContainer);
        btnClaimReward = findViewById(R.id.btnClaimReward);

        // Inicijalno sakrij equipment kontejner
        equipmentContainer.setVisibility(View.GONE);
    }

    private void setupViewModel() {
        RewardViewModel.Factory factory = new RewardViewModel.Factory(getApplication());
        rewardViewModel = new ViewModelProvider(this, factory).get(RewardViewModel.class);

        rewardViewModel.reward.observe(this, bossReward -> {
            if (bossReward != null) {
                // Prikaz osvojenih novčića
                tvCoins.setText("Osvojeni novčići: " + bossReward.getCoinsEarned());

                if (bossReward.getEquipmentId() != null && !bossReward.getEquipmentId().isEmpty()) {
                    tvEquipment.setText("Čestitamo! Dobili ste opremu!");
                } else {
                    tvEquipment.setText("Nema opreme ovaj put");
                    equipmentContainer.setVisibility(View.GONE);
                }
                updateClaimButtonState(bossReward);
            }
        });



        // Observer za opremu
        rewardViewModel.equipment.observe(this, equipment -> {
            if (equipment != null) {
                showEquipmentDetails(equipment);
            }
        });

        // Observer za loading
        rewardViewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                tvReward.setText("Učitavanje nagrade...");
                btnClaimReward.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                tvReward.setText("Nagrade");
                btnClaimReward.setEnabled(true);
            }
        });

        // Observer za greške
        rewardViewModel.error.observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

        rewardViewModel.successMessage.observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        // Observer za status preuzimanja nagrade
        rewardViewModel.rewardClaimed.observe(this, claimed -> {
            if (claimed != null && claimed) {
                btnClaimReward.setEnabled(false);
                btnClaimReward.setText("Preuzeto");
            }
        });

        // Click listener za dugme
        btnClaimReward.setOnClickListener(v -> {
            rewardViewModel.claimReward(userId, bossId, level);
        });
    }

    private void updateClaimButtonState(com.example.team11project.domain.model.BossReward bossReward) {
        // Provjeri da li nagrada ima sadržaj za preuzimanje
        boolean hasReward = (bossReward.getCoinsEarned() > 0) ||
                (bossReward.getEquipmentId() != null && !bossReward.getEquipmentId().isEmpty());

        if (hasReward) {
            btnClaimReward.setEnabled(true);
            btnClaimReward.setText("Preuzmi nagradu");
        } else {
            btnClaimReward.setEnabled(false);
            btnClaimReward.setText("Preuzeto");
        }
    }

    private void showEquipmentDetails(Equipment equipment) {
        equipmentContainer.setVisibility(View.VISIBLE);

        // Naziv opreme
        tvEquipmentName.setText(equipment.getName());

        // Učitavanje slike opreme
        loadEquipmentImage(equipment);
    }

    private void loadEquipmentImage(Equipment equipment) {
        if (equipment.getImage() != null && !equipment.getImage().isEmpty()) {
            // Pokušaj da učitaš sliku na osnovu imena
            int imageResourceId = getImageResourceId(equipment.getImage());
            if (imageResourceId != 0) {
                ivEquipment.setImageResource(imageResourceId);
                ivEquipment.setVisibility(View.VISIBLE);
            } else {
                ivEquipment.setVisibility(View.GONE);
            }
        } else {
            ivEquipment.setVisibility(View.GONE);
        }
    }

    private int getImageResourceId(String imageName) {
        // Konvertuje ime slike u resource ID
        // Na primer: "potion_10" -> R.drawable.potion_10
        if (imageName != null && !imageName.isEmpty()) {
            return getResources().getIdentifier(imageName, "drawable", getPackageName());
        }
        return 0;
    }
}