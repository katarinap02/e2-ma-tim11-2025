package com.example.team11project.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Category;
import com.example.team11project.presentation.adapters.CategoryAdapter;
import com.example.team11project.presentation.viewmodel.CategoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryActivity extends BaseActivity implements CategoryAdapter.OnCategoryClickListener{

    private CategoryViewModel viewModel;
    private CategoryAdapter categoryAdapter;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button buttonAdd, buttonEdit, buttonDelete;

    private String currentUserId;

    private String selectedColor = null;

    private Category selectedCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

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

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.categories_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        buttonAdd = findViewById(R.id.button_add);
        buttonEdit = findViewById(R.id.button_edit);
        buttonDelete = findViewById(R.id.button_delete);
        setSupportActionBar(toolbar);

        //moramo da prosledimo fabriku jer konstruktor ima parametre
        CategoryViewModel.Factory factory = new CategoryViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(CategoryViewModel.class);



        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setCategoryClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdapter);

        setupObservers();
        setupClickListeners();



        if (savedInstanceState == null) {
            viewModel.loadCategories(currentUserId);
        }



    }

    private void setupObservers() {
        // Posmatra listu kategorija iz ViewModel-a
        viewModel.categories.observe(this, categoryList -> {
            if (categoryList != null) {
                // Kada stigne nova lista, prosledi je adapteru
                categoryAdapter.setCategories(categoryList);
            }
        });

        // Posmatra stanje učitavanja (za ProgressBar)
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Posmatra greške
        viewModel.error.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Greška: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        buttonAdd.setOnClickListener(view -> {
            selectedCategory = null;
            categoryAdapter.setSelectedPosition(RecyclerView.NO_POSITION);
            updateButtonStates(); // Deselektuj sve pre dodavanja
            showAddCategoryDialog();
        });

        buttonEdit.setOnClickListener(view -> {
            if (selectedCategory != null) {
                showEditCategoryDialog(selectedCategory);
            }
        });

        buttonDelete.setOnClickListener(view -> {
            if (selectedCategory != null) {
                showDeleteConfirmationDialog(selectedCategory);
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_new_category_title);

        selectedColor = null;

        View dialogView = getLayoutInflater().inflate(R.layout.add_category_dialog, null);
        builder.setView(dialogView);

        final EditText categoryNameInput = dialogView.findViewById(R.id.edit_text_category_name);

        // --- POČETAK LOGIKE ZA COLOR PICKER ---

        LinearLayout colorPickerLayout = dialogView.findViewById(R.id.color_picker_layout);

        // Učitaj niz boja iz resursa
        int[] colors = getResources().getIntArray(R.array.category_colors);
        List<String> colorHexes = new ArrayList<>();

        // Kreiraj listu sa View-ovima za boje
        List<View> colorViews = new ArrayList<>();

        for (int color : colors) {
            View colorView = new View(this);

            int sizeInDp = 36;
            float scale = getResources().getDisplayMetrics().density;
            int sizeInPixels = (int) (sizeInDp * scale + 0.5f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeInPixels, sizeInPixels);

            // margina
            int marginInPixels = (int) (8 * scale + 0.5f);
            layoutParams.setMarginEnd(marginInPixels);
            colorView.setLayoutParams(layoutParams);

            // Postavi pozadinu i boju
            colorView.setBackgroundResource(R.drawable.color_picker_normal);
            String colorHex = String.format("#%06X", (0xFFFFFF & color));
            ((GradientDrawable) colorView.getBackground()).setColor(color);


            colorPickerLayout.addView(colorView);
            colorViews.add(colorView);
            colorHexes.add(colorHex);

            // Postavi listener za klik
            colorView.setOnClickListener(v -> {
                selectedColor = colorHex;

                // Ažuriraj izgled svih kružića
                for (int i = 0; i < colorViews.size(); i++) {
                    View view = colorViews.get(i);
                    int currentColor = colors[i];

                    if (colorHexes.get(i).equals(selectedColor)) {
                        view.setBackgroundResource(R.drawable.color_picker_selected);
                        LayerDrawable selectedBg = (LayerDrawable) view.getBackground();
                        GradientDrawable innerCircle = (GradientDrawable) selectedBg.getDrawable(1);
                        innerCircle.setColor(currentColor);
                    } else {
                        view.setBackgroundResource(R.drawable.color_picker_normal);
                        ((GradientDrawable) view.getBackground()).setColor(currentColor);
                    }
                }
            });
        }

        builder.setPositiveButton(R.string.button_add, (dialog, which) -> {
            String categoryName = categoryNameInput.getText().toString().trim();

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Morate popuniti naziv kategorije.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedColor == null) {
                Toast.makeText(this, "Morate izabrati boju.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Provera da li je boja već u upotrebi (UI PROVERA)
            List<Category> currentCategories = viewModel.categories.getValue();
            if (currentCategories != null) {
                for (Category category : currentCategories) {
                    if (category.getColor().equalsIgnoreCase(selectedColor)) {
                        Toast.makeText(this, "Ova boja se već koristi. Izaberite drugu.", Toast.LENGTH_LONG).show();
                        return; // Ne nastavljaj dalje
                    }
                }
            }

            viewModel.createNewCategory(categoryName, selectedColor, currentUserId);
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel());

        builder.show();

    }


    @Override
    public void onCategoryClick(Category category, int position) {
        if (selectedCategory != null && selectedCategory.getId().equals(category.getId())) {
            clearSelection();
        } else {
            // Inače, postavi novu selekciju
            selectedCategory = category;
            categoryAdapter.setSelectedPosition(position);
        }
        // Ažuriraj stanje dugmića
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean isEnabled = selectedCategory != null;
        buttonEdit.setEnabled(isEnabled);
        buttonDelete.setEnabled(isEnabled);
    }

    private void clearSelection() {
        selectedCategory = null;
        categoryAdapter.setSelectedPosition(RecyclerView.NO_POSITION);
        updateButtonStates();
    }


    private void showDeleteConfirmationDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.delete_confirm_dialog, null);
        builder.setView(dialogView);
        TextView deleteMessage = dialogView.findViewById(R.id.text_view_delete_message);

        String message = getString(R.string.delete_confirm_message, "kategoriju " + category.getName());
        deleteMessage.setText(message);

        final AlertDialog dialog = builder
                .setPositiveButton(R.string.button_delete, (d, which) -> {
                    viewModel.deleteCategory(category.getId(), currentUserId);
                    selectedCategory = null;
                    categoryAdapter.setSelectedPosition(RecyclerView.NO_POSITION);
                    updateButtonStates();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    private void showEditCategoryDialog(Category categoryToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_category_title);

        selectedColor = categoryToEdit.getColor();

        View dialogView = getLayoutInflater().inflate(R.layout.edit_category_dialog, null);
        builder.setView(dialogView);

        final EditText categoryNameInput = dialogView.findViewById(R.id.edit_text_category_name_edit);
        categoryNameInput.setText(categoryToEdit.getName());

        LinearLayout colorPickerLayout = dialogView.findViewById(R.id.color_picker_layout_edit);

        int[] colors = getResources().getIntArray(R.array.category_colors);
        List<String> colorHexes = new ArrayList<>();
        List<View> colorViews = new ArrayList<>();

        for (int color : colors) {
            View colorView = new View(this);
            int sizeInDp = 36;
            float scale = getResources().getDisplayMetrics().density;
            int sizeInPixels = (int) (sizeInDp * scale + 0.5f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeInPixels, sizeInPixels);
            int marginInPixels = (int) (8 * scale + 0.5f);
            layoutParams.setMarginEnd(marginInPixels);
            colorView.setLayoutParams(layoutParams);

            String colorHex = String.format("#%06X", (0xFFFFFF & color));

            //prikazujemo trenutno selektovanu boju
            if (colorHex.equalsIgnoreCase(selectedColor)) {
                colorView.setBackgroundResource(R.drawable.color_picker_selected);
                LayerDrawable selectedBg = (LayerDrawable) colorView.getBackground();
                GradientDrawable innerCircle = (GradientDrawable) selectedBg.getDrawable(1);
                innerCircle.setColor(color);
            } else {
                colorView.setBackgroundResource(R.drawable.color_picker_normal);
                ((GradientDrawable) colorView.getBackground()).setColor(color);
            }

            colorPickerLayout.addView(colorView);
            colorViews.add(colorView);
            colorHexes.add(colorHex);

            colorView.setOnClickListener(v -> {
                selectedColor = colorHex;

                for (int i = 0; i < colorViews.size(); i++) {
                    View view = colorViews.get(i);
                    int currentColor = colors[i];
                    if (colorHexes.get(i).equals(selectedColor)) {
                        view.setBackgroundResource(R.drawable.color_picker_selected);
                        LayerDrawable selectedBg = (LayerDrawable) view.getBackground();
                        GradientDrawable innerCircle = (GradientDrawable) selectedBg.getDrawable(1);
                        innerCircle.setColor(currentColor);
                    } else {
                        view.setBackgroundResource(R.drawable.color_picker_normal);
                        ((GradientDrawable) view.getBackground()).setColor(currentColor);
                    }
                }
            });
        }


        builder.setPositiveButton(R.string.save_changes, (dialog, which) -> {
            String newCategoryName = categoryNameInput.getText().toString().trim();

            if (newCategoryName.isEmpty()) {
                Toast.makeText(this, "Naziv ne može biti prazan.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Provera da li je nova boja već u upotrebi od strane neke druge kategorije
            List<Category> currentCategories = viewModel.categories.getValue();
            if (currentCategories != null) {
                for (Category cat : currentCategories) {
                    if (cat.getColor().equalsIgnoreCase(selectedColor) && !cat.getId().equals(categoryToEdit.getId())) {
                        Toast.makeText(this, "Ova boja se već koristi. Izaberite drugu.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            categoryToEdit.setName(newCategoryName);
            categoryToEdit.setColor(selectedColor);

            viewModel.updateCategory(categoryToEdit, currentUserId);

            clearSelection();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.cancel();
            clearSelection();
        });

        builder.show();
    }



}