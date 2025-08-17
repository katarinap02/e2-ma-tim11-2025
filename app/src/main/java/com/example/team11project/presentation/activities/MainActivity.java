package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team11project.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String sessionToken = prefs.getString("sessionToken", null);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (sessionToken != null) {
                intent = new Intent(MainActivity.this, AddAndEditActivity.class);
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 4000);
        
    }
}