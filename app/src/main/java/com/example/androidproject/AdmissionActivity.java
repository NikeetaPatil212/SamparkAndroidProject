package com.example.androidproject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.androidproject.adapters.AdmissionDetailAdapter;
import com.example.androidproject.model.AdmissionRequest;
import com.example.androidproject.model.AdmissionResponse;
import com.example.androidproject.model.CourseList;
import com.example.androidproject.model.ImageUploadResponse;
import com.example.androidproject.room.AdmissionDetail;
import com.example.androidproject.model.Batch;
import com.example.androidproject.model.BatchRequest;
import com.example.androidproject.model.BatchResponse;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.room.AppDatabase;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdmissionActivity extends AppCompatActivity {

    private List<Integer> selectedCourseIDs = new ArrayList<>();
    private AutoCompleteTextView spCourse, spBatch;

    TextInputEditText tvStudentName;
    TextInputEditText etAdmissionDate, etTotalFee, etPaidFee, etRemainingFee, etReceiptNo, etReminderDate;
    TextInputEditText etSummaryTotalFee, etSummaryPaidFee, etSummaryRemainingFee, etSummaryReceiptNo;

    MaterialButton btnFinish, btnAdd, btnCamera;
    RecyclerView rvFeeDetails;

    private List<Batch> batchList = new ArrayList<>();
    private List<Course> courseList = new ArrayList<>();
    private List<CourseList> selectedCourses = new ArrayList<>();

    // ── In-memory list of batch names added THIS session for THIS student ──────
    // Used for duplicate check — avoids Room cross-student contamination
    private List<String> addedBatchNamesThisSession = new ArrayList<>();

    private int selectedCourseId = -1;
    private int selectedBatchId = -1;
    private String selectedCourseName, selectedBatchName;

    private Uri imageUri;
    private ImageView currentPreview;
    private static final int CAMERA_PERMISSION_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 101;

    private String capturedFileName;
    private TextView tvPhotoInfo;

    // ── studentId parsed ONCE from intent, validated early ────────────────────
    private int studentId = -1;
    private String mobile;

    private AdmissionDetailAdapter adapter;
    private AppDatabase db;

    private int totalFeeSum = 0, paidFeeSum = 0, remainingFeeSum = 0;
    private String uploadedImageUrl = "", receiptNo;

    private ImageView ivBatchArrow;
    private TextView tvBatchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admission);

        mobile = getIntent().getStringExtra("mobile");
        String fullName = getIntent().getStringExtra("full_name");

        // ── Parse studentId from intent ONCE here ─────────────────────────────
        studentId = getIntent().getIntExtra("studentId", -1);
        Log.d("AdmissionActivity", "onCreate: studentId=" + studentId + ", mobile=" + mobile);

        if (studentId == -1) {
            Toast.makeText(this, "Invalid student. Please go back and try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupDatePicker();
        setupFeeCalculation();
        fetchCourses();

        // ── Clear stale Room records for THIS student from previous sessions ──
        db.feeDetailDao().deleteForStudent(studentId);

        // ── Clear in-memory session list ──────────────────────────────────────
        addedBatchNamesThisSession.clear();
        selectedCourses.clear();
    }

    private void initViews() {
        tvStudentName    = findViewById(R.id.tvStudentName);
        etAdmissionDate  = findViewById(R.id.etAdmissionDate);
        etTotalFee       = findViewById(R.id.etTotalFee);
        etPaidFee        = findViewById(R.id.etPaidFee);
        etRemainingFee   = findViewById(R.id.etRemainingFee);
        etReceiptNo      = findViewById(R.id.etReceiptNo);
        tvPhotoInfo      = findViewById(R.id.tvPhotoInfo);
        etReminderDate   = findViewById(R.id.etReminderDate);

        btnAdd    = findViewById(R.id.btnAdd);
        btnCamera = findViewById(R.id.btnCamera);
        btnFinish = findViewById(R.id.btnFinish);

        spCourse     = findViewById(R.id.spCourse);
        spBatch      = findViewById(R.id.spBatch);
        ivBatchArrow = findViewById(R.id.ivBatchArrow);
        tvBatchHelper= findViewById(R.id.tvBatchHelper);

        rvFeeDetails = findViewById(R.id.rvFeeDetails);
        rvFeeDetails.setLayoutManager(new LinearLayoutManager(this));

        etSummaryTotalFee     = findViewById(R.id.etSummaryTotalFee);
        etSummaryPaidFee      = findViewById(R.id.etSummaryPaidFee);
        etSummaryRemainingFee = findViewById(R.id.etSummaryRemainingFee);
        etSummaryReceiptNo    = findViewById(R.id.etSummaryReceiptNo);

        // ── Student name from intent ──────────────────────────────────────────
        String studentName = getIntent().getStringExtra("student_name");
        tvStudentName.setText(studentName);

        // ── Adapter + Room DB ─────────────────────────────────────────────────
        adapter = new AdmissionDetailAdapter();
        rvFeeDetails.setAdapter(adapter);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "admission-db")
                .allowMainThreadQueries()
                .build();

        // ── Course dropdown ───────────────────────────────────────────────────
        spCourse.setOnClickListener(v -> {
            if (courseList == null || courseList.isEmpty()) fetchCourses();
        });
        spBatch.setEnabled(false);

        // ── Camera button ─────────────────────────────────────────────────────
        btnCamera.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_capture_photo, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            ImageView imgPreview = dialogView.findViewById(R.id.imgPreview);
            Button btnCapture    = dialogView.findViewById(R.id.btnCapture);
            Button btnCancel     = dialogView.findViewById(R.id.btnCancel);
            Button btnDone       = dialogView.findViewById(R.id.btnDone);

            btnCapture.setOnClickListener(view -> checkCameraPermission(imgPreview));
            btnCancel.setOnClickListener(view -> dialog.dismiss());
            btnDone.setOnClickListener(view -> {
                if (imageUri != null) {
                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        capturedFileName = "IMG_" + timeStamp + ".jpg";
                        tvPhotoInfo.setText("Captured: " + capturedFileName);
                        tvPhotoInfo.setVisibility(View.VISIBLE);
                        File file = uriToFile(imageUri);
                        uploadImage(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Image processing error", Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            });
            dialog.show();
        });

        // ── Reminder date default = today + 10 days ───────────────────────────
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        etReminderDate.setText(sdf.format(calendar.getTime()));
        etReminderDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        Calendar sel = Calendar.getInstance();
                        sel.set(year, month, day);
                        etReminderDate.setText(sdf.format(sel.getTime()));
                    },
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // ── Add Course button ─────────────────────────────────────────────────
        btnAdd.setOnClickListener(v -> {

            // Validate course & batch selected
            String courseName = spCourse.getText().toString().trim();
            String batchName  = spBatch.getText().toString().trim();

            if (courseName.isEmpty()) {
                Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
                return;
            }
            if (batchName.isEmpty()) {
                Toast.makeText(this, "Please select a batch", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate fee fields
            String totalStr = etTotalFee.getText() != null ? etTotalFee.getText().toString().trim() : "";
            String paidStr  = etPaidFee.getText()  != null ? etPaidFee.getText().toString().trim()  : "";

            if (totalStr.isEmpty()) {
                Toast.makeText(this, "Please enter total fee", Toast.LENGTH_SHORT).show();
                return;
            }
            if (paidStr.isEmpty()) {
                Toast.makeText(this, "Please enter paid fee", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalFee, paidFee;
            try {
                totalFee = Integer.parseInt(totalStr);
                paidFee  = Integer.parseInt(paidStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid fee amount", Toast.LENGTH_SHORT).show();
                return;
            }

            int remainingFee = Math.max(totalFee - paidFee, 0);

            // ── DUPLICATE CHECK: only against THIS session's added batches ────
            // Uses in-memory list — completely isolated per student per screen open
            for (String addedBatch : addedBatchNamesThisSession) {
                if (addedBatch.equalsIgnoreCase(batchName)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Duplicate Entry")
                            .setMessage("This batch has already been added. Please select a different batch.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                    return;
                }
            }

            receiptNo = etSummaryReceiptNo.getText() != null
                    ? etSummaryReceiptNo.getText().toString().trim() : "";

            // ── Save to Room DB scoped to this studentId ──────────────────────
            AdmissionDetail detail = new AdmissionDetail();
            detail.setStudentId(studentId);          // uses the validated int field
            detail.setCourseName(courseName);
            detail.setCourseId(String.valueOf(selectedCourseId));
            detail.setBatchName(batchName);
            detail.setTotalFee(totalFee);
            detail.setPaidFee(paidFee);
            detail.setRemainingFee(remainingFee);
            detail.setBatchId(String.valueOf(selectedBatchId));
            db.feeDetailDao().insert(detail);

            // ── Track batch in session list to prevent duplicates ─────────────
            addedBatchNamesThisSession.add(batchName);

            // ── Reload table from Room for this studentId only ────────────────
            List<AdmissionDetail> updatedList = db.feeDetailDao().getDetailsForStudent(studentId);
            adapter.setItems(updatedList);

            // ── Recalculate totals from scratch ───────────────────────────────
            totalFeeSum = 0; paidFeeSum = 0; remainingFeeSum = 0;
            for (AdmissionDetail d : updatedList) {
                totalFeeSum     += d.getTotalFee();
                paidFeeSum      += d.getPaidFee();
                remainingFeeSum += d.getRemainingFee();
            }

            etSummaryTotalFee.setText(String.valueOf(totalFeeSum));
            etSummaryPaidFee.setText(String.valueOf(paidFeeSum));
            etSummaryRemainingFee.setText(String.valueOf(remainingFeeSum));
            etSummaryReceiptNo.setText(receiptNo);
            etReminderDate.setVisibility(VISIBLE);

            // ── Add to API payload list ───────────────────────────────────────
            CourseList courseObj = new CourseList(
                    selectedCourseId, selectedCourseName, "Regular",
                    selectedBatchId, selectedBatchName,
                    totalFee, paidFee, remainingFee,
                    remainingFee == 0 ? "Active" : "Pending",
                    receiptNo,
                    etReminderDate.getText().toString(),
                    "2026-04-04T11:18:11", 0
            );
            selectedCourses.add(courseObj);

            // ── Clear input fields for next course entry ──────────────────────
            spCourse.setText("");
            spBatch.setText("");
            spBatch.setEnabled(false);
            if (ivBatchArrow != null) ivBatchArrow.setColorFilter(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
            if (tvBatchHelper != null) {
                tvBatchHelper.setText("  ℹ️ Select a course first to load batches");
                tvBatchHelper.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            }
            etAdmissionDate.setText("");
            etTotalFee.setText("");
            etPaidFee.setText("");
            etRemainingFee.setText("");
            etReceiptNo.setText("");

            Toast.makeText(this, "Course added successfully!", Toast.LENGTH_SHORT).show();
        });

        // ── Finish button ─────────────────────────────────────────────────────
        btnFinish.setOnClickListener(v -> callAddAdmissionApi());
    }

    private void uploadImage(File imageFile) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        RetrofitClient.getApiService().uploadImage(body).enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ImageUploadResponse res = response.body();
                    if (res.isSuccess()) {
                        uploadedImageUrl = res.getImageUrl();
                        Toast.makeText(AdmissionActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdmissionActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdmissionActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                Toast.makeText(AdmissionActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File uriToFile(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File file = new File(getCacheDir(), "upload_image.jpg");
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return file;
    }

    private void callAddAdmissionApi() {
        if (selectedCourses.isEmpty()) {
            Toast.makeText(this, "Please add at least one course before finishing", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        String operatorId  = PrefManager.getInstance(this).getOperatorId();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String reminderDate = sdf.format(calendar.getTime());

        for (CourseList course : selectedCourses) {
            int remaining = course.getFee() - course.getPaid();
            course.setRemaining(remaining);
            course.setStatus(remaining <= 0 ? "Active" : "Pending");
            course.setReminderDate(reminderDate);
            course.setOperatorID(Integer.parseInt(operatorId));
        }

        AdmissionRequest request = new AdmissionRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId),
                studentId, "2026-03-17T18:55:25.148Z",
                uploadedImageUrl, paidFeeSum, selectedCourses
        );

        Log.d("ADMISSION_REQUEST", new Gson().toJson(request));

        RetrofitClient.getApiService().getAdmission(request).enqueue(new Callback<AdmissionResponse>() {
            @Override
            public void onResponse(Call<AdmissionResponse> call, Response<AdmissionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(AdmissionActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        String studentName = getIntent().getStringExtra("student_name");
                        if (mobile != null && !mobile.isEmpty()) {
                            String message = "Hello " + studentName + ", your admission is successfully completed. Welcome!";
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://wa.me/" + mobile + "?text=" + Uri.encode(message)));
                                startActivity(intent);

                                Intent dashIntent = new Intent(AdmissionActivity.this, DashboardActivity.class);
                                dashIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(dashIntent);
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(AdmissionActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AdmissionActivity.this, "Mobile number not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdmissionActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdmissionActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AdmissionResponse> call, Throwable t) {
                Toast.makeText(AdmissionActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateRemainingFee() {
        String totalStr = etTotalFee.getText() != null ? etTotalFee.getText().toString().trim() : "";
        String paidStr  = etPaidFee.getText()  != null ? etPaidFee.getText().toString().trim()  : "";
        if (!totalStr.isEmpty() && !paidStr.isEmpty()) {
            try {
                etRemainingFee.setText(String.valueOf(
                        Math.max(Integer.parseInt(totalStr) - Integer.parseInt(paidStr), 0)));
            } catch (NumberFormatException e) {
                etRemainingFee.setText("");
            }
        } else {
            etRemainingFee.setText("");
        }
    }

    private void setupCourseDropdown() {
        List<String> courseNames = new ArrayList<>();
        for (Course c : courseList) courseNames.add(c.getCouse_Name());

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, courseNames);
        spCourse.setAdapter(courseAdapter);
        spCourse.setOnClickListener(v -> spCourse.showDropDown());

        spCourse.setOnItemClickListener((parent, view, position, id) -> {
            Course selectedCourse = courseList.get(position);
            selectedCourseId   = selectedCourse.getCouseID();
            selectedCourseName = selectedCourse.getCouse_Name();

            batchList.clear();
            selectedBatchId = -1;
            spBatch.setText("");
            spBatch.setAdapter(null);
            spBatch.setEnabled(false);
            if (ivBatchArrow != null) ivBatchArrow.setColorFilter(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
            if (tvBatchHelper != null) {
                tvBatchHelper.setText("  ⏳ Loading batches...");
                tvBatchHelper.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            }

            fetchBatches(selectedCourseId);
        });
    }

    private void setupBatchDropdown() {
        List<String> batchNames = new ArrayList<>();
        for (Batch b : batchList) batchNames.add(b.getBatchName());

        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, batchNames);
        spBatch.setAdapter(batchAdapter);
        spBatch.setEnabled(true);
        spBatch.setOnClickListener(v -> spBatch.showDropDown());

        if (ivBatchArrow != null) ivBatchArrow.setColorFilter(
                getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ✅ " + batchNames.size() + " batch(es) available");
            tvBatchHelper.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        }

        spBatch.setOnItemClickListener((parent, view, position, id) -> {
            Batch selectedBatch = batchList.get(position);
            selectedBatchId   = selectedBatch.getCourseID();
            selectedBatchName = selectedBatch.getBatchName();
        });
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupDatePicker() {
        etAdmissionDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, day);
                        etAdmissionDate.setText(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                                .format(selected.getTime()));
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    private void setupFeeCalculation() {
        TextWatcher feeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateRemainingFee();
            }
        };
        etTotalFee.addTextChangedListener(feeWatcher);
        etPaidFee.addTextChangedListener(feeWatcher);
    }

    private void fetchCourses() {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        GetCoursesRequest request = new GetCoursesRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));
        RetrofitClient.getApiService().getCourses(request).enqueue(new Callback<GetCoursesResponse>() {
            @Override
            public void onResponse(Call<GetCoursesResponse> call, Response<GetCoursesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courseList = response.body().getCouseList();
                    setupCourseDropdown();
                } else {
                    Toast.makeText(AdmissionActivity.this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetCoursesResponse> call, Throwable t) {
                Toast.makeText(AdmissionActivity.this, "Course API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBatches(int courseId) {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        BatchRequest request = new BatchRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId), courseId);
        RetrofitClient.getApiService().getBatch(request).enqueue(new Callback<BatchResponse>() {
            @Override
            public void onResponse(Call<BatchResponse> call, Response<BatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    batchList = response.body().getBatchList();
                    spBatch.setEnabled(true);
                    setupBatchDropdown();
                } else {
                    Toast.makeText(AdmissionActivity.this, "Failed to fetch batches", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BatchResponse> call, Throwable t) {
                Toast.makeText(AdmissionActivity.this, "Batch API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCamera(ImageView preview) {
        currentPreview = preview;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Student Photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Captured by Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && currentPreview != null) {
            currentPreview.setImageURI(imageUri);
        }
    }

    private void checkCameraPermission(ImageView preview) {
        currentPreview = preview;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera(preview);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera(currentPreview);
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}