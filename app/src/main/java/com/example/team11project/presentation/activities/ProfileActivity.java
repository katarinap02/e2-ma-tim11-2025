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
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.StatisticRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.StatisticRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.presentation.viewmodel.StatisticViewModel;
import com.example.team11project.presentation.viewmodel.UserViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private UserViewModel viewModel;
    private StatisticViewModel statisticViewModel;
    private TextView textUsername, textTitle, textLevel, textPp, textXp, textCoins;
    private TextView textBadgesCount, txtActiveDays, txtStartedSpecialMissions, txtFinishedSpecialMissions;
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
        txtStartedSpecialMissions = findViewById(R.id.txtStartedSpecialMissions);
        txtFinishedSpecialMissions = findViewById(R.id.txtFinishedSpecialMissions);


        UserRepository userRepository = new UserRepositoryImpl(getApplicationContext());
        AllianceMissionRepository allianceMissionRepository = new AllianceMissionRepositoryImpl(getApplicationContext());
        UserViewModel.Factory factory = new UserViewModel.Factory(userRepository, allianceMissionRepository);
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
            viewModel.getBadgeCount().observe(this, count -> {
                if (count != null) {
                    textBadgesCount.setText("Broj bedževa: " + count);
                }
            });

            if (!isOwner) {
                textCoins.setVisibility(View.GONE);
                btnChangePassword.setVisibility(View.GONE);
                textPp.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(this, message ->
                Toast.makeText(this, "Greska: " + message, Toast.LENGTH_SHORT).show());

        viewModel.loadUser(userId);
        viewModel.getBadgeCount(userId);

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


        LineChart lineChart = findViewById(R.id.lineChartTaskDifficulty);

        statisticViewModel.loadAverageTaskDifficulty(userId);
        statisticViewModel.getAverageTaskDifficulty().observe(this, avgDifficulty -> {
            if (avgDifficulty == null) return;

            List<Entry> entries = new ArrayList<>();
            entries.add(new Entry(0f, avgDifficulty));

            LineDataSet dataSet = new LineDataSet(entries, "Prosečna težina zadataka");
            dataSet.setColor(Color.MAGENTA);
            dataSet.setCircleColor(Color.MAGENTA);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(5f);
            dataSet.setValueTextSize(14f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            lineChart.getXAxis().setDrawLabels(true);
            lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    return "XP";
                }
            });
            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            lineChart.getAxisLeft().setAxisMinimum(0f);
            lineChart.getAxisRight().setEnabled(false);

            lineChart.getDescription().setEnabled(false);
            lineChart.invalidate();
        });


        TextView txtLongestStreak = findViewById(R.id.txtLongestStreak);

        statisticViewModel.loadLongestStreak(userId);

        statisticViewModel.getLongestStreak().observe(this, streak -> {
            if (streak != null) {
                txtLongestStreak.setText("Najduži niz uspešno urađenih zadataka: " + streak + " dana");
            }
        });

        statisticViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Greška: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        statisticViewModel.loadAllianceMissionsSummary(userId);

        statisticViewModel.getAllianceMissionsSummary().observe(this, summary -> {
            if (summary != null) {
                int started = summary.getOrDefault("STARTED", 0);
                int finished = summary.getOrDefault("FINISHED", 0);

                txtStartedSpecialMissions.setText("Broj započetih specijalnih misija: " + started);
                txtFinishedSpecialMissions.setText("Broj završenih specijalnih misija: " + finished);
            }
        });



        LineChart xpLineChart = findViewById(R.id.xpLineChart);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 120));
        entries.add(new Entry(1, 40));
        entries.add(new Entry(2, 40));
        entries.add(new Entry(3, 30));
        entries.add(new Entry(4, 0));
        entries.add(new Entry(5, 0));
        entries.add(new Entry(6, 80));

        LineDataSet dataSet = new LineDataSet(entries, "XP u poslednjih 7 dana");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#388E3C"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#A5D6A7"));

        LineData lineData = new LineData(dataSet);
        xpLineChart.setData(lineData);

        String[] days = new String[]{"03-10", "04-10", "05-10", "06-10", "07-10", "08-10", "09-10"};
        XAxis xAxis = xpLineChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < days.length) {
                    return days[index];
                } else {
                    return "";
                }
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.DKGRAY);

        YAxis leftAxis = xpLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.DKGRAY);
        xpLineChart.getAxisRight().setEnabled(false);

        xpLineChart.getDescription().setEnabled(false);
        xpLineChart.animateY(1200);
        xpLineChart.invalidate();

    }
}
