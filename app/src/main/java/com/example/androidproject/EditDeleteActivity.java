package com.example.androidproject;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.InquiryResponse;
import com.example.androidproject.model.UpdateStudentRequest;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDeleteActivity extends AppCompatActivity {

    EditText etFirstName, etMiddleName, etLastName, etFullName;
    EditText etMobile, etAlternateNo, etEmail, etAddress;

    String inquiryDate = "";

    Button btnSave, btnCancel;
    View loaderLayout;

    int studentId;
    private ImageButton btnBack;

    private boolean fullNameManuallyEdited = false;

    private final TextWatcher fullNameWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!etFirstName.getText().toString().trim().isEmpty()
                    || !etMiddleName.getText().toString().trim().isEmpty()
                    || !etLastName.getText().toString().trim().isEmpty()) {
                fullNameManuallyEdited = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete);

        initViews();
        setupNameAutoFill();
        loadIntentData();
        setupButtons();
    }

    private void initViews() {
        // ✅ FIX: XML id is "toolbar" — matched here
        btnBack       = findViewById(R.id.btnBack);
        etFirstName   = findViewById(R.id.etFirstName);
        etMiddleName  = findViewById(R.id.etMiddleName);
        etLastName    = findViewById(R.id.etLastName);
        etFullName    = findViewById(R.id.etFullName);
        etMobile      = findViewById(R.id.etMobile);
        etAlternateNo = findViewById(R.id.etAlternateNo);
        etEmail       = findViewById(R.id.etEmail);
        etAddress     = findViewById(R.id.etAddress);
        btnSave       = findViewById(R.id.btnSave);
        btnCancel     = findViewById(R.id.btnCancel);
        loaderLayout  = findViewById(R.id.loaderLayout);
    }

    private void setupNameAutoFill() {
        TextWatcher nameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (fullNameManuallyEdited) return;

                String f = etFirstName.getText().toString().trim();
                String m = etMiddleName.getText().toString().trim();
                String l = etLastName.getText().toString().trim();

                String full = !m.isEmpty()
                        ? f + " " + m + " " + l
                        : (f + " " + l).trim();

                etFullName.removeTextChangedListener(fullNameWatcher);
                etFullName.setText(full);
                etFullName.setSelection(full.length());
                etFullName.addTextChangedListener(fullNameWatcher);
            }
        };

        etFirstName.addTextChangedListener(nameWatcher);
        etMiddleName.addTextChangedListener(nameWatcher);
        etLastName.addTextChangedListener(nameWatcher);
        etFullName.addTextChangedListener(fullNameWatcher);
    }

    private void loadIntentData() {
        studentId = getIntent().getIntExtra("studentId", 0);

        String fullName = getIntent().getStringExtra("name");
        String mobile   = getIntent().getStringExtra("mobile");
        String altMobile= getIntent().getStringExtra("altMobile");
        String email    = getIntent().getStringExtra("email");
        String address  = getIntent().getStringExtra("address");
        inquiryDate     = getIntent().getStringExtra("inquiry_date");

        // Populate contact fields
        etMobile.setText(mobile != null ? mobile : "");
        etAlternateNo.setText(altMobile != null ? altMobile : "");
        etEmail.setText(email != null ? email : "");
        etAddress.setText(address != null ? address : "");

        // Split full name → first / middle / last
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            String f = parts.length > 0 ? parts[0] : "";
            String m = parts.length > 2 ? parts[1] : "";
            String l = parts.length > 2 ? parts[2] : (parts.length > 1 ? parts[1] : "");

            // Populate without triggering auto-fill watcher
            etFullName.removeTextChangedListener(fullNameWatcher);
            etFirstName.setText(f);
            etMiddleName.setText(m);
            etLastName.setText(l);
            etFullName.setText(fullName);
            etFullName.addTextChangedListener(fullNameWatcher);
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> callUpdateApi());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void callUpdateApi() {
        String fName = etFirstName.getText().toString().trim();
        String mName = etMiddleName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();

        // Basic validation
        if (fName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }
        if (mobile.length() != 10) {
            etMobile.setError("Enter valid 10-digit mobile");
            etMobile.requestFocus();
            return;
        }

        loaderLayout.setVisibility(VISIBLE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        String fullName = !mName.isEmpty()
                ? fName + " " + mName + " " + lName
                : (fName + " " + lName).trim();

        etFullName.setText(fullName);

        UpdateStudentRequest request = new UpdateStudentRequest();
        request.fName       = fName;
        request.mName       = mName;
        request.lName       = lName;
        request.fullName    = fullName;
        request.mobile      = mobile;
        request.alternateNo = etAlternateNo.getText().toString().trim();
        request.emailID     = etEmail.getText().toString().trim();
        request.address     = etAddress.getText().toString().trim();
        request.imgurl      = "";
        request.inquiryDate = inquiryDate;
        request.studentID   = studentId;
        request.userID      = Integer.parseInt(userId);
        request.instituteID = Integer.parseInt(instituteId);

        Log.d("UPDATE_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().updateStudent(request)
                .enqueue(new Callback<InquiryResponse>() {
                    @Override
                    public void onResponse(Call<InquiryResponse> call,
                                           Response<InquiryResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditDeleteActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditDeleteActivity.this,
                                    "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<InquiryResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(EditDeleteActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}