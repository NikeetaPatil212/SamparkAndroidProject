package com.example.androidproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class InquiryActivity extends AppCompatActivity {

    private EditText etReminderDate, etSource, etSchoolName, etFirstName, etMiddleName, etMobile, etAltMobile;
    private Button btnSaveInquiry, btnAttachFile;
    private AutoCompleteTextView etInquiryAbout;
    private static final int REQUEST_CODE_FILE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inquiry);

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etMobile = findViewById(R.id.etMobile);
        etAltMobile = findViewById(R.id.etAltMobile);
        etInquiryAbout = findViewById(R.id.etInquiryAbout);
        etReminderDate = findViewById(R.id.etReminderDate);
        etSource = findViewById(R.id.etSource);
        etSchoolName = findViewById(R.id.etSchoolName);
        btnSaveInquiry = findViewById(R.id.btnSave);
        btnAttachFile = findViewById(R.id.btnAttachFile);

        // Mobile length filters
        etMobile.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        etAltMobile.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        // Dropdown values for Inquiry About
        String[] inquiryOptions = {"Admission", "Fees", "Courses", "Scholarship", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, inquiryOptions
        );
        etInquiryAbout.setAdapter(adapter);
        etInquiryAbout.setOnClickListener(v -> etInquiryAbout.showDropDown());
        etInquiryAbout.setOnItemClickListener((parent, view, position, id) -> etInquiryAbout.setError(null));

        // Date picker
        etReminderDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    InquiryActivity.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                etReminderDate.setText(date);
                etReminderDate.setError(null);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Attach file button — no permission needed
        btnAttachFile.setOnClickListener(v -> openFileChooser());

        // Save inquiry
        btnSaveInquiry.setOnClickListener(v -> validateInquiry());
    }

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
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String type = getContentResolver().getType(fileUri);
                if (type != null && type.startsWith("image/")) {
                    Toast.makeText(this, "Image selected: " + fileUri, Toast.LENGTH_SHORT).show();
                } else if ("application/pdf".equals(type)) {
                    Toast.makeText(this, "PDF selected: " + fileUri, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void validateInquiry() {
        String firstName = etFirstName.getText().toString().trim();
        String middleName = etMiddleName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String altMobile = etAltMobile.getText().toString().trim();
        String inquiryAbout = etInquiryAbout.getText().toString().trim();
        String reminderDate = etReminderDate.getText().toString().trim();
        String source = etSource.getText().toString().trim();
        String schoolName = etSchoolName.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }
        if (mobile.length() != 10) {
            etMobile.setError("Mobile number must be 10 digits");
            etMobile.requestFocus();
            return;
        }
        if (!(mobile.startsWith("7") || mobile.startsWith("8") || mobile.startsWith("9"))) {
            etMobile.setError("Mobile number must start with 7, 8, or 9");
            etMobile.requestFocus();
            return;
        }
        if (!altMobile.isEmpty()) {
            if (altMobile.length() != 10) {
                etAltMobile.setError("Alternate mobile must be 10 digits");
                etAltMobile.requestFocus();
                return;
            }
            if (!(altMobile.startsWith("7") || altMobile.startsWith("8") || altMobile.startsWith("9"))) {
                etAltMobile.setError("Alternate mobile must start with 7, 8, or 9");
                etAltMobile.requestFocus();
                return;
            }
        }
        if (inquiryAbout.isEmpty()) {
            etInquiryAbout.setError("Please select inquiry type");
            etInquiryAbout.requestFocus();
            return;
        }
        if (reminderDate.isEmpty()) {
            etReminderDate.setError("Reminder date is required");
            etReminderDate.requestFocus();
            return;
        }
        if (source.isEmpty()) {
            etSource.setError("Source of inquiry is required");
            etSource.requestFocus();
            return;
        }
        if (schoolName.isEmpty()) {
            etSchoolName.setError("School name is required");
            etSchoolName.requestFocus();
            return;
        }

        Toast.makeText(this, "Inquiry saved successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(InquiryActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
