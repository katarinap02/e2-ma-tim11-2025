package com.example.team11project.presentation.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team11project.R;
import com.example.team11project.domain.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories = new ArrayList<>();

    private int selectedPosition = RecyclerView.NO_POSITION; // Čuva poziciju selektovanog reda
    private OnCategoryClickListener listener;
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }
    public void setCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    //jedan red iz view model
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryNameText;
        private final View colorIndicator;

        private final LinearLayout itemLayout;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Povezujemo elemente iz item_category.xml
            categoryNameText = itemView.findViewById(R.id.category_name_text);
            colorIndicator = itemView.findViewById(R.id.color_indicator);
            itemLayout = (LinearLayout) itemView; //povezujemo sa jednim item_category
        }

        public void bind(Category category, boolean isSelected, OnCategoryClickListener listener, int position) {
            categoryNameText.setText(category.getName());

            // Postavljamo boju na kružić. Drawable zbog kruzica
            GradientDrawable backgroundDrawable = (GradientDrawable) colorIndicator.getBackground();
            // `parseColor` pretvara string kao "#FF0000" u integer boju.
            backgroundDrawable.setColor(Color.parseColor(category.getColor()));

            //prikaz selekcije
            if (isSelected) {
                itemLayout.setBackgroundColor(Color.parseColor("#E0E0E0")); // Neka svetlo siva
            } else {
                itemLayout.setBackgroundColor(Color.TRANSPARENT); // Vrati na providnu
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });

        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Kreiramo View za jedan red
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Uzimamo kategoriju za trenutnu poziciju
        Category currentCategory = categories.get(position);
        // I pozivamo `bind` metodu na ViewHolder-u da popuni taj red podacima
        holder.bind(currentCategory, position == selectedPosition, listener, position);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public void setSelectedPosition(int position) {
        // Obavesti adapter da se stari i novi selektovani redovi moraju ponovo iscrtati
        notifyItemChanged(selectedPosition);
        selectedPosition = position;
        notifyItemChanged(selectedPosition);
    }

    // Metoda koju će Activity pozvati da prosledi novu listu podataka adapteru
    public void setCategories(List<Category> newCategories) {
        this.categories = newCategories;
        // Obaveštavamo RecyclerView da su podaci promenjeni i da treba ponovo da se iscrta
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }




}
