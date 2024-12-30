package com.example.stressless.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.stressless.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private LineChart hrvChart, hrChart;
    private TextView selectedDateText, averageHrvText, averageHrText, stressTimeText;

    private final Map<String, List<Float>> hrvDataByDay = new HashMap<>();
    private final Map<String, List<Float>> hrDataByDay = new HashMap<>();
    private final List<EventDay> events = new ArrayList<>();

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        hrvChart = view.findViewById(R.id.hrv_chart);
        hrChart = view.findViewById(R.id.hr_chart);
        selectedDateText = view.findViewById(R.id.selected_date_text);
        averageHrvText = view.findViewById(R.id.average_hrv_text);
        averageHrText = view.findViewById(R.id.average_hr_text);
        stressTimeText = view.findViewById(R.id.stress_time_text);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        int userThreshold = sharedPreferences.getInt("stress_threshold", 50);

        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPrefs, key) -> {
            if ("stress_threshold".equals(key)) {
                int updatedThreshold = sharedPrefs.getInt("stress_threshold", 50);
                refreshCalendarEvents(updatedThreshold);
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        loadDataFromAssets();
        calendarView.setEvents(events);

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar selectedDate = eventDay.getCalendar();
            @SuppressLint("DefaultLocale") String dateKey = String.format("%04d_%02d_%02d", selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH));
            selectedDateText.setText("Selected Date: " + dateKey);

            List<Float> hrvData = hrvDataByDay.get(dateKey);
            List<Float> hrData = hrDataByDay.get(dateKey);

            if (hrvData == null || hrData == null) {
                Toast.makeText(requireContext(), "No data for this day", Toast.LENGTH_SHORT).show();
                hrvChart.clear();
                hrChart.clear();
                averageHrvText.setText("");
                averageHrText.setText("");
                stressTimeText.setText("Time in High Stress: 0 minutes");
            } else {
                showData(hrvData, hrvChart, "HRV Throughout the Day", averageHrvText, "HRV", userThreshold);
                showData(hrData, hrChart, "HR Throughout the Day", averageHrText, "HR", userThreshold);

                int highStressTime = calculateHighStressTime(hrvData, userThreshold - 10);
                stressTimeText.setText(String.format("Time in High Stress: %d minutes", highStressTime));
            }
        });

        return view;
    }


    private void loadDataFromAssets() {
        AssetManager assetManager = requireContext().getAssets();

        try {
            String[] files = assetManager.list("");
            if (files == null) return;

            for (String fileName : files) {
                if (fileName.startsWith("hrv_") && fileName.endsWith(".txt")) {
                    if (fileName.length() >= 14) {
                        String dateKey = fileName.substring(4, 14);
                        List<Float> hrvData = readDataFromAssets(assetManager, fileName);
                        if (!hrvData.isEmpty()) {
                            hrvDataByDay.put(dateKey, hrvData);

                            float averageHrv = calculateAverage(hrvData);
                            int drawableRes = getDrawableForHrv(averageHrv);

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, Integer.parseInt(dateKey.substring(0, 4)));
                            calendar.set(Calendar.MONTH, Integer.parseInt(dateKey.substring(5, 7)) - 1);
                            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateKey.substring(8, 10)));

                            EventDay eventDay = new EventDay(calendar, drawableRes);
                            events.add(eventDay);
                        }
                    }
                } else if (fileName.startsWith("hr_") && fileName.endsWith(".txt")) {
                    if (fileName.length() >= 13) {
                        String dateKey = fileName.substring(3, 13);
                        List<Float> hrData = readDataFromAssets(assetManager, fileName);
                        if (!hrData.isEmpty()) {
                            hrDataByDay.put(dateKey, hrData);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e("CalendarFragment", "Error loading assets", e);
        }
    }

    private List<Float> readDataFromAssets(AssetManager assetManager, String fileName) {
        List<Float> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(fileName)))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] values = line.split("\t");
                if (values.length > 1) {
                    try {
                        data.add(Float.parseFloat(values[1]));
                    } catch (NumberFormatException e) {
                        Log.e("CalendarFragment", "Error parsing value: " + values[1], e);
                    }
                }
            }
        } catch (IOException e) {
            Log.e("CalendarFragment", "Error reading file: " + fileName, e);
        }
        return data;
    }


    private float calculateAverage(List<Float> data) {
        float total = 0;
        for (float value : data) {
            total += value;
        }
        return data.isEmpty() ? 0 : total / data.size();
    }

    private int getDrawableForHrv(float averageHrv) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        int userThreshold = sharedPreferences.getInt("stress_threshold", 55);


        if (averageHrv > userThreshold) {
            return R.drawable.dot_green;
        } else if (averageHrv > userThreshold - 10) {
            return R.drawable.dot_yellow;
        } else {
            return R.drawable.dot_red;
        }
    }

    private int calculateHighStressTime(List<Float> hrvData, float mediumToHighThreshold) {

        int highStressPoints = 0;
        for (float hrv : hrvData) {
            if (hrv <= mediumToHighThreshold) {
                highStressPoints++;
            }
        }
        int timePerPoint = 20;
        return highStressPoints * timePerPoint;

    }

    @SuppressLint("DefaultLocale")
    private void showData(List<Float> data, LineChart chart, String descriptionText, TextView averageTextView, String label, int userThreshold) {
        ArrayList<Entry> entries = new ArrayList<>();
        float total = 0;

        for (int i = 0; i < data.size(); i++) {
            float value = data.get(i);
            entries.add(new Entry(i, value));
            total += value;
        }

        float average = total / data.size();
        averageTextView.setText(String.format("Average %s: %.2f", label, average));
        LineDataSet lineDataSet = new LineDataSet(entries, label + " Data");

        if ("HRV".equals(label)) {
            lineDataSet.setColor(getResources().getColor(R.color.teal_700));
            lineDataSet.setCircleColor(getResources().getColor(R.color.teal_700));
            lineDataSet.setFillColor(getResources().getColor(R.color.teal_200));
            lineDataSet.setDrawFilled(true);
            chart.getAxisLeft().removeAllLimitLines();
            LimitLine limitLine = new LimitLine(userThreshold, "Threshold");
            limitLine.setLineColor(getResources().getColor(android.R.color.holo_blue_dark));
            limitLine.setLineWidth(2f);
            limitLine.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            limitLine.setTextSize(12f);
            chart.getAxisLeft().addLimitLine(limitLine);
        } else if ("HR".equals(label)) {
            lineDataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
            lineDataSet.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));
            lineDataSet.setFillColor(getResources().getColor(android.R.color.holo_orange_light));
            lineDataSet.setDrawFilled(true);
        }



        chart.setData(new LineData(lineDataSet));
        Description description = new Description();
        description.setText(descriptionText);
        chart.setDescription(description);
        chart.animateX(1500);
        chart.invalidate();
    }

    private void refreshCalendarEvents(int updatedThreshold) {
        events.clear();
        for (Map.Entry<String, List<Float>> entry : hrvDataByDay.entrySet()) {
            String dateKey = entry.getKey();
            List<Float> hrvData = entry.getValue();
            float averageHrv = calculateAverage(hrvData);
            int drawableRes = getDrawableForHrv(averageHrv);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(dateKey.substring(0, 4)));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateKey.substring(5, 7)) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateKey.substring(8, 10)));

            events.add(new EventDay(calendar, drawableRes));
        }
        calendarView.setEvents(events);
    }

}
