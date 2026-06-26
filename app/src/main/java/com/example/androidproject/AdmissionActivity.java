package com.example.androidproject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.androidproject.model.SuggestReceiptRequest;
import com.example.androidproject.model.SuggestReceiptResponse;
import com.example.androidproject.model.profile.BatchTimingResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.room.AdmissionDetail;
import com.example.androidproject.model.Batch;
import com.example.androidproject.model.Course;
import com.example.androidproject.room.AppDatabase;
import com.example.androidproject.room.BatchEntity;
import com.example.androidproject.room.CourseBatchRepository;
import com.example.androidproject.room.CourseEntity;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdmissionActivity extends AppCompatActivity {

    private List<Integer> selectedCourseIDs = new ArrayList<>();

    // ── Changed from AutoCompleteTextView → Spinner ───────────────────────────
    private Spinner spCourse, spBatch;

    private boolean admissionDone = false;
    private List<String> whatsAppMessageQueue = new ArrayList<>();

    TextInputEditText tvStudentName;
    TextInputEditText etAdmissionDate, etTotalFee, etPaidFee, etRemainingFee, etReceiptNo, etReminderDate;
    TextInputEditText etSummaryTotalFee, etSummaryPaidFee, etSummaryRemainingFee, etSummaryReceiptNo;

    MaterialButton btnFinish, btnAdd, btnCamera;
    RecyclerView rvFeeDetails;

    private List<Batch>  batchList  = new ArrayList<>();
    private List<Course> courseList = new ArrayList<>();
    private List<CourseList> selectedCourses = new ArrayList<>();

    private List<String> addedBatchNamesThisSession = new ArrayList<>();

    private int selectedCourseId = -1;
    private int selectedBatchId  = -1;
    private String selectedCourseName, selectedBatchName;

    // ── Spinner ready flags (suppress first auto-fire) ────────────────────────
    private boolean courseSpinnerReady = false;
    private boolean batchSpinnerReady  = false;

    private Uri imageUri;
    private ImageView currentPreview;
    private static final int CAMERA_PERMISSION_CODE = 200;
    private static final int CAMERA_REQUEST_CODE    = 101;

    private String capturedFileName;
    private TextView tvPhotoInfo;

    private int studentId = -1;
    private String mobile;

    private AdmissionDetailAdapter adapter;
    private AppDatabase db;

    private int totalFeeSum = 0, paidFeeSum = 0, remainingFeeSum = 0;
    private String uploadedImageUrl = "", receiptNo, instituteName;

    // ivBatchArrow removed (Spinner has its own arrow); keep ivBatchIcon for tint
    private ImageView ivBatchIcon;
    private TextView tvBatchHelper;
    private MaterialAutoCompleteTextView spBatchTiming;
    private int selectedTimingId   = -1;
    private String selectedTimingName = "";
    private String admissionDateForApi = "";
    private static final String TIMING_URL =
            "http://160.187.87.113:8081/api/InstituteControllersV1/batch_time_test";

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admission);

        PrefManager pref = PrefManager.getInstance(this);
        Log.d("READ_TEST", "InstituteId = " + pref.getInstituteId());

        mobile    = getIntent().getStringExtra("mobile");
        studentId = getIntent().getIntExtra("studentId", 0);
        Log.d("AdmissionActivity", "onCreate: studentId=" + studentId + ", mobile=" + mobile);

        if (studentId == -1) {
            Toast.makeText(this, "Invalid student. Please go back and try again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupDatePicker();
        setupFeeCalculation();
        fetchCourses();
        getSuggestedReceiptNo();

        db.feeDetailDao().deleteForStudent(studentId);
        addedBatchNamesThisSession.clear();
        selectedCourses.clear();
    }

    // ── Init Views ────────────────────────────────────────────────────────────
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

        // ── Spinners (replaced AutoCompleteTextView) ──────────────────────────
        spCourse     = findViewById(R.id.spCourse);
        spBatch      = findViewById(R.id.spBatch);
        ivBatchIcon  = findViewById(R.id.ivBatchIcon);   // tint-only, no arrow needed
        tvBatchHelper= findViewById(R.id.tvBatchHelper);

        spBatch.setEnabled(false); // disabled until course selected

        // ── Batch Timing (keep as dialog-based AutoComplete) ──────────────────
        spBatchTiming = findViewById(R.id.spBatchTiming);
        spBatchTiming.setInputType(InputType.TYPE_NULL);
        spBatchTiming.setKeyListener(null);
        spBatchTiming.setFocusable(false);
        spBatchTiming.setClickable(true);
        spBatchTiming.setCursorVisible(false);
        spBatchTiming.setOnClickListener(v -> fetchTimingsAndShowDialog());
        spBatchTiming.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) fetchTimingsAndShowDialog();
        });

        // ── RecyclerView ──────────────────────────────────────────────────────
        rvFeeDetails = findViewById(R.id.rvFeeDetails);
        rvFeeDetails.setLayoutManager(new LinearLayoutManager(this));

        etSummaryTotalFee     = findViewById(R.id.etSummaryTotalFee);
        etSummaryPaidFee      = findViewById(R.id.etSummaryPaidFee);
        etSummaryRemainingFee = findViewById(R.id.etSummaryRemainingFee);
        etSummaryReceiptNo    = findViewById(R.id.etSummaryReceiptNo);

        // Student name from intent
        tvStudentName.setText(getIntent().getStringExtra("student_name"));

        // Adapter + Room DB
        adapter = new AdmissionDetailAdapter();
        rvFeeDetails.setAdapter(adapter);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "admission-db")
                .allowMainThreadQueries().build();

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
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                                Locale.getDefault()).format(new Date());
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
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── Add Course button ─────────────────────────────────────────────────
        btnAdd.setOnClickListener(v -> {

            // Validate selections — Spinner gives -1 for nothing or 0 for hint row
            if (selectedCourseId == -1) {
                Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedBatchId == -1) {
                Toast.makeText(this, "Please select a batch", Toast.LENGTH_SHORT).show();
                return;
            }

            String admissionDateRaw = etAdmissionDate.getText() != null
                    ? etAdmissionDate.getText().toString().trim() : "";
            if (admissionDateRaw.isEmpty()) {
                Toast.makeText(this, "Please select an admission date", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat inputFmt  = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                admissionDateForApi = outputFmt.format(inputFmt.parse(admissionDateRaw));
            } catch (Exception e) {
                admissionDateForApi = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()).format(Calendar.getInstance().getTime());
            }

            if (selectedTimingId == -1) {
                Toast.makeText(this, "Please select a batch timing", Toast.LENGTH_SHORT).show();
                return;
            }

            String totalStr = etTotalFee.getText() != null
                    ? etTotalFee.getText().toString().trim() : "";
            String paidStr  = etPaidFee.getText()  != null
                    ? etPaidFee.getText().toString().trim()  : "";

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

            // Duplicate batch check
            for (String addedBatch : addedBatchNamesThisSession) {
                if (addedBatch.equalsIgnoreCase(selectedBatchName)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Duplicate Entry")
                            .setMessage("This batch has already been added. Please select a different batch.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                    return;
                }
            }

            receiptNo = etReceiptNo.getText() != null
                    ? etReceiptNo.getText().toString().trim() : "";

            // Save to Room
            AdmissionDetail detail = new AdmissionDetail();
            detail.setStudentId(studentId);
            detail.setCourseName(selectedCourseName);
            detail.setCourseId(String.valueOf(selectedCourseId));
            detail.setBatchName(selectedBatchName);
            detail.setTotalFee(totalFee);
            detail.setPaidFee(paidFee);
            detail.setRemainingFee(remainingFee);
            detail.setBatchId(String.valueOf(selectedBatchId));
            db.feeDetailDao().insert(detail);

            addedBatchNamesThisSession.add(selectedBatchName);

            List<AdmissionDetail> updatedList =
                    db.feeDetailDao().getDetailsForStudent(studentId);
            adapter.setItems(updatedList);

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

            CourseList courseObj = new CourseList(
                    selectedCourseId, selectedCourseName, "Regular",
                    selectedBatchId, selectedBatchName,
                    totalFee, paidFee, remainingFee,
                    remainingFee == 0 ? "Active" : "Pending",
                    receiptNo,
                    etReminderDate.getText().toString(),
                    "2026-04-04T11:18:11", selectedTimingId, 0
            );
            selectedCourses.add(courseObj);

            // Reset course/batch spinners to hint position
            resetCourseSpinner();
            resetBatchSpinner();

            // Reset timing
            spBatchTiming.setText("");
            selectedTimingId   = -1;
            selectedTimingName = "";

            // Reset fee fields
            etAdmissionDate.setText("");
            etTotalFee.setText("");
            etPaidFee.setText("");
            etRemainingFee.setText("");

            Toast.makeText(this, "Course added successfully!", Toast.LENGTH_SHORT).show();
        });

        btnFinish.setOnClickListener(v -> callAddAdmissionApi());
    }

    // ── Course Spinner setup ──────────────────────────────────────────────────
    private void setupCourseSpinner() {
        List<String> names = new ArrayList<>();
        names.add("-- Select Course --");            // hint at position 0
        for (Course c : courseList) names.add(c.getCouse_Name());

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCourse.setAdapter(aa);

        courseSpinnerReady = false;                  // suppress first auto-fire

        spCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!courseSpinnerReady) {
                    courseSpinnerReady = true;
                    return;
                }
                if (position == 0) {
                    // User re-selected hint row
                    resetBatchSpinner();
                    selectedCourseId   = -1;
                    selectedCourseName = "";
                    return;
                }

                // position - 1 because index 0 is hint
                Course selected    = courseList.get(position - 1);
                selectedCourseId   = selected.getCouseID();
                selectedCourseName = selected.getCouse_Name();

                resetBatchSpinner();
                fetchBatches(selectedCourseId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Batch Spinner setup ───────────────────────────────────────────────────
    private void setupBatchSpinner() {
        List<String> names = new ArrayList<>();
        names.add("-- Select Batch --");             // hint at position 0
        for (Batch b : batchList) names.add(b.getBatchName());

        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBatch.setAdapter(batchAdapter);
        spBatch.setEnabled(true);

        if (ivBatchIcon != null)
            ivBatchIcon.setColorFilter(
                    getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ✅ " + batchList.size() + " batch(es) available");
            tvBatchHelper.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        }

        batchSpinnerReady = false;                   // suppress first auto-fire

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!batchSpinnerReady) {
                    batchSpinnerReady = true;
                    return;
                }
                if (position == 0) {
                    selectedBatchId   = -1;
                    selectedBatchName = "";
                    return;
                }

                // position - 1 because index 0 is hint
                Batch selected    = batchList.get(position - 1);
                selectedBatchId   = selected.getBatchID();
                selectedBatchName = selected.getBatchName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Reset helpers ─────────────────────────────────────────────────────────
    private void resetCourseSpinner() {
        // Scroll course spinner back to hint position without triggering the listener
        courseSpinnerReady = false;
        spCourse.setSelection(0);
        selectedCourseId   = -1;
        selectedCourseName = "";
    }

    private void resetBatchSpinner() {
        batchList.clear();
        selectedBatchId   = -1;
        selectedBatchName = "";
        batchSpinnerReady = false;

        ArrayAdapter<String> empty = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        empty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBatch.setAdapter(empty);
        spBatch.setEnabled(false);

        if (ivBatchIcon != null)
            ivBatchIcon.setColorFilter(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ℹ️ Select a course first to load batches");
            tvBatchHelper.setTextColor(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
        }
    }

    // ── Fetch courses from Room ───────────────────────────────────────────────
    private void fetchCourses() {
        CourseBatchRepository.getInstance(this).getCourses(courses -> {
            courseList.clear();
            for (CourseEntity e : courses) {
                courseList.add(new Course(e.courseId, e.courseName, "", "", 0, 0));
            }
            setupCourseSpinner();
        });
    }

    // ── Fetch batches from Room ───────────────────────────────────────────────
    private void fetchBatches(int courseId) {
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ⏳ Loading batches...");
            tvBatchHelper.setTextColor(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
        }
        CourseBatchRepository.getInstance(this).getBatchesByCourse(courseId, batches -> {
            batchList.clear();
            for (BatchEntity e : batches) {
                batchList.add(new Batch(e.batchId, courseId, e.batchName, "", "", ""));
            }
            setupBatchSpinner();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Everything below is UNCHANGED from your original AdmissionActivity
    // ─────────────────────────────────────────────────────────────────────────

    private void fetchTimingsAndShowDialog() {
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Loading batch timings...")
                .setCancelable(false).create();
        loadingDialog.show();

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("userID",      Integer.parseInt(PrefManager.getInstance(this).getUserId()));
                body.put("instituteID", Integer.parseInt(PrefManager.getInstance(this).getInstituteId()));

                URL url = new URL(TIMING_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8");
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) sb.append(scanner.nextLine());
                scanner.close();

                BatchTimingResponse response = new Gson().fromJson(sb.toString(), BatchTimingResponse.class);

                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (response != null && response.isSuccess() && response.getBatchList() != null) {
                        showTimingDialog(response.getBatchList());
                    } else {
                        Toast.makeText(this, "No timings found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showTimingDialog(List<BatchTimingResponse.BatchTimingItem> timings) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_batch_time_admission, null);

        MaterialAutoCompleteTextView spDialogTiming = view.findViewById(R.id.spBatchTiming);
        MaterialButton btnAllot = view.findViewById(R.id.btnAllotTiming);
        MaterialButton btnClose = view.findViewById(R.id.btnClose);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        List<String> labels = new ArrayList<>();
        for (BatchTimingResponse.BatchTimingItem item : timings) labels.add(item.dropdownLabel());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, labels);
        spDialogTiming.setThreshold(0);
        spDialogTiming.setAdapter(adapter);
        spDialogTiming.setOnClickListener(v -> spDialogTiming.showDropDown());

        final BatchTimingResponse.BatchTimingItem[] selected = {null};

        spDialogTiming.setOnItemClickListener((parent, v, position, id) -> {
            selected[0] = timings.get(position);
            if (selected[0].getAvailableSeats() <= 0) {
                Toast.makeText(this, "⚠️ This timing slot is full!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAllot.setOnClickListener(v -> {
            if (selected[0] == null) {
                Toast.makeText(this, "Please select batch timing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selected[0].getAvailableSeats() <= 0) {
                Toast.makeText(this, "This timing is full. Please choose another.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTimingId   = selected[0].getTimingID();
            selectedTimingName = selected[0].getTimingDescription();
            spBatchTiming.setText(selectedTimingName, false);
            dialog.dismiss();
            Toast.makeText(this, "✅ Batch timing selected successfully", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void getSuggestedReceiptNo() {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        SuggestReceiptRequest request = new SuggestReceiptRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));
        Log.d("SUGGEST_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().getSuggestedReceipt(request)
                .enqueue(new Callback<SuggestReceiptResponse>() {
                    @Override
                    public void onResponse(Call<SuggestReceiptResponse> call,
                                           Response<SuggestReceiptResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            etReceiptNo.setText(response.body().getSuggestedReceiptNo());
                        }
                    }
                    @Override
                    public void onFailure(Call<SuggestReceiptResponse> call, Throwable t) {
                        Toast.makeText(AdmissionActivity.this,
                                "Receipt No Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage(File imageFile) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
        RetrofitClient.getApiService().uploadImage(body).enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    uploadedImageUrl = response.body().getImageUrl();
                    Toast.makeText(AdmissionActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdmissionActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
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
        while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
        outputStream.close();
        inputStream.close();
        return file;
    }

    private void callAddAdmissionApi() {
        if (selectedCourses.isEmpty()) {
            Toast.makeText(this, "Please add at least one course before finishing",
                    Toast.LENGTH_SHORT).show();
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
            if (course.getRecieptNo() == null || course.getRecieptNo().isEmpty()) {
                course.setRecieptNo(receiptNo);
            }
        }

        AdmissionRequest request = new AdmissionRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId),
                studentId, admissionDateForApi, uploadedImageUrl, paidFeeSum, selectedCourses);

        Log.d("ADMISSION_REQUEST", new Gson().toJson(request));

        RetrofitClient.getApiService().getAdmission(request)
                .enqueue(new Callback<AdmissionResponse>() {
                    @Override
                    public void onResponse(Call<AdmissionResponse> call,
                                           Response<AdmissionResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isSuccess()) {
                                Toast.makeText(AdmissionActivity.this,
                                        response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                if (mobile != null && !mobile.isEmpty()) {
                                    whatsAppMessageQueue.clear();
                                    buildFeeReceiptQueueThenSendWelcome();
                                } else {
                                    Toast.makeText(AdmissionActivity.this,
                                            "Mobile number not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(AdmissionActivity.this,
                                        response.body().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void buildFeeReceiptQueueThenSendWelcome() {
        PrefManager pref = PrefManager.getInstance(this);
        String studentName = getIntent().getStringExtra("student_name");

        boolean fullyPaid = remainingFeeSum <= 0;
        String feeCategory = fullyPaid ? "Fee Receipt Total" : "Fee Receipt outstanding";

        TemplateRepository.getInstance(this)
                .getTemplateByCategory(feeCategory, new TemplateRepository.SingleTemplateCallback() {
                    @Override
                    public void onSuccess(TemplateEntity feeTemplate) {
                        if (feeTemplate.isActive) {
                            String lang = pref.getLanguage();
                            String templateText;
                            switch (lang) {
                                case "MR": templateText = feeTemplate.wa_MR; break;
                                case "HI": templateText = feeTemplate.wa_HI; break;
                                default:   templateText = feeTemplate.wa_EN; break;
                            }

                            Map<String, String> feeData = new HashMap<>();
                            feeData.put("StudentName", studentName != null ? studentName : "");
                            feeData.put("course",      getCourseNamesString());
                            feeData.put("batch",       getBatchNamesString());
                            feeData.put("institute",   pref.getInstituteName());
                            feeData.put("Authority",   pref.getOwnerName());
                            feeData.put("mobile1",     pref.getInstituteMobile1());
                            feeData.put("mobile2",     pref.getInstituteMobile2());
                            feeData.put("email",       pref.getInstituteEmail());
                            feeData.put("address1",    pref.getInstituteAddress1());
                            feeData.put("address2",    pref.getInstituteAddress2());
                            feeData.put("ownerName",   pref.getOwnerName());
                            feeData.put("amount",      String.valueOf(paidFeeSum));
                            feeData.put("fees",        String.valueOf(totalFeeSum));
                            feeData.put("paid",        String.valueOf(paidFeeSum));
                            feeData.put("outstanding", String.valueOf(Math.max(remainingFeeSum, 0)));
                            feeData.put("receiptNo",   receiptNo != null ? receiptNo : "");
                            feeData.put("date",        new SimpleDateFormat("dd/MM/yyyy",
                                    Locale.getDefault()).format(Calendar.getInstance().getTime()));
                            feeData.put("DueDate",     new SimpleDateFormat("dd/MM/yyyy",
                                    Locale.getDefault()).format(Calendar.getInstance().getTime()));

                            String feeMessage = TemplateRepository.fillTemplate(templateText, feeData);
                            whatsAppMessageQueue.add(feeMessage);
                            sendSmsInBackground(mobile, feeMessage);
                        }
                        sendAdmissionWhatsApp();
                    }
                    @Override
                    public void onError(String error) {
                        Log.w("QUEUE_DEBUG", "Fee template not found: " + error);
                        sendAdmissionWhatsApp();
                    }
                });
    }

    private String getCourseNamesString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedCourses.size(); i++) {
            sb.append(selectedCourses.get(i).getCourseName());
            if (i < selectedCourses.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private String getBatchNamesString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedCourses.size(); i++) {
            sb.append(selectedCourses.get(i).getBatchName());
            if (i < selectedCourses.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private void sendAdmissionWhatsApp() {
        String studentName = getIntent().getStringExtra("student_name");
        StringBuilder courseNames = new StringBuilder();
        for (int i = 0; i < selectedCourses.size(); i++) {
            courseNames.append(selectedCourses.get(i).getCourseName());
            if (i < selectedCourses.size() - 1) courseNames.append(", ");
        }

        TemplateRepository.getInstance(this)
                .getTemplateByCategory("New Admission", new TemplateRepository.SingleTemplateCallback() {
                    @Override
                    public void onSuccess(TemplateEntity template) {
                        if (!template.isActive) {
                            Toast.makeText(AdmissionActivity.this,
                                    "WhatsApp notifications are currently disabled.",
                                    Toast.LENGTH_SHORT).show();
                            goToDashboard();
                            return;
                        }

                        PrefManager pref = PrefManager.getInstance(AdmissionActivity.this);
                        Map<String, String> data = new HashMap<>();
                        data.put("studentName", studentName != null ? studentName : "");
                        data.put("course",      courseNames.toString());
                        data.put("institute",   pref.getInstituteName());
                        data.put("Authority",   pref.getOwnerName());
                        data.put("mobile1",     pref.getInstituteMobile1());
                        data.put("mobile2",     pref.getInstituteMobile2());
                        data.put("email",       pref.getInstituteEmail());
                        data.put("address1",    pref.getInstituteAddress1());
                        data.put("address2",    pref.getInstituteAddress2());
                        data.put("ownerName",   pref.getOwnerName());

                        String lang = pref.getLanguage();
                        String templateText;
                        switch (lang) {
                            case "MR": templateText = template.wa_MR; break;
                            case "HI": templateText = template.wa_HI; break;
                            default:   templateText = template.wa_EN; break;
                        }
                        String message = TemplateRepository.fillTemplate(templateText, data);

                        if (ContextCompat.checkSelfPermission(AdmissionActivity.this,
                                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                            sendSmsInBackground(mobile, message);
                        }
                        openWhatsAppAndGoToDashboard(message);
                    }
                    @Override
                    public void onError(String error) {
                        String fallback = "Hello " + studentName
                                + ", your admission is successfully completed. Welcome!";
                        openWhatsAppAndGoToDashboard(fallback);
                    }
                });
    }

    private void openWhatsAppAndGoToDashboard(String message) {
        whatsAppMessageQueue.add(0, message);
        admissionDone = true;
        sendNextQueuedWhatsApp();
    }

    private void sendNextQueuedWhatsApp() {
        if (whatsAppMessageQueue.isEmpty()) {
            goToDashboard();
            return;
        }
        String nextMessage = whatsAppMessageQueue.remove(0);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            /*intent.setData(Uri.parse("https://wa.me/" + mobile
                    + "?text=" + Uri.encode(nextMessage)));*/
            String formattedNumber = mobile.startsWith("+91") ? mobile.substring(1)
                    : mobile.startsWith("91") ? mobile
                    : "91" + mobile;
            intent.setData(Uri.parse("https://wa.me/" + formattedNumber
                    + "?text=" + Uri.encode(nextMessage)));

            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            whatsAppMessageQueue.clear();
            goToDashboard();
        }
    }

    private void sendSmsInBackground(String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("SMS", "SEND_SMS permission not granted");
                return;
            }
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0,
                    new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("SMS_DEBUG", getResultCode() == Activity.RESULT_OK
                            ? "✅ SMS sent" : "❌ SMS failed: " + getResultCode());
                }
            }, new IntentFilter("SMS_SENT"));
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) sentIntents.add(sentIntent);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null);
        } catch (Exception e) {
            Log.e("SMS", "SMS failed: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (admissionDone) {
            if (!whatsAppMessageQueue.isEmpty()) {
                sendNextQueuedWhatsApp();
            } else {
                admissionDone = false;
                goToDashboard();
            }
        }
    }

    private void goToDashboard() {
        Intent dashIntent = new Intent(AdmissionActivity.this, DashboardActivity.class);
        dashIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashIntent);
        finish();
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
                        etAdmissionDate.setText(new SimpleDateFormat("yyyy/MM/dd",
                                Locale.getDefault()).format(selected.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera(preview);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera(currentPreview);
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}