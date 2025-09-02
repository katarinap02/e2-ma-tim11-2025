package com.example.team11project.presentation.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.RecurrenceUnit;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskDifficulty;
import com.example.team11project.domain.model.TaskImportance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.presentation.viewmodel.TaskViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddAndEditActivity extends BaseActivity {

    private TaskViewModel viewModel;

    // UI Elementi
    private MaterialToolbar toolbar;
    private EditText etTitle, etDescription;
    private Spinner spinnerCategory;
    private SwitchMaterial switchRecurring;
    private LinearLayout layoutRecurringOptions, layoutExecutionTime;
    private EditText etRecurrenceInterval, etRecurrenceStart, etRecurrenceEnd, etRecurrenceTime;
    private EditText etExecutionTime;
    private RadioGroup rgRecurrenceUnit, rgDifficulty, rgImportance;
    private Button btnSave;
    private ProgressBar progressBar;

    // Podaci
    private List<Category> categoryList = new ArrayList<>();
    private Calendar selectedDateTime = Calendar.getInstance();
    private Calendar recurrenceStartDate = Calendar.getInstance();
    private Calendar recurrenceEndDate = Calendar.getInstance();
    private String currentUserId;

    // Za edit
    private boolean isEditMode = false;
    private String taskIdToEdit = null;
    private Date instanceDateToEdit = null;
    private Task originalTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit);
        Intent intent1 = getIntent();
        String mode = intent1.getStringExtra("MODE");
        if ("EDIT".equals(mode)) {
            isEditMode = true;
            taskIdToEdit = intent1.getStringExtra("TASK_ID");
            long instanceTime = intent1.getLongExtra("INSTANCE_DATE", -1L);
            if (instanceTime != -1L) {
                instanceDateToEdit = new Date(instanceTime);
            }
        }

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

        setupNavbar();

        // Inicijalizacija ViewModel-a preko fabrike
        TaskViewModel.Factory factory = new TaskViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);

        setupUI();
        setSupportActionBar(toolbar);
        setupListeners();
        setupObservers();

        // Učitaj kategorije samo pri prvom kreiranju
        if (savedInstanceState == null) {
            viewModel.loadCategories(currentUserId);
            if (isEditMode && taskIdToEdit != null) {
                viewModel.loadTaskDetails(taskIdToEdit, currentUserId);
            }
        }
    }

    private void setupUI() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.edit_text_task_title);
        etDescription = findViewById(R.id.edit_text_task_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        switchRecurring = findViewById(R.id.switch_recurring);
        layoutRecurringOptions = findViewById(R.id.layout_recurring_options);
        layoutExecutionTime = findViewById(R.id.layout_execution_time);
        etRecurrenceInterval = findViewById(R.id.edit_text_recurrence_interval);
        rgRecurrenceUnit = findViewById(R.id.radio_group_recurrence_unit);
        etRecurrenceStart = findViewById(R.id.edit_text_recurrence_start);
        etRecurrenceEnd = findViewById(R.id.edit_text_recurrence_end);
        etRecurrenceTime = findViewById(R.id.edit_text_recurrence_time);
        etExecutionTime = findViewById(R.id.edit_text_execution_time);
        rgDifficulty = findViewById(R.id.radio_group_difficulty);
        rgImportance = findViewById(R.id.radio_group_importance);
        btnSave = findViewById(R.id.button_save_task);
        progressBar = findViewById(R.id.progress_bar_add_task);
    }

    private void setupListeners() {
        // Prikazuje ili sakriva opcije za ponavljanje u zavisnosti od switch-a
        switchRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutRecurringOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            layoutExecutionTime.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        etExecutionTime.setOnClickListener(v -> showDateTimePicker());
        etRecurrenceStart.setOnClickListener(v -> showDatePicker(recurrenceStartDate, etRecurrenceStart));
        etRecurrenceEnd.setOnClickListener(v -> showDatePicker(recurrenceEndDate, etRecurrenceEnd));
        etRecurrenceTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> collectDataAndSaveTask());
    }

    private void setupObservers() {
        // popunjava Spinner sa kategorijama
        viewModel.categories.observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                this.categoryList = categories;
                List<String> categoryNames = new ArrayList<>();
                for (Category c : categories) {
                    categoryNames.add(c.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
                if (isEditMode && originalTask != null) {
                    populateFieldsForEdit(originalTask);
                }
            } else {
                Toast.makeText(this, "Nema dostupnih kategorija. Kreirajte ih prvo.", Toast.LENGTH_LONG).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(AddAndEditActivity.this, CategoryActivity.class);
                    startActivity(intent);
                    finish();
                }, 2500);

                return;
            }
        });

        viewModel.selectedTask.observe(this, task -> {
            if (isEditMode && task != null) {
                originalTask = task;
                updateUIForEditMode();

                // AKO SU KATEGORIJE VEĆ UČITANE, POPUNI ODMAH
                if (categoryList != null && !categoryList.isEmpty()) {
                    populateFieldsForEdit(task);
                }
            }
        });

        // Posmatra signal za uspešno čuvanje
        viewModel.taskSaveSuccess.observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Zadatak uspešno sačuvan!", Toast.LENGTH_SHORT).show();
                viewModel.onSaveSuccessNavigated(); // Resetuj signal
                if (isEditMode) {
                    Intent intent = new Intent(this, TaskActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Za novi zadatak, samo finish()
                    finish();
                }
            }
        });

        // Posmatra stanja učitavanja
        viewModel.isLoadingCategory.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        viewModel.isLoading.observe(this, isSaving -> {
            progressBar.setVisibility(isSaving ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!isSaving);
        });

        // Posmatra greške
        viewModel.errorCategory.observe(this, error -> {
            if (error != null) Toast.makeText(this, "Greška pri učitavanju: " + error, Toast.LENGTH_LONG).show();
        });
        viewModel.error.observe(this, error -> {
            if (error != null) Toast.makeText(this, "Greška pri čuvanju: " + error, Toast.LENGTH_LONG).show();
        });
    }

    // Prikazuje dijalog samo za izbor datuma
    private void showDatePicker(Calendar calendar, EditText editText) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            editText.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Prikazuje dijalog samo za izbor vremena
    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etRecurrenceTime.setText(sdf.format(selectedDateTime.getTime()));
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true).show();
    }

    // Prikazuje dijalog prvo za datum, pa onda za vreme
    private void showDateTimePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(year, month, dayOfMonth);
            showTimePickerForExecutionTime();
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerForExecutionTime() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            etExecutionTime.setText(sdf.format(selectedDateTime.getTime()));
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true).show();
    }

    private void collectDataAndSaveTask() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Naziv je obavezan");
            return;
        }

        if (rgDifficulty.getCheckedRadioButtonId() == -1 || rgImportance.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Morate izabrati težinu i bitnost.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isEditMode) {
            // EDIT MODE - kreiraj editovani task
            Task editedTask = new Task();
            editedTask.setTitle(title);
            editedTask.setDescription(etDescription.getText().toString().trim());
            editedTask.setDifficulty(getSelectedDifficulty());
            editedTask.setImportance(getSelectedImportance());

            // Vreme izvršenja
            Date finalExecutionDate;
            if (originalTask.isRecurring()) {
                // Za ponavljajuće - uzmi vreme iz recurrenceTime
                if (etRecurrenceTime.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Vreme izvršenja je obavezno.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar executionCal = Calendar.getInstance();
                executionCal.setTime(originalTask.getRecurrenceStartDate());
                executionCal.set(Calendar.HOUR_OF_DAY, selectedDateTime.get(Calendar.HOUR_OF_DAY));
                executionCal.set(Calendar.MINUTE, selectedDateTime.get(Calendar.MINUTE));
                finalExecutionDate = executionCal.getTime();
            } else {
                // Za jednokratne
                if (etExecutionTime.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Vreme izvršenja je obavezno.", Toast.LENGTH_SHORT).show();
                    return;
                }
                finalExecutionDate = selectedDateTime.getTime();

                if (finalExecutionDate.before(new Date())) {
                    Toast.makeText(this, "Vreme izvršenja ne može biti u prošlosti.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            editedTask.setExecutionTime(finalExecutionDate);

            // Pozovi edit funkciju
            viewModel.editTask(originalTask, editedTask, currentUserId, instanceDateToEdit);

        } else {


        Task newTask = new Task();
        newTask.setUserId(currentUserId);
        newTask.setTitle(title);
        newTask.setDescription(etDescription.getText().toString().trim());
        newTask.setCategoryId(categoryList.get(spinnerCategory.getSelectedItemPosition()).getId());
        newTask.setDifficulty(getSelectedDifficulty());
        newTask.setImportance(getSelectedImportance());
        newTask.setStatus(TaskStatus.ACTIVE);

        boolean isRecurring = switchRecurring.isChecked();
        newTask.setRecurring(isRecurring);

        Date finalExecutionDate;

        if (isRecurring) {
            if (etRecurrenceStart.getText().toString().isEmpty() || etRecurrenceEnd.getText().toString().isEmpty() || etRecurrenceTime.getText().toString().isEmpty()) {
                Toast.makeText(this, "Morate popuniti sva polja za ponavljanje.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (recurrenceEndDate.before(recurrenceStartDate)) {
                Toast.makeText(this, "Datum završetka ne može biti pre datuma početka.", Toast.LENGTH_SHORT).show();
                return;
            }

            String intervalStr = etRecurrenceInterval.getText().toString();
            if (intervalStr.isEmpty() || Integer.parseInt(intervalStr) <= 0) {
                etRecurrenceInterval.setError("Interval mora biti veći od 0");
                return;
            }
            newTask.setRecurrenceInterval(Integer.parseInt(intervalStr));
            newTask.setRecurrenceUnit(rgRecurrenceUnit.getCheckedRadioButtonId() == R.id.radio_day ? RecurrenceUnit.DAY : RecurrenceUnit.WEEK);
            newTask.setRecurrenceStartDate(recurrenceStartDate.getTime());
            newTask.setRecurrenceEndDate(recurrenceEndDate.getTime());

            // Kreira finalni datum izvršenja od datuma početka i izabranog vremena
            Calendar executionCal = Calendar.getInstance();
            executionCal.setTime(recurrenceStartDate.getTime());
            executionCal.set(Calendar.HOUR_OF_DAY, selectedDateTime.get(Calendar.HOUR_OF_DAY));
            executionCal.set(Calendar.MINUTE, selectedDateTime.get(Calendar.MINUTE));
            finalExecutionDate = executionCal.getTime();

        } else {
            if (etExecutionTime.getText().toString().isEmpty()) {
                Toast.makeText(this, "Vreme izvršenja je obavezno.", Toast.LENGTH_SHORT).show();
                return;
            }
            finalExecutionDate = selectedDateTime.getTime();
        }

        if (finalExecutionDate.before(new Date())) {
            Toast.makeText(this, "Vreme izvršenja ne može biti u prošlosti.", Toast.LENGTH_SHORT).show();
            return;
        }

        newTask.setExecutionTime(finalExecutionDate);
        viewModel.createNewTask(newTask);
    }
    }

    private TaskDifficulty getSelectedDifficulty() {
        int selectedId = rgDifficulty.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_very_easy)
            return TaskDifficulty.VERY_EASY;
        else if (selectedId == R.id.radio_easy)
            return TaskDifficulty.EASY;
        else if (selectedId == R.id.radio_hard)
            return TaskDifficulty.HARD;
        else if (selectedId == R.id.radio_extreme)
            return TaskDifficulty.EXTREME;
        else return TaskDifficulty.EASY;
    }

    private TaskImportance getSelectedImportance() {
        int selectedId = rgImportance.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_normal) {
            return TaskImportance.NORMAL;
        } else if (selectedId == R.id.radio_important) {
            return TaskImportance.IMPORTANT;
        } else if (selectedId == R.id.radio_very_important) {
            return TaskImportance.VERY_IMPORTANT;
        } else if (selectedId == R.id.radio_special) {
            return TaskImportance.SPECIAL;
        } else {
            return TaskImportance.NORMAL;
        }
    }

    // DEO ZA EDIT

    private void populateFieldsForEdit(Task task) {
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());

        // Popuni kategoriju
        if (task.getCategoryId() != null && categoryList != null) {
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId().equals(task.getCategoryId())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
        spinnerCategory.setEnabled(false);

        // Popuni težinu
        switch (task.getDifficulty()) {
            case VERY_EASY: rgDifficulty.check(R.id.radio_very_easy); break;
            case EASY: rgDifficulty.check(R.id.radio_easy); break;
            case HARD: rgDifficulty.check(R.id.radio_hard); break;
            case EXTREME: rgDifficulty.check(R.id.radio_extreme); break;
        }

        // Popuni bitnost
        switch (task.getImportance()) {
            case NORMAL: rgImportance.check(R.id.radio_normal); break;
            case IMPORTANT: rgImportance.check(R.id.radio_important); break;
            case VERY_IMPORTANT: rgImportance.check(R.id.radio_very_important); break;
            case SPECIAL: rgImportance.check(R.id.radio_special); break;
        }

        // Popuni vreme izvršenja
        if (task.getExecutionTime() != null) {
            selectedDateTime.setTime(task.getExecutionTime());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            etExecutionTime.setText(sdf.format(task.getExecutionTime()));
        }

        // Za ponavljajuće zadatke - ograniči opcije
        if (task.isRecurring()) {
            switchRecurring.setChecked(true);
            switchRecurring.setEnabled(false); // Ne dozvoli menjanje tipa


            etRecurrenceInterval.setText(String.valueOf(task.getRecurrenceInterval()));
            etRecurrenceInterval.setEnabled(false);
            etRecurrenceInterval.setFocusable(false);

            if (task.getRecurrenceUnit() == RecurrenceUnit.DAY) {
                rgRecurrenceUnit.check(R.id.radio_day);
            } else {
                rgRecurrenceUnit.check(R.id.radio_week);
            }
            for (int i = 0; i < rgRecurrenceUnit.getChildCount(); i++) {
                rgRecurrenceUnit.getChildAt(i).setEnabled(false);
            }

            // Datumi se ne menjaju
            if (task.getRecurrenceStartDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                etRecurrenceStart.setText(sdf.format(task.getRecurrenceStartDate()));
            }
            etRecurrenceStart.setEnabled(false);
            etRecurrenceStart.setFocusable(false);
            etRecurrenceStart.setClickable(false);

            if (task.getRecurrenceEndDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                etRecurrenceEnd.setText(sdf.format(task.getRecurrenceEndDate()));
            }
            etRecurrenceEnd.setEnabled(false);
            etRecurrenceEnd.setFocusable(false);
            etRecurrenceEnd.setClickable(false);

            // Vreme se može menjati - OSTAVI ENABLED
            if (task.getExecutionTime() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                etRecurrenceTime.setText(timeFormat.format(task.getExecutionTime()));
            }
        } else {
            // Za jednokratne zadatke - DISABLE switch
            switchRecurring.setEnabled(false);
        }
    }

    private void updateUIForEditMode() {
        toolbar.setTitle("Izmeni zadatak");
        btnSave.setText("Sačuvaj izmene");

        // Sakrij opcije za ponavljanje ako je recurring task
        if (originalTask != null && originalTask.isRecurring()) {
            // Prikaži ograničene opcije
            layoutRecurringOptions.setVisibility(View.VISIBLE);
            layoutExecutionTime.setVisibility(View.GONE);
        }
    }
}