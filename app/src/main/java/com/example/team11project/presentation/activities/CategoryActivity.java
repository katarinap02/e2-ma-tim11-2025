package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.presentation.adapters.CategoryAdapter;
import com.example.team11project.presentation.viewmodel.CategoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class CategoryActivity extends AppCompatActivity {

    private CategoryViewModel viewModel;
    private CategoryAdapter categoryAdapter;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button buttonAdd, buttonEdit, buttonDelete;

    private String currentUserId = "12345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdapter);

        setupObservers();


        buttonAdd.setOnClickListener(view -> {
            showAddCategoryDialog(); // Pozivamo novu metodu
        });

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

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_new_category_title);

        View dialogView = getLayoutInflater().inflate(R.layout.add_category_dialog, null);
        builder.setView(dialogView);

        final EditText categoryNameInput = dialogView.findViewById(R.id.edit_text_category_name);

        builder.setPositiveButton(R.string.button_add, (dialog, which) -> {
            String categoryName = categoryNameInput.getText().toString().trim();

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Naziv ne može biti prazan.", Toast.LENGTH_SHORT).show();
                return;
            }

            String randomColor = String.format("#%06X", (int) (Math.random() * 0xFFFFFF));

            viewModel.createNewCategory(categoryName, randomColor, currentUserId);
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel());

        builder.show();

    }


}