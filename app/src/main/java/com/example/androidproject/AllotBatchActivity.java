package com.example.androidproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.example.androidproject.model.Course;
import com.example.androidproject.model.StudentBasicRequest;
import com.example.androidproject.model.profile.BatchTimingResponse;
import com.example.androidproject.model.profile.TimingLessStudentResponse;
import com.example.androidproject.model.profile.WithTimeStudentResponse;
import com.example.androidproject.room.BatchEntity;
import com.example.androidproject.room.CourseBatchRepository;
import com.example.androidproject.room.CourseEntity;
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
    private Spinner      spCourse, spBatch;
    private ImageView    ivBatchIcon;
    private TextView     tvBatchHelper, tvHint, tvStudentCount, tvTitle;
    private CardView     cardStudentList, cardBatchTimings;
    private RecyclerView rvStudents;
    private LinearLayout llTimingTiles;
    private FrameLayout  loaderLayout;

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<Course> courseList = new ArrayList<>();
    private List<Batch>  batchList  = new ArrayList<>();

    private int selectedCourseId = -1;
    private int selectedBatchId  = -1;

    // ── Track if spinner selection is a real user pick or programmatic ─────────
    private boolean courseSpinnerReady = false;
    private boolean batchSpinnerReady  = false;

    // ── Adapters ──────────────────────────────────────────────────────────────
    private TimingLessStudentAdapter adapterAllot;
    private WithTimeStudentAdapter   adapterChange;

    // ── Screen mode ───────────────────────────────────────────────────────────
    private int screenMode = MODE_ALLOT;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_allot_batch);

        screenMode = getIntent().getIntExtra(EXTRA_MODE, MODE_ALLOT);

        initViews();
        setupBackButton();
        fetchCourses();
    }

    // ── Init views ────────────────────────────────────────────────────────────
    private void initViews() {
        spCourse         = findViewById(R.id.spCourse);
        spBatch          = findViewById(R.id.spBatch);
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

        // Disable batch spinner until course is chosen
        spBatch.setEnabled(false);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        if (screenMode == MODE_ALLOT) {
            adapterAllot = new TimingLessStudentAdapter(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId));
            adapterAllot.setOnTimingsFetchedListener(this::showTimingTiles);
            rvStudents.setAdapter(adapterAllot);

        } else {
            adapterChange = new WithTimeStudentAdapter(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId));
            rvStudents.setAdapter(adapterChange);

            if (tvTitle != null) tvTitle.setText("Change Batch Timing");
            if (tvHint  != null) tvHint.setText("Tap \"Change\" on a student to reassign their slot");
        }
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Fetch courses from Room DB ────────────────────────────────────────────
    private void fetchCourses() {
        CourseBatchRepository.getInstance(this).getCourses(courses -> {
            courseList.clear();
            for (CourseEntity e : courses) {
                courseList.add(new Course(e.courseId, e.courseName, "", "", 0, 0));
            }
            setupCourseSpinner();
        });
    }

    // ── Course Spinner ────────────────────────────────────────────────────────
    private void setupCourseSpinner() {
        // "-- Select Course --" as first hint item
        List<String> names = new ArrayList<>();
        names.add("Select Course --");
        for (Course c : courseList) names.add(c.getCouse_Name());

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCourse.setAdapter(aa);

        courseSpinnerReady = false; // suppress the first auto-fire

        spCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!courseSpinnerReady) {
                    // first call is always position 0 — ignore it
                    courseSpinnerReady = true;
                    return;
                }
                if (position == 0) {
                    // user re-selected the hint row
                    resetBatchSpinner();
                    hideStudentList();
                    return;
                }

                // position - 1 because index 0 is the hint
                Course selected  = courseList.get(position - 1);
                selectedCourseId = selected.getCouseID();

                if (adapterChange != null) adapterChange.setCourseName(selected.getCouse_Name());
                if (adapterAllot  != null) adapterAllot.setCourseNameTimingLess(selected.getCouse_Name());

                resetBatchSpinner();
                hideStudentList();
                fetchBatches(selectedCourseId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Fetch batches from Room DB ────────────────────────────────────────────
    private void fetchBatches(int courseId) {
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ⏳ Loading batches...");
            tvBatchHelper.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        }

        CourseBatchRepository.getInstance(this).getBatchesByCourse(courseId, batches -> {
            batchList.clear();
            for (BatchEntity e : batches) {
                batchList.add(new Batch(e.batchId, courseId, e.batchName, "", "", ""));
            }
            setupBatchSpinner();
        });
    }

    // ── Batch Spinner ─────────────────────────────────────────────────────────
    private void setupBatchSpinner() {
        List<String> names = new ArrayList<>();
        names.add("Select Batch --");
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

        batchSpinnerReady = false; // suppress the first auto-fire

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!batchSpinnerReady) {
                    batchSpinnerReady = true;
                    return;
                }
                if (position == 0) {
                    // user re-selected hint row
                    hideStudentList();
                    return;
                }

                // position - 1 because index 0 is the hint
                Batch selected  = batchList.get(position - 1);
                selectedBatchId = selected.getBatchID();

                tvHint.setVisibility(View.VISIBLE);

                if (adapterChange != null) adapterChange.setBatchName(selected.getBatchName());
                if (adapterAllot  != null) adapterAllot.setBatchNameTimingLess(selected.getBatchName());

                Log.d("BatchName---", selected.getBatchName());

                if (screenMode == MODE_CHANGE) {
                    fetchStudentsWithTime();
                } else {
                    fetchStudentsWithoutTime();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MODE_ALLOT  →  students WITHOUT timing
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
                    selectedCourseId,
                    selectedBatchId);

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

                                Log.d("TIMING_LESS_RESPONSE", new Gson().toJson(response.body()));

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
                                cardBatchTimings.setVisibility(View.GONE);

                            } else {
                                hideStudentList();
                                String msg = "Failed to fetch students";
                                if (response.body() != null && response.body().getMessage() != null)
                                    msg = response.body().getMessage();
                                toast(msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<TimingLessStudentResponse> call, Throwable t) {
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
                    selectedBatchId);   // ← now uses real selectedBatchId (was hardcoded 1)

            Log.d("WITH_TIME_REQUEST", new Gson().toJson(request));

            RetrofitClient.getApiService()
                    .getStudentsWithTime(request)
                    .enqueue(new Callback<WithTimeStudentResponse>() {

                        @Override
                        public void onResponse(Call<WithTimeStudentResponse> call,
                                               Response<WithTimeStudentResponse> response) {
                            loaderLayout.setVisibility(View.GONE);

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d("WITH_TIME_RESPONSE", new Gson().toJson(response.body()));

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
                                cardBatchTimings.setVisibility(View.GONE);

                            } else {
                                hideStudentList();
                                String msg = "Failed to fetch students";
                                if (response.body() != null && response.body().getMessage() != null)
                                    msg = response.body().getMessage();
                                toast(msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<WithTimeStudentResponse> call, Throwable t) {
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

    // ── Card-3: timing tiles (MODE_ALLOT only) ────────────────────────────────
    private void showTimingTiles(List<BatchTimingResponse.BatchTimingItem> timings) {
        llTimingTiles.removeAllViews();
        cardBatchTimings.setVisibility(View.VISIBLE);

        for (BatchTimingResponse.BatchTimingItem t : timings) {
            View tile = LayoutInflater.from(this)
                    .inflate(R.layout.item_timing_title, llTimingTiles, false);

            ((TextView) tile.findViewById(R.id.tvTimingName)).setText(t.getTimingDescription());
            ((TextView) tile.findViewById(R.id.tvCapacity)).setText(String.valueOf(t.getCapacity()));
            ((TextView) tile.findViewById(R.id.tvFilled)).setText(String.valueOf(t.getFilled()));

            TextView tvFree = tile.findViewById(R.id.tvFree);
            tvFree.setText(String.valueOf(t.getAvailableSeats()));
            tvFree.setTextColor(t.getAvailableSeats() > 0 ? 0xFF2E7D32 : 0xFFE53935);

            llTimingTiles.addView(tile);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void resetBatchSpinner() {
        batchList.clear();
        selectedBatchId = -1;
        batchSpinnerReady = false;

        // Empty adapter to clear the spinner
        ArrayAdapter<String> empty = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
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