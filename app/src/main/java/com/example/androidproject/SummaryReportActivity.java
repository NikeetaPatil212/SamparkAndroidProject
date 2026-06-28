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

import com.example.androidproject.model.summary.InquirySummaryRequest;
import com.example.androidproject.model.summary.InquirySummaryResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummaryReportActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private TextView chipAllTime, chipToday, chipLast7, chipLast30;
    private TextView tvTotalCount, tvActiveCount, tvConvertedCount, tvCancelledCount;
    private TextView tvTotalBadge, tvCourseCount;
    private LinearLayout llTableRows;
    private CardView cardStats, cardTable, cardChart;
    private InquiryPieChartView pieChartView;   // ← changed from bar chart
    private FrameLayout loaderLayout;

    private String currentFilter = "All Time";

    private static final int COLOR_SELECTED_BG    = Color.parseColor("#2E7D32");
    private static final int COLOR_UNSELECTED_BG  = Color.parseColor("#E8F5E9");
    private static final int COLOR_SELECTED_TEXT  = Color.WHITE;
    private static final int COLOR_UNSELECTED_TEXT= Color.parseColor("#2E7D32");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_report);

        initViews();
        setupBackButton();
        setupFilterChips();
        fetchSummary("All Time");
    }

    private void initViews() {
        chipAllTime  = findViewById(R.id.chipAllTime);
        chipToday    = findViewById(R.id.chipToday);
        chipLast7    = findViewById(R.id.chipLast7);
        chipLast30   = findViewById(R.id.chipLast30);

        tvTotalCount     = findViewById(R.id.tvTotalCount);
        tvActiveCount    = findViewById(R.id.tvActiveCount);
        tvConvertedCount = findViewById(R.id.tvConvertedCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        tvTotalBadge     = findViewById(R.id.tvTotalBadge);
        tvCourseCount    = findViewById(R.id.tvCourseCount);

        llTableRows  = findViewById(R.id.llTableRows);
        cardStats    = findViewById(R.id.cardStats);
        cardTable    = findViewById(R.id.cardTable);
        cardChart    = findViewById(R.id.cardChart);
        pieChartView = findViewById(R.id.pieChartView);   // ← changed id
        loaderLayout = findViewById(R.id.loaderLayout);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupFilterChips() {
        chipAllTime.setOnClickListener(v -> selectFilter("All Time"));
        chipToday.setOnClickListener(v   -> selectFilter("Today"));
        chipLast7.setOnClickListener(v   -> selectFilter("Last Seven Days"));
        chipLast30.setOnClickListener(v  -> selectFilter("Last 30 Days"));
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        updateChipUI();
        fetchSummary(filter);
    }

    private void updateChipUI() {
        setChipState(chipAllTime, currentFilter.equals("All Time"));
        setChipState(chipToday,   currentFilter.equals("Today"));
        setChipState(chipLast7,   currentFilter.equals("Last Seven Days"));
        setChipState(chipLast30,  currentFilter.equals("Last 30 Days"));
    }

    private void setChipState(TextView chip, boolean selected) {
        chip.setBackgroundColor(selected ? COLOR_SELECTED_BG  : COLOR_UNSELECTED_BG);
        chip.setTextColor(      selected ? COLOR_SELECTED_TEXT : COLOR_UNSELECTED_TEXT);
    }

    private void fetchSummary(String filterType) {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStats.setVisibility(View.GONE);
        cardTable.setVisibility(View.GONE);
        cardChart.setVisibility(View.GONE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        InquirySummaryRequest request = new InquirySummaryRequest(
                filterType, Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getInquirySummary(request)
                .enqueue(new Callback<InquirySummaryResponse>() {
                    @Override
                    public void onResponse(Call<InquirySummaryResponse> call,
                                           Response<InquirySummaryResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("SUMMARY_RAW", new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<InquirySummaryResponse.SummaryItem> list =
                                    response.body().getSummaryList();

                            if (list == null || list.isEmpty()) {
                                Toast.makeText(SummaryReportActivity.this,
                                        "No data for selected period", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            populateUI(list);

                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Failed to load summary";
                            Toast.makeText(SummaryReportActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<InquirySummaryResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(SummaryReportActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void populateUI(List<InquirySummaryResponse.SummaryItem> list) {

        // ── Totals ────────────────────────────────────────────────
        int sumTotal = 0, sumActive = 0, sumConverted = 0, sumCancelled = 0;
        for (InquirySummaryResponse.SummaryItem item : list) {
            sumTotal     += item.getTotalInquiries();
            sumActive    += item.getActive();
            sumConverted += item.getConverted();
            sumCancelled += item.getCancelled();
        }

        tvTotalCount.setText(String.valueOf(sumTotal));
        tvActiveCount.setText(String.valueOf(sumActive));
        tvConvertedCount.setText(String.valueOf(sumConverted));
        tvCancelledCount.setText(String.valueOf(sumCancelled));
        tvTotalBadge.setText(sumTotal + " Total");
        tvTotalBadge.setVisibility(View.VISIBLE);
        tvCourseCount.setText(list.size() + " Courses");
        cardStats.setVisibility(View.VISIBLE);

        // ── Table rows ────────────────────────────────────────────
        llTableRows.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            llTableRows.addView(buildTableRow(list.get(i), i));
        }
        cardTable.setVisibility(View.VISIBLE);

        // ── Pie chart ─────────────────────────────────────────────
        List<InquiryPieChartView.PieEntry> chartData = new ArrayList<>();
        for (InquirySummaryResponse.SummaryItem item : list) {
            if (item.getTotalInquiries() > 0) {
                chartData.add(new InquiryPieChartView.PieEntry(
                        item.getCourseName(),
                        item.getTotalInquiries(),
                        item.getActive(),
                        item.getConverted()
                ));
            }
        }
        pieChartView.setData(chartData);
        cardChart.setVisibility(View.VISIBLE);
    }

    private View buildTableRow(InquirySummaryResponse.SummaryItem item, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        row.setBackgroundColor(index % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        TextView tvCourse = new TextView(this);
        tvCourse.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2.2f));
        tvCourse.setText(item.getCourseName());
        tvCourse.setTextColor(Color.parseColor("#1A1A1A"));
        tvCourse.setTextSize(13f);
        tvCourse.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(tvCourse);
        row.addView(makeStatCell(String.valueOf(item.getTotalInquiries()), "#1565C0", 1f));
        row.addView(makeStatCell(String.valueOf(item.getActive()),         "#E65100", 1f));
        row.addView(makeStatCell(String.valueOf(item.getConverted()),      "#2E7D32", 1f));
        row.addView(makeStatCell(String.valueOf(item.getCancelled()),
                item.getCancelled() > 0 ? "#C62828" : "#888888", 1f));

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

    private TextView makeStatCell(String text, String hexColor, float weight) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hexColor));
        tv.setTextSize(13f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setGravity(android.view.Gravity.CENTER);
        return tv;
    }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}