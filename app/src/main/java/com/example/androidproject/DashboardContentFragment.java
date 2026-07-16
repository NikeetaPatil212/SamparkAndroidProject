package com.example.androidproject;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.KpiCard;
import com.example.androidproject.adapters.KpiCardAdapter;
import com.example.androidproject.model.dashboard.AdmissionLineTrendView;
import com.example.androidproject.model.dashboard.DashboardCardsResponse;
import com.example.androidproject.model.dashboard.DashboardChartsResponse;
import com.example.androidproject.model.dashboard.DashboardRequest;
import com.example.androidproject.model.dashboard.FeeDonutChartView;
import com.example.androidproject.model.dashboard.InquiryBarTrendView;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardContentFragment extends Fragment {

    // ── Views ────────────────────────────────────────────
    private TextView tvGreeting;
    private FrameLayout btnNotifications;

    private RecyclerView rvKpiCards;
    private LinearLayout dotsIndicator;
    private KpiCardAdapter kpiAdapter;
    private ImageView[] dots;

    private FeeDonutChartView      feeDonutChartView;
    private InquiryBarTrendView    inquiryBarTrendView;
    private AdmissionLineTrendView admissionLineTrendView;
    private FrameLayout            loaderLayout;

    Toolbar toolbar;
    private int pendingCalls = 0;

    private static final SimpleDateFormat MONTH_KEY_FORMAT =
            new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat MONTH_LABEL_FORMAT =
            new SimpleDateFormat("MMM yyyy", Locale.getDefault());

    // 8 cards → 2 rows × 4 columns; each "page" shows 2 cards (1 col of 2 rows)
    // so 4 pages total. Adjust CARDS_PER_PAGE if you change span/orientation.
    private static final int PAGE_COUNT = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupKpiCarousel();
        setupGreeting();
        loadDashboard();
    }

    // ── Init ─────────────────────────────────────────────
    private void initViews(View view) {
        tvGreeting       = view.findViewById(R.id.tvGreeting);
     //   btnNotifications = view.findViewById(R.id.btnNotifications);
        rvKpiCards       = view.findViewById(R.id.rvKpiCards);
        dotsIndicator    = view.findViewById(R.id.dotsIndicator);

        feeDonutChartView      = view.findViewById(R.id.feeDonutChartView);
        inquiryBarTrendView    = view.findViewById(R.id.inquiryBarTrendView);
        admissionLineTrendView = view.findViewById(R.id.admissionLineTrendView);
        loaderLayout           = view.findViewById(R.id.loaderLayout);


    }

    // ── KPI Carousel ─────────────────────────────────────
    private void setupKpiCarousel() {
        // Card order shown when user swipes L → R
        List<KpiCard> cards = new ArrayList<>(Arrays.asList(
                new KpiCard(KpiCard.Type.TOTAL_INQUIRIES,   "📄", "Total Inquiries",
                        "All inquiries",     Color.parseColor("#1565C0"), Color.parseColor("#1565C0")),
                new KpiCard(KpiCard.Type.PENDING_INQUIRIES, "⏳", "Pending",
                        "Awaiting response", Color.parseColor("#F57C00"), Color.parseColor("#F57C00")),
                new KpiCard(KpiCard.Type.TOTAL_ADMISSIONS,  "🎓", "Admissions",
                        "Enrolled",          Color.parseColor("#2E7D32"), Color.parseColor("#2E7D32")),
                new KpiCard(KpiCard.Type.INQUIRY_ABORTED,   "✕",  "Aborted",
                        "Dropped inquiries", Color.parseColor("#E53935"), Color.parseColor("#E53935")),
                new KpiCard(KpiCard.Type.FEE_COLLECTED,     "₹",  "Fee Collected",
                        "This period",       Color.parseColor("#2E7D32"), Color.parseColor("#2E7D32")),
                new KpiCard(KpiCard.Type.REFUNDED,          "↩",  "Refunded",
                        "Total refunds",     Color.parseColor("#E53935"), Color.parseColor("#E53935")),
                new KpiCard(KpiCard.Type.NON_REFUNDED,      "🔒", "Non-Refunded",
                        "Locked in",         Color.parseColor("#1565C0"), Color.parseColor("#1565C0")),
                new KpiCard(KpiCard.Type.EXPENSES,          "📉", "Expenses",
                        "Operational",       Color.parseColor("#FB8C00"), Color.parseColor("#FB8C00"))
        ));

        kpiAdapter = new KpiCardAdapter(cards, type -> {
            // TODO: navigate to detail screen based on type
            Toast.makeText(requireContext(), type.name(), Toast.LENGTH_SHORT).show();
        });

        // 2 rows, horizontal scroll
        GridLayoutManager glm = new GridLayoutManager(
                getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        rvKpiCards.setLayoutManager(glm);
        rvKpiCards.setAdapter(kpiAdapter);

        // Snap so cards land nicely
        new PagerSnapHelper().attachToRecyclerView(rvKpiCards);

        buildDots();

        rvKpiCards.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView r, int dx, int dy) {
                int offset = r.computeHorizontalScrollOffset();
                int range  = r.computeHorizontalScrollRange()
                        - r.computeHorizontalScrollExtent();
                if (range <= 0) return;
                float ratio = (float) offset / range;      // 0.0 → 1.0
                int page = Math.round(ratio * (PAGE_COUNT - 1));
                page = Math.max(0, Math.min(page, PAGE_COUNT - 1));
                updateDots(page);
            }
        });
    }

    private void buildDots() {
        dotsIndicator.removeAllViews();
        dots = new ImageView[PAGE_COUNT];
        int size = (int) (8 * getResources().getDisplayMetrics().density);
        int margin = (int) (5 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < PAGE_COUNT; i++) {
            ImageView d = new ImageView(getContext());
            d.setImageResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, 0, margin, 0);
            dotsIndicator.addView(d, lp);
            dots[i] = d;
        }
    }

    private void updateDots(int activePage) {
        if (dots == null) return;
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == activePage
                    ? R.drawable.dot_active
                    : R.drawable.dot_inactive);
        }
    }

    // ── Greeting ─────────────────────────────────────────
    private void setupGreeting() {
        if (getContext() == null || tvGreeting == null) return;

        String name = PrefManager.getInstance(requireContext()).getOwnerName();
        if (name == null || name.trim().isEmpty()) name = "there";
        tvGreeting.setText("👋 " + getTimeGreeting() + ", " + name);

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    Toast.makeText(requireContext(),
                            "No new notifications", Toast.LENGTH_SHORT).show());
        }
    }

    private String getTimeGreeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h < 12) return "Good Morning";
        if (h < 17) return "Good Afternoon";
        return "Good Evening";
    }

    // ── Load ─────────────────────────────────────────────
    private void loadDashboard() {
        if (getContext() == null) return;
        loaderLayout.setVisibility(View.VISIBLE);
        pendingCalls = 2;

        String userId      = PrefManager.getInstance(requireContext()).getUserId();
        String instituteId = PrefManager.getInstance(requireContext()).getInstituteId();

        DashboardRequest request = new DashboardRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        fetchCards(request);
        fetchCharts(request);
    }

    private void fetchCards(DashboardRequest request) {
        RetrofitClient.getApiService().getDashboardCards(request)
                .enqueue(new retrofit2.Callback<DashboardCardsResponse>() {
                    @Override public void onResponse(retrofit2.Call<DashboardCardsResponse> call,
                                                     retrofit2.Response<DashboardCardsResponse> response) {
                        onCallFinished();
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            populateCards(response.body().getDashboard());
                        } else {
                            toast(response.body() != null
                                    ? response.body().getMessage()
                                    : "Failed to load cards");
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<DashboardCardsResponse> call, Throwable t) {
                        onCallFinished();
                        if (isAdded()) toast("Network error: " + t.getMessage());
                    }
                });
    }

    private void fetchCharts(DashboardRequest request) {
        RetrofitClient.getApiService().getDashboardCharts(request)
                .enqueue(new retrofit2.Callback<DashboardChartsResponse>() {
                    @Override public void onResponse(retrofit2.Call<DashboardChartsResponse> call,
                                                     retrofit2.Response<DashboardChartsResponse> response) {
                        onCallFinished();
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            populateFeeSummary(response.body().getFeeSummary());
                            populateMonthlyTrends(response.body().getMonthlySummary());
                        } else {
                            toast(response.body() != null
                                    ? response.body().getMessage()
                                    : "Failed to load charts");
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<DashboardChartsResponse> call, Throwable t) {
                        onCallFinished();
                        if (isAdded()) toast("Network error: " + t.getMessage());
                    }
                });
    }

    private void onCallFinished() {
        pendingCalls--;
        if (pendingCalls <= 0 && loaderLayout != null)
            loaderLayout.setVisibility(View.GONE);
    }

    // ── Populate: push values into carousel ──────────────
    private void populateCards(DashboardCardsResponse.DashboardCards d) {
        if (d == null || kpiAdapter == null) return;

        kpiAdapter.updateValue(KpiCard.Type.TOTAL_INQUIRIES,   String.valueOf(d.getTotalInquiries()));
        kpiAdapter.updateValue(KpiCard.Type.PENDING_INQUIRIES, String.valueOf(d.getPendingInquiries()));
        kpiAdapter.updateValue(KpiCard.Type.INQUIRY_ABORTED,   String.valueOf(d.getInquiryAborted()));
        kpiAdapter.updateValue(KpiCard.Type.TOTAL_ADMISSIONS,  String.valueOf(d.getTotalAdmissions()));

        kpiAdapter.updateValue(KpiCard.Type.FEE_COLLECTED, "₹" + formatAmount((long) d.getTotalFeeCollected()));
        kpiAdapter.updateValue(KpiCard.Type.REFUNDED,      "₹" + formatAmount((long) d.getTotalRefunded()));
        kpiAdapter.updateValue(KpiCard.Type.NON_REFUNDED,  "₹" + formatAmount((long) d.getNonRefunded()));
        kpiAdapter.updateValue(KpiCard.Type.EXPENSES,      "₹" + formatAmount((long) d.getTotalExpenses()));
    }

    // ── Populate: Fee donut ──────────────────────────────
    private void populateFeeSummary(List<DashboardChartsResponse.FeeSummaryItem> feeSummary) {
        if (feeSummary == null || feeSummary.isEmpty()) return;

        double totalFee = 0, paidFee = 0, remainingFee = 0, waivedFee = 0;
        for (DashboardChartsResponse.FeeSummaryItem item : feeSummary) {
            String cat = item.getCategory() != null ? item.getCategory() : "";
            switch (cat) {
                case "Total Fee":           totalFee     = item.getAmount(); break;
                case "Paid Fee":            paidFee      = item.getAmount(); break;
                case "Remaining Fee":       remainingFee = item.getAmount(); break;
                case "Waived/Refunded Fee": waivedFee    = item.getAmount(); break;
            }
        }

        // ★★ Always add all 3 slices — legend shows even ₹0 categories ★★
        List<FeeDonutChartView.Slice> slices = new ArrayList<>();
        slices.add(new FeeDonutChartView.Slice(
                "Paid Fee",      paidFee,      Color.parseColor("#43A047")));   // green
        slices.add(new FeeDonutChartView.Slice(
                "Remaining Fee", remainingFee, Color.parseColor("#FB8C00")));   // orange
        slices.add(new FeeDonutChartView.Slice(
                "Waived Fee",    waivedFee,    Color.parseColor("#FDD835")));   // yellow

        feeDonutChartView.setData(slices, "Total Fee", totalFee);
        feeDonutChartView.setShowSliceLabels(false);   // ← hides % on donut
    }


    // ── Populate: Monthly trends ─────────────────────────
    private void populateMonthlyTrends(List<DashboardChartsResponse.MonthlySummaryItem> monthly) {
        if (monthly == null || monthly.isEmpty()) return;

        List<DashboardChartsResponse.MonthlySummaryItem> sorted = new ArrayList<>(monthly);
        Collections.sort(sorted, Comparator.comparing(
                DashboardChartsResponse.MonthlySummaryItem::getMonthYear));

        List<InquiryBarTrendView.Entry>    barData  = new ArrayList<>();
        List<AdmissionLineTrendView.Entry> lineData = new ArrayList<>();

        for (DashboardChartsResponse.MonthlySummaryItem item : sorted) {
            String label = formatMonthLabel(item.getMonthYear());
            barData.add(new InquiryBarTrendView.Entry(label, item.getInquiries()));
            lineData.add(new AdmissionLineTrendView.Entry(label, item.getAdmissions()));
        }

        inquiryBarTrendView.setData(barData);
        admissionLineTrendView.setData(lineData);
    }

    // ── Helpers ──────────────────────────────────────────
    private String formatMonthLabel(String monthYear) {
        if (monthYear == null) return "";
        try {
            return MONTH_LABEL_FORMAT.format(MONTH_KEY_FORMAT.parse(monthYear));
        } catch (ParseException e) {
            return monthYear;
        }
    }

    private String formatAmount(long val) {
        if (val >= 100000) return String.format(Locale.getDefault(), "%.1fL", val / 100000.0);
        if (val >= 1000)   return String.format(Locale.getDefault(), "%.1fK", val / 1000.0);
        return String.valueOf(val);
    }

    private void toast(String msg) {
        if (getContext() != null)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}