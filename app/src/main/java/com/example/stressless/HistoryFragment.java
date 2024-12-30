package com.example.stressless;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment {

    private ListView sessionListView;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        sessionListView = view.findViewById(R.id.session_list);
        setupSessionListView();
        return view;
    }

    private void setupSessionListView() {
        List<String> sessionTitles = new ArrayList<>();
        sessionTitles.add("Session 1: 5 min");
        sessionTitles.add("Session 2: 10 min");
        sessionTitles.add("Session 3: 5 min");
        sessionTitles.add("Session 4: 10 min");
        sessionTitles.add("Session 5: 5 min");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sessionTitles);
        sessionListView.setAdapter(adapter);
        sessionListView.setOnItemClickListener((parent, view, position, id) -> {
            String sessionTitle = sessionTitles.get(position);
            Toast.makeText(requireContext(), "Showing details for " + sessionTitle, Toast.LENGTH_SHORT).show();
            displaySessionChart(position);
        });
    }

    @SuppressLint("SetTextI18n")
    private void displaySessionChart(int sessionIndex) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_session_details);

        TextView sessionTitle = dialog.findViewById(R.id.session_title);
        TextView sessionDuration = dialog.findViewById(R.id.session_duration);
        TextView hrvScore = dialog.findViewById(R.id.hrv_score);
        LineChart sessionChart = dialog.findViewById(R.id.session_chart);
        Button closeButton = dialog.findViewById(R.id.close_button);

        sessionTitle.setText("Session " + (sessionIndex + 1));
        sessionDuration.setText(sessionIndex % 2 == 0 ? "Duration: 5 minutes" : "Duration: 10 minutes");

        List<Entry> heartRateEntries = readHeartRateDataFromAssets("session_" + (sessionIndex + 1) + ".csv");

        if (heartRateEntries.isEmpty()) {
            Toast.makeText(requireContext(), "No data found for this session", Toast.LENGTH_SHORT).show();
            return;
        }

        hrvScore.setText("HRV Score: +15.5%");

        LineDataSet dataSet = new LineDataSet(heartRateEntries, "Heart Rate Over Time");
        dataSet.setColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        sessionChart.setData(lineData);
        Description description = new Description();
        description.setText("Session Details");
        description.setTextSize(12f);
        sessionChart.setDescription(description);
        sessionChart.invalidate();
        closeButton.setOnClickListener(v -> dialog.dismiss());

        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private List<Entry> readHeartRateDataFromAssets(String fileName) {
        List<Entry> entries = new ArrayList<>();
        try {
            InputStream is = requireContext().getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 2) {
                    float time = Float.parseFloat(values[0]);
                    float heartRate = Float.parseFloat(values[1]);
                    entries.add(new Entry(time, heartRate));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error reading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return entries;
    }
}
