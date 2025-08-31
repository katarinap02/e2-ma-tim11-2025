package com.example.team11project.presentation.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.example.team11project.R;
import android.widget.TextView;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private List<Task> tasks = new ArrayList<>();
    private Map<String, Category> categoryMap = new HashMap<>();
    private OnTaskClickListener listener;



    // Interfejs za komunikaciju sa Fragmentom/Activity-jem
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onCompleteClick(Task task);
    }
    public void setTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    // Jedan red
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View categoryColorView;
        private final TextView taskTitleText;
        private final TextView executionTimeText;
        private final TextView xpValueText;
        private final Context context; // Potreban za formatiranje datuma





        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            categoryColorView = itemView.findViewById(R.id.view_category_color);
            taskTitleText = itemView.findViewById(R.id.text_view_task_title);
            executionTimeText = itemView.findViewById(R.id.text_view_execution_time);
            xpValueText = itemView.findViewById(R.id.text_view_xp_value);
        }



    public void bind(Task task, Category category, OnTaskClickListener listener) {
        taskTitleText.setText(task.getTitle());

        // Formatiranje i prikaz vremena izvršenja
        if (task.getExecutionTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yyyy, HH:mm", Locale.getDefault());
            executionTimeText.setText(sdf.format(task.getExecutionTime()));
        } else {
            executionTimeText.setText("");
        }

        // Izračunavanje i prikaz XP vrednosti
        int totalXp = task.getDifficulty().getXpValue() + task.getImportance().getXpValue();
        xpValueText.setText("Vrednost: " + totalXp + " XP");

        // Postavljanje boje kategorije
        if (category != null) {
            categoryColorView.setBackgroundColor(Color.parseColor(category.getColor()));
        } else {
            categoryColorView.setBackgroundColor(Color.GRAY); // Podrazumevana boja
        }

        // Postavljanje listenera
        itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        // Pronalazimo odgovarajuću kategoriju iz mape
        Category category = categoryMap.get(currentTask.getCategoryId());
        holder.bind(currentTask, category, listener);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    // Metoda za postavljanje liste zadataka
    public void setTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    // Metoda za postavljanje mape kategorija
    public void setCategoryMap(Map<String, Category> newMap) {
        this.categoryMap = newMap;
        notifyDataSetChanged();
}

}
