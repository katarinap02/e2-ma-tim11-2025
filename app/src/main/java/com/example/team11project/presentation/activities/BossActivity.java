package com.example.team11project.presentation.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team11project.R;

public class BossActivity extends BaseActivity {

    private ImageView ivBoss;
    private Button btnAttack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_boss);

        // IvBoss i dugme
        ivBoss = findViewById(R.id.ivBoss);
        btnAttack = findViewById(R.id.btnAttack);


        // Klik listener za ATTACK dugme
        btnAttack.setOnClickListener(v -> {
            // Menja sprite na "hit"
            ivBoss.setImageResource(R.drawable.boss_hit);

            // Vraća idle nakon kratkog vremena (npr. 500ms)
            ivBoss.postDelayed(() -> ivBoss.setImageResource(R.drawable.boss_idle), 500);
        });
    }
}
