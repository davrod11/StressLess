package com.example.stressless.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.stressless.R;
import com.example.stressless.main.home.ExercisesFragment;
import com.example.stressless.main.home.MeasurementFragment;
import com.example.stressless.main.home.RelaxationFragment;
import com.example.stressless.main.home.WeeklyGoalsFragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.layoutMeasurement).setOnClickListener(v -> navigateToFragment(new MeasurementFragment()));

        view.findViewById(R.id.layoutExercises).setOnClickListener(v -> navigateToFragment(new ExercisesFragment()));

        view.findViewById(R.id.layoutWeeklyGoals).setOnClickListener(v -> navigateToFragment(new WeeklyGoalsFragment()));

        view.findViewById(R.id.layoutRelaxation).setOnClickListener(v -> navigateToFragment(new RelaxationFragment()));

        return view;
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
