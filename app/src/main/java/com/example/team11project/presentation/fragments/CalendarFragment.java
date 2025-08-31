package com.example.team11project.presentation.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.team11project.domain.model.RecurrenceUnit;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskInstance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.presentation.activities.LoginActivity;
import com.example.team11project.presentation.activities.TaskDetailActivity;
import com.example.team11project.presentation.adapters.TaskAdapter;
import com.example.team11project.presentation.view.decorators.EventDecorator;
import com.example.team11project.presentation.viewmodel.TaskViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.DateTimeUtils;

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
    private String currentUserId;

    private List<Task> allTaskInstances = new ArrayList<>();

    private Map<String, List<TaskInstance>> instancesMap = new HashMap<>();

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

                // Generiši sve instance i osveži i kalendar i listu
                generateAllTaskInstances();
                updateCalendarDecorators();

                // Prikazi zadatke za danas po defaultu
               // filterTasksForDate(CalendarDay.today());
            }
        });

        // instance
        viewModel.instancesMap.observe(getViewLifecycleOwner(), map -> {
            if (map != null) {
                this.instancesMap = map;
                // Kada stignu nove instance, takođe osveži prikaz
                generateAllTaskInstances();
                updateCalendarDecorators();
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

        HashMap<CalendarDay, List<Integer>> eventsByDay = new HashMap<>();

        for (Task taskInstance : allTaskInstances) {
            if (taskInstance.getExecutionTime() == null) continue;
            Category category = categoryMap.get(taskInstance.getCategoryId());
            if (category == null) continue;

            LocalDate localDate = Instant.ofEpochMilli(taskInstance.getExecutionTime().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            CalendarDay day = CalendarDay.from(localDate);

            int color = Color.parseColor(category.getColor());

            // Proveri da li već imamo listu za ovaj dan
            if (!eventsByDay.containsKey(day)) {
                eventsByDay.put(day, new ArrayList<>());
            }

            // Dodaj boju u listu za taj dan (spreči duplikate iste boje)
            if (!eventsByDay.get(day).contains(color)) {
                eventsByDay.get(day).add(color);
            }
        }

        List<DayViewDecorator> decorators = new ArrayList<>();

        // Prolazimo kroz našu mapu
        for (Map.Entry<CalendarDay, List<Integer>> entry : eventsByDay.entrySet()) {
            CalendarDay day = entry.getKey();
            List<Integer> colors = entry.getValue();

            // Za svaki dan koji ima događaje, kreiramo novi, specifičan dekorator
            decorators.add(new EventDecorator(day, colors));
        }

        // Na kraju, dodaj sve kreirane dekoratore u kalendar
        calendarView.addDecorators(decorators);
    }

    // Metoda koja filtrira i prikazuje zadatke za selektovani dan
    private void filterTasksForDate(CalendarDay date) {
        List<Task> tasksForDay = new ArrayList<>();
        Instant instant = date.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date selectedDate = DateTimeUtils.toDate(instant);

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);

        Calendar taskCal = Calendar.getInstance();

        for (Task task : allTaskInstances) {
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
        if (task.getExecutionTime() != null) {
            intent.putExtra("INSTANCE_DATE", task.getExecutionTime().getTime());
        }
        startActivity(intent);
    }

    @Override
    public void onCompleteClick(Task task) {
        Toast.makeText(getContext(), "Završi zadatak (iz kalendara): " + task.getTitle(), Toast.LENGTH_SHORT).show();
        // viewModel.completeTask(task, currentUserId);
    }

    private void generateAllTaskInstances() {
        allTaskInstances.clear();
        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        Calendar threeDaysAgo = Calendar.getInstance();
        threeDaysAgo.add(Calendar.DAY_OF_YEAR, -3);
        threeDaysAgo.set(Calendar.HOUR_OF_DAY, 0);

        //pronalazimo koliko ima izuzetaka sa instancama
        for (Task originalTask : allTasks) {
            if (originalTask.isRecurring()) {
                Map<Date, TaskInstance> exceptionsForThisTask = new HashMap<>();
                List<TaskInstance> instances = instancesMap.get(originalTask.getId());
                if (instances != null) {
                    for (TaskInstance instance : instances) {
                        exceptionsForThisTask.put(getStartOfDay(instance.getOriginalDate()), instance);
                    }
                }

                cal.setTime(originalTask.getRecurrenceStartDate());
                while (!cal.getTime().after(originalTask.getRecurrenceEndDate())) {
                    Date instanceDate = cal.getTime();
                    Task virtualTask = createVirtualTask(originalTask, instanceDate);
                    // --- HIJERARHIJA PRAVILA ZA ODREĐIVANJE STATUSA ---

                    TaskStatus finalStatus;
                    TaskInstance exception = exceptionsForThisTask.get(getStartOfDay(instanceDate));

                    // PRAVILO 1: Da li postoji "izuzetak" (COMPLETED ili CANCELED)?
                    if (exception != null) {
                        finalStatus = exception.getNewStatus();
                    }
                    // PRAVILO 2: Da li je instanca istekla (starija od 3 dana)?
                    else if (instanceDate.before(threeDaysAgo.getTime())) {
                        finalStatus = TaskStatus.UNCOMPLETED;
                    }
                    // PRAVILO 3: Da li je CEO NIZ pauziran?
                    else if (originalTask.getStatus() == TaskStatus.PAUSED) {
                        finalStatus = TaskStatus.PAUSED;
                    }// PRAVILO 4: Da li je CEO NIZ obrisan?
                    else if (originalTask.getStatus() == TaskStatus.DELETED) {
                        finalStatus = TaskStatus.DELETED;
                    }
                    // PRAVILO 5: Ako ništa od gore navedenog nije tačno, instanca je aktivna.
                    else {
                        finalStatus = TaskStatus.ACTIVE;
                    }

                    virtualTask.setStatus(finalStatus);
                    //ne upisuje se u kalendar jedino ako je deleted
                    if(finalStatus != TaskStatus.DELETED)
                    {
                        allTaskInstances.add(virtualTask);
                    }


                    // Pomeri kalendar na sledeću instancu
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
                if(originalTask.getStatus() != TaskStatus.DELETED)
                {
                    allTaskInstances.add(originalTask);
                }

            }
        }
        CalendarDay selectedDay = calendarView.getSelectedDate();
        if (selectedDay == null) {
            selectedDay = CalendarDay.today();
        }
        filterTasksForDate(selectedDay);
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
        virtual.setExecutionTime(executionDate);
        virtual.setRecurring(true);
        virtual.setRecurrenceInterval(original.getRecurrenceInterval());
        virtual.setRecurrenceUnit(original.getRecurrenceUnit());
        virtual.setRecurrenceStartDate(original.getRecurrenceStartDate());
        virtual.setRecurrenceEndDate(original.getRecurrenceEndDate());

        return virtual;
    }

    private Date getStartOfDay(Date date) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }



    }