package com.example.team11project.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team11project.R;
import com.example.team11project.presentation.adapters.ViewPagerAdapter;
import com.example.team11project.presentation.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TaskActivity extends BaseActivity {
    private TabLayout tabLayout;

    private TaskViewModel viewModel;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAddTask;
    private ViewPagerAdapter viewPagerAdapter;

    private boolean isInitialLoad = true;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //deo za usera
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            // Ako ID ne postoji, to znači da korisnik nije ulogovan.
            Toast.makeText(this, "Greška: Korisnik nije pronađen. Molimo ulogujte se.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setupUI();
        setSupportActionBar(findViewById(R.id.toolbar_tasks));

        TaskViewModel.Factory factory = new TaskViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        //pravimo tabove, jedan prikazuje kalendar drugi listu
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Lista");
                tab.setIcon(R.drawable.ic_list);
            } else {
                tab.setText("Kalendar");
                tab.setIcon(R.drawable.ic_calendar);
            }
        }).attach();

        //dugme za dodavnaje zadatka
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, AddAndEditActivity.class);
            startActivity(intent);
        });
        if (savedInstanceState == null) {
            viewModel.loadInitialData(currentUserId);
        }

    }

    private void setupUI() {
        tabLayout = findViewById(R.id.tab_layout_tasks);
        viewPager = findViewById(R.id.view_pager_tasks);
        fabAddTask = findViewById(R.id.fab_add_task);
        setSupportActionBar(findViewById(R.id.toolbar_tasks));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Osveži podatke svaki put kad se vratiš na ovaj ekran,
        // OSIM pri prvom kreiranju (jer se tada podaci već učitavaju u onCreate).
        if (!isInitialLoad) {
            viewModel.loadInitialData(currentUserId);
        }
        isInitialLoad = false;
    }
}