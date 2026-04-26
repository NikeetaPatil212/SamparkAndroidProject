package com.example.androidproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.AddStudentRequest;
import com.example.androidproject.model.InquiryResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewAdmissionActivity extends AppCompatActivity {

    EditText    etFName, etMName, etLName, etFullName,
            etMobile, etAltMobile, etAddress,
            etSchool, etDob;
    private boolean fullNameManuallyEdited = false;


    Spinner     spGender;
    CheckBox    cbDob;
    Button      btnSave;
    ImageButton btnBack;
    FrameLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_admission);

        initViews();
        setupGenderSpinner();
        setupListeners();
    }

    // ── Init ──────────────────────────────────────────────────────
    private void initViews() {
        etFName      = findViewById(R.id.etFirstName);
        etMName      = findViewById(R.id.etMiddleName);
        etLName      = findViewById(R.id.etLastName);
        etFullName   = findViewById(R.id.etFullName);
        etMobile     = findViewById(R.id.etMobile);
        etAltMobile  = findViewById(R.id.etAlternate);
        etAddress    = findViewById(R.id.etAddress);
        etSchool     = findViewById(R.id.etSchool);
        etDob        = findViewById(R.id.etDob);
        spGender     = findViewById(R.id.spGender);
        cbDob        = findViewById(R.id.cbDob);
        btnSave      = findViewById(R.id.btnSave);
        btnBack      = findViewById(R.id.btnBack);
        loaderLayout = findViewById(R.id.loaderLayout);
    }

    // ── Gender Spinner ────────────────────────────────────────────
    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Select Gender", "Male", "Female"}
        );
        spGender.setAdapter(adapter);
    }

    // ── Listeners ─────────────────────────────────────────────────
   /* private void setupListeners() {

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // DOB checkbox — show/hide date field
        cbDob.setOnCheckedChangeListener((btn, isChecked) ->
                etDob.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        // DOB picker
        etDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);
                String iso = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        .format(selected.getTime());
                etDob.setText(iso);
            }, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Save
        btnSave.setOnClickListener(v -> callAddStudentApi());
    }*/

    private void setupListeners() {

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // DOB checkbox — show/hide date field
        cbDob.setOnCheckedChangeListener((btn, isChecked) ->
                etDob.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        // DOB picker
        etDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);
                String iso = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        .format(selected.getTime());
                etDob.setText(iso);
            }, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── Auto-populate Full Name from F+M+L ──
        TextWatcher nameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (fullNameManuallyEdited) return; // user took control — don't override

                String f = etFName.getText().toString().trim();
                String m = etMName.getText().toString().trim();
                String l = etLName.getText().toString().trim();

                // Build full name — skip middle if empty
                String full;
                if (!m.isEmpty()) {
                    full = f + " " + m + " " + l;
                } else {
                    full = (f + " " + l).trim();
                }

                // Temporarily remove the manual-edit watcher to avoid triggering it
                etFullName.removeTextChangedListener(fullNameWatcher);
                etFullName.setText(full);
                etFullName.setSelection(full.length()); // cursor at end
                etFullName.addTextChangedListener(fullNameWatcher);
            }
        };

        etFName.addTextChangedListener(nameWatcher);
        etMName.addTextChangedListener(nameWatcher);
        etLName.addTextChangedListener(nameWatcher);

        // ── Detect if user manually edits Full Name ──
        etFullName.addTextChangedListener(fullNameWatcher);

        // Save
        btnSave.setOnClickListener(v -> callAddStudentApi());
    }

    // Detects manual edits to Full Name field
    private final TextWatcher fullNameWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // If all three name fields are empty, user is typing freely — allow it
            // If any name field has content, this edit is manual override
            String f = etFName.getText().toString().trim();
            String m = etMName.getText().toString().trim();
            String l = etLName.getText().toString().trim();

            if (!f.isEmpty() || !m.isEmpty() || !l.isEmpty()) {
                fullNameManuallyEdited = true;
            }
        }
    };

    // ── API Call ──────────────────────────────────────────────────
    private void callAddStudentApi() {

        String fName  = etFName.getText().toString().trim();
        String lName  = etLName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();

        // Validation
        if (fName.isEmpty()) {
            etFName.setError("Enter First Name");
            etFName.requestFocus();
            return;
        }
        if (lName.isEmpty()) {
            etLName.setError("Enter Last Name");
            etLName.requestFocus();
            return;
        }
        if (mobile.length() != 10) {
            etMobile.setError("Enter valid 10-digit mobile number");
            etMobile.requestFocus();
            return;
        }

        showLoader();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        String operatorId  = PrefManager.getInstance(this).getOperatorId();

        AddStudentRequest request = new AddStudentRequest();
        request.fName       = fName;
        request.mName       = etMName.getText().toString().trim();
        request.lName       = lName;
        request.fullName    = etFullName.getText().toString().trim();
        request.mobile      = mobile;
        request.alternateNo = etAltMobile.getText().toString().trim();
        request.emailID     = "";
        request.address     = etAddress.getText().toString().trim();
        request.school_name = etSchool.getText().toString().trim();
        request.dob         = cbDob.isChecked() ? etDob.getText().toString() : "";
        request.gender      = spGender.getSelectedItem().toString();
        request.userID      = Integer.parseInt(userId);
        request.instituteID = Integer.parseInt(instituteId);
        request.operatorID  = Integer.parseInt(operatorId);

        Log.d("ADD_STUDENT_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().addStudent(request)
                .enqueue(new Callback<InquiryResponse>() {

                    @Override
                    public void onResponse(Call<InquiryResponse> call,
                                           Response<InquiryResponse> response) {
                        hideLoader();

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(NewAdmissionActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                            // Navigate to AdmissionActivity — only once
                            Intent intent = new Intent(
                                    NewAdmissionActivity.this, AdmissionActivity.class);
                            intent.putExtra("student_name",
                                    etFullName.getText().toString().trim());
                            intent.putExtra("mobile", mobile);
                            startActivity(intent);
                        } else {
                            Toast.makeText(NewAdmissionActivity.this,
                                    "Failed to add student", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<InquiryResponse> call, Throwable t) {
                        hideLoader();
                        Toast.makeText(NewAdmissionActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Loader ────────────────────────────────────────────────────
    private void showLoader() { loaderLayout.setVisibility(View.VISIBLE); }
    private void hideLoader() { loaderLayout.setVisibility(View.GONE); }
}