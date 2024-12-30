package com.example.stressless.main.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.stressless.R;

public class SettingsFragment extends Fragment {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchNotifications;
    private SeekBar seekBarStressThreshold;
    private TextView tvThresholdValue;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchNotifications = view.findViewById(R.id.switch_notifications);
        seekBarStressThreshold = view.findViewById(R.id.seekbar_stress_threshold);
        tvThresholdValue = view.findViewById(R.id.tv_threshold_value);
        Button btnSave = view.findViewById(R.id.btn_save_settings);

        seekBarStressThreshold.setMin(18);
        seekBarStressThreshold.setMax(60);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false);
        int stressThreshold = sharedPreferences.getInt("stress_threshold", 50);

        switchNotifications.setChecked(notificationsEnabled);
        seekBarStressThreshold.setProgress(stressThreshold);
        tvThresholdValue.setText("Current Threshold: " + stressThreshold);

        seekBarStressThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvThresholdValue.setText("Current Threshold: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSave.setOnClickListener(v -> saveSettings());

        return view;
    }


    private void saveSettings() {
        boolean notificationsEnabled = switchNotifications.isChecked();
        int stressThreshold = seekBarStressThreshold.getProgress();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications_enabled", notificationsEnabled);
        editor.putInt("stress_threshold", stressThreshold);
        editor.apply();

        Toast.makeText(requireContext(), "Settings saved!", Toast.LENGTH_SHORT).show();
    }

}
