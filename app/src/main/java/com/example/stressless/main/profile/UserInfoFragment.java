package com.example.stressless.main.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stressless.DatabaseHelper;
import com.example.stressless.R;

public class UserInfoFragment extends Fragment {

    private ImageView profilePicture;
    private NumberPicker npAge, npWeight, npHeight;
    private Spinner spinnerSportPractice;
    private EditText etNotes;
    private Uri selectedImageUri;

    private DatabaseHelper databaseHelper;

    public UserInfoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);

        databaseHelper = new DatabaseHelper(requireContext());

        profilePicture = view.findViewById(R.id.profile_picture);
        npAge = view.findViewById(R.id.np_age);
        npWeight = view.findViewById(R.id.np_weight);
        npHeight = view.findViewById(R.id.np_height);
        spinnerSportPractice = view.findViewById(R.id.spinner_sport_practice);
        Button btnSave = view.findViewById(R.id.btn_save_user_info);
        etNotes = view.findViewById(R.id.et_notes);

        setupNumberPickers();
        setupSpinner();
        loadUserInfo();

        profilePicture.setOnClickListener(v -> selectProfilePicture());
        btnSave.setOnClickListener(v -> saveUserInfo());

        return view;
    }

    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return -1;
        }

        return databaseHelper.getUserId(username);
    }

    private void setupNumberPickers() {
        npAge.setMinValue(10);
        npAge.setMaxValue(100);
        npAge.setValue(25);

        npWeight.setMinValue(30);
        npWeight.setMaxValue(150);
        npWeight.setValue(70);

        npHeight.setMinValue(100);
        npHeight.setMaxValue(250);
        npHeight.setValue(170);
    }
    private String calculateRMSSDThreshold(int age) {
            if (age >= 10 && age <= 19) {
                return "53 ms";
            } else if (age >= 20 && age <= 24) {
                return "43 ms";
            } else if (age >= 25 && age <= 34) {
                return "39.7 ms";
            } else if (age >= 35 && age <= 44) {
                return "32.0 ms";
            } else if (age >= 45 && age <= 54) {
                return "23.0 ms";
            } else if (age >= 55 && age <= 64) {
                return "19.9 ms";
            } else if (age >= 65) {
                return "19.1 ms";
            }
        return "No data available";
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sport_practice_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSportPractice.setAdapter(adapter);
    }

    private void loadUserInfo() {
        int userId = getCurrentUserId();
        if (userId == -1) return;

        Cursor cursor = databaseHelper.getLastUserInfo(userId);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                String photoUri = cursor.getString(cursor.getColumnIndexOrThrow("photo"));
                String age = cursor.getString(cursor.getColumnIndexOrThrow("age"));
                String weight = cursor.getString(cursor.getColumnIndexOrThrow("weight"));
                String height = cursor.getString(cursor.getColumnIndexOrThrow("height"));
                int sportPracticeIndex = cursor.getInt(cursor.getColumnIndexOrThrow("sport_practice"));
                if (sportPracticeIndex >= 0 && sportPracticeIndex < spinnerSportPractice.getCount()) {
                    spinnerSportPractice.setSelection(sportPracticeIndex);
                } else {
                    spinnerSportPractice.setSelection(0);
                }

                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));

                if (photoUri != null && !photoUri.equals("default_image_uri")) {
                    selectedImageUri = Uri.parse(photoUri);
                    profilePicture.setImageURI(selectedImageUri);
                }
                npAge.setValue(Integer.parseInt(age));
                npWeight.setValue(Integer.parseInt(weight));
                npHeight.setValue(Integer.parseInt(height));
                etNotes.setText(notes);

                int ageValue = Integer.parseInt(age);
                String recommended = calculateRMSSDThreshold(ageValue);
                Toast.makeText(requireContext(), "Recommended: " + recommended, Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        } else {
            Toast.makeText(requireContext(), "No user information found!", Toast.LENGTH_SHORT).show();
        }
    }


    private void selectProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profilePicture.setImageURI(selectedImageUri);
        }
    }

    private void saveUserInfo() {
        int age = npAge.getValue();
        int weight = npWeight.getValue();
        int height = npHeight.getValue();
        String sportPractice = spinnerSportPractice.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();

        if (age == 0 || weight == 0 || height == 0 || sportPractice.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int sportPracticeValue;
        switch (sportPractice) {
            case "Never":
                sportPracticeValue = 0;
                break;
            case "Occasional":
                sportPracticeValue = 1;
                break;
            case "Regular":
                sportPracticeValue = 2;
                break;
            default:
                sportPracticeValue = 3;

                break;
        }

        int userId = getCurrentUserId();
        if (userId == -1) return;

        boolean isSaved = databaseHelper.saveUserInfo(
                userId,
                selectedImageUri != null ? selectedImageUri.toString() : "default_image_uri",
                String.valueOf(age),
                String.valueOf(weight),
                String.valueOf(height),
                sportPracticeValue,
                notes
        );

        if (isSaved) {
            Toast.makeText(requireContext(), "User information saved!", Toast.LENGTH_SHORT).show();
            loadUserInfo();
        } else {
            Toast.makeText(requireContext(), "Failed to save user information", Toast.LENGTH_SHORT).show();
        }
    }

}
