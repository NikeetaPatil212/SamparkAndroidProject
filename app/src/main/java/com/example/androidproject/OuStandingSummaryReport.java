package com.example.androidproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import com.example.androidproject.model.summary.FeeOutstandingRequest;
import com.example.androidproject.model.summary.FeeOutstandingResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OuStandingSummaryReport extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private TextView tvTotalFees, tvTotalPaid, tvTotalRemaining;
    private TextView tvTotalBadge, tvCourseCount;
    private LinearLayout llTableRows;
    private CardView cardStats, cardTable, cardChart;
    private FeesPieChartView pieChartView;
    private FrameLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ou_standing_summary_report);

        initViews();
        setupBackButton();
        fetchOutstandingSummary();
    }

    private void initViews() {
        tvTotalFees      = findViewById(R.id.tvTotalFees);
        tvTotalPaid      = findViewById(R.id.tvTotalPaid);
        tvTotalRemaining = findViewById(R.id.tvTotalRemaining);
        tvTotalBadge     = findViewById(R.id.tvTotalBadge);
        tvCourseCount    = findViewById(R.id.tvCourseCount);

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

    private void fetchOutstandingSummary() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStats.setVisibility(View.GONE);
        cardTable.setVisibility(View.GONE);
        cardChart.setVisibility(View.GONE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        FeeOutstandingRequest request = new FeeOutstandingRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getOutstandingSummary(request)
                .enqueue(new Callback<FeeOutstandingResponse>() {
                    @Override
                    public void onResponse(Call<FeeOutstandingResponse> call,
                                           Response<FeeOutstandingResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("FEE_SUMMARY_RAW", new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<FeeOutstandingResponse.SummaryItem> list =
                                    response.body().getSummaryList();

                            if (list == null || list.isEmpty()) {
                                Toast.makeText(OuStandingSummaryReport.this,
                                        "No fee data found", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            populateUI(list);

                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Failed to load fee summary";
                            Toast.makeText(OuStandingSummaryReport.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FeeOutstandingResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(OuStandingSummaryReport.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void populateUI(List<FeeOutstandingResponse.SummaryItem> list) {

        // ── Totals ────────────────────────────────────────────────
        double sumTotalFees = 0, sumTotalPaid = 0, sumTotalRemaining = 0;
        for (FeeOutstandingResponse.SummaryItem item : list) {
            sumTotalFees      += item.getTotalFees();
            sumTotalPaid      += item.getTotalPaid();
            sumTotalRemaining += item.getTotalRemaining();
        }

        tvTotalFees.setText(formatAmount(sumTotalFees));
        tvTotalPaid.setText(formatAmount(sumTotalPaid));
        tvTotalRemaining.setText(formatAmount(sumTotalRemaining));
        tvTotalBadge.setText(formatAmount(sumTotalFees) + " Total");
        tvTotalBadge.setVisibility(View.VISIBLE);
        tvCourseCount.setText(list.size() + " Courses");
        cardStats.setVisibility(View.VISIBLE);

        // ── Table rows ────────────────────────────────────────────
        llTableRows.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            llTableRows.addView(buildTableRow(list.get(i), i));
        }
        cardTable.setVisibility(View.VISIBLE);

        // ── Pie chart (overall Paid vs Pending) ──────────────────
        pieChartView.setData(sumTotalPaid, sumTotalRemaining);
        cardChart.setVisibility(View.VISIBLE);
    }

    private View buildTableRow(FeeOutstandingResponse.SummaryItem item, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        row.setBackgroundColor(index % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        TextView tvCourse = new TextView(this);
        tvCourse.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
        tvCourse.setText(item.getCourseName());
        tvCourse.setTextColor(Color.parseColor("#1A1A1A"));
        tvCourse.setTextSize(13f);
        tvCourse.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(tvCourse);
        row.addView(makeStatCell(formatAmount(item.getTotalFees()),      "#1565C0", 1f));
        row.addView(makeStatCell(formatAmount(item.getTotalPaid()),      "#2E7D32", 1f));
        row.addView(makeStatCell(formatAmount(item.getTotalRemaining()),
                item.getTotalRemaining() > 0 ? "#C62828" : "#888888", 1f));
        row.addView(makeStatCell(String.format("%.0f%%", item.getCollectionPercentage()),
                "#6A1B9A", 0.8f));

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
        tv.setTextSize(12.5f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setGravity(android.view.Gravity.CENTER);
        return tv;
    }

    private String formatAmount(double value) {
        long rounded = Math.round(value);
        return String.valueOf(rounded);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}