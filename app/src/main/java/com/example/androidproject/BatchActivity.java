package com.example.androidproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.BatchAdapter;
import com.example.androidproject.model.course.BatchAddRequest;
import com.example.androidproject.model.course.BatchAddResponse;
import com.example.androidproject.model.course.BatchItem;
import com.example.androidproject.model.course.CourseItem;
import com.example.androidproject.room.BatchEntity;
import com.example.androidproject.room.CourseBatchRepository;
import com.example.androidproject.room.CourseEntity;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BatchActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────
    private AutoCompleteTextView spCourse;
    private EditText             etBatchName, etStartDate, etEndDate;
    private MaterialButton       btnSave, btnUpdate, btnCancelEdit;
    private TextView             tvFormTitle, tvBatchCount;
    private CardView             cardBatchList;
    private RecyclerView         rvBatches;
    private FrameLayout          loaderLayout;

    // ── State ──────────────────────────────────────────────────────
    private BatchAdapter     batchAdapter;
    private List<CourseItem> courseItems = new ArrayList<>();

    // 0 = new batch (Save), > 0 = existing batch (Update)
    private int    editingBatchId    = 0;
    private int    selectedCourseId  = 0;
    private String selectedCourseName = "";

    private final SimpleDateFormat displaySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch);

        initViews();
        setupBackButton();
        setupDatePickers();
        setupRecyclerView();
        setDefaultDates();
        loadCoursesForDropdown();
    }

    // ── Init ───────────────────────────────────────────────────────
    private void initViews() {
        spCourse      = findViewById(R.id.spCourse);
        etBatchName   = findViewById(R.id.etBatchName);
        etStartDate   = findViewById(R.id.etStartDate);
        etEndDate     = findViewById(R.id.etEndDate);
        btnSave       = findViewById(R.id.btnSave);
        btnUpdate     = findViewById(R.id.btnUpdate);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        tvFormTitle   = findViewById(R.id.tvFormTitle);
        tvBatchCount  = findViewById(R.id.tvBatchCount);
        cardBatchList = findViewById(R.id.cardBatchList);
        rvBatches     = findViewById(R.id.rvBatches);
        loaderLayout  = findViewById(R.id.loaderLayout);

        // ── Save → always the "add" endpoint, batchID forced to 0 ──
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                editingBatchId = 0; // explicitly 0 for new
                callAddBatchApi();
            }
        });

        // ── Update → always the "update" endpoint, batchID from row tap ──
        btnUpdate.setOnClickListener(v -> {
            if (validateForm()) callUpdateBatchApi();
        });

        btnCancelEdit.setOnClickListener(v -> cancelEditMode());
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Default dates: today & today + 1 year ─────────────────────
    private void setDefaultDates() {
        Calendar today = Calendar.getInstance();
        etStartDate.setText(displaySdf.format(today.getTime()));

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        etEndDate.setText(displaySdf.format(nextYear.getTime()));
    }

    // ── Date Pickers ───────────────────────────────────────────────
    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v   -> showDatePicker(etEndDate));
    }

    private void showDatePicker(EditText target) {
        Calendar cal = Calendar.getInstance();
        String current = target.getText().toString().trim();
        if (!current.isEmpty()) {
            try { cal.setTime(displaySdf.parse(current)); }
            catch (Exception ignored) {}
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(year, month, day);
            target.setText(displaySdf.format(sel.getTime()));
        }, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ── Course Dropdown ───────────────────────────────────────────
    private void loadCoursesForDropdown() {
        CourseBatchRepository.getInstance(this).getCourses(courses -> {
            runOnUiThread(() -> {
                if (courses == null || courses.isEmpty()) {
                    toast("No courses found. Please add courses first.");
                    return;
                }

                courseItems.clear();
                List<String> names = new ArrayList<>();
                for (CourseEntity e : courses) {
                    CourseItem ci = new CourseItem();
                    ci.courseID   = e.courseId;
                    ci.courseName = e.courseName;
                    courseItems.add(ci);
                    names.add(e.courseName);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        names);
                spCourse.setThreshold(0);
                spCourse.setAdapter(adapter);
                spCourse.setOnClickListener(v -> spCourse.showDropDown());

                spCourse.setOnItemClickListener((parent, view, pos, id) -> {
                    CourseItem sel  = courseItems.get(pos);
                    selectedCourseId   = sel.courseID;
                    selectedCourseName = sel.courseName;
                    spCourse.dismissDropDown();
                    loadBatchesForCourse(selectedCourseId);
                });
            });
        });
    }

    // ── RecyclerView ───────────────────────────────────────────────
    private void setupRecyclerView() {
        batchAdapter = new BatchAdapter();
        rvBatches.setLayoutManager(new LinearLayoutManager(this));
        rvBatches.setAdapter(batchAdapter);

        batchAdapter.setOnBatchClickListener(item -> {
            editingBatchId = item.batchID;
            Log.d("BATCH_EDIT", "Editing batchID=" + editingBatchId
                    + " name=" + item.batchName);
            populateFormForEdit(item);
            switchToEditMode();
        });
    }

    // ══ LIST LOADING ══
    //
    // IMPORTANT: this always calls fetchAndCacheBatches() directly — it does
    // NOT go through getBatchesByCourse() first. getBatchesByCourse() only
    // hits the network when the Room cache is empty, so calling it here
    // would keep showing a stale list after every save/update. We only use
    // getBatchesByCourse() for the actual read, right after the cache has
    // been force-refreshed.
    private void loadBatchesForCourse(int courseId) {
        showLoader(true);
        cardBatchList.setVisibility(View.GONE);

        CourseBatchRepository.getInstance(this)
                .fetchAndCacheBatches(courseId, () -> loadBatchesFromRoom(courseId));
    }

    private void loadBatchesFromRoom(int courseId) {

        Log.d("123456789", "loadBatchesFromRoom: " );
        CourseBatchRepository.getInstance(this).getBatchesByCourse(courseId, batches -> {
            showLoader(false);

            if (batches == null || batches.isEmpty()) {
                toast("No batches found for this course");
                cardBatchList.setVisibility(View.GONE);
                updateBadge(0);
                return;
            }

            List<BatchItem> items = new ArrayList<>();
            for (BatchEntity e : batches) {
                Log.d("BATCH_DEBUG", "Room row: batchId=" + e.batchId + " batchName='" + e.batchName + "'");
                BatchItem bi  = new BatchItem();
                bi.batchID    = e.batchId;
                bi.batchName  = e.batchName;
                bi.startDate  = e.startDate;
                bi.endDate    = e.endDate;
                bi.courseID   = e.courseId;
                bi.courseName = selectedCourseName;
                items.add(bi);
            }

            batchAdapter.setData(items);
            cardBatchList.setVisibility(View.VISIBLE);
            updateBadge(items.size());
        });
        // NOTE: CourseBatchRepository already posts this callback on the
        // main thread via mainHandler, so no runOnUiThread() wrapper needed
        // here (unlike loadCoursesForDropdown, which wraps its own).
    }

    // ══ SAVE (new batch) — hits the add endpoint ══
    private void callAddBatchApi() {
        hideKeyboard();
        showLoader(true);

        PrefManager pref = PrefManager.getInstance(this);
        int userID       = Integer.parseInt(pref.getUserId());
        int instituteID  = Integer.parseInt(pref.getInstituteId());
        int capturedCourseId = selectedCourseId;

        BatchAddRequest request = new BatchAddRequest(
                capturedCourseId,
                0,                 // 0 = new batch
                etBatchName.getText().toString().trim(),
                toIso(etStartDate.getText().toString().trim()),
                toIso(etEndDate.getText().toString().trim()),
                userID,
                instituteID);

        Log.d("BATCH_ADD_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().addBatch(request)
                .enqueue(new Callback<BatchAddResponse>() {
                    @Override
                    public void onResponse(Call<BatchAddResponse> call,
                                           Response<BatchAddResponse> response) {
                        Log.d("BATCH_ADD_RESP", "code=" + response.code()
                                + " body=" + new Gson().toJson(response.body()));
                        handleSaveResponse(response, capturedCourseId);
                    }

                    @Override
                    public void onFailure(Call<BatchAddResponse> call, Throwable t) {
                        showLoader(false);
                        Log.e("BATCH_ADD", "onFailure: " + t.getMessage());
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    // ══ UPDATE (existing batch) — hits a separate update endpoint ══
    //
    // NOTE: assumes ApiService has (or you'll add) an `updateBatch` method.
    // If your backend actually uses ONE endpoint for both add & update
    // (branching server-side on batchID), just point this at addBatch()
    // instead — the Room refresh logic below doesn't care which endpoint
    // was hit.
    private void callUpdateBatchApi() {
        hideKeyboard();
        showLoader(true);

        PrefManager pref = PrefManager.getInstance(this);
        int userID       = Integer.parseInt(pref.getUserId());
        int instituteID  = Integer.parseInt(pref.getInstituteId());
        int capturedBatchId  = editingBatchId;
        int capturedCourseId = selectedCourseId;

        BatchAddRequest request = new BatchAddRequest(
                capturedCourseId,
                capturedBatchId,    // > 0 = update this batch
                etBatchName.getText().toString().trim(),
                toIso(etStartDate.getText().toString().trim()),
                toIso(etEndDate.getText().toString().trim()),
                userID,
                instituteID);

        Log.d("BATCH_UPDATE_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().addBatch(request)
                .enqueue(new Callback<BatchAddResponse>() {
                    @Override
                    public void onResponse(Call<BatchAddResponse> call,
                                           Response<BatchAddResponse> response) {
                        Log.d("BATCH_UPDATE_RESP", "code=" + response.code()
                                + " body=" + new Gson().toJson(response.body()));
                        handleSaveResponse(response, capturedCourseId);
                    }

                    @Override
                    public void onFailure(Call<BatchAddResponse> call, Throwable t) {
                        showLoader(false);
                        Log.e("BATCH_UPDATE", "onFailure: " + t.getMessage());
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    // ── Shared success/failure handling for both Save and Update ────
    // Always calls loadBatchesForCourse() → fetchAndCacheBatches() on
    // success, which force-refreshes Room from the server. This is the
    // actual fix for the list not updating after a save/edit.
    private void handleSaveResponse(Response<BatchAddResponse> response, int courseId) {
        if (response.isSuccessful()
                && response.body() != null
                && response.body().isSuccess) {

            toast("✅ " + response.body().message);
            cancelEditMode();
            loadBatchesForCourse(courseId);

        } else {
            showLoader(false);
            String msg = (response.body() != null && response.body().message != null)
                    ? response.body().message : "Save failed";
            Log.e("BATCH_SAVE", "Failed: " + msg);
            toast("❌ " + msg);
        }
    }

    // ── Validation ─────────────────────────────────────────────────
    private boolean validateForm() {
        if (selectedCourseId == 0) {
            toast("Please select a course");
            return false;
        }
        if (etBatchName.getText().toString().trim().isEmpty()) {
            etBatchName.setError("Batch name is required");
            etBatchName.requestFocus();
            return false;
        }
        if (etStartDate.getText().toString().trim().isEmpty()) {
            toast("Please select start date");
            return false;
        }
        if (etEndDate.getText().toString().trim().isEmpty()) {
            toast("Please select end date");
            return false;
        }
        return true;
    }

    // ── Populate form for edit ─────────────────────────────────────
    private void populateFormForEdit(BatchItem item) {
        spCourse.setText(item.courseName != null ? item.courseName : "");
        selectedCourseId   = item.courseID;
        selectedCourseName = item.courseName != null ? item.courseName : "";

        etBatchName.setText(item.batchName  != null ? item.batchName  : "");
        etStartDate.setText(item.startDate  != null ? item.startDate  : "");
        etEndDate.setText  (item.endDate    != null ? item.endDate    : "");
    }

    // ── Edit mode ──────────────────────────────────────────────────
    private void switchToEditMode() {
        tvFormTitle.setText("Edit Batch");
        btnSave.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.VISIBLE);
        btnCancelEdit.setVisibility(View.VISIBLE);
    }

    // ── Cancel → back to Add mode ──────────────────────────────────
    private void cancelEditMode() {
        editingBatchId = 0;
        tvFormTitle.setText("Add New Batch");
        etBatchName.setText("");
        setDefaultDates();
        btnSave.setVisibility(View.VISIBLE);
        btnUpdate.setVisibility(View.GONE);
        btnCancelEdit.setVisibility(View.GONE);
        batchAdapter.clearSelection();
    }

    // ── Badge ──────────────────────────────────────────────────────
    private void updateBadge(int count) {
        String label = count + " Batches";
        tvBatchCount.setText(label);
        tvBatchCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    // ── Helpers ────────────────────────────────────────────────────
    private String toIso(String displayDate) {
        try {
            return apiSdf.format(displaySdf.parse(displayDate));
        } catch (Exception e) {
            return displayDate + "T00:00:00.000Z";
        }
    }

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