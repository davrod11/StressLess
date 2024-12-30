package com.example.stressless.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.stressless.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Random;

public class StatisticsFragment extends Fragment {

    private BarChart stressBarChart;
    private PieChart stressPieChart;
    private LineChart hourlyStressLineChart;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        stressBarChart = view.findViewById(R.id.bar_chart);
        stressPieChart = view.findViewById(R.id.pie_chart);
        hourlyStressLineChart = view.findViewById(R.id.line_chart);
        setupStressBarChart();
        setupStressPieChart();
        setupHourlyStressLineChart();
        return view;
    }

    private void setupStressBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1f, 20));
        entries.add(new BarEntry(2f, 25));
        entries.add(new BarEntry(3f, 30));
        entries.add(new BarEntry(4f, 35));
        entries.add(new BarEntry(5f, 50));
        entries.add(new BarEntry(6f, 15));
        entries.add(new BarEntry(7f, 10));

        BarDataSet dataSet = new BarDataSet(entries, "Stress Levels");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        BarData data = new BarData(dataSet);
        stressBarChart.setData(data);

        Description description = new Description();
        description.setText("Weekly Stress Levels (Mon-Sun)");
        stressBarChart.setDescription(description);

        stressBarChart.animateY(1000);
        stressBarChart.invalidate();
    }

    private void setupStressPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(60f, "Low Stress"));
        entries.add(new PieEntry(30f, "Medium Stress"));
        entries.add(new PieEntry(10f, "High Stress"));
        PieDataSet dataSet = new PieDataSet(entries, "Stress Distribution");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        PieData data = new PieData(dataSet);
        stressPieChart.setData(data);
        Description description = new Description();
        description.setText("Stress Distribution");
        stressPieChart.setDescription(description);
        stressPieChart.animateY(1000);
        stressPieChart.invalidate();
    }

    private void setupHourlyStressLineChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        Random random = new Random();

        for (int hour = 0; hour < 24; hour++) {
            entries.add(new Entry(hour, random.nextInt(50) + 10));
        }



        LineDataSet dataSet = new LineDataSet(entries, "Hourly Stress Levels");

        dataSet.setColor(getResources().getColor(R.color.teal_700));
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.teal_200));

        LineData data = new LineData(dataSet);
        hourlyStressLineChart.setData(data);
        Description description = new Description();
        description.setText("Average Hourly Stress Levels (Mon-Sun)");
        hourlyStressLineChart.setDescription(description);

        hourlyStressLineChart.animateX(1000);
        hourlyStressLineChart.invalidate();
    }
}
