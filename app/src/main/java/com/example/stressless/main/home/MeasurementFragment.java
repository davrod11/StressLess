package com.example.stressless.main.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import com.example.stressless.DatabaseHelper;
import com.example.stressless.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import Bio.Library.namespace.BioLib;

public class MeasurementFragment extends Fragment {
    private BioLib bioLib;
    private TextView tvStatus;
    private TextView tvHR;
    private final List<Integer> rrIntervalsPerMinute = new ArrayList<>();
    private final Handler handlerHRV = new Handler();
    private static final int HRV_CALCULATION_INTERVAL_MS = 10000; // 60 seconds
    private TextView tvHRV;
    private boolean isConnected = false;
    private final List<Integer> rrIntervals = new ArrayList<>();
    private final List<Double> hrvDataList = new ArrayList<>();
    private LineChart chartECG;
    private LineData ecgData;
    private LineDataSet ecgDataSet;
    private final Queue<Integer> ecgQueue = new LinkedList<>();
    private static final float SCALE = 1.5f / 128f;
    private DatabaseHelper databaseHelper;

    private final Handler handlerUI = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BioLib.MESSAGE_BLUETOOTH_NOT_SUPPORTED:
                    showToast("Bluetooth is not supported.");
                    break;
                case BioLib.MESSAGE_BLUETOOTH_ENABLED:
                    tvStatus.setText("Status: Bluetooth enabled.");
                    break;
                case BioLib.MESSAGE_DEVICE_NAME:
                    String device = (String) msg.obj;
                    tvStatus.setText("Status: Connected to device: " + device);
                    break;
                case BioLib.MESSAGE_ECG_STREAM:
                    handleECGData(msg);
                    break;
                case BioLib.MESSAGE_PEAK_DETECTION:
                    handlePeakDetection(msg);
                    break;
                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    tvStatus.setText("Status: Device disconnected.");
                    isConnected = false;
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    private void saveHRVToFile(double hrvValue) {
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        java.time.LocalTime currentTime = java.time.LocalTime.now();
        String fileName = "hrv_" + currentDate.toString() + ".txt";
        String line = currentTime.toString() + "," + hrvValue + "\n";
        File dir = new File(requireContext().getFilesDir(), "HRVLogs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(line);
            showToast("HRV data saved in: " + file.getAbsolutePath());
        } catch (IOException e) {
            showToast("Error saving HRV data: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_measurement, container, false);

        chartECG = rootView.findViewById(R.id.chart_ecg);
        setupECGChart();

        tvStatus = rootView.findViewById(R.id.tv_status);
        tvHR = rootView.findViewById(R.id.tv_hr);
        tvHRV = rootView.findViewById(R.id.tv_hrv);
        Button btnConnectToggle = rootView.findViewById(R.id.btn_connect_toggle);
        databaseHelper = new DatabaseHelper(requireContext());

        try {
            bioLib = new BioLib(getContext(), handlerUI);
        } catch (Exception e) {
            showToast("Error initializing BioLib: " + e.getMessage());
        }

        btnConnectToggle.setOnClickListener(v -> {
            if (isConnected) {
                disconnectFromDevice();
            } else {
                connectToDevice();
            }
        });
        checkPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }



        return rootView;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isConnected) {
            disconnectFromDevice();
        }
        handlerHRV.removeCallbacks(calculateHRVTask);
    }

    @SuppressLint("SetTextI18n")
    private void connectToDevice() {
        if (bioLib.mBluetoothAdapter == null || !bioLib.mBluetoothAdapter.isEnabled()) {
            showToast("Bluetooth is off.");
            return;
        }
        try {
            tvStatus.setText("Status: Attempting to connect...");
            String deviceAddress = "00:23:FE:00:0B:41";
            boolean result = bioLib.Connect(deviceAddress, 20);
            if (result) {
                isConnected = true;
                tvStatus.setText("Status: Connected.");
                showToast("Successfully connected to the device.");
                handlerHRV.postDelayed(calculateHRVTask, HRV_CALCULATION_INTERVAL_MS);
            } else {
                tvStatus.setText("Status: Connection failed.");
                showToast("Error: Unable to connect to the device.");
            }
        } catch (Exception e) {
            showToast("Error while attempting to connect: " + e.getMessage());
            tvStatus.setText("Status: Connection error.");
        }
    }

    @SuppressLint("SetTextI18n")
    private void disconnectFromDevice() {
        try {
            boolean result = bioLib.Disconnect();
            if (result) {
                isConnected = false;
                tvStatus.setText("Status: Device disconnected.");
                showToast("Disconnected from the device.");
                handlerHRV.removeCallbacks(calculateHRVTask);
            } else {
                showToast("Error while disconnecting.");
            }
        } catch (Exception e) {
            showToast("Error while disconnecting: " + e.getMessage());
        }
    }

    private final Runnable calculateHRVTask = new Runnable() {
        @Override
        public void run() {
            calculateAndUpdateHRV();
            handlerHRV.postDelayed(this, HRV_CALCULATION_INTERVAL_MS);
        }
    };

