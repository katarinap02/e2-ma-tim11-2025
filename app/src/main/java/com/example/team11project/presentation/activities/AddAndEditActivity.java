package com.example.team11project.presentation.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class AddAndEditActivity extends AppCompatActivity {

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
    private final String currentUserId = "12345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit);

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
            } else {
                Toast.makeText(this, "Nema dostupnih kategorija. Kreirajte ih prvo.", Toast.LENGTH_LONG).show();
            }
        });

        // Posmatra signal za uspešno čuvanje
        viewModel.taskSaveSuccess.observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Zadatak uspešno sačuvan!", Toast.LENGTH_SHORT).show();
                viewModel.onSaveSuccessNavigated(); // Resetuj signal
                finish(); // Zatvori activity i vrati se na prethodni
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

        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Nema dostupnih kategorija.", Toast.LENGTH_LONG).show();
            return;
        }

        if (rgDifficulty.getCheckedRadioButtonId() == -1 || rgImportance.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Morate izabrati težinu i bitnost.", Toast.LENGTH_SHORT).show();
            return;
        }

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
}