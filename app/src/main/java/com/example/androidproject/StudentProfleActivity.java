package com.example.androidproject;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.androidproject.model.profile.ProfileDetailsRequest;
import com.example.androidproject.model.profile.ProfileDetailsResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentProfleActivity extends AppCompatActivity {

    // Base URL constant for relative image paths
    private static final String BASE_URL = "http://160.187.87.113:8081/";

    // ── Toolbar ────────────────────────────────────────────────────────────────
    private ImageButton btnBack;
    private FrameLayout loaderLayout;

    // ── Admission Summary ──────────────────────────────────────────────────────
    private TextView tvCourse, tvBatch, tvFee, tvOutstanding, tvDueDate;

    // ── Personal Details ───────────────────────────────────────────────────────
    private ImageView ivStudentPhoto;
    private TextView tvFirstName, tvMiddleName, tvLastName, tvFullName;
    private TextView tvMobile, tvAlternate, tvAddress;

    // ── Dynamic transaction container ──────────────────────────────────────────
    private LinearLayout llTransactions;

    // ── Expand / collapse — card bodies ───────────────────────────────────────
    private LinearLayout bodyAdmissionSummary;
    private LinearLayout bodyPersonalDetails;
    private LinearLayout bodyTransactions;

    // ── Expand / collapse — arrow icons ───────────────────────────────────────
    private ImageView ivArrowAdmission;
    private ImageView ivArrowPersonal;
    private ImageView ivArrowTransactions;

    // ── Expand states (all start expanded = true) ──────────────────────────────
    private boolean isAdmissionExpanded    = true;
    private boolean isPersonalExpanded     = true;
    private boolean isTransactionExpanded  = false;

    // ── Intent extras ─────────────────────────────────────────────────────────
    private int studentId, admissionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profle);

        studentId   = getIntent().getIntExtra("studentId",   -1);
        admissionId = getIntent().getIntExtra("admissionId", -1);

        Log.d("studentId", "onCreate: " + studentId + "--" + admissionId);

        initViews();
        setupToolbar();
        setupExpandCollapse();   // ← wire the toggle listeners
        fetchProfileDetails();
    }

    // ── Bind views ─────────────────────────────────────────────────────────────
    private void initViews() {
        btnBack      = findViewById(R.id.btnBack);
        loaderLayout = findViewById(R.id.loaderLayout);

        // Admission Summary fields
        tvCourse      = findViewById(R.id.tvCourse);
        tvBatch       = findViewById(R.id.tvBatch);
        tvFee         = findViewById(R.id.tvFee);
        tvOutstanding = findViewById(R.id.tvOutstanding);
        tvDueDate     = findViewById(R.id.tvDueDate);

        // Personal Details fields
        ivStudentPhoto = findViewById(R.id.ivStudentPhoto);
        tvFirstName    = findViewById(R.id.tvFirstName);
        tvMiddleName   = findViewById(R.id.tvMiddleName);
        tvLastName     = findViewById(R.id.tvLastName);
        tvFullName     = findViewById(R.id.tvFullName);
        tvMobile       = findViewById(R.id.tvMobile);
        tvAlternate    = findViewById(R.id.tvAlternate);
        tvAddress      = findViewById(R.id.tvAddress);

        // Transaction container
        llTransactions = findViewById(R.id.llTransactions);

        // Collapsible bodies + arrows
        bodyAdmissionSummary = findViewById(R.id.bodyAdmissionSummary);
        bodyPersonalDetails  = findViewById(R.id.bodyPersonalDetails);
        bodyTransactions     = findViewById(R.id.bodyTransactions);

        ivArrowAdmission    = findViewById(R.id.ivArrowAdmission);
        ivArrowPersonal     = findViewById(R.id.ivArrowPersonal);
        ivArrowTransactions = findViewById(R.id.ivArrowTransactions);

        bodyTransactions.setVisibility(View.GONE);
        ivArrowTransactions.setRotation(-90f);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> finish());
    }

    // ── Expand / Collapse setup ────────────────────────────────────────────────
    private void setupExpandCollapse() {

        // Card 1 — Admission Summary
        findViewById(R.id.headerAdmissionSummary).setOnClickListener(v -> {
            isAdmissionExpanded = !isAdmissionExpanded;
            toggleSection(bodyAdmissionSummary, ivArrowAdmission, isAdmissionExpanded);
        });

        // Card 2 — Personal Details
        findViewById(R.id.headerPersonalDetails).setOnClickListener(v -> {
            isPersonalExpanded = !isPersonalExpanded;
            toggleSection(bodyPersonalDetails, ivArrowPersonal, isPersonalExpanded);
        });

        // Card 3 — Transaction History
        findViewById(R.id.headerTransactions).setOnClickListener(v -> {
            isTransactionExpanded = !isTransactionExpanded;
            toggleSection(bodyTransactions, ivArrowTransactions, isTransactionExpanded);
        });
    }

    private void toggleSection(LinearLayout body, ImageView arrow, boolean expand) {
        if (expand) {
            body.setVisibility(View.VISIBLE);
        } else {
            body.setVisibility(View.GONE);
        }

        float fromDeg = expand ? -90f :   0f;
        float toDeg   = expand ?   0f : -90f;
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(arrow, "rotation", fromDeg, toDeg);
        rotateAnim.setDuration(200);
        rotateAnim.start();
    }

    // ── API Call ───────────────────────────────────────────────────────────────
    private void fetchProfileDetails() {
        loaderLayout.setVisibility(View.VISIBLE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        ProfileDetailsRequest request = new ProfileDetailsRequest(
                studentId,
                admissionId,
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        Log.d("PROFILE_REQUEST", new Gson().toJson(request));

        RetrofitClient.getApiService().getProfileDetails(request)
                .enqueue(new Callback<ProfileDetailsResponse>() {
                    @Override
                    public void onResponse(Call<ProfileDetailsResponse> call,
                                           Response<ProfileDetailsResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            ProfileDetailsResponse res = response.body();
                            if (res.isSuccess()) {
                                populateUI(res);
                            } else {
                                Toast.makeText(StudentProfleActivity.this,
                                        res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(StudentProfleActivity.this,
                                    "Server error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileDetailsResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(StudentProfleActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("PROFILE_ERROR", t.getMessage());
                    }
                });
    }

    // ── Populate all UI ────────────────────────────────────────────────────────
    private void populateUI(ProfileDetailsResponse res) {

        // ── Admission Summary ──────────────────────────────────────────────────
        ProfileDetailsResponse.DetailsOfAdmission detail = res.getDetailsOfAdmission();
        if (detail != null) {
            tvCourse.setText(safe(detail.getCourse()));
            tvBatch.setText(safe(detail.getBatch()));
            tvFee.setText(String.valueOf(detail.getFees()));
            tvOutstanding.setText(String.valueOf(detail.getOutstanding()));
            tvDueDate.setText(safe(detail.getAdmissionDate()));
        }

        // ── Profile ────────────────────────────────────────────────────────────
        ProfileDetailsResponse.Profile profile = res.getProfile();
        if (profile != null) {
            String fullName = safe(profile.getFull_Name());
            tvFullName.setText(fullName);

            // Split full name into first / middle / last
            String[] parts = fullName.split(" ", 3);
            tvFirstName.setText(parts.length > 0 ? parts[0] : "—");
            tvMiddleName.setText(parts.length > 2 ? parts[1] : "—");
            tvLastName.setText(parts.length > 2 ? parts[2] : (parts.length > 1 ? parts[1] : "—"));

            tvMobile.setText(safe(profile.getMobile()));
            tvAlternate.setText(safe(profile.getAlternateNo()));
            tvAddress.setText(safe(profile.getAddress()));

            // ── Load profile photo ─────────────────────────────────────────────
            // ── Load profile photo ─────────────────────────────────────────────
            String imgUrl = profile.getImgurl();

            if (imgUrl == null || imgUrl.trim().isEmpty()) {
                imgUrl = PrefManager.getInstance(this).getProfileImage();
            }

            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                imgUrl = imgUrl.trim();

                if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                    imgUrl = BASE_URL + (imgUrl.startsWith("/") ? imgUrl.substring(1) : imgUrl);
                }

                Log.d("IMAGE_LOADING_DEBUG", "Final Image URL: " + imgUrl);

                // Make sure the view and its container are visible
                bodyPersonalDetails.setVisibility(View.VISIBLE);
                ivStudentPhoto.setVisibility(View.VISIBLE);

                Glide.with(getApplicationContext()) // Use Application Context to prevent lifecycle cancels
                        .load(imgUrl)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .error(R.drawable.baseline_account_circle_24)
                        .dontTransform() // Disable transformations temporarily to test
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                Log.e("IMAGE_LOADING_DEBUG", "Failed to load image: " + model, e);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource dataSource,
                                                           boolean isFirstResource) {
                                Log.d("IMAGE_LOADING_DEBUG", "Image loaded successfully!");
                                ivStudentPhoto.post(() -> ivStudentPhoto.invalidate()); // Force UI redraw
                                return false;
                            }
                        })
                        .into(ivStudentPhoto);
            } else {
                Log.d("IMAGE_LOADING_DEBUG", "No image URL available. Showing default placeholder.");
                ivStudentPhoto.setImageResource(R.drawable.baseline_account_circle_24);
            }
        }
    }

    // ── Row builder — Transaction ──────────────────────────────────────────────
    private void addTransactionRow(String trNo, String type, String fee,
                                   String paid, String recNo, boolean isEven) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int dp8 = dp(8);
        row.setPadding(dp(12), dp8, dp(12), dp8);
        row.setBackgroundColor(isEven ? Color.WHITE : Color.parseColor("#F9FBF9"));

        row.addView(makeCell(trNo,  0.8f, "#1A1A1A", false));
        row.addView(makeCell(type,  1.4f, "#1A1A1A", false));
        row.addView(makeCell(fee,   1.0f, "#1A1A1A", true));
        row.addView(makeCell(paid,  1.0f, "#2E7D32", true));
        row.addView(makeCell(recNo, 1.0f, "#1565C0", true));

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));

        llTransactions.addView(row);
        llTransactions.addView(divider);
    }

    private TextView makeCell(String text, float weight, String hexColor, boolean alignEnd) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setTextColor(Color.parseColor(hexColor));
        tv.setMaxLines(1);
        tv.setGravity(alignEnd ? Gravity.END : Gravity.START);
        if (alignEnd) tv.setPadding(0, 0, dp(4), 0);
        return tv;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private String safe(String value) {
        return (value == null || value.isEmpty()) ? "—" : value;
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}