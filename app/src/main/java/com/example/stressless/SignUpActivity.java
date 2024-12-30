package com.example.stressless;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    DatabaseHelper db;
    EditText firstName, lastName, username, email, password, confirmPassword;
    Button signupButton;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        backButton = findViewById(R.id.back_button);
        db = new DatabaseHelper(this);
        firstName = findViewById(R.id.first_name_signup);
        lastName = findViewById(R.id.last_name_signup);
        username = findViewById(R.id.username_signup);
        email = findViewById(R.id.email_signup);
        password = findViewById(R.id.password_signup);
        confirmPassword = findViewById(R.id.confirm_password_signup);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(v -> {
            String firstNameText = firstName.getText().toString().trim();
            String lastNameText = lastName.getText().toString().trim();
            String usernameText = username.getText().toString().trim();
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString();
            String confirmPasswordText = confirmPassword.getText().toString();

            if (!passwordText.equals(confirmPasswordText)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firstNameText.isEmpty() || lastNameText.isEmpty() || usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.insertUser(usernameText, firstNameText, lastNameText, emailText, passwordText)) {
                Toast.makeText(SignUpActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();

                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", usernameText);
                editor.putString("firstName", firstNameText);
                editor.putString("lastName", lastNameText);
                editor.apply();

                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "Username or Email already exists!", Toast.LENGTH_SHORT).show();
            }

        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            finish();
        });
    }
}

