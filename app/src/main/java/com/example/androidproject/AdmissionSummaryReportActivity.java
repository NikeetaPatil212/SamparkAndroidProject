package com.example.androidproject;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidproject.model.summary.AdmissionSummaryRequest;
import com.example.androidproject.model.summary.AdmissionSummaryResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdmissionSummaryReportActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────
    private TextView     chipAllTime, chipToday, chipLast7,
            chipThisMonth, chipLastMonth;
    private TextView     tvTotalAdmissions, tvTotalFees;
    private TextView     tvTotalBadge, tvCourseCount;
    private LinearLayout llTableRows;
    private CardView     cardStats, cardTable, cardChart;
    private InquiryPieChartView pieChartView;
    private FrameLayout  loaderLayout;

    private String currentFilter = "All Time";

    private static final int COLOR_SELECTED_BG    = Color.parseColor("#2E7D32");
    private static final int COLOR_UNSELECTED_BG  = Color.parseColor("#E8F5E9");
    private static final int COLOR_SELECTED_TEXT  = Color.WHITE;
    private static final int COLOR_UNSELECTED_TEXT = Color.parseColor("#2E7D32");

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admission_summary_report);

        initViews();
        setupBackButton();
        setupFilterChips();
        fetchSummary("All Time");
    }

    // ── Init ───────────────────────────────────────────────────────
    private void initViews() {
        chipAllTime    = findViewById(R.id.chipAllTime);
        chipToday      = findViewById(R.id.chipToday);
        chipLast7      = findViewById(R.id.chipLast7);
        chipThisMonth  = findViewById(R.id.chipThisMonth);
        chipLastMonth  = findViewById(R.id.chipLastMonth);

        tvTotalAdmissions = findViewById(R.id.tvTotalAdmissions);
        tvTotalFees       = findViewById(R.id.tvTotalFees);
        tvTotalBadge      = findViewById(R.id.tvTotalBadge);
        tvCourseCount     = findViewById(R.id.tvCourseCount);

        llTableRows  = findViewById(R.id.llTableRows);
        cardStats    = findViewById(R.id.cardStats);
        cardTable    = findViewById(R.id.cardTable);
        cardChart    = findViewById(R.id.cardChart);
        pieChartView = findViewById(R.id.pieChartView);
        loaderLayout = findViewById(R.id.loaderLayout);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Filter chips ───────────────────────────────────────────────
    private void setupFilterChips() {
        chipAllTime.setOnClickListener(v   -> selectFilter("All Time"));
        chipToday.setOnClickListener(v     -> selectFilter("Today"));
        chipLast7.setOnClickListener(v     -> selectFilter("Last Seven Days"));
        chipThisMonth.setOnClickListener(v -> selectFilter("This Month"));
        chipLastMonth.setOnClickListener(v -> selectFilter("Last Month"));
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        updateChipUI();
        fetchSummary(filter);
    }

    private void updateChipUI() {
        setChipState(chipAllTime,   currentFilter.equals("All Time"));
        setChipState(chipToday,     currentFilter.equals("Today"));
        setChipState(chipLast7,     currentFilter.equals("Last Seven Days"));
        setChipState(chipThisMonth, currentFilter.equals("This Month"));
        setChipState(chipLastMonth, currentFilter.equals("Last Month"));
    }

    private void setChipState(TextView chip, boolean selected) {
        chip.setBackgroundColor(selected ? COLOR_SELECTED_BG   : COLOR_UNSELECTED_BG);
        chip.setTextColor(      selected ? COLOR_SELECTED_TEXT  : COLOR_UNSELECTED_TEXT);
    }

    // ── API ────────────────────────────────────────────────────────
    private void fetchSummary(String filterType) {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStats.setVisibility(View.GONE);
        cardTable.setVisibility(View.GONE);
        cardChart.setVisibility(View.GONE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        AdmissionSummaryRequest request = new AdmissionSummaryRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId));

        Log.d("AdmissionSummaryRequest", new Gson().toJson(request));

        RetrofitClient.getApiService().getAdmissionSummary(request)
                .enqueue(new Callback<AdmissionSummaryResponse>() {
                    @Override
                    public void onResponse(Call<AdmissionSummaryResponse> call,
                                           Response<AdmissionSummaryResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("ADM_SUMMARY", new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<AdmissionSummaryResponse.SummaryItem> list =
                                    response.body().getSummaryList();

                            if (list == null || list.isEmpty()) {
                                Toast.makeText(AdmissionSummaryReportActivity.this,
                                        "No data for selected period",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            populateUI(list);

                        } else {
                            String msg = (response.body() != null
                                    && response.body().getMessage() != null)
                                    ? response.body().getMessage()
                                    : "Failed to load summary";
                            Toast.makeText(AdmissionSummaryReportActivity.this,
                                    msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdmissionSummaryResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(AdmissionSummaryReportActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Populate UI ────────────────────────────────────────────────
    private void populateUI(List<AdmissionSummaryResponse.SummaryItem> list) {

        // ── Group raw items by courseName ─────────────────────────
        // API returns one row per admission, so we aggregate here
        Map<String, CourseGroup> grouped = new LinkedHashMap<>();
        for (AdmissionSummaryResponse.SummaryItem item : list) {
            String key = item.getCourseName() != null
                    ? item.getCourseName() : "Unknown";
            if (!grouped.containsKey(key)) {
                grouped.put(key, new CourseGroup(key));
            }
            grouped.get(key).addAdmission(item.getFinalFees());
        }

        List<CourseGroup> courseGroups = new ArrayList<>(grouped.values());

        // ── Grand totals ──────────────────────────────────────────
        int    totalAdmissions = 0;
        double totalFees       = 0;
        for (CourseGroup g : courseGroups) {
            totalAdmissions += g.admissionCount;
            totalFees       += g.totalFees;
        }

        // ── Stats card ────────────────────────────────────────────
        tvTotalAdmissions.setText(String.valueOf(totalAdmissions));
        tvTotalFees.setText(formatFees(totalFees));
        tvTotalBadge.setText(totalAdmissions + " Admissions");
        tvTotalBadge.setVisibility(View.VISIBLE);
        tvCourseCount.setText(courseGroups.size() + " Courses");
        cardStats.setVisibility(View.VISIBLE);

        // ── Table rows ────────────────────────────────────────────
        llTableRows.removeAllViews();
        for (int i = 0; i < courseGroups.size(); i++) {
            llTableRows.addView(buildTableRow(courseGroups.get(i), i));
        }
        cardTable.setVisibility(View.VISIBLE);

        // ── Pie chart ─────────────────────────────────────────────
        List<InquiryPieChartView.PieEntry> chartData = new ArrayList<>();
        for (CourseGroup g : courseGroups) {
            if (g.admissionCount > 0) {
                chartData.add(new InquiryPieChartView.PieEntry(
                        g.courseName,
                        g.admissionCount,   // total — used as slice size
                        0,                  // active — not applicable here
                        g.admissionCount    // converted — reuse for display
                ));
            }
        }
        pieChartView.setData(chartData);
        cardChart.setVisibility(View.VISIBLE);
    }

    // ── Build one table row ────────────────────────────────────────
    private View buildTableRow(CourseGroup group, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        row.setBackgroundColor(index % 2 == 0
                ? Color.WHITE : Color.parseColor("#F5FBF5"));

        // Course Name
        TextView tvCourse = new TextView(this);
        tvCourse.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.5f));
        tvCourse.setText(group.courseName);
        tvCourse.setTextColor(Color.parseColor("#1A1A1A"));
        tvCourse.setTextSize(13f);
        tvCourse.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tvCourse);

        // Total Admissions
        row.addView(makeCell(
                String.valueOf(group.admissionCount), "#1565C0", 1f));

        // Total Fees
        row.addView(makeCell(
                formatFees(group.totalFees), "#2E7D32", 1.2f));

        // Wrapper + divider
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(row);
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        wrapper.addView(divider);
        return wrapper;
    }

    private TextView makeCell(String text, String hexColor, float weight) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hexColor));
        tv.setTextSize(13f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setGravity(android.view.Gravity.CENTER);
        return tv;
    }

    // ── Helpers ────────────────────────────────────────────────────
    private String formatFees(double v) {
        return "₹" + (v == (long) v
                ? String.valueOf((long) v) : String.valueOf(v));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // ── Inner model: aggregated per-course data ────────────────────
    private static class CourseGroup {
        String courseName;
        int    admissionCount = 0;
        double totalFees      = 0;

        CourseGroup(String name) { this.courseName = name; }

        void addAdmission(double fees) {
            admissionCount++;
            totalFees += fees;
        }
    }
}