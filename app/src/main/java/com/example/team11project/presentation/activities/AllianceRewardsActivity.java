package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.presentation.adapters.AllianceRewardsAdapter;
import com.example.team11project.presentation.viewmodel.AllianceRewardsViewModel;

import java.util.ArrayList;

public class AllianceRewardsActivity extends BaseActivity {

    private AllianceRewardsViewModel viewModel;
    private AllianceRewardsAdapter adapter;

    private RecyclerView rvRewards;
    private ProgressBar progressBar;
    private TextView tvNoRewards;
    private TextView tvTotalBadges;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_rewards);

        setupNavbar();

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "Korisnik nije prijavljen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupViewModel();

        // Učitaj nagrade
        viewModel.loadRewards(userId);
    }

    private void initializeViews() {
        rvRewards = findViewById(R.id.rvRewards);
        progressBar = findViewById(R.id.progressBar);
        tvNoRewards = findViewById(R.id.tvNoRewards);
        tvTotalBadges = findViewById(R.id.tvTotalBadges);
    }

    private void setupRecyclerView() {
        adapter = new AllianceRewardsAdapter(new ArrayList<>());
        rvRewards.setLayoutManager(new LinearLayoutManager(this));
        rvRewards.setAdapter(adapter);
    }

    private void setupViewModel() {
        AllianceMissionRepository repository = new AllianceMissionRepositoryImpl(getApplicationContext());
        AllianceRewardsViewModel.Factory factory = new AllianceRewardsViewModel.Factory(repository);
        viewModel = new ViewModelProvider(this, factory).get(AllianceRewardsViewModel.class);

        // Observer za nagrade
        viewModel.getRewards().observe(this, rewards -> {
            if (rewards != null && !rewards.isEmpty()) {
                adapter.updateRewards(rewards);
                rvRewards.setVisibility(View.VISIBLE);
                tvNoRewards.setVisibility(View.GONE);
            } else {
                rvRewards.setVisibility(View.GONE);
                tvNoRewards.setVisibility(View.VISIBLE);
            }
        });

        // Observer za ukupan broj bedževa
        viewModel.getTotalBadges().observe(this, total -> {
            if (total != null) {
                tvTotalBadges.setText("Ukupno bedževa: " + total);
            }
        });

        // Observer za loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observer za greške
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}