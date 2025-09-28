package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.presentation.adapters.AddFriendAdapter;
import com.example.team11project.presentation.viewmodel.FriendsViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class AddFriendsActivity extends BaseActivity {

    private FriendsViewModel viewModel;
    private AddFriendAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        setupNavbar();

        RecyclerView recyclerView = findViewById(R.id.rvUsers);

        currentUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        adapter = new AddFriendAdapter(new ArrayList<>(), user -> {
            viewModel.addFriend(currentUserId, user.getId());

            Toast.makeText(this, user.getUsername() + " dodat u prijatelje", Toast.LENGTH_SHORT).show();
            viewModel.loadNonFriends(currentUserId);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FriendsViewModel.Factory factory = new FriendsViewModel.Factory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(FriendsViewModel.class);

        viewModel.getNonFriendsLiveData().observe(this, nonFriends -> {
            adapter.setUsers(nonFriends);
        });

        Button btnScanQr = findViewById(R.id.btnScanQr);
        btnScanQr.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setOrientationLocked(false);
            options.setBeepEnabled(true);
            barcodeLauncher.launch(options);
        });


        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Greška: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadNonFriends(currentUserId);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    String scannedUserId = result.getContents();
                    viewModel.addFriend(currentUserId, scannedUserId);
                    Toast.makeText(this, "Korisnik dodat preko QR koda!", Toast.LENGTH_SHORT).show();
                    viewModel.loadNonFriends(currentUserId);
                }
            }
    );

}
