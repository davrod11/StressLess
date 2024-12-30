package com.example.stressless.main.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stressless.R;

public class ExercisesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        view.findViewById(R.id.highExercise1).setOnClickListener(v ->
                showExerciseDetails("Diaphragmatic Breathing",
                        "A deep breathing technique to calm your mind and body.",
                        "5-10 minutes",
                        R.drawable.lung));

        view.findViewById(R.id.highExercise2).setOnClickListener(v ->
                showExerciseDetails("Muscle Relaxation",
                        "Progressively relax your muscle groups to release tension.",
                        "10-15 minutes",
                        R.drawable.muscle_relax));

        view.findViewById(R.id.highExercise3).setOnClickListener(v ->
                showExerciseDetails("Stretching",
                        "Gentle stretches to improve flexibility and reduce stress.",
                        "5-10 minutes",
                        R.drawable.stretch));

        view.findViewById(R.id.mediumExercise1).setOnClickListener(v ->
                showExerciseDetails("Outdoor Walk",
                        "A light walk outdoors to refresh your mind and reduce stress.",
                        "15-20 minutes",
                        R.drawable.walking));

        view.findViewById(R.id.mediumExercise2).setOnClickListener(v ->
                showExerciseDetails("Mindfulness",
                        "A practice to increase your awareness and presence in the moment.",
                        "10-15 minutes",
                        R.drawable.mind));

        view.findViewById(R.id.mediumExercise3).setOnClickListener(v ->
                showExerciseDetails("Yoga",
                        "A sequence of poses to enhance relaxation and flexibility.",
                        "20-30 minutes",
                        R.drawable.yoga));

        view.findViewById(R.id.lowExercise1).setOnClickListener(v ->
                showExerciseDetails("Jumping Rope",
                        "A fun way to stay active and release some energy.",
                        "10-15 minutes",
                        R.drawable.jumping));

        view.findViewById(R.id.lowExercise2).setOnClickListener(v ->
                showExerciseDetails("Read a Book",
                        "Immerse yourself in a good book to relax and enjoy some quiet time.",
                        "20-30 minutes",
                        R.drawable.read));

        view.findViewById(R.id.lowExercise3).setOnClickListener(v ->
                showExerciseDetails("Guided Meditation",
                        "Follow a guided session to relax your mind and body.",
                        "15-20 minutes",
                        R.drawable.meditation1));

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void showExerciseDetails(String title, String description, String duration, int iconResId) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_exercise_details, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogDescription = dialogView.findViewById(R.id.dialogDescription);
        TextView dialogDuration = dialogView.findViewById(R.id.dialogDuration);
        ImageView dialogIcon = dialogView.findViewById(R.id.dialogIcon);
        Button dialogButton = dialogView.findViewById(R.id.dialogButton);

        dialogTitle.setText(title);
        dialogDescription.setText(description);
        dialogDuration.setText("Recommended Duration: " + duration);
        dialogIcon.setImageResource(iconResId);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialogButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

}
