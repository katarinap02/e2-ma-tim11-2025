package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.data.repository.AllianceMessageRepositoryImpl;
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.TaskInstanceRepositoryImpl;
import com.example.team11project.data.repository.TaskRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.AllianceMessage;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceMessageRepository;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.AllianceMissionUseCase;
import com.example.team11project.presentation.adapters.AllianceChatAdapter;
import com.example.team11project.presentation.viewmodel.AllianceChatViewModel;
import com.example.team11project.presentation.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AllianceChatActivity extends BaseActivity {

    private AllianceChatViewModel viewModel;
    private UserViewModel userViewModel;
    private AllianceChatAdapter adapter;
    private String allianceId;
    private String allianceLeaderId;
    private String currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_chat);

        setupNavbar();

        RecyclerView rvMessages = findViewById(R.id.rvMessages);
        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        userViewModel = new ViewModelProvider(this,
                new UserViewModel.Factory(userRepository))
                .get(UserViewModel.class);

        allianceId = getIntent().getStringExtra("allianceId");
        allianceLeaderId = getIntent().getStringExtra("allianceLeaderId");
        currentId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        adapter = new AllianceChatAdapter(new ArrayList<>(), currentId);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        Executor executor = Executors.newSingleThreadExecutor();
        AllianceMessageRepository allianceMessageRepository = new AllianceMessageRepositoryImpl(getApplicationContext(), executor);
        AllianceMissionRepository allianceMissionRepository = new AllianceMissionRepositoryImpl(getApplicationContext());
        AllianceRepository allianceRepository = new AllianceRepositoryImpl(getApplicationContext());
        UserRepository userRepository1 = new UserRepositoryImpl(getApplicationContext());
        TaskRepository taskRepository = new TaskRepositoryImpl(getApplicationContext());
        TaskInstanceRepository taskInstanceRepository = new TaskInstanceRepositoryImpl(getApplicationContext());
        AllianceMissionUseCase allianceMissionUseCase = new AllianceMissionUseCase(allianceMissionRepository, allianceRepository, userRepository1, taskRepository, taskInstanceRepository);

        viewModel = new ViewModelProvider(this,
                new AllianceChatViewModel.Factory(
                        allianceMessageRepository,
                        executor,
                        allianceMissionUseCase
                )).get(AllianceChatViewModel.class);

        // load messages
        viewModel.loadMessages(allianceLeaderId, allianceId);
        viewModel.getMessages().observe(this, messages -> {
            adapter.setMessages(messages);
            rvMessages.scrollToPosition(messages.size() - 1);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
        });

        Log.d("Chat", "currentId = " + currentId);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;

            // prvo učitaj username korisnika
            userViewModel.getUserById(currentId, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    // sada imamo username, možemo da pošaljemo poruku
                    AllianceMessage msg = new AllianceMessage(
                            UUID.randomUUID().toString(), // id
                            currentId,                    // senderId
                            user.getUsername(),           // senderUsername
                            allianceId,                   // allianceId
                            text,                         // message
                            System.currentTimeMillis()    // timestamp
                    );

                    viewModel.sendMessage(allianceLeaderId, msg);
                    etMessage.setText("");
                    Log.d("Chat", "sendMessage leaderId=" + allianceLeaderId + ", senderId=" + currentId);

                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AllianceChatActivity.this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


    }
}
