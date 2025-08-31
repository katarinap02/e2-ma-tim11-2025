package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.team11project.R;
import com.example.team11project.domain.model.UserTitle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        addLevelInfoToExistingUsers();


        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String sessionToken = prefs.getString("sessionToken", null);

        MaterialToolbar toolbar = findViewById(R.id.navBar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                prefs.edit().remove("sessionToken").remove("userId").apply();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                 startActivity(intent);
                 finish();
                return true;
            }
            return false;
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (sessionToken != null) {
                intent = new Intent(MainActivity.this, HomeScreenActivity.class);
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 4000);
        
    }


    private void addLevelInfoToExistingUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> levelInfoMap = new HashMap<>();
                        levelInfoMap.put("level", 0);
                        levelInfoMap.put("xp", 0);
                        levelInfoMap.put("xpForNextLevel", 200);
                        levelInfoMap.put("xpTaskImportance", 0);
                        levelInfoMap.put("xpTaskDifficulty", 0);
                        levelInfoMap.put("pp", 0);
                        levelInfoMap.put("title", UserTitle.POČETNIK.name());

                        db.collection("users")
                                .document(doc.getId())
                                .update("levelInfo", levelInfoMap); // dodaje polje bez brisanja ostalih
                    }
                });

    }
}