    private void handleECGData(Message msg) {
        byte[][] ecgBytes = (byte[][]) msg.obj;
        for (byte b : ecgBytes[0]) {
            int value = b & 0xFF;
            ecgQueue.add(value);
            if (ecgQueue.size() > 1000) {
                ecgQueue.poll();
            }
        }
        requireActivity().runOnUiThread(this::updateECGChart);
    }

    @SuppressLint("SetTextI18n")
    private void handlePeakDetection(Message msg) {
        try {
            BioLib.QRS peak = (BioLib.QRS) msg.obj;
            int hr = peak.bpmi;
            int rr = peak.rr;
            rrIntervalsPerMinute.add(rr);
            requireActivity().runOnUiThread(() -> tvHR.setText("HR: " + hr + " bpm"));
        } catch (Exception e) {
            showToast("Error processing peaks: " + e.getMessage());
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void calculateAndUpdateHRV() {
        if (rrIntervalsPerMinute.size() < 2) {
            requireActivity().runOnUiThread(() -> {
                tvHRV.setText("HRV: Insufficient data.");
                updateStressLevel("Undetermined", android.R.color.darker_gray);
            });
            rrIntervalsPerMinute.clear();
            return;
        }
        double rmssd = calculateRMSSD(rrIntervalsPerMinute);
        saveHRVToFile(rmssd);
        String currentDate = java.time.LocalDate.now().toString();
        int userId = getCurrentUserId();
        boolean isSaved = databaseHelper.saveHRVData(
                userId,
                currentDate,
                rmssd,
                new ArrayList<>(rrIntervalsPerMinute),
                new ArrayList<>(ecgQueue)
        );
        if (isSaved) {
            showToast("HRV data successfully saved.");
        } else {
            showToast("Error saving HRV data.");
        }
        String stressLevel;
        int color;
        if (rmssd > 40) {
            stressLevel = "Low";
            color = android.R.color.holo_green_light;
        } else if (rmssd > 20) {
            stressLevel = "Medium";
            color = android.R.color.holo_orange_light;
            sendStressNotification(stressLevel);
        } else {
            stressLevel = "High";
            color = android.R.color.holo_red_light;
            sendStressNotification(stressLevel);
        }

        hrvDataList.add(rmssd);
        requireActivity().runOnUiThread(() -> {
            tvHRV.setText("HRV: " + String.format("%.2f", rmssd) + " ms");
            updateStressLevel(stressLevel, color);
        });

        rrIntervalsPerMinute.clear();
    }

    private double calculateRMSSD(List<Integer> rrIntervals) {
        if (rrIntervals.size() < 2) {
            return 0;
        }
        double sumSquaredDifferences = 0.0;
        for (int i = 1; i < rrIntervals.size(); i++) {
            int diff = rrIntervals.get(i) - rrIntervals.get(i - 1);
            sumSquaredDifferences += Math.pow(diff, 2);
        }
        return Math.sqrt(sumSquaredDifferences / (rrIntervals.size() - 1));
    }

    @SuppressLint("SetTextI18n")
    private void updateStressLevel(String level, int colorResource) {
        TextView tvStressLevel = requireView().findViewById(R.id.tv_stress_level);
        View circleIndicator = requireView().findViewById(R.id.circle_stress_indicator);
        tvStressLevel.setText("Stress Level: " + level);
        circleIndicator.setBackgroundResource(colorResource);
    }

    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        if (username == null) {
            Toast.makeText(requireContext(), "No user is logged in.", Toast.LENGTH_SHORT).show();
            return -1;
        }
        return databaseHelper.getUserId(username);
    }

    private void setupECGChart() {
        ecgDataSet = new LineDataSet(new ArrayList<>(), "ECG");
        ecgDataSet.setDrawCircles(false);
        ecgDataSet.setLineWidth(1f);

        ecgData = new LineData(ecgDataSet);
        chartECG.setData(ecgData);
        chartECG.getDescription().setEnabled(false);
        chartECG.setTouchEnabled(true);
        chartECG.setDragEnabled(true);
        chartECG.setScaleEnabled(true);
        chartECG.setPinchZoom(true);
        chartECG.getXAxis().setDrawGridLines(false);
        chartECG.getAxisLeft().setDrawGridLines(false);
        chartECG.invalidate();
    }

    private void updateECGChart() {
        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (Integer value : ecgQueue) {
            float mV = (value - 128) * SCALE;
            entries.add(new Entry(i++, mV));
        }
        ecgDataSet.setValues(entries);
        ecgData.notifyDataChanged();
        chartECG.notifyDataSetChanged();
        chartECG.invalidate();
    }

    private void createNotificationChannel() {
        String channelId = "stress_notification_channel";
        CharSequence name = "Stress Notifications";
        String description = "Notifications for stress levels";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendStressNotification(String stressLevel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        String channelId = "stress_notification_channel";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.noti)
                .setContentTitle("Stress Level Alert")
                .setContentText("Your stress level is " + stressLevel)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(1, builder.build());
    }



    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
        } else {
            permissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT};
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, 1);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
