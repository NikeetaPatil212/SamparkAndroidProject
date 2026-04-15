package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.androidproject.model.InstituteModel;
import com.example.androidproject.model.LoginRequest;
import com.example.androidproject.model.LoginResponse;
import com.example.androidproject.model.MobileRequest;
import com.example.androidproject.model.MobileResponse;
import com.example.androidproject.model.UserDetails;
import com.example.androidproject.utils.ApiService;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private EditText etPhoneNo, etUsername, etPassword;
    private MaterialAutoCompleteTextView etInstitute;
    private Button btnLogin;
    private ApiService apiService;
    private boolean isApiCalled = false;
    private String selectedInstituteId = "";

    FrameLayout loaderLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // INIT VIEWS
        etPhoneNo = findViewById(R.id.etPhoneNo);
        etInstitute = findViewById(R.id.etInstitute);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loaderLayout = findViewById(R.id.loaderLayout);

        apiService = RetrofitClient.getApiService();

        etPhoneNo.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String phone = s.toString().trim();
                etPhoneNo.setError(null);

                if (phone.length() < 10) {
                    isApiCalled = false;
                    etInstitute.setText("");
                    return;
                }

                if (phone.length() == 10 &&
                        phone.matches("^[0-9]{10}$") &&
                        !isApiCalled) {

                    isApiCalled = true;
                    callMobileApi(phone);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etInstitute.setOnClickListener(v -> {

            String phone = etPhoneNo.getText().toString().trim();

            if (phone.length() != 10) {
                Toast.makeText(
                        MainActivity.this,
                        "Please enter mobile number first",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // mobile entered → open dropdown
            etInstitute.showDropDown();
        });



        // 🔹 LOGIN BUTTON FLOW (UNCHANGED LOGIC)
        btnLogin.setOnClickListener(v -> {
            validateInputs();

           /* Toast.makeText(MainActivity.this,
                    "Login Successful",
                    Toast.LENGTH_SHORT).show();

            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();*/

        });
    }



    private void callMobileApi(String phone) {

        MobileRequest request = new MobileRequest(phone);

        apiService.getInstitute(request)
                .enqueue(new Callback<MobileResponse>() {

                    @Override
                    public void onResponse(Call<MobileResponse> call,
                                           Response<MobileResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            MobileResponse res = response.body();

                            // 🔐 Save userID
                            PrefManager.getInstance(MainActivity.this)
                                    .saveUserId(res.getUserID());

                            if (res.getDataList() != null && !res.getDataList().isEmpty()) {
                                setInstituteDropdown(res.getDataList());
                            } else {
                                // keep dropdown visible but empty
                                setInstituteDropdown(new ArrayList<>());
                            }

                            Toast.makeText(MainActivity.this,
                                    res.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MobileResponse> call, Throwable t) {
                        isApiCalled = false; // allow retry
                        Toast.makeText(MainActivity.this,
                                "API Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setInstituteDropdown(List<InstituteModel> list) {

        if (list == null) list = new ArrayList<>();

        ArrayAdapter<InstituteModel> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        list
                );

        etInstitute.setAdapter(adapter);

        // MaterialAutoCompleteTextView best practice
        etInstitute.setThreshold(1);

        // Always clear old selection
        etInstitute.setText("", false);

        // Force dropdown to open on click
        etInstitute.setOnClickListener(v -> {
            if (!adapter.isEmpty()) {
                etInstitute.showDropDown();
            }
        });

        etInstitute.setOnItemClickListener((parent, view, position, id) -> {

            InstituteModel selectedInstitute = adapter.getItem(position);

            if (selectedInstitute != null) {

                // Display institute name
                etInstitute.setText(selectedInstitute.getName(), false);

                // Save selected instituteID
                PrefManager.getInstance(this)
                        .saveInstituteId(selectedInstitute.getId());
            }
        });
    }





    // ✅ VALIDATION + LOGIN
    private void validateInputs() {

        String phone = etPhoneNo.getText().toString().trim();
        String institute = etInstitute.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || !phone.matches("^[0-9]{10}$")) {
            etPhoneNo.setError("Enter valid phone number");
            etPhoneNo.requestFocus();
            return;
        }

       /* if (institute.isEmpty()) {
            etInstitute.setError("Please select institute/department");
            etInstitute.requestFocus();
            return;
        }*/

        if (username.isEmpty() || username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        callLoginApi(username, password);
    }

    private void callLoginApi(String username, String password) {

        String userId = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        LoginRequest request = new LoginRequest(
                username,
                password,
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        Log.d("LoginRequest--", request.toString());

        apiService.loginUser(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call,
                                   Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse loginResponse = response.body();

                    // ✅ CORRECT CHECK
                    if (loginResponse.getUserDetails() == null) {
                        Toast.makeText(MainActivity.this,
                                loginResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ SAVE LOGIN DETAILS
                    UserDetails user = loginResponse.getUserDetails();

                    PrefManager pref = PrefManager.getInstance(MainActivity.this);
                    pref.saveUserRole(user.getUserRole());
                    pref.saveOperatorId(user.getOperatorID());
                    pref.saveUserName(user.getUserName());

                    Toast.makeText(MainActivity.this,
                            loginResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(
                            MainActivity.this,
                            DashboardActivity.class
                    ));
                    finish();

                } else {
                    Toast.makeText(MainActivity.this,
                            "Server error",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Something went wrong: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
