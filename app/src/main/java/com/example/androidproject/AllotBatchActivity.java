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

import com.example.androidproject.adapters.AllotBatchAdapter;
import com.example.androidproject.adapters.TimingLessStudentAdapter;
import com.example.androidproject.model.Batch;
import com.example.androidproject.model.BatchRequest;
import com.example.androidproject.model.BatchResponse;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.model.StudentBasicRequest;
import com.example.androidproject.model.StudentBasicResponse;
import com.example.androidproject.model.StudentBasicItem;
import com.example.androidproject.model.profile.TimingLessStudentResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllotBatchActivity extends AppCompatActivity {

    private AutoCompleteTextView spCourse, spBatch;
    private ImageView ivBatchArrow, ivBatchIcon;
    private TextView tvBatchHelper, tvHint, tvStudentCount;
    private CardView cardStudentList, cardBatchTimings;
    private RecyclerView rvStudents;
    private LinearLayout llTimingTiles;
    private FrameLayout loaderLayout;

    private List<Course> courseList = new ArrayList<>();
    private List<Batch> batchList   = new ArrayList<>();

    private int selectedCourseId = -1;
    private int selectedBatchId  = -1;
    private String selectedCourseName = "";
    private String selectedBatchName  = "";

    private TimingLessStudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_allot_batch);

        initViews();
        setupBackButton();
        fetchCourses();
    }

    // ── Init views ────────────────────────────────────────────────────────────
    private void initViews() {
        spCourse        = findViewById(R.id.spCourse);
        spBatch         = findViewById(R.id.spBatch);
        ivBatchArrow    = findViewById(R.id.ivBatchArrow);
        ivBatchIcon     = findViewById(R.id.ivBatchIcon);
        tvBatchHelper   = findViewById(R.id.tvBatchHelper);
        tvHint          = findViewById(R.id.tvHint);
        tvStudentCount  = findViewById(R.id.tvStudentCount);
        cardStudentList = findViewById(R.id.cardStudentList);
        cardBatchTimings= findViewById(R.id.cardBatchTimings);
        rvStudents      = findViewById(R.id.rvStudents);
        llTimingTiles   = findViewById(R.id.llTimingTiles);
        loaderLayout    = findViewById(R.id.loaderLayout);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
       /* adapter = new AllotBatchAdapter(this, student -> {
            // Row tap → show timing allot dialog (implement as needed)
            Toast.makeText(this, "Selected: " + student.getFullName(), Toast.LENGTH_SHORT).show();
        });*/

            adapter = new TimingLessStudentAdapter();
            rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);

        spBatch.setEnabled(false);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Fetch courses for dropdown ────────────────────────────────────────────
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
                    Toast.makeText(AllotBatchActivity.this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetCoursesResponse> call, Throwable t) {
                Toast.makeText(AllotBatchActivity.this, "Course API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Setup course dropdown ─────────────────────────────────────────────────
    private void setupCourseDropdown() {
        List<String> names = new ArrayList<>();
        for (Course c : courseList) names.add(c.getCouse_Name());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spCourse.setAdapter(adapter);
        spCourse.setOnClickListener(v -> spCourse.showDropDown());

        spCourse.setOnItemClickListener((parent, view, position, id) -> {
            Course selected = courseList.get(position);
            selectedCourseId   = selected.getCouseID();
            selectedCourseName = selected.getCouse_Name();

            // Reset batch
            resetBatchDropdown();
            // Reset student list
            hideStudentList();

            fetchBatches(selectedCourseId);
        });
    }

    // ── Fetch batches for selected course ─────────────────────────────────────
    private void fetchBatches(int courseId) {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ⏳ Loading batches...");
            tvBatchHelper.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        }

        BatchRequest request = new BatchRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId), courseId);

        RetrofitClient.getApiService().getBatch(request).enqueue(new Callback<BatchResponse>() {
            @Override
            public void onResponse(Call<BatchResponse> call, Response<BatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    batchList = response.body().getBatchList();
                    setupBatchDropdown();
                } else {
                    Toast.makeText(AllotBatchActivity.this, "Failed to fetch batches", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BatchResponse> call, Throwable t) {
                Toast.makeText(AllotBatchActivity.this, "Batch API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Setup batch dropdown ──────────────────────────────────────────────────
    private void setupBatchDropdown() {
        List<String> names = new ArrayList<>();
        for (Batch b : batchList) names.add(b.getBatchName());

        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spBatch.setAdapter(batchAdapter);
        spBatch.setEnabled(true);
        spBatch.setOnClickListener(v -> spBatch.showDropDown());

        if (ivBatchArrow != null)
            ivBatchArrow.setColorFilter(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        if (ivBatchIcon != null)
            ivBatchIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ✅ " + names.size() + " batch(es) available");
            tvBatchHelper.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        }

        spBatch.setOnItemClickListener((parent, view, position, id) -> {
            Batch selected   = batchList.get(position);
            selectedBatchId  = selected.getBatchID();
            selectedBatchName= selected.getBatchName();

            // Show hint
            tvHint.setVisibility(View.VISIBLE);

            // Call student list API
            fetchStudentList();
        });
    }

    // ── Fetch student list ────────────────────────────────────────────────────
  /*  private void fetchStudentList() {
        loaderLayout.setVisibility(View.VISIBLE);
        hideStudentList();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        StudentBasicRequest request = new StudentBasicRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        Log.d("ALLOT_REQUEST", new Gson().toJson(request));

        RetrofitClient.getApiService().getStudentBasic(request).enqueue(new Callback<StudentBasicResponse>() {
            @Override
            public void onResponse(Call<StudentBasicResponse> call, Response<StudentBasicResponse> response) {
                loaderLayout.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ALLOT_RESPONSE", new Gson().toJson(response.body()));

                    List<StudentBasicItem> list = response.body().getStudentList();

                    if (list == null || list.isEmpty()) {
                        Toast.makeText(AllotBatchActivity.this, "No students found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Show student list card
                    cardStudentList.setVisibility(View.VISIBLE);
                    tvStudentCount.setText(list.size() + " students");
                    adapter.setData(list);

                    // Show timing card with mock/static tiles (replace with real API data)
                    showTimingTiles();

                } else {
                    Toast.makeText(AllotBatchActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StudentBasicResponse> call, Throwable t) {
                loaderLayout.setVisibility(View.GONE);
                Toast.makeText(AllotBatchActivity.this, "API Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/



    // ================= COMPLETE REPLACE fetchStudentList() =================
    private void fetchStudentList() {

        loaderLayout.setVisibility(View.VISIBLE);
        hideStudentList();

        try {

            String userId = PrefManager.getInstance(this).getUserId();
            String instituteId = PrefManager.getInstance(this).getInstituteId();

            if (userId == null || instituteId == null ||
                    userId.isEmpty() || instituteId.isEmpty()) {

                loaderLayout.setVisibility(View.GONE);

                Toast.makeText(this,
                        "User or Institute ID missing",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCourseId == -1 || selectedBatchId == -1) {

                loaderLayout.setVisibility(View.GONE);

                Toast.makeText(this,
                        "Please select Course and Batch",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // API Request
          /*  StudentBasicRequest request = new StudentBasicRequest(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId),
                    selectedCourseId,
                    selectedBatchId
            );*/

            StudentBasicRequest request = new StudentBasicRequest(
                    Integer.parseInt(userId),
                    Integer.parseInt(instituteId),
                    1,
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

                                    Toast.makeText(AllotBatchActivity.this,
                                            "No students found",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Show list
                                cardStudentList.setVisibility(View.VISIBLE);

                                // Count
                                tvStudentCount.setText(
                                        studentList.size() + " Students"
                                );

                                // Hint
                                tvHint.setVisibility(View.VISIBLE);

                                // Adapter
                                adapter.setData(studentList);

                                // Timing tiles
                                showTimingTiles();

                            } else {

                                hideStudentList();

                                String msg = "Failed to fetch students";

                                if (response.body() != null &&
                                        response.body().getMessage() != null) {
                                    msg = response.body().getMessage();
                                }

                                Toast.makeText(AllotBatchActivity.this,
                                        msg,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<TimingLessStudentResponse> call,
                                              Throwable t) {

                            loaderLayout.setVisibility(View.GONE);

                            hideStudentList();

                            Log.e("TIMING_LESS_ERROR",
                                    t.getMessage(),
                                    t);

                            Toast.makeText(AllotBatchActivity.this,
                                    "API Failed: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {

            loaderLayout.setVisibility(View.GONE);

            hideStudentList();

            Log.e("FETCH_STUDENT_EXCEPTION",
                    e.getMessage(),
                    e);

            Toast.makeText(this,
                    "Something went wrong",
                    Toast.LENGTH_SHORT).show();
        }
    }


    // ── Show batch timing tiles ───────────────────────────────────────────────
    // Replace mock data with real API response when timing API is available
    private void showTimingTiles() {
        llTimingTiles.removeAllViews();
        cardBatchTimings.setVisibility(View.VISIBLE);

        // Mock timing slots — replace with API data
        String[][] timings = {
                {"Morning 9",  "30", "1",  "29"},
                {"Morning 10", "30", "0",  "30"},
                {"Evening 5",  "30", "5",  "25"},
                {"Evening 6",  "30", "2",  "28"},
        };

        for (String[] t : timings) {
            View tile = LayoutInflater.from(this)
                    .inflate(R.layout.item_timing_title, llTimingTiles, false);

            ((TextView) tile.findViewById(R.id.tvTimingName)).setText(t[0]);
            ((TextView) tile.findViewById(R.id.tvCapacity)).setText(t[1]);
            ((TextView) tile.findViewById(R.id.tvFilled)).setText(t[2]);
            ((TextView) tile.findViewById(R.id.tvFree)).setText(t[3]);

            llTimingTiles.addView(tile);
        }
    }

    private void resetBatchDropdown() {
        batchList.clear();
        selectedBatchId = -1;
        selectedBatchName = "";
        spBatch.setText("");
        spBatch.setAdapter(null);
        spBatch.setEnabled(false);
        if (ivBatchArrow != null)
            ivBatchArrow.setColorFilter(getResources().getColor(android.R.color.darker_gray, getTheme()));
        if (ivBatchIcon != null)
            ivBatchIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray, getTheme()));
        if (tvBatchHelper != null) {
            tvBatchHelper.setText("  ℹ️ Select a course first to load batches");
            tvBatchHelper.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        }
    }

    private void hideStudentList() {
        cardStudentList.setVisibility(View.GONE);
        cardBatchTimings.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
        llTimingTiles.removeAllViews();
    }
}