package com.example.team11project.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.team11project.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_home) {
            // Ako je kliknuta "Home" ikonica
            Intent intent = new Intent(this, HomeScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    protected void setupNavbar() {
        MaterialToolbar toolbar = findViewById(R.id.navbar);
        setSupportActionBar(toolbar);
    }

    public void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .remove("sessionToken")
                .remove("userId")
                .apply();
    }
}
