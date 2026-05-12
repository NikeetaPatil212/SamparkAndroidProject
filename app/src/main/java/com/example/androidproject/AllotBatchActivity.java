package com.example.androidproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.TimingLessStudentAdapter;
import com.example.androidproject.adapters.WithTimeStudentAdapter;
import com.example.androidproject.model.Batch;
import com.example.androidproject.model.BatchRequest;
import com.example.androidproject.model.BatchResponse;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.model.StudentBasicRequest;
import com.example.androidproject.model.profile.BatchTimingResponse;
import com.example.androidproject.model.profile.TimingLessStudentResponse;
import com.example.androidproject.model.profile.WithTimeStudentResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllotBatchActivity extends AppCompatActivity {

    // ── Mode constants ────────────────────────────────────────────────────────
    public static final String EXTRA_MODE = "mode";
    public static final int MODE_ALLOT  = 0;  // students with NO timing yet
    public static final int MODE_CHANGE = 1;  // students who ALREADY have timing

    // ── Views ─────────────────────────────────────────────────────────────────
    private AutoCompleteTextView spCourse, spBatch;
    private ImageView ivBatchArrow, ivBatchIcon;
    private TextView tvBatchHelper, tvHint, tvStudentCount, tvTitle;
    private CardView cardStudentList, cardBatchTimings;
    private RecyclerView rvStudents;
    private LinearLayout llTimingTiles;
    private FrameLayout loaderLayout;

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<Course> courseList = new ArrayList<>();
    private List<Batch>  batchList  = new ArrayList<>();

    private int selectedCourseId  = -1;
    private int selectedBatchId   = -1;

    // ── Adapters ──────────────────────────────────────────────────────────────
    private TimingLessStudentAdapter adapterAllot;   // MODE_ALLOT
    private WithTimeStudentAdapter   adapterChange;  // MODE_CHANGE

    // ── Screen mode ───────────────────────────────────────────────────────────
    private int screenMode = MODE_ALLOT;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_allot_batch);

        // Read mode BEFORE initViews so applyChangeModeUI() can use views
        screenMode = getIntent().getIntExtra(EXTRA_MODE, MODE_ALLOT);

        initViews();
        setupBackButton();
        fetchCourses();
    }

    // ── Init views ────────────────────────────────────────────────────────────
    private void initViews() {
        spCourse         = findViewById(R.id.spCourse);
        spBatch          = findViewById(R.id.spBatch);
        ivBatchArrow     = findViewById(R.id.ivBatchArrow);
        ivBatchIcon      = findViewById(R.id.ivBatchIcon);
        tvBatchHelper    = findViewById(R.id.tvBatchHelper);
        tvHint           = findViewById(R.id.tvHint);
        tvStudentCount   = findViewById(R.id.tvStudentCount);
        tvTitle          = findViewById(R.id.tvTitle);
        cardStudentList  = findViewById(R.id.cardStudentList);
        cardBatchTimings = findViewById(R.id.cardBatchTimings);
        rvStudents       = findViewById(R.id.rvStudents);
        llTimingTiles    = findViewById(R.id.llTimingTiles);
        loaderLayout     = findViewById(R.id.loaderLayout);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        spBatch.setEnabled(false);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        if (screenMode == MODE_ALLOT) {
            // ── Allot mode: students with NO timing ───────────────────────────
            adapterAllot = new TimingLessStudentAdapter(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId));

            // When a student row is tapped, refresh Card-3 timing tiles
            adapterAllot.setOnTimingsFetchedListener(this::showTimingTiles);

            rvStudents.setAdapter(adapterAllot);

        } else {
            // ── Change mode: students who ALREADY have a timing ───────────────
            adapterChange = new WithTimeStudentAdapter(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId)
            );

            rvStudents.setAdapter(adapterChange);

            // Update UI labels for change mode
            if (tvTitle != null) tvTitle.setText("Change Batch Timing");
            if (tvHint  != null) tvHint.setText(
                    "Tap \"Change\" on a student to reassign their slot");
        }
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Fetch courses ─────────────────────────────────────────────────────────
    private void fetchCourses() {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        GetCoursesRequest request = new GetCoursesRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getCourses(request)
                .enqueue(new Callback<GetCoursesResponse>() {
                    @Override
                    public void onResponse(Call<GetCoursesResponse> call,
                                           Response<GetCoursesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            courseList = response.body().getCouseList();
                            setupCourseDropdown();
                        } else {
                            toast("Failed to fetch courses");
                        }
                    }
                    @Override
                    public void onFailure(Call<GetCoursesResponse> call, Throwable t) {
                        toast("Course API error: " + t.getMessage());
                    }
                });
    }

    // ── Course dropdown ───────────────────────────────────────────────────────
    private void setupCourseDropdown() {
        List<String> names = new ArrayList<>();
        for (Course c : courseList) names.add(c.getCouse_Name());

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spCourse.setAdapter(aa);
        spCourse.setOnClickListener(v -> spCourse.showDropDown());

        spCourse.setOnItemClickListener((parent, view, position, id) -> {
            Course selected    = courseList.get(position);
            selectedCourseId   = selected.getCouseID();

            resetBatchDropdown();
            hideStudentList();
            fetchBatches(selectedCourseId);
        });
    }

    // ── Fetch batches ─────────────────────────────────────────────────────────
    private void fetchBatches(int courseId) {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ⏳ Loading batches...");
            tvBatchHelper.setTextColor(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
        }

        BatchRequest request = new BatchRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId), courseId);

        RetrofitClient.getApiService().getBatch(request)
                .enqueue(new Callback<BatchResponse>() {
                    @Override
                    public void onResponse(Call<BatchResponse> call,
                                           Response<BatchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            batchList = response.body().getBatchList();
                            setupBatchDropdown();
                        } else {
                            toast("Failed to fetch batches");
                        }
                    }
                    @Override
                    public void onFailure(Call<BatchResponse> call, Throwable t) {
                        toast("Batch API error: " + t.getMessage());
                    }
                });
    }

    // ── Batch dropdown ────────────────────────────────────────────────────────
    private void setupBatchDropdown() {
        List<String> names = new ArrayList<>();
        for (Batch b : batchList) names.add(b.getBatchName());

        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spBatch.setAdapter(batchAdapter);
        spBatch.setEnabled(true);
        spBatch.setOnClickListener(v -> spBatch.showDropDown());

        if (ivBatchArrow != null)
            ivBatchArrow.setColorFilter(
                    getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        if (ivBatchIcon != null)
            ivBatchIcon.setColorFilter(
                    getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ✅ " + names.size() + " batch(es) available");
            tvBatchHelper.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        }

        spBatch.setOnItemClickListener((parent, view, position, id) -> {
            Batch selected  = batchList.get(position);
            selectedBatchId = selected.getBatchID();

            tvHint.setVisibility(View.VISIBLE);

            // ── KEY BRANCH: which API to call depends on mode ─────────────────
            if (screenMode == MODE_CHANGE) {
                fetchStudentsWithTime();
            } else {
                fetchStudentsWithoutTime();
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MODE_ALLOT  →  students WITHOUT timing  (your original logic)
    // ═════════════════════════════════════════════════════════════════════════
    private void fetchStudentsWithoutTime() {
        loaderLayout.setVisibility(View.VISIBLE);
        hideStudentList();

        try {
            String userId      = PrefManager.getInstance(this).getUserId();
            String instituteId = PrefManager.getInstance(this).getInstituteId();

            if (isNullOrEmpty(userId) || isNullOrEmpty(instituteId)) {
                loaderLayout.setVisibility(View.GONE);
                toast("User or Institute ID missing");
                return;
            }
            if (selectedCourseId == -1 || selectedBatchId == -1) {
                loaderLayout.setVisibility(View.GONE);
                toast("Please select Course and Batch");
                return;
            }

            StudentBasicRequest request = new StudentBasicRequest(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId),
                    selectedCourseId,   // ← use real IDs (was hardcoded to 1,1)
                    //selectedBatchId
                    1
            );

            Log.d("TIMING_LESS_REQUEST", new Gson().toJson(request));

            RetrofitClient.getApiService()
                    .getBatchAllotment(request)
                    .enqueue(new Callback<TimingLessStudentResponse>() {

                        @Override
                        public void onResponse(Call<TimingLessStudentResponse> call,
                                               Response<TimingLessStudentResponse> response) {
                            loaderLayout.setVisibility(View.GONE);

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d("TIMING_LESS_RESPONSE",
                                        new Gson().toJson(response.body()));

                                List<TimingLessStudentResponse.StudentItem> studentList =
                                        response.body().getStudents();

                                if (studentList == null || studentList.isEmpty()) {
                                    hideStudentList();
                                    toast("No students found");
                                    return;
                                }

                                cardStudentList.setVisibility(View.VISIBLE);
                                tvStudentCount.setText(studentList.size() + " Students");
                                tvHint.setVisibility(View.VISIBLE);
                                adapterAllot.setData(studentList);

                                // Card-3 populates when first student is tapped
                                cardBatchTimings.setVisibility(View.GONE);

                            } else {
                                hideStudentList();
                                String msg = "Failed to fetch students";
                                if (response.body() != null
                                        && response.body().getMessage() != null) {
                                    msg = response.body().getMessage();
                                }
                                toast(msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<TimingLessStudentResponse> call,
                                              Throwable t) {
                            loaderLayout.setVisibility(View.GONE);
                            hideStudentList();
                            Log.e("TIMING_LESS_ERROR", t.getMessage(), t);
                            toast("API Failed: " + t.getMessage());
                        }
                    });

        } catch (Exception e) {
            loaderLayout.setVisibility(View.GONE);
            hideStudentList();
            Log.e("FETCH_STUDENT_EXCEPTION", e.getMessage(), e);
            toast("Something went wrong");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MODE_CHANGE  →  students WITH timing already assigned
    // ═════════════════════════════════════════════════════════════════════════
    private void fetchStudentsWithTime() {
        loaderLayout.setVisibility(View.VISIBLE);
        hideStudentList();

        try {
            String userId      = PrefManager.getInstance(this).getUserId();
            String instituteId = PrefManager.getInstance(this).getInstituteId();

            if (isNullOrEmpty(userId) || isNullOrEmpty(instituteId)) {
                loaderLayout.setVisibility(View.GONE);
                toast("User or Institute ID missing");
                return;
            }
            if (selectedCourseId == -1 || selectedBatchId == -1) {
                loaderLayout.setVisibility(View.GONE);
                toast("Please select Course and Batch");
                return;
            }

            StudentBasicRequest request = new StudentBasicRequest(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId),
                    selectedCourseId,
                    //selectedBatchId
                    1
            );

            Log.d("WITH_TIME_REQUEST", new Gson().toJson(request));

            RetrofitClient.getApiService()
                    .getStudentsWithTime(request)   // ← NEW endpoint
                    .enqueue(new Callback<WithTimeStudentResponse>() {

                        @Override
                        public void onResponse(Call<WithTimeStudentResponse> call,
                                               Response<WithTimeStudentResponse> response) {
                            loaderLayout.setVisibility(View.GONE);

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d("WITH_TIME_RESPONSE",
                                        new Gson().toJson(response.body()));

                                List<WithTimeStudentResponse.StudentItem> students =
                                        response.body().getStudents();

                                if (students == null || students.isEmpty()) {
                                    hideStudentList();
                                    toast("No students found");
                                    return;
                                }

                                cardStudentList.setVisibility(View.VISIBLE);
                                tvStudentCount.setText(students.size() + " Students");
                                tvHint.setVisibility(View.VISIBLE);
                                adapterChange.setData(students);

                                // Card-3 not needed in change mode
                                cardBatchTimings.setVisibility(View.GONE);

                            } else {
                                hideStudentList();
                                String msg = "Failed to fetch students";
                                if (response.body() != null
                                        && response.body().getMessage() != null) {
                                    msg = response.body().getMessage();
                                }
                                toast(msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<WithTimeStudentResponse> call,
                                              Throwable t) {
                            loaderLayout.setVisibility(View.GONE);
                            hideStudentList();
                            Log.e("WITH_TIME_ERROR", t.getMessage(), t);
                            toast("API Failed: " + t.getMessage());
                        }
                    });

        } catch (Exception e) {
            loaderLayout.setVisibility(View.GONE);
            hideStudentList();
            Log.e("WITH_TIME_EXCEPTION", e.getMessage(), e);
            toast("Something went wrong");
        }
    }

    // ── Timing picker dialog (MODE_CHANGE) ────────────────────────────────────
    private void showChangeTimingDialog(WithTimeStudentResponse.StudentItem student) {
        // TODO: Replace Toast with your BottomSheetDialog / AlertDialog
        // that lists available time slots and calls a change-timing API on confirm.
        toast("Change timing for: " + student.getStudentName());
    }

    // ── Card-3: timing tiles (MODE_ALLOT only) ────────────────────────────────
    private void showTimingTiles(List<BatchTimingResponse.BatchTimingItem> timings) {
        llTimingTiles.removeAllViews();
        cardBatchTimings.setVisibility(View.VISIBLE);

        for (BatchTimingResponse.BatchTimingItem t : timings) {
            View tile = LayoutInflater.from(this)
                    .inflate(R.layout.item_timing_title, llTimingTiles, false);

            ((TextView) tile.findViewById(R.id.tvTimingName))
                    .setText(t.getTimingDescription());
            ((TextView) tile.findViewById(R.id.tvCapacity))
                    .setText(String.valueOf(t.getCapacity()));
            ((TextView) tile.findViewById(R.id.tvFilled))
                    .setText(String.valueOf(t.getFilled()));

            TextView tvFree = tile.findViewById(R.id.tvFree);
            tvFree.setText(String.valueOf(t.getAvailableSeats()));
            tvFree.setTextColor(t.getAvailableSeats() > 0
                    ? 0xFF2E7D32    // green
                    : 0xFFE53935); // red

            llTimingTiles.addView(tile);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void resetBatchDropdown() {
        batchList.clear();
        selectedBatchId = -1;
        spBatch.setText("");
        spBatch.setAdapter(null);
        spBatch.setEnabled(false);
        if (ivBatchArrow != null)
            ivBatchArrow.setColorFilter(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
        if (ivBatchIcon != null)
            ivBatchIcon.setColorFilter(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ℹ️ Select a course first to load batches");
            tvBatchHelper.setTextColor(
                    getResources().getColor(android.R.color.darker_gray, getTheme()));
        }
    }

    private void hideStudentList() {
        cardStudentList.setVisibility(View.GONE);
        cardBatchTimings.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
        llTimingTiles.removeAllViews();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}