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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.example.androidproject.adapters.KpiCard;
import com.example.androidproject.adapters.KpiCardAdapter;
import com.example.androidproject.adapters.RecentEntryAdapter;
import com.example.androidproject.model.dashboard.AdmissionLineTrendView;
import com.example.androidproject.model.dashboard.DashboardCardsResponse;
import com.example.androidproject.model.dashboard.DashboardChartsResponse;
import com.example.androidproject.model.dashboard.DashboardGridsResponse;
import com.example.androidproject.model.dashboard.DashboardRequest;
import com.example.androidproject.model.dashboard.FeeDonutChartView;
import com.example.androidproject.model.dashboard.InquiryBarTrendView;
import com.example.androidproject.model.dashboard.RecentEntry;
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

    // ── NEW: Collapsible Recent Activity cards ────────────
    private View cardRecentInquiries, cardRecentAdmissions, cardRecentFees;

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
        setupRecentActivityCards();   // ← NEW
        loadDashboard();
        loadRecentActivity();         // ← NEW
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

        // ── NEW: Recent Activity collapsible cards ──
        cardRecentInquiries  = view.findViewById(R.id.cardRecentInquiries);
        cardRecentAdmissions = view.findViewById(R.id.cardRecentAdmissions);
        cardRecentFees       = view.findViewById(R.id.cardRecentFees);
    }

    // ── KPI Carousel ─────────────────────────────────────
    // ── Paste this into DashboardContentFragment, replacing setupKpiCarousel() ──

    private void setupKpiCarousel() {
        List<KpiCard> cards = new ArrayList<>(Arrays.asList(
                new KpiCard(KpiCard.Type.TOTAL_INQUIRIES,   "📄", "Total Inquiries",   "All inquiries received",     0, 0),
                new KpiCard(KpiCard.Type.PENDING_INQUIRIES, "⏳", "Pending Inquiries", "Awaiting follow-up",         0, 0),
                new KpiCard(KpiCard.Type.TOTAL_ADMISSIONS,  "🎓", "Total Admissions",  "Students enrolled",          0, 0),
                new KpiCard(KpiCard.Type.INQUIRY_ABORTED,   "✕",  "Aborted",           "Dropped inquiries",          0, 0),
                new KpiCard(KpiCard.Type.FEE_COLLECTED,     "₹",  "Fee Collected",     "Total collected this period",0, 0),
                new KpiCard(KpiCard.Type.REFUNDED,          "↩",  "Refunded",          "Total refund amount",        0, 0),
                new KpiCard(KpiCard.Type.NON_REFUNDED,      "🔒", "Non-Refunded",      "Secured revenue",            0, 0),
                new KpiCard(KpiCard.Type.EXPENSES,          "📉", "Expenses",          "Operational costs",          0, 0)
        ));

        kpiAdapter = new KpiCardAdapter(cards, type ->
                Toast.makeText(requireContext(), type.name(), Toast.LENGTH_SHORT).show());

        // ✅ KEY: horizontal LinearLayoutManager + PagerSnapHelper = 1 card per swipe
        LinearLayoutManager llm = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvKpiCards.setLayoutManager(llm);

        // ✅ Each card uses match_parent width in its XML (margin 16dp each side)
        // so exactly ONE card is visible at a time
        rvKpiCards.setAdapter(kpiAdapter);
        rvKpiCards.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Snap: one full card per swipe
        new PagerSnapHelper().attachToRecyclerView(rvKpiCards);

        // Build dots
        buildDots(cards.size());

        // Update dots as user scrolls
        rvKpiCards.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView r, int dx, int dy) {
                // Find the currently fully visible item
                int first = llm.findFirstCompletelyVisibleItemPosition();
                if (first >= 0) updateDots(first);
            }
        });
    }

    private void buildDots(int count) {
        dotsIndicator.removeAllViews();
        dots = new ImageView[count];
        float density = getResources().getDisplayMetrics().density;
        int size   = (int) (8  * density);
        int margin = (int) (5  * density);

        for (int i = 0; i < count; i++) {
            ImageView d = new ImageView(getContext());
            d.setImageResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, 0, margin, 0);
            dotsIndicator.addView(d, lp);
            dots[i] = d;
        }
    }

    private void updateDots(int activePage) {
        if (dots == null) return;
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(
                    i == activePage ? R.drawable.dot_active : R.drawable.dot_inactive);
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

    // ── NEW: Recent Activity — collapsible cards setup ────
    private void setupRecentActivityCards() {
        setupCollapsibleCard(cardRecentInquiries,  "📋", "Recent Inquiries");
        setupCollapsibleCard(cardRecentAdmissions, "🎓", "Recent Admissions");
        setupCollapsibleCard(cardRecentFees,       "💰", "Recent Fees Collected");
    }

    private void setupCollapsibleCard(View cardView, String icon, String title) {
        if (cardView == null) return;

        TextView tvIcon  = cardView.findViewById(R.id.tvCardIcon);
        TextView tvTitle = cardView.findViewById(R.id.tvCardTitle);
        if (tvIcon != null)  tvIcon.setText(icon);
        if (tvTitle != null) tvTitle.setText(title);

        View header          = cardView.findViewById(R.id.headerRow);
        LinearLayout content  = cardView.findViewById(R.id.contentContainer);
        ImageView chevron     = cardView.findViewById(R.id.ivChevron);

        if (header != null) {
            header.setOnClickListener(v -> toggleCollapsibleCard(cardView, content, chevron));
        }
    }

    private void toggleCollapsibleCard(View cardView, LinearLayout content, ImageView chevron) {
        if (content == null) return;

        ViewGroup parent = (ViewGroup) cardView.getParent();
        if (parent != null) {
            TransitionManager.beginDelayedTransition(parent, new AutoTransition());
        }

        boolean expanding = content.getVisibility() != View.VISIBLE;
        content.setVisibility(expanding ? View.VISIBLE : View.GONE);
        if (chevron != null) {
            chevron.animate().rotation(expanding ? 180f : 0f).setDuration(200).start();
        }
    }

    // ── NEW: Recent Activity — fetch from DashboardGrids API ──
    private void loadRecentActivity() {
        if (getContext() == null) return;

        String userId      = PrefManager.getInstance(requireContext()).getUserId();
        String instituteId = PrefManager.getInstance(requireContext()).getInstituteId();

        DashboardRequest request = new DashboardRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getDashboardGrids(request)
                .enqueue(new retrofit2.Callback<DashboardGridsResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<DashboardGridsResponse> call,
                                           retrofit2.Response<DashboardGridsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            bindRecentActivity(response.body());
                        } else {
                            toast(response.body() != null
                                    ? response.body().getMessage()
                                    : "Failed to load recent activity");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<DashboardGridsResponse> call, Throwable t) {
                        if (isAdded()) toast("Network error: " + t.getMessage());
                    }
                });
    }

    private void bindRecentActivity(DashboardGridsResponse body) {
        bindCollapsibleCardData(cardRecentInquiries,  body.getRecentInquiries(),  false);
        bindCollapsibleCardData(cardRecentAdmissions, body.getRecentAdmissions(), false);
        bindCollapsibleCardData(cardRecentFees,       body.getRecentFees(),       true);
    }

    private void bindCollapsibleCardData(View cardView, List<RecentEntry> items, boolean showAmount) {
        if (cardView == null) return;

        RecyclerView rv   = cardView.findViewById(R.id.rvCardItems);
        TextView tvCount  = cardView.findViewById(R.id.tvCardCount);
        TextView tvEmpty  = cardView.findViewById(R.id.tvEmptyState);

        List<RecentEntry> safeItems = items == null ? Collections.emptyList() : items;
        if (tvCount != null) tvCount.setText(String.valueOf(safeItems.size()));

        if (safeItems.isEmpty()) {
            if (rv != null)      rv.setVisibility(View.GONE);
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (rv != null) {
                rv.setVisibility(View.VISIBLE);
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                rv.setAdapter(new RecentEntryAdapter(safeItems, showAmount));
            }
        }
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