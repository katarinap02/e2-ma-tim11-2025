package com.example.team11project.presentation.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.R;
import com.example.team11project.data.repository.StatisticRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.StatisticRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.viewmodel.StatisticViewModel;
import com.example.team11project.presentation.viewmodel.UserViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private UserViewModel viewModel;
    private StatisticViewModel statisticViewModel;
    private TextView textUsername, textTitle, textLevel, textPp, textXp, textCoins;
    private TextView textBadgesCount, txtActiveDays;
    private PieChart pieChartTasks;


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
        txtActiveDays = findViewById(R.id.txtActiveDays);

        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        UserViewModel.Factory factory = new UserViewModel.Factory(userRepository);
        viewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        StatisticRepository statisticRepository = new StatisticRepositoryImpl(getApplicationContext());
        StatisticViewModel.Factory statisticFactory = new StatisticViewModel.Factory(statisticRepository);
        statisticViewModel = new ViewModelProvider(this, statisticFactory).get(StatisticViewModel.class);

        pieChartTasks = findViewById(R.id.pieChartTasks);

        String userId = getIntent().getStringExtra("userId");
        String currentUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", null);
        boolean isOwner = userId != null && userId.equals(currentUserId);

        viewModel.getUser().observe(this, user -> {
            if (user == null) return;
            layoutEquipment.removeAllViews();
            layoutBadges.removeAllViews();


            int resId = avatarMap.getOrDefault(user.getAvatar(), R.drawable.avatar1);
            imgAvatar.setImageResource(resId);

            textUsername.setText(user.getUsername());
            textTitle.setText("Titula: " + user.getLevelInfo().getTitle().name());
            textLevel.setText("Level: " + user.getLevelInfo().getLevel());
            textPp.setText("Snaga (PP): " + user.getLevelInfo().getPp());
            textXp.setText("XP: " + user.getLevelInfo().getXp());
            textCoins.setText("Novčići: " + user.getCoins());
            txtActiveDays.setText("Broj uzastopnih aktivnih dana: " + user.getActiveDays());

            // QR code
            String qrText = user.getId();
            QRCodeWriter writer = new QRCodeWriter();
            try {
                int size = 150;
                BitMatrix bitMatrix = writer.encode(qrText, BarcodeFormat.QR_CODE, size, size);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(bitMatrix);
                imgQr.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            // Oprema
            if (user.getClothing() != null && !user.getClothing().isEmpty()) {
                TextView clothingTitle = new TextView(this);
                clothingTitle.setText("Odeca:");
                clothingTitle.setTextSize(16f);
                clothingTitle.setTypeface(null, Typeface.BOLD);
                layoutEquipment.addView(clothingTitle);

                for (Clothing c : user.getClothing()) {
                    TextView tv = new TextView(this);
                    tv.setText("Naziv: " + c.getName() + " \nKolicina: " + c.getQuantity() + "\n");
                    layoutEquipment.addView(tv);
                }
            }

            if (user.getPotions() != null && !user.getPotions().isEmpty()) {
                TextView potionTitle = new TextView(this);
                potionTitle.setText("Napitci:");
                potionTitle.setTextSize(16f);
                potionTitle.setTypeface(null, Typeface.BOLD);
                layoutEquipment.addView(potionTitle);

                for (Potion p : user.getPotions()) {
                    TextView tv = new TextView(this);
                    tv.setText("Naziv: " + p.getName() + "\nKolicina: " + p.getQuantity() + "\n");
                    layoutEquipment.addView(tv);
                }
            }

            if (user.getWeapons() != null && !user.getWeapons().isEmpty()) {
                TextView weaponTitle = new TextView(this);
                weaponTitle.setText("Oruzje:");
                weaponTitle.setTextSize(16f);
                weaponTitle.setTypeface(null, Typeface.BOLD);
                layoutEquipment.addView(weaponTitle);

                for (Weapon w : user.getWeapons()) {
                    TextView tv = new TextView(this);
                    tv.setText("Naziv: " + w.getName() + "\nKolicina: " + w.getQuantity() + "\n");
                    layoutEquipment.addView(tv);
                }
            }


            if ((user.getClothing() == null || user.getClothing().isEmpty()) &&
                    (user.getPotions() == null || user.getPotions().isEmpty())) {
                TextView tv = new TextView(this);
                tv.setText("Trenutno nema opreme");
                layoutEquipment.addView(tv);
            }

            // Bedzevi
            textBadgesCount.setText("Broj bedzeva: " + 12);

            if (!isOwner) {
                textCoins.setVisibility(View.GONE);
                btnChangePassword.setVisibility(View.GONE);
                textPp.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(this, message ->
                Toast.makeText(this, "Greska: " + message, Toast.LENGTH_SHORT).show());

        viewModel.loadUser(userId);

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        statisticViewModel.loadTasksSummary(userId);

        statisticViewModel.getTasksSummary().observe(this, summary -> {
            if (summary == null || summary.isEmpty()) {
                Log.d("BAR_CHART", "Summary je null ili prazna!");
                return;
            }

            for (Map.Entry<String, Integer> entry : summary.entrySet()) {
                Log.d("BAR_CHART", "Kategorija: " + entry.getKey() + " -> " + entry.getValue());
            }
            if (summary == null || summary.isEmpty()) return;

            List<PieEntry> entries = new ArrayList<>();
            if (summary.containsKey("CREATED"))
                entries.add(new PieEntry(summary.get("CREATED"), "Kreirani"));
            if (summary.containsKey("COMPLETED"))
                entries.add(new PieEntry(summary.get("COMPLETED"), "Urađeni"));
            if (summary.containsKey("UNCOMPLETED"))
                entries.add(new PieEntry(summary.get("UNCOMPLETED"), "Neurađeni"));
            if (summary.containsKey("CANCELED"))
                entries.add(new PieEntry(summary.get("CANCELED"), "Otkazani"));

            PieDataSet dataSet = new PieDataSet(entries, "Zadaci");
            dataSet.setColors(new int[]{Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED});
            dataSet.setValueTextSize(14f);

            PieData pieData = new PieData(dataSet);
            pieChartTasks.setData(pieData);
            pieChartTasks.invalidate();
        });

        BarChart barChart = findViewById(R.id.barChartTasksByCategory);

        statisticViewModel.loadCompletedTasksPerCategory(userId);
        statisticViewModel.getCompletedTasksPerCategory().observe(this, summary -> {
            if (summary == null || summary.isEmpty()) {
                Log.d("BAR_CHART", "Summary je null ili prazna!");
                return;
            }

            for (Map.Entry<String, Integer> entry : summary.entrySet()) {
                Log.d("BAR_CHART", "Kategorija: " + entry.getKey() + " -> " + entry.getValue());
            }

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            int index = 0;

            for (Map.Entry<String, Integer> entry : summary.entrySet()) {
                entries.add(new BarEntry(index, entry.getValue()));
                labels.add(entry.getKey());
                index++;
            }

            BarDataSet dataSet = new BarDataSet(entries, "Završeni zadaci po kategoriji");
            dataSet.setColors(Color.BLUE);
            dataSet.setValueTextSize(14f);

            BarData barData = new BarData(dataSet);
            barChart.setData(barData);

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barChart.getAxisLeft().setAxisMinimum(0f);
            barChart.getAxisRight().setEnabled(false);

            barChart.invalidate();
        });

    }
}
