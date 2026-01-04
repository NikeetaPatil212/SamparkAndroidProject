package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword;
    private AutoCompleteTextView etInstitute;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Button btnLogin = findViewById(R.id.btnLogin);

        etEmail = findViewById(R.id.etEmail);
        etInstitute = findViewById(R.id.etInstitute);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        String[] institutes = {
                "Computer Science",
                "Information Technology",
                "Mechanical Engineering",
                "Electrical Engineering",
                "Civil Engineering",
                "MBA Department"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                institutes
        );

        etInstitute.setAdapter(adapter);

        etInstitute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etInstitute.showDropDown();
            }
        });

        etInstitute.setOnItemClickListener((parent, view, position, id) -> {
            etInstitute.setError(null);
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user types
                etEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                validateInputs();
            }
        });

       /* btnLogin.setOnClickListener(v -> {
            // TODO: validate login later

            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish(); // prevent back to login
        });*/


    }

    private void validateInputs() {
        String email = etEmail.getText().toString().trim();
        String institute = etInstitute.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Email validation
        if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(null); // clear error
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return; }
        // Institute validation
        if (institute.isEmpty()) { etInstitute.setError("Please select institute/department"); etInstitute.requestFocus(); return; }
        // Username validation
        if (username.isEmpty()) { etUsername.setError("Username is required"); etUsername.requestFocus(); return; } if (username.length() < 3) { etUsername.setError("Username must be at least 3 characters"); etUsername.requestFocus(); return; }
        // Password validation
        if (password.isEmpty()) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; } if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return; }

        // If all validations pass
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}