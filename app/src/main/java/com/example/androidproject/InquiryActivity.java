package com.example.androidproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.Course;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.model.InquiryRequest;
import com.example.androidproject.model.InquiryResponse;
import com.example.androidproject.utils.ApiService;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InquiryActivity extends AppCompatActivity {

    // ── Fields ────────────────────────────────────────────────────
    private EditText etFirstName, etMiddleName, etLastName, etFullName,
            etMobile, etAltMobile, etEmail, etAddress,
            etSchoolName, etSource, etFeedback,
            etReminderDate, etInquiryDate, etBirthDate;

    private Button btnSaveInquiry, btnAttachFile;
    private AutoCompleteTextView etInquiryAbout;
    private MaterialAutoCompleteTextView etGender;
    private LinearLayout layoutBirthDate;
    private FrameLayout loaderLayout;

    private static final int REQUEST_CODE_FILE = 100;
    private Uri selectedFileUri;

    private List<Course> courseList = new ArrayList<>();
    private List<Integer> selectedCourseIDs = new ArrayList<>();

    // Date values for API
    private String reminderDateForApi, inquiryDateForApi, dobForApi;

    // State flags
    private boolean fullNameManuallyEdited = false;
    private boolean isFetchingCourses = false;
    private boolean isCourseDialogShowing = false;

    // ── fullNameWatcher — class-level so it can be removed/added ──
    private final TextWatcher fullNameWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Only flag as manual edit if at least one name field has content
            if (!etFirstName.getText().toString().trim().isEmpty()
                    || !etMiddleName.getText().toString().trim().isEmpty()
                    || !etLastName.getText().toString().trim().isEmpty()) {
                fullNameManuallyEdited = true;
            }
        }
    };

    // ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry);

        initViews();
        setupToolbar();
        setupNameAutoFill();
        setupGenderDropdown();
        setupInquiryAbout();
        setupDatePickers();
        setupDobToggle();
        setupButtons();
    }

    // ── Init ──────────────────────────────────────────────────────
    private void initViews() {
        etFirstName     = findViewById(R.id.etFirstName);
        etMiddleName    = findViewById(R.id.etMiddleName);
        etLastName      = findViewById(R.id.etLastName);
        etFullName      = findViewById(R.id.etFullName);
        etMobile        = findViewById(R.id.etMobile);
        etAltMobile     = findViewById(R.id.etAltMobile);
        etEmail         = findViewById(R.id.etEmail);
        etAddress       = findViewById(R.id.etAddress);
        etSchoolName    = findViewById(R.id.etSchoolName);
        etSource        = findViewById(R.id.etSource);
        etFeedback      = findViewById(R.id.etFeedback);
        etReminderDate  = findViewById(R.id.etReminderDate);
        etInquiryDate   = findViewById(R.id.etInquiryDate);
        etBirthDate     = findViewById(R.id.etBirthDate);
        etInquiryAbout  = findViewById(R.id.etInquiryAbout);
        etGender        = findViewById(R.id.etGender);
        layoutBirthDate = findViewById(R.id.layoutBirthDate);
        loaderLayout    = findViewById(R.id.loaderLayout);
        btnSaveInquiry  = findViewById(R.id.btnSave);
        btnAttachFile   = findViewById(R.id.btnAttachFile);

        // Mobile input filters
        etMobile.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        etAltMobile.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    // ── Toolbar back button ───────────────────────────────────────
    private void setupToolbar() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    // ── Auto-fill Full Name from F + M + L ───────────────────────
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

                // Remove watcher temporarily to avoid triggering manual-edit flag
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

    // ── Gender Dropdown ───────────────────────────────────────────
    private void setupGenderDropdown() {
        String[] genderList = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, genderList);
        etGender.setThreshold(0);
        etGender.setAdapter(genderAdapter);
        etGender.setOnClickListener(v -> etGender.showDropDown());
        etGender.setOnItemClickListener((parent, view, position, id) -> {
            etGender.clearFocus();
            etGender.setError(null);
        });
    }

    // ── Inquiry About (course multi-select) ───────────────────────
    private void setupInquiryAbout() {
        etInquiryAbout.setOnClickListener(v -> fetchCourses());
    }

    // ── All Date Pickers ──────────────────────────────────────────
    private void setupDatePickers() {

        // Reminder Date — future dates only (min = tomorrow)
        etReminderDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        Calendar sel = Calendar.getInstance();
                        sel.set(year, month, day);

                        etReminderDate.setText(
                                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(sel.getTime()));
                        reminderDateForApi =
                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                        .format(sel.getTime());
                        etReminderDate.setError(null);
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

            dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
            dialog.show();
        });

        // Inquiry Date — any date
        etInquiryDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        Calendar sel = Calendar.getInstance();
                        sel.set(year, month, day);

                        etInquiryDate.setText(
                                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(sel.getTime()));
                        inquiryDateForApi =
                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                        .format(sel.getTime());
                        etInquiryDate.setError(null);
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        // Birth Date — past dates only (max = today)
        etBirthDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        Calendar sel = Calendar.getInstance();
                        sel.set(year, month, day);

                        etBirthDate.setText(
                                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(sel.getTime()));
                        dobForApi =
                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                        .format(sel.getTime());
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    // ── DOB Toggle Switch ─────────────────────────────────────────
    private void setupDobToggle() {
        Switch switchDob = findViewById(R.id.switchDob);
        // ✅ Toggle the wrapper layout, NOT etBirthDate directly
        switchDob.setOnCheckedChangeListener((btn, isChecked) ->
                layoutBirthDate.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    // ── Buttons ───────────────────────────────────────────────────
    private void setupButtons() {
        btnAttachFile.setOnClickListener(v -> openFileChooser());
        btnSaveInquiry.setOnClickListener(v -> validateInquiry());
    }

    // ── Fetch Courses (with loading state + duplicate-call guard) ─
    private void fetchCourses() {
        if (isFetchingCourses) return;

        // Already loaded — show dialog directly, no API call
        if (!courseList.isEmpty()) {
            showCourseSelectionDialog();
            return;
        }

        isFetchingCourses = true;

        // ✅ Show loading state on the field
        etInquiryAbout.setText("Fetching courses...");
        etInquiryAbout.setEnabled(false);
        etInquiryAbout.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        GetCoursesRequest request = new GetCoursesRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getCourses(request).enqueue(new Callback<GetCoursesResponse>() {
            @Override
            public void onResponse(Call<GetCoursesResponse> call, Response<GetCoursesResponse> response) {
                isFetchingCourses = false;
                restoreInquiryAboutField();

                if (response.isSuccessful() && response.body() != null) {
                    courseList = response.body().getCouseList();
                    showCourseSelectionDialog();
                } else {
                    Toast.makeText(InquiryActivity.this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetCoursesResponse> call, Throwable t) {
                isFetchingCourses = false;
                restoreInquiryAboutField();
                Toast.makeText(InquiryActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Restores etInquiryAbout after loading
    private void restoreInquiryAboutField() {
        etInquiryAbout.setText("");
        etInquiryAbout.setEnabled(true);
        etInquiryAbout.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
    }

    // ── Course Selection Dialog ───────────────────────────────────
    private void showCourseSelectionDialog() {
        if (courseList.isEmpty() || isCourseDialogShowing) return;

        isCourseDialogShowing = true;

        String[] courseNames  = new String[courseList.size()];
        boolean[] checkedItems = new boolean[courseList.size()];
        List<Integer> selectedIndices = new ArrayList<>();

        for (int i = 0; i < courseList.size(); i++)
            courseNames[i] = courseList.get(i).getCouse_Name();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Courses");
        builder.setMultiChoiceItems(courseNames, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) selectedIndices.add(which);
            else selectedIndices.remove(Integer.valueOf(which));
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder selectedNames = new StringBuilder();
            selectedCourseIDs.clear();

            for (int index : selectedIndices) {
                selectedNames.append(courseList.get(index).getCouse_Name()).append(", ");
                selectedCourseIDs.add(courseList.get(index).getCouseID());
            }
            if (selectedNames.length() > 0)
                selectedNames.setLength(selectedNames.length() - 2);

            etInquiryAbout.setText(selectedNames.toString());
            Log.d("SelectedCourseIDs", selectedCourseIDs.toString());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> isCourseDialogShowing = false);
        dialog.show();
    }

    // ── File Chooser ──────────────────────────────────────────────
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_CODE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                Toast.makeText(this, "File selected: " + getFileName(selectedFileUri), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        return result != null ? result : uri.getLastPathSegment();
    }

    // ── Validation ────────────────────────────────────────────────
    private void validateInquiry() {
        String firstName    = etFirstName.getText().toString().trim();
        String middleName   = etMiddleName.getText().toString().trim();
        String lastName     = etLastName.getText().toString().trim();
        String fullName     = etFullName.getText().toString().trim();
        String mobile       = etMobile.getText().toString().trim();
        String altMobile    = etAltMobile.getText().toString().trim();
        String email        = etEmail.getText().toString().trim();
        String address      = etAddress.getText().toString().trim();
        String inquiryAbout = etInquiryAbout.getText().toString().trim();
        String reminderDate = etReminderDate.getText().toString().trim();
        String source       = etSource.getText().toString().trim();
        String schoolName   = etSchoolName.getText().toString().trim();
        String feedback     = etFeedback.getText().toString().trim();
        String gender       = etGender.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus(); return;
        }
        if (mobile.length() != 10) {
            etMobile.setError("Mobile number must be 10 digits");
            etMobile.requestFocus(); return;
        }
        if (!(mobile.startsWith("7") || mobile.startsWith("8") || mobile.startsWith("9"))) {
            etMobile.setError("Mobile number must start with 7, 8, or 9");
            etMobile.requestFocus(); return;
        }
        if (!altMobile.isEmpty()) {
            if (altMobile.length() != 10) {
                etAltMobile.setError("Alternate mobile must be 10 digits");
                etAltMobile.requestFocus(); return;
            }
            if (!(altMobile.startsWith("7") || altMobile.startsWith("8") || altMobile.startsWith("9"))) {
                etAltMobile.setError("Alternate mobile must start with 7, 8, or 9");
                etAltMobile.requestFocus(); return;
            }
        }
        if (inquiryAbout.isEmpty()) {
            etInquiryAbout.setError("Please select inquiry type");
            etInquiryAbout.requestFocus(); return;
        }
        if (reminderDate.isEmpty()) {
            etReminderDate.setError("Reminder date is required");
            etReminderDate.requestFocus(); return;
        }
        if (source.isEmpty()) {
            etSource.setError("Source of inquiry is required");
            etSource.requestFocus(); return;
        }
        if (schoolName.isEmpty()) {
            etSchoolName.setError("School name is required");
            etSchoolName.requestFocus(); return;
        }

        callAddInquiryApi(firstName, middleName, lastName, fullName,
                mobile, altMobile, email, address,
                "Inquiry", inquiryAbout, inquiryDateForApi,
                schoolName, source, dobForApi, gender, feedback, reminderDateForApi);
    }

    // ── API Call ──────────────────────────────────────────────────
    private void callAddInquiryApi(String firstName, String middleName, String lastName,
                                   String fullName, String mobile, String altMobile,
                                   String emailId, String address, String type,
                                   String inquiryAbout, String inquiryDate,
                                   String schoolName, String source,
                                   String DOB, String gender, String feedback,
                                   String reminderDate) {

        loaderLayout.setVisibility(View.VISIBLE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        String operatorId  = PrefManager.getInstance(this).getOperatorId();

        InquiryRequest request = new InquiryRequest(
                firstName, middleName, lastName, fullName,
                mobile, altMobile, emailId, address,
                "Inquiry", inquiryAbout, inquiryDate,
                schoolName, source, DOB, gender, feedback,
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                Integer.parseInt(operatorId),
                reminderDate
        );

        Log.d("REQUEST_JSON", new Gson().toJson(request));

        RetrofitClient.getApiService().addInquiry(request).enqueue(new Callback<InquiryResponse>() {
            @Override
            public void onResponse(Call<InquiryResponse> call, Response<InquiryResponse> response) {
                loaderLayout.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    InquiryResponse res = response.body();
                    Toast.makeText(InquiryActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    if (res.isSuccess()) {
                        startActivity(new Intent(InquiryActivity.this, DashboardActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(InquiryActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InquiryResponse> call, Throwable t) {
                loaderLayout.setVisibility(View.GONE);
                Toast.makeText(InquiryActivity.this, "Something went wrong: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
    }
}