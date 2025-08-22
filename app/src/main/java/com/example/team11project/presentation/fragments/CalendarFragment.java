package com.example.team11project.presentation.fragments;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import com.example.team11project.R;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.presentation.activities.TaskDetailActivity;
import com.example.team11project.presentation.adapters.TaskAdapter;
import com.example.team11project.presentation.view.decorators.EventDecorator;
import com.example.team11project.presentation.viewmodel.TaskViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.DateTimeUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private TaskViewModel viewModel;
    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;

    private List<Task> allTasks = new ArrayList<>();
    private Map<String, Category> categoryMap = new HashMap<>();
    private String currentUserId = "12345678";

    public CalendarFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendar_view);
        recyclerView = view.findViewById(R.id.recycler_view_calendar_tasks);

        // Koristimo deljeni ViewModel iz roditeljskog Activity-ja
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        setupRecyclerView();
        setupCalendarListener();
        setupObservers();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter();
        taskAdapter.setTaskClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);
    }

    private void setupObservers() {
        // Za boje gledamo kategorije
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Map<String, Category> map = new HashMap<>();
                for (Category c : categories) map.put(c.getId(), c);
                this.categoryMap = map;
                taskAdapter.setCategoryMap(map); // Prosledi mapu i adapteru
                updateCalendarDecorators(); // Osveži dekoratore kada stignu kategorije
            }
        });

        // Posmatramo zadatke
        viewModel.tasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                this.allTasks = tasks;
                updateCalendarDecorators(); // Osveži dekoratore kada stignu zadaci
                // Prikazi zadatke za danas po defaultu
                filterTasksForDate(CalendarDay.today());
            }
        });
    }

    private void setupCalendarListener() {
        // Listener koji se aktivira kada korisnik klikne na datum
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selected) {
                filterTasksForDate(date);
            } else {
                taskAdapter.setTasks(new ArrayList<>()); // Očisti listu ako se datum deselektuje
            }
        });
    }

    private void updateCalendarDecorators() {
        if (allTasks.isEmpty() || categoryMap.isEmpty()) {
            return;
        }
        // Ukloni sve stare dekoratore
        calendarView.removeDecorators();

        // Grupiši datume po bojama kategorija
        Map<Integer, HashSet<CalendarDay>> eventsByColor = new HashMap<>();

        for (Task task : allTasks) {
            if (task.getExecutionTime() == null) continue;

            Category category = categoryMap.get(task.getCategoryId());
            if (category == null) continue;

            int color = Color.parseColor(category.getColor());
            LocalDate localDate = Instant.ofEpochMilli(task.getExecutionTime().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            CalendarDay day = CalendarDay.from(localDate);

            if (!eventsByColor.containsKey(color)) {
                eventsByColor.put(color, new HashSet<>());
            }
            eventsByColor.get(color).add(day);
        }

        // Kreiraj i dodaj dekorator za svaku boju
        for (Map.Entry<Integer, HashSet<CalendarDay>> entry : eventsByColor.entrySet()) {
            calendarView.addDecorator(new EventDecorator(entry.getKey(), entry.getValue()));
        }
    }

    // Metoda koja filtrira i prikazuje zadatke za selektovani dan
    private void filterTasksForDate(CalendarDay date) {
        List<Task> tasksForDay = new ArrayList<>();
        Instant instant = date.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date selectedDate = DateTimeUtils.toDate(instant);

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);

        Calendar taskCal = Calendar.getInstance();

        for (Task task : allTasks) {
            if (task.getExecutionTime() == null) continue;

            taskCal.setTime(task.getExecutionTime());
            if (selectedCal.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.DAY_OF_YEAR) == taskCal.get(Calendar.DAY_OF_YEAR)) {
                tasksForDay.add(task);
            }
        }

        // Sortiraj po vremenu
        tasksForDay.sort((t1, t2) -> t1.getExecutionTime().compareTo(t2.getExecutionTime()));

        taskAdapter.setTasks(tasksForDay);
    }


    @Override
    public void onTaskClick(Task task) {
            Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
            intent.putExtra("TASK_ID", task.getId());
            startActivity(intent);
    }

    @Override
    public void onCompleteClick(Task task) {
        Toast.makeText(getContext(), "Završi zadatak (iz kalendara): " + task.getTitle(), Toast.LENGTH_SHORT).show();
        // viewModel.completeTask(task, currentUserId);
    }
}