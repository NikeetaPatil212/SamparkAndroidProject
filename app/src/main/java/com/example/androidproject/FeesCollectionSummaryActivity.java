package com.example.androidproject;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidproject.model.summary.CollectionSummaryRequest;
import com.example.androidproject.model.summary.CollectionSummaryResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeesCollectionSummaryActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private TextView chipLast7, chipLast15, chipThisMonth, chipLastMonth;
    private TextView tvTotalCollected, tvReceiptCount, tvAvgPerDay;
    private TextView tvTotalBadge, tvDayCount;
    private TextView tvFooterReceipts, tvFooterAmount;
    private LinearLayout llTableRows, llTableFooter;
    private CardView cardStats, cardTable, cardChart;
    private FeesCollectionPieChartView pieChartView;
    private FrameLayout loaderLayout;

    // ── No filter param in this API — always fetches all, we filter client-side ──
    private String currentFilter = "Last Seven Days";
    private List<CollectionSummaryResponse.SummaryItem> allData = new ArrayList<>();

    private static final int COLOR_SELECTED_BG    = Color.parseColor("#2E7D32");
    private static final int COLOR_UNSELECTED_BG  = Color.parseColor("#E8F5E9");
    private static final int COLOR_SELECTED_TEXT  = Color.WHITE;
    private static final int COLOR_UNSELECTED_TEXT= Color.parseColor("#2E7D32");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fees_collection_summary);

        initViews();
        setupBackButton();
        setupFilterChips();
        fetchSummary();  // single API call — filter applied client-side
    }

    // ── Init ──────────────────────────────────────────────────────
    private void initViews() {
        chipLast7      = findViewById(R.id.chipLast7);
        chipLast15     = findViewById(R.id.chipLast15);
        chipThisMonth  = findViewById(R.id.chipThisMonth);
        chipLastMonth  = findViewById(R.id.chipLastMonth);

        tvTotalCollected = findViewById(R.id.tvTotalCollected);
        tvReceiptCount   = findViewById(R.id.tvReceiptCount);
        tvAvgPerDay      = findViewById(R.id.tvAvgPerDay);
        tvTotalBadge     = findViewById(R.id.tvTotalBadge);
        tvDayCount       = findViewById(R.id.tvDayCount);
        tvFooterReceipts = findViewById(R.id.tvFooterReceipts);
        tvFooterAmount   = findViewById(R.id.tvFooterAmount);

        llTableRows   = findViewById(R.id.llTableRows);
        llTableFooter = findViewById(R.id.llTableFooter);
        cardStats     = findViewById(R.id.cardStats);
        cardTable     = findViewById(R.id.cardTable);
        cardChart     = findViewById(R.id.cardChart);
        pieChartView  = findViewById(R.id.barChartView);
        loaderLayout  = findViewById(R.id.loaderLayout);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Filter Chips ──────────────────────────────────────────────
    private void setupFilterChips() {
        chipLast7.setOnClickListener(v     -> selectFilter("Last Seven Days"));
        chipLast15.setOnClickListener(v    -> selectFilter("Last 15 Days"));
        chipThisMonth.setOnClickListener(v -> selectFilter("This Month"));
        chipLastMonth.setOnClickListener(v -> selectFilter("Last Month"));
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        updateChipUI();
        // Filter already-fetched data client-side — no repeat API call needed
        if (!allData.isEmpty()) {
            populateUI(filterData(allData));
        }
    }

    private void updateChipUI() {
        setChipState(chipLast7,     currentFilter.equals("Last Seven Days"));
        setChipState(chipLast15,    currentFilter.equals("Last 15 Days"));
        setChipState(chipThisMonth, currentFilter.equals("This Month"));
        setChipState(chipLastMonth, currentFilter.equals("Last Month"));
    }

    private void setChipState(TextView chip, boolean selected) {
        chip.setBackgroundColor(selected ? COLOR_SELECTED_BG  : COLOR_UNSELECTED_BG);
        chip.setTextColor(      selected ? COLOR_SELECTED_TEXT : COLOR_UNSELECTED_TEXT);
    }

    // ── API ───────────────────────────────────────────────────────
    private void fetchSummary() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStats.setVisibility(View.GONE);
        cardTable.setVisibility(View.GONE);
        cardChart.setVisibility(View.GONE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        CollectionSummaryRequest request = new CollectionSummaryRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId));

        Log.d("COLLECTION_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().getCollectionSummary(request)
                .enqueue(new Callback<CollectionSummaryResponse>() {

                    @Override
                    public void onResponse(Call<CollectionSummaryResponse> call,
                                           Response<CollectionSummaryResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("COLLECTION_RAW", new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<CollectionSummaryResponse.SummaryItem> list =
                                    response.body().getSummaryList();

                            if (list == null || list.isEmpty()) {
                                Toast.makeText(FeesCollectionSummaryActivity.this,
                                        "No data found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            allData = list;  // cache full list
                            populateUI(filterData(allData));

                        } else {
                            String msg = (response.body() != null
                                    && response.body().getMessage() != null)
                                    ? response.body().getMessage()
                                    : "Failed to load summary";
                            Toast.makeText(FeesCollectionSummaryActivity.this,
                                    msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CollectionSummaryResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(FeesCollectionSummaryActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("COLLECTION_ERR", t.getMessage(), t);
                    }
                });
    }

    // ── Client-side filter by date ─────────────────────────────────
    private List<CollectionSummaryResponse.SummaryItem> filterData(
            List<CollectionSummaryResponse.SummaryItem> full) {

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());

        // Determine cutoff date based on chip
        java.util.Calendar cutoff = java.util.Calendar.getInstance();

        switch (currentFilter) {
            case "Last Seven Days":
                cutoff.add(java.util.Calendar.DAY_OF_YEAR, -7);
                break;
            case "Last 15 Days":
                cutoff.add(java.util.Calendar.DAY_OF_YEAR, -15);
                break;
            case "This Month":
                cutoff.set(java.util.Calendar.DAY_OF_MONTH, 1);
                break;
            case "Last Month":
                cutoff.add(java.util.Calendar.MONTH, -1);
                cutoff.set(java.util.Calendar.DAY_OF_MONTH, 1);
                // For last month, also set an upper bound
                java.util.Calendar upperBound = java.util.Calendar.getInstance();
                upperBound.set(java.util.Calendar.DAY_OF_MONTH, 1);
                upperBound.add(java.util.Calendar.DAY_OF_YEAR, -1);

                List<CollectionSummaryResponse.SummaryItem> lastMonthResult = new ArrayList<>();
                for (CollectionSummaryResponse.SummaryItem item : full) {
                    try {
                        java.util.Date d = sdf.parse(item.getReceiptDate());
                        if (d != null && !d.before(cutoff.getTime())
                                && !d.after(upperBound.getTime())) {
                            lastMonthResult.add(item);
                        }
                    } catch (Exception ignored) {}
                }
                return lastMonthResult;
        }

        // For all other filters: date >= cutoff
        List<CollectionSummaryResponse.SummaryItem> result = new ArrayList<>();
        for (CollectionSummaryResponse.SummaryItem item : full) {
            try {
                java.util.Date d = sdf.parse(item.getReceiptDate());
                if (d != null && !d.before(cutoff.getTime())) {
                    result.add(item);
                }
            } catch (Exception ignored) {}
        }
        return result;
    }

    // ── Populate UI ───────────────────────────────────────────────
    private void populateUI(List<CollectionSummaryResponse.SummaryItem> list) {

        if (list == null || list.isEmpty()) {
            cardStats.setVisibility(View.GONE);
            cardTable.setVisibility(View.GONE);
            cardChart.setVisibility(View.GONE);
            Toast.makeText(this, "No data for selected period", Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Totals ────────────────────────────────────────────────
        double sumAmount  = 0;
        int    sumReceipts = 0;
        for (CollectionSummaryResponse.SummaryItem item : list) {
            sumAmount   += item.getTotalAmount();
            sumReceipts += item.getReceiptCount();
        }
        double avgPerDay = list.size() > 0 ? sumAmount / list.size() : 0;

        tvTotalCollected.setText("₹" + formatAmount((long) sumAmount));
        tvReceiptCount.setText(String.valueOf(sumReceipts));
        tvAvgPerDay.setText("₹" + formatAmount((long) avgPerDay));

        tvTotalBadge.setText("₹" + formatAmount((long) sumAmount));
        tvTotalBadge.setVisibility(View.VISIBLE);

        tvDayCount.setText(list.size() + " Days");

        cardStats.setVisibility(View.VISIBLE);

        // ── Table rows ────────────────────────────────────────────
        llTableRows.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            llTableRows.addView(buildTableRow(list.get(i), i));
        }

        // Footer totals
        tvFooterReceipts.setText(String.valueOf(sumReceipts));
        tvFooterAmount.setText("₹" + formatAmount((long) sumAmount));
        llTableFooter.setVisibility(View.VISIBLE);

        cardTable.setVisibility(View.VISIBLE);

        // ── Pie Chart ─────────────────────────────────────────────
        List<FeesCollectionPieChartView.PieEntry> chartData = new ArrayList<>();
        for (CollectionSummaryResponse.SummaryItem item : list) {
            if (item.getTotalAmount() > 0) {
                chartData.add(new FeesCollectionPieChartView.PieEntry(
                        item.getReceiptDate(),
                        item.getTotalAmount()
                ));
            }
        }
        pieChartView.setData(chartData);
        cardChart.setVisibility(View.VISIBLE);
    }

    // ── Table row ─────────────────────────────────────────────────
    private View buildTableRow(CollectionSummaryResponse.SummaryItem item, int index) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(13), dp(12), dp(13));
        row.setBackgroundColor(index % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        // Date
        TextView tvDate = new TextView(this);
        tvDate.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
        tvDate.setText(formatDisplayDate(item.getReceiptDate()));
        tvDate.setTextColor(Color.parseColor("#1A1A1A"));
        tvDate.setTextSize(13f);
        tvDate.setTypeface(null, Typeface.BOLD);

        // Receipts count
        TextView tvReceipts = makeCell(
                String.valueOf(item.getReceiptCount()), "#2E7D32", 1f);

        // Amount
        TextView tvAmount = makeCell(
                "₹" + formatAmount((long) item.getTotalAmount()), "#1565C0", 1.5f);
        tvAmount.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        row.addView(tvDate);
        row.addView(tvReceipts);
        row.addView(tvAmount);

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
        tv.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hexColor));
        tv.setTextSize(13f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    // ── Helpers ───────────────────────────────────────────────────

    // "2026-03-18" → "18 Mar 2026"
    private String formatDisplayDate(String iso) {
        try {
            String[] parts  = iso.split("-");
            String[] months = {"","Jan","Feb","Mar","Apr","May","Jun",
                    "Jul","Aug","Sep","Oct","Nov","Dec"};
            int m = Integer.parseInt(parts[1]);
            return parts[2] + " " + months[m] + " " + parts[0];
        } catch (Exception e) {
            return iso;
        }
    }

    // 170674 → "1.7L", 11000 → "11K", 500 → "500"
    private String formatAmount(long val) {
        if (val >= 100000) return String.format(Locale.getDefault(), "%.1fL", val / 100000.0);
        if (val >= 1000)   return String.format(Locale.getDefault(), "%.0fK", val / 1000.0);
        return String.valueOf(val);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}