package com.example.androidproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.androidproject.model.InstituteModel;
import com.example.androidproject.model.LoginRequest;
import com.example.androidproject.model.LoginResponse;
import com.example.androidproject.model.MobileRequest;
import com.example.androidproject.model.MobileResponse;
import com.example.androidproject.model.UserDetails;
import com.example.androidproject.model.template.InstituteProfileResponse;
import com.example.androidproject.model.template.InstituteRequest;
import com.example.androidproject.model.template.SettingsResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.room.InstituteProfileRepository;
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
    private static final int SMS_PERMISSION_CODE = 101;

    FrameLayout loaderLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        checkSmsPermission();

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

            /*Toast.makeText(MainActivity.this,
                    "Login Successful",
                    Toast.LENGTH_SHORT).show();

            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();*/

        });
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("SMS", "✅ SMS permission granted");
            } else {
                Log.w("SMS", "❌ SMS permission denied");
            }
        }
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
         //  PrefManager.getInstance(this).saveInstituteId(selectedInstitute.getId());
                PrefManager pref = PrefManager.getInstance(this);
                pref.saveInstituteId(selectedInstitute.getId());
                pref.saveInstituteName(selectedInstitute.getName());

                Log.d("SAVE_TEST", "InstituteId = " + pref.getInstituteId());
                Log.d("SAVE_TEST", "InstituteName = " + pref.getInstituteName());
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

   /* private void callLoginApi(String username, String password) {

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
                  //  pref.saveInstituteName(user.getInstituteName());


                    Toast.makeText(MainActivity.this,
                            loginResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    TemplateRepository.getInstance(MainActivity.this).fetchAndCache(
                            new TemplateRepository.TemplateCallback() {
                                @Override
                                public void onSuccess(List<TemplateEntity> templates) {
                                    // Templates saved to Room DB — all screens ready to use them
                                    // No UI change needed here
                                    Toast.makeText(MainActivity.this,
                                            "Template saved to DB",
                                            Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(String error) {
                                    // Non-critical — other screens have fallback message
                                    // Optionally log: Log.w("Login", "Template cache failed: " + error);
                                }
                            }
                    );

                    int userID      = Integer.parseInt(PrefManager.getInstance(MainActivity.this).getUserId());      // however you store it
                    int instituteID = Integer.parseInt(PrefManager.getInstance(MainActivity.this).getInstituteId()); // however you store it

                    InstituteProfileRepository.fetchAndSave(MainActivity.this, userID, instituteID,
                            new InstituteProfileRepository.InstituteProfileCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("Login", "Institute profile cached");
                                }
                                @Override
                                public void onError(String error) {
                                    Log.w("Login", "Institute profile fetch failed: " + error);
                                    // Non-critical — WhatsApp message will just have empty fields
                                }
                            }
                    );

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
    }*/


    private void callLoginApi(String username, String password) {

        String userId      = PrefManager.getInstance(this).getUserId();
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

                    if (loginResponse.getUserDetails() == null) {
                        Toast.makeText(MainActivity.this,
                                loginResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save login details
                    UserDetails user = loginResponse.getUserDetails();
                    PrefManager pref = PrefManager.getInstance(MainActivity.this);
                    pref.saveUserRole(user.getUserRole());
                    pref.saveOperatorId(user.getOperatorID());
                    pref.saveUserName(user.getUserName());

                    Toast.makeText(MainActivity.this,
                            loginResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    int userID      = Integer.parseInt(pref.getUserId());
                    int instituteID = Integer.parseInt(pref.getInstituteId());

                    InstituteRequest instituteRequest = new InstituteRequest(userID, instituteID);

                    // 1. Fetch templates
                    TemplateRepository.getInstance(MainActivity.this).fetchAndCache(
                            new TemplateRepository.TemplateCallback() {
                                @Override
                                public void onSuccess(List<TemplateEntity> templates) {
                                    Toast.makeText(MainActivity.this,
                                            "Template saved to DB",
                                            Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(String error) {
                                    Log.w("Login", "Template cache failed: " + error);
                                }
                            }
                    );

                    // 2. Fetch institute profile
                    apiService.getInstituteProfile(instituteRequest)
                            .enqueue(new Callback<InstituteProfileResponse>() {
                                @Override
                                public void onResponse(Call<InstituteProfileResponse> call,
                                                       Response<InstituteProfileResponse> response) {
                                    if (response.isSuccessful() && response.body() != null
                                            && response.body().isSuccess) {

                                        InstituteProfileResponse.InstituteDetails d =
                                                response.body().instituteDetails;

                                        PrefManager.getInstance(MainActivity.this).saveInstituteProfile(
                                                d.instituteName, d.mobile, d.alternate,
                                                d.email, d.address1, d.address2
                                        );
                                        Log.d("Login", "✅ Institute profile saved: " + d.instituteName);

                                    }
                                }
                                @Override
                                public void onFailure(Call<InstituteProfileResponse> call, Throwable t) {
                                    Log.w("Login", "Institute profile failed: " + t.getMessage());
                                }
                            });

                    // 3. Fetch settings (language)
                    apiService.getSettings(instituteRequest)
                            .enqueue(new Callback<SettingsResponse>() {
                                @Override
                                public void onResponse(Call<SettingsResponse> call,
                                                       Response<SettingsResponse> response) {
                                    if (response.isSuccessful() && response.body() != null
                                            && response.body().isSuccess) {

                                        for (SettingsResponse.SettingItem item
                                                : response.body().settingsList) {
                                            if ("3".equals(item.settingID)) {
                                                String language;
                                                switch (item.value) {
                                                    case "1": language = "MR"; break;
                                                    case "2": language = "HI"; break;
                                                    default:  language = "EN"; break;
                                                }

                                                PrefManager.getInstance(MainActivity.this).saveLanguage(language);
                                                Log.d("LANG_DEBUG", "Saved language: " + language);
                                                break;
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<SettingsResponse> call, Throwable t) {
                                    Log.w("Login", "Settings fetch failed: " + t.getMessage());
                                }
                            });

                    // Navigate to Dashboard
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                    finish();

                } else {
                    Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
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
