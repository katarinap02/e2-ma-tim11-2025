package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.viewmodel.UserViewModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private UserViewModel viewModel;
    private TextView textUsername, textTitle, textLevel, textPp, textXp, textCoins;
    private TextView textBadgesCount;
    private LinearLayout layoutBadges, layoutEquipment;
    private ImageView imgAvatar, imgQr;
    private Button btnChangePassword;

    private static final Map<String, Integer> avatarMap = new HashMap<String, Integer>() {{
        put("avatar1", R.drawable.avatar1);
        put("avatar2", R.drawable.avatar2);
        put("avatar3", R.drawable.avatar3);
        put("avatar4", R.drawable.avatar4);
        put("avatar5", R.drawable.avatar5);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupNavbar();

        textUsername = findViewById(R.id.txtUsername);
        textTitle = findViewById(R.id.txtTitle);
        textLevel = findViewById(R.id.txtLevel);
        textPp = findViewById(R.id.txtPP);
        textXp = findViewById(R.id.txtXP);
        textCoins = findViewById(R.id.txtCoins);
        textBadgesCount = findViewById(R.id.txtBadgesCount);
        layoutBadges = findViewById(R.id.layoutBadges);
        layoutEquipment = findViewById(R.id.layoutEquipment);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgQr = findViewById(R.id.imgQrCode);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        UserViewModel.Factory factory = new UserViewModel.Factory(userRepository);
        viewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        String userId = getIntent().getStringExtra("userId");
        String currentUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);
        boolean isOwner = userId != null && userId.equals(currentUserId);

        viewModel.getUser().observe(this, user -> {
            if (user == null) return;

            int resId = avatarMap.getOrDefault(user.getAvatar(), R.drawable.avatar1);
            imgAvatar.setImageResource(resId);

            textUsername.setText(user.getUsername());
            textTitle.setText("Titula: " + user.getLevelInfo().getTitle().name());
            textLevel.setText("Level: " + user.getLevelInfo().getLevel());
            textPp.setText("Snaga (PP): " + user.getLevelInfo().getPp());
            textXp.setText("XP: " + user.getLevelInfo().getXp());

            // QR code
            String qrText = user.getId();
            QRCodeWriter writer = new QRCodeWriter();
            try {
                int size = 150;
                com.google.zxing.common.BitMatrix bitMatrix = writer.encode(qrText, BarcodeFormat.QR_CODE, size, size);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(bitMatrix);
                imgQr.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            // Populate badges and equipment
            textBadgesCount.setText("Broj bedževa: " + 12);
            layoutBadges.removeAllViews();
            layoutEquipment.removeAllViews();

            // Sakrij privatne podatke ako nije vlasnik
            if (!isOwner) {
                textCoins.setVisibility(View.GONE);
                btnChangePassword.setVisibility(View.GONE);
                textPp.setVisibility(View.GONE);
                // Ako postoje dodatna privatna polja, postavi i njih GONE
                // npr. email, phone, stats koje ne smeju da vide drugi
            }
        });

        viewModel.getError().observe(this, message ->
                Toast.makeText(this, "Greška: " + message, Toast.LENGTH_SHORT).show());

        viewModel.loadUser(userId);

        // Dugme za promenu lozinke
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
}
