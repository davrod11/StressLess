package com.example.stressless;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.stressless.main.CalendarFragment;
import com.example.stressless.main.StatisticsFragment;
import com.example.stressless.main.HomeFragment;
import com.example.stressless.main.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        Objects.requireNonNull(getSupportActionBar()).setTitle("StressLess");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("firstName", "User");
        String lastName = sharedPreferences.getString("lastName", "User");
        SpannableString greeting = new SpannableString("Hi,  " + firstName + " " + lastName + "!  ");
        greeting.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 2, 0);
        greeting.setSpan(new android.text.style.RelativeSizeSpan(1.2f), 0, 2, 0);
        greeting.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.NORMAL), 3, greeting.length(), 0);

        TextView userGreeting = new TextView(this);
        userGreeting.setText(greeting);
        userGreeting.setTextSize(16f);
        userGreeting.setTextColor(Color.WHITE);
        userGreeting.setPadding(0, 0, 16, 0);

        androidx.appcompat.widget.Toolbar.LayoutParams params = new androidx.appcompat.widget.Toolbar.LayoutParams(
                androidx.appcompat.widget.Toolbar.LayoutParams.WRAP_CONTENT,
                androidx.appcompat.widget.Toolbar.LayoutParams.WRAP_CONTENT,
                Gravity.END
        );
        toolbar.addView(userGreeting, params);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);
        replaceFragment(new HomeFragment());
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.calendar) {
                replaceFragment(new CalendarFragment());
            } else if (itemId == R.id.exercises) {
                replaceFragment(new StatisticsFragment());
            } else if (itemId == R.id.reports) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
        fab.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_select_session);

            Button session5Min = dialog.findViewById(R.id.session5Min);
            Button session10Min = dialog.findViewById(R.id.session10Min);
            Button historyButton = dialog.findViewById(R.id.sessionHistory);

            session5Min.setOnClickListener(view -> {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, BiofeedbackActivity5.class);
                startActivity(intent);
            });

            session10Min.setOnClickListener(view -> {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, BiofeedbackActivity10.class);
                startActivity(intent);
            });

            historyButton.setOnClickListener(view -> {
                dialog.dismiss();
                replaceFragment(new HistoryFragment());
            });

            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, fragment);
        fragmentTransaction.commit();
        Log.d("MainActivity", "Fragment replaced: " + fragment.getClass().getSimpleName());
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout ECGLayout = dialog.findViewById(R.id.layoutECG);
        LinearLayout GoalsLayout = dialog.findViewById(R.id.layoutGoals);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        ECGLayout.setOnClickListener(v -> {

            dialog.dismiss();
            Toast.makeText(MainActivity.this, "Record an ECG is clicked", Toast.LENGTH_SHORT).show();

        });

        GoalsLayout.setOnClickListener(v -> {

            dialog.dismiss();
            Toast.makeText(MainActivity.this, "Week goal is clicked", Toast.LENGTH_SHORT).show();

        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}