package com.example.androidproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.CourseAdapter;
import com.example.androidproject.model.course.CourseItem;
import com.example.androidproject.model.course.CourseRequest;
import com.example.androidproject.model.course.CourseResponse;
import com.example.androidproject.room.CourseBatchRepository;
import com.example.androidproject.room.CourseEntity;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────
    private EditText       etCourseName, etFees, etDuration;
    private Spinner        spScheme, spCertificate;
    private MaterialButton btnSave, btnUpdate, btnCancelEdit;
    private TextView       tvFormTitle, tvCourseCount, tvListCount;
    private CardView       cardCourseList;
    private RecyclerView   rvCourses;
    private FrameLayout    loaderLayout;

    // ── State ──────────────────────────────────────────────────────
    private CourseAdapter adapter;

    // editingCourseId = 0  → Save (new course)
    // editingCourseId > 0  → Update (existing course)
    private int     editingCourseId = 0;
    private boolean isEditMode      = false;

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        initViews();
        setupBackButton();
        setupSpinners();
        setupRecyclerView();
        setupListeners();
        loadCoursesFromRoom(); // initial load from Room cache
    }

    // ── Init ───────────────────────────────────────────────────────
    private void initViews() {
        etCourseName   = findViewById(R.id.etCourseName);
        etFees         = findViewById(R.id.etFees);
        etDuration     = findViewById(R.id.etDuration);
        spScheme       = findViewById(R.id.spScheme);
        spCertificate  = findViewById(R.id.spCertificate);
        btnSave        = findViewById(R.id.btnSave);
        btnUpdate      = findViewById(R.id.btnUpdate);
        btnCancelEdit  = findViewById(R.id.btnCancelEdit);
        tvFormTitle    = findViewById(R.id.tvFormTitle);
        tvCourseCount  = findViewById(R.id.tvCourseCount);
        tvListCount    = findViewById(R.id.tvListCount);
        cardCourseList = findViewById(R.id.cardCourseList);
        rvCourses      = findViewById(R.id.rvCourses);
        loaderLayout   = findViewById(R.id.loaderLayout);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Spinners ───────────────────────────────────────────────────
    private void setupSpinners() {
        // index 0 = Regular, index 1 = Monthly
        spScheme.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Regular", "Monthly")));
        spScheme.setSelection(0);

        // index 0 = Yes  → certificate value = 1
        // index 1 = No   → certificate value = 0
        spCertificate.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Yes", "No")));
        spCertificate.setSelection(0);
    }

    // ── RecyclerView ───────────────────────────────────────────────
    private void setupRecyclerView() {
        adapter = new CourseAdapter();
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(adapter);

        adapter.setOnCourseClickListener(item -> {
            // item.courseID comes directly from Room — always valid here
            // because loadCoursesFromRoom() stores it from CourseEntity.courseId
            editingCourseId = item.courseID;
            isEditMode      = true;
            populateFormForEdit(item);
            switchToEditMode();
        });
    }

    // ── Button listeners ───────────────────────────────────────────
    private void setupListeners() {

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                editingCourseId = 0;     // ← 0 = new course for API
                isEditMode      = false;
                callSaveApi();
            }
        });

        btnUpdate.setOnClickListener(v -> {
            // editingCourseId is already set from the row tap (> 0)
            if (validateForm()) {
                callSaveApi();
            }
        });

        btnCancelEdit.setOnClickListener(v -> cancelEditMode());
    }

    // ── Validation ─────────────────────────────────────────────────
    private boolean validateForm() {
        String name = etCourseName.getText().toString().trim();
        String fees = etFees.getText().toString().trim();
        String dur  = etDuration.getText().toString().trim();

        if (name.isEmpty()) {
            etCourseName.setError("Course name is required");
            etCourseName.requestFocus();
            return false;
        }
        if (fees.isEmpty()) {
            etFees.setError("Fee is required");
            etFees.requestFocus();
            return false;
        }
        if (dur.isEmpty()) {
            etDuration.setError("Duration is required");
            etDuration.requestFocus();
            return false;
        }
        return true;
    }

    // ── Save / Update API ──────────────────────────────────────────
    private void callSaveApi() {
        hideKeyboard();
        showLoader(true);

        PrefManager pref   = PrefManager.getInstance(this);
        int userID         = Integer.parseInt(pref.getUserId());
        int instituteID    = Integer.parseInt(pref.getInstituteId());

        String name   = etCourseName.getText().toString().trim();
        double fees   = Double.parseDouble(etFees.getText().toString().trim());
        int    dur    = Integer.parseInt(etDuration.getText().toString().trim());
        String scheme = spScheme.getSelectedItem() != null
                ? spScheme.getSelectedItem().toString() : "Regular";

        // "Yes" (index 0) → 1,  "No" (index 1) → 0
        int cert = (spCertificate.getSelectedItemPosition() == 0) ? 1 : 0;

        // editingCourseId = 0 for new save, > 0 for update
        CourseRequest request = new CourseRequest(
                editingCourseId,  // courseID: 0 = new, >0 = update
                name, fees, scheme, cert, dur,
                userID, instituteID);

        Log.d("COURSE_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().saveCourse(request)
                .enqueue(new Callback<CourseResponse>() {
                    @Override
                    public void onResponse(Call<CourseResponse> call,
                                           Response<CourseResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {

                            toast("✅ " + response.body().message);
                            cancelEditMode(); // reset form first

                            // ── Refresh Room cache from API, then reload list ──
                            // This ensures we get the real courseID assigned by
                            // the server for new courses, and updated fields for edits.
                            CourseBatchRepository.getInstance(CourseActivity.this)
                                    .fetchAndCacheCourses(() -> {
                                        // onDone runs on main thread
                                        showLoader(false);
                                        loadCoursesFromRoom();
                                    });

                        } else {
                            showLoader(false);
                            String msg = (response.body() != null
                                    && response.body().message != null)
                                    ? response.body().message : "Save failed";
                            toast("❌ " + msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseResponse> call, Throwable t) {
                        showLoader(false);
                        Log.e("COURSE_REQ", "onFailure: " + t.getMessage());
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    // ── Load from Room (after cache is up to date) ─────────────────
    private void loadCoursesFromRoom() {
        showLoader(true);

        CourseBatchRepository.getInstance(this).getCourses(courses -> {
            runOnUiThread(() -> {
                showLoader(false);

                if (courses == null || courses.isEmpty()) {
                    cardCourseList.setVisibility(View.GONE);
                    updateBadges(0);
                    return;
                }

                List<CourseItem> items = new ArrayList<>();
                for (CourseEntity e : courses) {
                    CourseItem item    = new CourseItem();
                    item.courseID      = e.courseId;       // real ID from server via Room
                    item.courseName    = e.courseName;
                    item.fees          = e.fees;           // ← stored in Room (see note below)
                    item.scheme        = e.scheme;
                    item.certificate   = e.certificate;    // 1 or 0
                    item.duration      = e.duration;
                    items.add(item);
                }

                adapter.setData(items);
                cardCourseList.setVisibility(View.VISIBLE);
                updateBadges(items.size());
            });
        });
    }

    // ── Populate form when editing ─────────────────────────────────
    private void populateFormForEdit(CourseItem item) {
        etCourseName.setText(item.courseName != null ? item.courseName : "");

        // Fees: show as integer if whole number, else decimal
        etFees.setText(item.fees == (long) item.fees
                ? String.valueOf((long) item.fees)
                : String.valueOf(item.fees));

        etDuration.setText(String.valueOf(item.duration));

        // Scheme: "Monthly" → index 1, anything else → index 0 (Regular)
        spScheme.setSelection("Monthly".equalsIgnoreCase(item.scheme) ? 1 : 0);

        // Certificate: 1 (Yes) → index 0, 0 (No) → index 1
        spCertificate.setSelection(item.certificate == 1 ? 0 : 1);
    }

    // ── Switch form to Edit mode ───────────────────────────────────
    private void switchToEditMode() {
        tvFormTitle.setText("Edit Course");
        btnSave.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.VISIBLE);
        btnCancelEdit.setVisibility(View.VISIBLE);
    }

    // ── Reset form back to Add mode ────────────────────────────────
    private void cancelEditMode() {
        editingCourseId = 0;
        isEditMode      = false;
        tvFormTitle.setText("Add New Course");
        etCourseName.setText("");
        etFees.setText("");
        etDuration.setText("");
        spScheme.setSelection(0);       // Regular
        spCertificate.setSelection(0);  // Yes
        btnSave.setVisibility(View.VISIBLE);
        btnUpdate.setVisibility(View.GONE);
        btnCancelEdit.setVisibility(View.GONE);
        adapter.clearSelection();
    }

    // ── Badges ────────────────────────────────────────────────────
    private void updateBadges(int count) {
        String label = count + " Courses";
        tvListCount.setText(label);
        tvCourseCount.setText(label);
        tvCourseCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    // ── Helpers ────────────────────────────────────────────────────
    private void showLoader(boolean show) {
        loaderLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(INPUT_METHOD_SERVICE);
            View focus = getCurrentFocus();
            if (focus != null)
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}