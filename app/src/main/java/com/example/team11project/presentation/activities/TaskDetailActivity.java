package com.example.team11project.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.team11project.domain.model.TaskStatus;
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
        setupUI();

        TaskViewModel.Factory factory = new TaskViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);


        setupNavbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Detalji zadatka");
        }

        setupObservers();
        setupListeners();
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
        btnComplete.setOnClickListener(v -> {
            if (currentTask != null) {
                viewModel.completeTask(currentTask, currentUserId);
            }
        });
        btnPause.setOnClickListener(v -> {
            if (currentTask != null) {
                // Ako je zadatak pauziran, aktiviraj ga, i obrnuto
                TaskStatus newStatus = currentTask.getStatus() == TaskStatus.PAUSED ? TaskStatus.ACTIVE : TaskStatus.PAUSED;
                viewModel.changeTaskStatus(currentTask, newStatus, currentUserId);

            }
        });

        btnCancel.setOnClickListener(v -> {
            if (currentTask != null) {
                viewModel.changeTaskStatus(currentTask, TaskStatus.CANCELED, currentUserId);
            }
        });
        btnEdit.setOnClickListener(v-> Toast.makeText(this, "Akcija: Izmeni", Toast.LENGTH_SHORT).show());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void setupObservers() {
        viewModel.selectedTask.observe(this, task -> {
            if (task != null) {
                this.currentTask = task;
                populateTaskData(task);
            } else {
                Toast.makeText(this, "Zadatak nije pronađen.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.selectedTaskCategory.observe(this, category -> {
            populateCategoryData(category);
        });

        // DEO GDE PISE KOLIKO JE XP NEKO OSVOJIO
        viewModel.taskCompletedXp.observe(this, earnedXp -> {
            if (earnedXp > 0) {
                Toast.makeText(this, "Zadatak završen! Osvojeno: " + earnedXp + " XP", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Zadatak završen! Dostignut je limit za XP poene za ovu vrstu zadatka.", Toast.LENGTH_LONG).show();
            }
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

        // DEO KOJI RADI SA DUGMICIMA
        // Prvo, resetujmo sve na podrazumevano stanje
        btnPause.setText("Pauziraj");

        switch (task.getStatus()) {
            case ACTIVE:
                // Svi dugmići su aktivni (osim pauziranja za jednokratne)
                btnComplete.setEnabled(true);
                btnCancel.setEnabled(true);
                btnEdit.setEnabled(true);
                btnDelete.setEnabled(true);

                if (task.isRecurring()) {
                    btnPause.setVisibility(View.VISIBLE);
                    btnPause.setEnabled(true);
                } else {
                    btnPause.setVisibility(View.GONE);
                }
                break;

            case PAUSED:
                // Samo "Aktiviraj", "Izmeni" i "Obriši" su aktivni
                btnComplete.setEnabled(false);
                btnCancel.setEnabled(false);
                btnEdit.setEnabled(true);
                btnDelete.setEnabled(true);

                // Dugme "Pauziraj" menja svoju funkciju i tekst
                btnPause.setVisibility(View.VISIBLE);
                btnPause.setEnabled(true);
                btnPause.setText("Aktiviraj"); // Promena teksta
                break;

            case COMPLETED:
            case UNCOMPLETED:
            case CANCELED:
                // Za sve završne statuse, svi dugmići su onemogućeni
                btnComplete.setEnabled(false);
                btnPause.setEnabled(false);
                btnCancel.setEnabled(false);
                btnEdit.setEnabled(false);
                btnDelete.setEnabled(false);

                // Prikazujemo pauziraj/aktiviraj dugme samo ako je zadatak ponavljajući
                btnPause.setVisibility(task.isRecurring() ? View.VISIBLE : View.GONE);

                // Postavi finalni status na glavno dugme radi jasnoće
                btnComplete.setText("Zadatak je " + task.getStatus().name().toLowerCase());
                break;
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
