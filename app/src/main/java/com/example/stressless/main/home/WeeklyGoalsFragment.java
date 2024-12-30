package com.example.stressless.main.home;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stressless.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WeeklyGoalsFragment extends Fragment {

    private static final String SHARED_PREFS = "WeeklyGoalsPrefs";
    private static final String PENDING_GOALS_KEY = "PendingGoals";
    private static final String COMPLETED_GOALS_KEY = "CompletedGoals";

    private LinearLayout pendingGoalsContainer;
    private LinearLayout completedGoalsContainer;

    private List<String> pendingGoals;
    private List<String> completedGoals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly_goals, container, false);

        pendingGoals = new ArrayList<>();
        completedGoals = new ArrayList<>();
        loadGoalsOrSetDefaults();

        pendingGoalsContainer = view.findViewById(R.id.pendingGoalsContainer);
        completedGoalsContainer = view.findViewById(R.id.completedGoalsContainer);

        displayGoals();

        return view;
    }

    private void displayGoals() {
        pendingGoalsContainer.removeAllViews();
        completedGoalsContainer.removeAllViews();
        for (String goal : pendingGoals) {
            addGoalToContainer(goal, pendingGoalsContainer, false);
        }
        for (String goal : completedGoals) {
            addGoalToContainer(goal, completedGoalsContainer, true);
        }
    }

    private void addEmptyMessage(LinearLayout container, String message) {
        TextView emptyMessage = new TextView(getContext());
        emptyMessage.setText(message);
        emptyMessage.setTextSize(16);
        emptyMessage.setTextColor(getResources().getColor(R.color.gray));
        emptyMessage.setPadding(16, 16, 16, 16);
        container.addView(emptyMessage);
    }




    private void moveGoalWithAnimation(View goalView, LinearLayout fromContainer, LinearLayout toContainer, Runnable onComplete) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(goalView, "alpha", 1f, 0f);
        animator.setDuration(300);
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (fromContainer.indexOfChild(goalView) >= 0) {
                    fromContainer.removeView(goalView);
                }
                goalView.setAlpha(1f);
                toContainer.addView(goalView);
                onComplete.run();
            }
        });
        animator.start();
    }

    private void saveGoals() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PENDING_GOALS_KEY, new JSONArray(pendingGoals).toString());
        editor.putString(COMPLETED_GOALS_KEY, new JSONArray(completedGoals).toString());
        editor.apply();
    }

    private void loadGoalsOrSetDefaults() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        try {
            String pendingGoalsJson = sharedPreferences.getString(PENDING_GOALS_KEY, null);
            String completedGoalsJson = sharedPreferences.getString(COMPLETED_GOALS_KEY, null);

            if (pendingGoalsJson == null && completedGoalsJson == null) {
                pendingGoals.add("Go for a walk every morning");
                pendingGoals.add("Meditate for 10 minutes daily");
                pendingGoals.add("Drink 2 liters of water");
                pendingGoals.add("Read a book for 30 minutes");
                pendingGoals.add("Practice deep breathing exercises");
                pendingGoals.add("Write in a journal every evening");
                pendingGoals.add("Do a short workout session daily");
                pendingGoals.add("Spend time in nature");
                pendingGoals.add("Limit screen time to 2 hours per day");
                pendingGoals.add("Try a new healthy recipe");
                pendingGoals = new ArrayList<>(new HashSet<>(pendingGoals));
                saveGoals();
            } else {
                JSONArray pendingArray = new JSONArray(pendingGoalsJson);
                for (int i = 0; i < pendingArray.length(); i++) {
                    pendingGoals.add(pendingArray.getString(i));
                }
                pendingGoals = new ArrayList<>(new HashSet<>(pendingGoals));

                JSONArray completedArray = new JSONArray(completedGoalsJson);
                for (int i = 0; i < completedArray.length(); i++) {
                    completedGoals.add(completedArray.getString(i));
                }
                completedGoals = new ArrayList<>(new HashSet<>(completedGoals));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addGoalToContainer(String goal, LinearLayout container, boolean isCompleted) {
        if (pendingGoals.contains(goal) && completedGoals.contains(goal)) {
            pendingGoals.remove(goal);
            completedGoals.remove(goal);
            saveGoals();
        }
        View goalView = LayoutInflater.from(getContext()).inflate(R.layout.item_goal, container, false);
        TextView goalText = goalView.findViewById(R.id.goalText);
        CheckBox goalCheckBox = goalView.findViewById(R.id.goalCheckBox);
        goalText.setText(goal);
        goalCheckBox.setChecked(isCompleted);
        goalCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (pendingGoals.contains(goal)) {
                    pendingGoals.remove(goal);
                    completedGoals.add(goal);
                }
            } else {
                if (completedGoals.contains(goal)) {
                    completedGoals.remove(goal);
                    pendingGoals.add(goal);
                }
            }
            pendingGoals = new ArrayList<>(new HashSet<>(pendingGoals));
            completedGoals = new ArrayList<>(new HashSet<>(completedGoals));
            displayGoals();
            saveGoals();
        });
        container.addView(goalView);
    }
}
