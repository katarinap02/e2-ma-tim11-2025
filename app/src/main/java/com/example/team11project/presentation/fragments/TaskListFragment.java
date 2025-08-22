package com.example.team11project.presentation.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.team11project.R;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.RecurrenceUnit;
import com.example.team11project.domain.model.Task;
import com.example.team11project.presentation.activities.LoginActivity;
import com.example.team11project.presentation.activities.TaskDetailActivity;
import com.example.team11project.presentation.adapters.TaskAdapter;
import com.example.team11project.presentation.viewmodel.TaskViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener{

    private TaskViewModel viewModel;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;

    private List<Task> originalTasks = new ArrayList<>();
    private String currentUserId;

    private ChipGroup chipGroupFilter; // Nova promenljiva za filter
    private enum TaskFilter { ALL, SINGLE, RECURRING }
    private TaskFilter currentFilter = TaskFilter.ALL;

    // Obavezni prazan konstruktor
    public TaskListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //deo za usera
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Greška: Korisnik nije pronađen. Molimo ulogujte se.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        recyclerView = view.findViewById(R.id.recycler_view_task_list);
        progressBar = view.findViewById(R.id.progress_bar_list);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupFilterListener();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter();
        taskAdapter.setTaskClickListener(this); // Postavljamo fragment kao slušaoca
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);
    }

    private void setupObservers() {
        // Posmatra kategorije i prosleđuje mapu adapteru
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Map<String, Category> map = new HashMap<>();
                for (Category c : categories) {
                    map.put(c.getId(), c);
                }
                taskAdapter.setCategoryMap(map);
            }
        });

        // Posmatra zadatke i prosleđuje listu adapteru
        viewModel.tasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                this.originalTasks = tasks;
                filterAndDisplayTasks();
            }
        });

        viewModel.isLoadingCategory.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorCategory.observe(getViewLifecycleOwner(), error -> { /* ... */ });
        viewModel.error.observe(getViewLifecycleOwner(), error -> { /* ... */ });
    }

    //
    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivity(intent);
    }

    @Override
    public void onCompleteClick(Task task) {
        // TODO: Pozovi UseCase za završavanje zadatka
        Toast.makeText(getContext(), "Završi zadatak: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void filterAndDisplayTasks() {
        if (originalTasks == null) return;

        List<Task> displayList = new ArrayList<>();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        for (Task originalTask : originalTasks) {
            if (originalTask.isRecurring()) {
                // --- LOGIKA ZA PONAVLJAJUĆE ZADATKE ---
                if (currentFilter == TaskFilter.SINGLE) continue;
                if (originalTask.getRecurrenceEndDate() != null && !originalTask.getRecurrenceEndDate().after(now)) {
                    continue;
                }

                cal.setTime(originalTask.getRecurrenceStartDate());

                while (!cal.getTime().after(originalTask.getRecurrenceEndDate())) {
                    if (!cal.getTime().before(getStartOfDay(now))) {
                        Task virtualTask = createVirtualTask(originalTask, cal.getTime());
                        displayList.add(virtualTask);
                    }

                    // Pomeri kalendar na sledeće ponavljanje
                    int interval = originalTask.getRecurrenceInterval();
                    if (originalTask.getRecurrenceUnit() == RecurrenceUnit.DAY) {
                        cal.add(Calendar.DAY_OF_MONTH, interval);
                    } else if (originalTask.getRecurrenceUnit() == RecurrenceUnit.WEEK) {
                        cal.add(Calendar.WEEK_OF_YEAR, interval);
                    } else {
                        break;
                    }
                }

            } else {
                // --- LOGIKA ZA JEDNOKRATNE ZADATKE ---
                if (currentFilter == TaskFilter.RECURRING) continue;
                if (originalTask.getExecutionTime() != null && !originalTask.getExecutionTime().before(now)) {
                    displayList.add(originalTask);
                }
            }
        }


        displayList.sort((t1, t2) -> t1.getExecutionTime().compareTo(t2.getExecutionTime()));

        taskAdapter.setTasks(displayList);
    }

    private Task createVirtualTask(Task original, Date executionDate) {
        Task virtual = new Task();
        virtual.setId(original.getId());
        virtual.setTitle(original.getTitle());
        virtual.setDescription(original.getDescription());
        virtual.setCategoryId(original.getCategoryId());
        virtual.setDifficulty(original.getDifficulty());
        virtual.setImportance(original.getImportance());
        virtual.setStatus(original.getStatus());
        virtual.setUserId(original.getUserId());

        //Postavlja novo, generisano vreme izvršenja
        virtual.setExecutionTime(executionDate);

        // Ponavljajući podaci se takođe kopiraju, u slučaju da zatrebaju
        virtual.setRecurring(true);
        virtual.setRecurrenceInterval(original.getRecurrenceInterval());
        virtual.setRecurrenceUnit(original.getRecurrenceUnit());
        virtual.setRecurrenceStartDate(original.getRecurrenceStartDate());
        virtual.setRecurrenceEndDate(original.getRecurrenceEndDate());

        return virtual;
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void setupFilterListener() {
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all_tasks) {
                currentFilter = TaskFilter.ALL;
            } else if (checkedId == R.id.chip_single_tasks) {
                currentFilter = TaskFilter.SINGLE;
            } else if (checkedId == R.id.chip_recurring_tasks) {
                currentFilter = TaskFilter.RECURRING;
            }

            filterAndDisplayTasks();
        });
    }

    }