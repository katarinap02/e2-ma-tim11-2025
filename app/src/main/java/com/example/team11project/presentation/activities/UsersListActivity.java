package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.adapters.UsersAdapter;
import com.example.team11project.presentation.viewmodel.UserViewModel;

public class UsersListActivity extends BaseActivity {

    private RecyclerView rvUsers;
    private UsersAdapter adapter;
    private UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        setupNavbar();

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());

        UserViewModel.Factory factory = new UserViewModel.Factory(userRepository);
        viewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        Log.d("DEBUG", "Activity created");
        viewModel.loadAllUsers();
        viewModel.getAllUsers().observe(this, users -> {
            Log.d("DEBUG", "Users size: " + users.size());
            adapter = new UsersAdapter(users, user -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("userId", user.getId());
                startActivity(intent);
            });
            rvUsers.setAdapter(adapter);
        });
    }
}
