package com.example.team11project.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.RecurrenceUnit;
import com.example.team11project.domain.model.Task;
import com.example.team11project.presentation.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskDetailActivity extends BaseActivity {

    private TaskViewModel viewModel;
    private String taskId;

    private Task currentTask;
    private TextView tvTitle, tvStatus, tvCategory, tvDescription, tvTime, tvDifficulty, tvImportance, tvXp;
    private View categoryColorView;
    private LinearLayout recurringLayout;

    private Toolbar toolbar;
    private TextView tvRecurrenceRule, tvRecurrencePeriod;
    private Button btnComplete, btnPause, btnCancel, btnEdit, btnDelete;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

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

        //Uzmemo id prosledjen iz zadatka o kome saznajemo vise
        taskId = getIntent().getStringExtra("TASK_ID");
        if (taskId == null) {
            Toast.makeText(this, "Greška: ID zadatka nije prosleđen.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TaskViewModel.Factory factory = new TaskViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);

        setupUI();
        setupNavbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Detalji zadatka");
        }

        setupListeners();
        setupObservers();
        viewModel.loadTaskDetails(taskId, currentUserId);;
    }

    private void setupUI() {
        tvTitle = findViewById(R.id.text_view_detail_title);
        tvStatus = findViewById(R.id.text_view_detail_status);
        tvCategory = findViewById(R.id.text_view_detail_category);
        tvDescription = findViewById(R.id.text_view_detail_description);
        tvTime = findViewById(R.id.text_view_detail_time);
        tvDifficulty = findViewById(R.id.text_view_detail_difficulty);
        tvImportance = findViewById(R.id.text_view_detail_importance);
        tvXp = findViewById(R.id.text_view_detail_xp);
        categoryColorView = findViewById(R.id.view_detail_category_color);
        recurringLayout = findViewById(R.id.layout_detail_recurring);
        tvRecurrenceRule = findViewById(R.id.text_view_detail_recurrence_rule);
        tvRecurrencePeriod = findViewById(R.id.text_view_detail_recurrence_period);
        btnComplete = findViewById(R.id.button_detail_complete);
        btnPause = findViewById(R.id.button_detail_pause);
        btnCancel = findViewById(R.id.button_detail_cancel);
        btnEdit = findViewById(R.id.button_detail_edit);
        btnDelete = findViewById(R.id.button_detail_delete);
    }

    private void setupListeners() {
        btnComplete.setOnClickListener(v -> Toast.makeText(this, "Akcija: Urađeno", Toast.LENGTH_SHORT).show());
        btnPause.setOnClickListener(v -> Toast.makeText(this, "Akcija: Pauziraj", Toast.LENGTH_SHORT).show());
        btnCancel.setOnClickListener(v -> Toast.makeText(this, "Akcija: Otkaži", Toast.LENGTH_SHORT).show());
        btnEdit.setOnClickListener(v-> Toast.makeText(this, "Akcija: Izmeni", Toast.LENGTH_SHORT).show());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void setupObservers() {
        viewModel.selectedTask.observe(this, task -> {
            if (task != null) {
                currentTask = task;
                populateTaskData(task);
            } else {
                Toast.makeText(this, "Zadatak nije pronađen.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.selectedTaskCategory.observe(this, category -> {
            populateCategoryData(category);
        });

        //ucitavanje i error
        viewModel.isLoading.observe(this, isLoading -> { /* ... */ });
        viewModel.error.observe(this, error -> { /* ... */ });
    }

    private void populateTaskData(Task task) {
        tvTitle.setText(task.getTitle());
        tvStatus.setText("Status: " + task.getStatus().name());

        // Sakrivam opis ako je prazan
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            tvDescription.setText(task.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yyyy, HH:mm", Locale.getDefault());
        tvTime.setText("Vreme: " + sdf.format(task.getExecutionTime()));

        // Lepši ispis za težinu i bitnost
        tvDifficulty.setText("Težina: " + task.getDifficulty().toString() + " (" + task.getDifficulty().getXpValue() + " XP)");
        tvImportance.setText("Bitnost: " + task.getImportance().toString() + " (" + task.getImportance().getXpValue() + " XP)");

        int totalXp = task.getDifficulty().getXpValue() + task.getImportance().getXpValue();
        tvXp.setText("Ukupna vrednost: " + totalXp + " XP");

        if (task.isRecurring()) {
            recurringLayout.setVisibility(View.VISIBLE);
            String unit = task.getRecurrenceUnit() == RecurrenceUnit.DAY ? " dana" : " nedelje";
            tvRecurrenceRule.setText("Ponavlja se svakog " + task.getRecurrenceInterval() + unit);

            SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String period = "Od: " + dateOnlySdf.format(task.getRecurrenceStartDate()) +
                    " do: " + dateOnlySdf.format(task.getRecurrenceEndDate());
            tvRecurrencePeriod.setText(period);
        } else {
            recurringLayout.setVisibility(View.GONE);
        }
    }

    private void populateCategoryData(Category category) {
        if (category != null) {
            tvCategory.setText("Kategorija: " + category.getName());
            GradientDrawable bg = (GradientDrawable) categoryColorView.getBackground();
            bg.setColor(Color.parseColor(category.getColor()));
        } else {
            tvCategory.setText("Kategorija: Nije dodeljena");
            categoryColorView.setBackgroundColor(Color.GRAY);
        }
    }


        private void showDeleteConfirmationDialog() {
        if (currentTask == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Potvrda brisanja")
                .setMessage("Da li ste sigurni da želite da obrišete zadatak '" + currentTask.getTitle() + "'?")
                .setPositiveButton("Obriši", (dialog, which) -> {
                    // viewModel.deleteTask(taskId, currentTask.getUserId());
                    finish(); // Zatvori ekran nakon što je akcija pokrenuta
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    //strelica nazad
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Proveri da li je kliknuta strelica za nazad
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        // Za sve ostale klikove (home, logout), pozovi metodu iz BaseActivity-ja
        return super.onOptionsItemSelected(item);
    }
}
