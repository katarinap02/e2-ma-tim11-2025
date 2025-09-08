package com.example.team11project.presentation.activities;

import android.app.Application;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team11project.R;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.adapters.StorePagerAdapter;
import com.example.team11project.presentation.viewmodel.RegisterViewModel;
import com.example.team11project.presentation.viewmodel.StoreViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StoreActivity extends BaseActivity {

    private StoreViewModel storeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        setupNavbar();

        StoreViewModel.Factory factory = new StoreViewModel.Factory(getApplication());
        storeViewModel = new ViewModelProvider(this, factory)
                .get(StoreViewModel.class);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        StorePagerAdapter pagerAdapter = new StorePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("Napitci");
                    else tab.setText("Odeća");
                }).attach();

    }

}