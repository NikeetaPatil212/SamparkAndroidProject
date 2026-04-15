package com.example.androidproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.InquiryAdapter;
import com.example.androidproject.model.InquiryListRequest;
import com.example.androidproject.model.InquiryListResponse;
import com.example.androidproject.utils.ApiService;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetInquiryActivity extends AppCompatActivity {

    EditText etFromDate, etToDate, etStudentName, etContactNo;
    MaterialButton btnSearch;
    RecyclerView rvInquiry;
    TextView tvResultCount;

    InquiryAdapter adapter;

    private String fromDateForApi = "";
    private String toDateForApi   = "";

    FrameLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ✅ FIX: Removed EdgeToEdge.enable(this) — caused toolbar/status bar overlap
        setContentView(R.layout.activity_get_inquiry);

        // ── Views ──────────────────────────────────────────────────────────────
        etFromDate    = findViewById(R.id.etFromDate);
        etToDate      = findViewById(R.id.etToDate);
        btnSearch     = findViewById(R.id.btnSearch);
        rvInquiry     = findViewById(R.id.rvInquiry);
        etStudentName = findViewById(R.id.etStudentName);
        etContactNo   = findViewById(R.id.etContactNo);
        loaderLayout  = findViewById(R.id.loaderLayout);
        tvResultCount = findViewById(R.id.tvResultCount);

        // ── Back button ────────────────────────────────────────────────────────
        ImageButton btnBack = findViewById(R.id.toolbar);
        btnBack.setOnClickListener(v -> onBackPressed());

        // ── RecyclerView ───────────────────────────────────────────────────────
        rvInquiry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InquiryAdapter(this);
        rvInquiry.setAdapter(adapter);

        // ── Set default dates and auto-load data on open ───────────────────────
        setDefaultDatesAndLoadData();

        // ── Date pickers ───────────────────────────────────────────────────────
        // ✅ FIX: After picking either date, API is called immediately.
        // No need to press "View Report" after changing dates — it refreshes automatically.
        etFromDate.setOnClickListener(v -> openDatePicker(etFromDate, true));
        etToDate.setOnClickListener(v -> openDatePicker(etToDate, false));

        // ── "View Report" button — always visible, re-fetches with current dates ─
        btnSearch.setOnClickListener(v -> callInquiryApi());

        // ── Live filter by name (client-side, no API call needed) ─────────────
        etStudentName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterByName(s.toString());
                updateResultCount();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ── Live filter by contact (client-side) ───────────────────────────────
        etContactNo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterByContact(s.toString());
                updateResultCount();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ── Set default dates (today − 6 months → today) and auto-load ────────────
    private void setDefaultDatesAndLoadData() {
        Calendar today = Calendar.getInstance();

        Calendar toCal   = (Calendar) today.clone();
        Calendar fromCal = (Calendar) today.clone();
        fromCal.add(Calendar.MONTH, -6);

        SimpleDateFormat uiFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        SimpleDateFormat apiFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault()
        );
        apiFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 🔥 IMPORTANT

        // UI
        etFromDate.setText(uiFormat.format(fromCal.getTime()));
        etToDate.setText(uiFormat.format(toCal.getTime()));

        // FROM → start of day
        fromCal.set(Calendar.HOUR_OF_DAY, 0);
        fromCal.set(Calendar.MINUTE, 0);
        fromCal.set(Calendar.SECOND, 0);
        fromCal.set(Calendar.MILLISECOND, 0);

        // TO → end of day
        toCal.set(Calendar.HOUR_OF_DAY, 23);
        toCal.set(Calendar.MINUTE, 59);
        toCal.set(Calendar.SECOND, 59);
        toCal.set(Calendar.MILLISECOND, 999);

        fromDateForApi = apiFormat.format(fromCal.getTime());
        toDateForApi   = apiFormat.format(toCal.getTime());

        Log.d("DATE_INIT", "FROM: " + fromDateForApi);
        Log.d("DATE_INIT", "TO: " + toDateForApi);

        callInquiryApi();
    }

    // ── Date picker: picks date, updates API strings, triggers API call ────────
    private void openDatePicker(EditText editText, boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);

                    // UI format
                    SimpleDateFormat uiFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                    editText.setText(uiFormat.format(selected.getTime()));

                    // API format (FIXED)
                    SimpleDateFormat apiFormat = new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            Locale.getDefault()
                    );
                    apiFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 🔥 IMPORTANT

                    if (isFromDate) {
                        selected.set(Calendar.HOUR_OF_DAY, 0);
                        selected.set(Calendar.MINUTE, 0);
                        selected.set(Calendar.SECOND, 0);
                        selected.set(Calendar.MILLISECOND, 0);

                        fromDateForApi = apiFormat.format(selected.getTime());
                    } else {
                        selected.set(Calendar.HOUR_OF_DAY, 23);
                        selected.set(Calendar.MINUTE, 59);
                        selected.set(Calendar.SECOND, 59);
                        selected.set(Calendar.MILLISECOND, 999);

                        toDateForApi = apiFormat.format(selected.getTime());
                    }

                    Log.d("DATE_PICK", "FROM: " + fromDateForApi);
                    Log.d("DATE_PICK", "TO: " + toDateForApi);

                    callInquiryApi(); // auto refresh
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ── API call with current fromDateForApi / toDateForApi ────────────────────
    private void callInquiryApi() {
        loaderLayout.setVisibility(View.VISIBLE);

        // Clear filters
        etStudentName.setText("");
        etContactNo.setText("");

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        // 🔥 Safety check (important)
        if (fromDateForApi == null || toDateForApi == null ||
                fromDateForApi.isEmpty() || toDateForApi.isEmpty()) {
            Toast.makeText(this, "Invalid date range", Toast.LENGTH_SHORT).show();
            loaderLayout.setVisibility(View.GONE);
            return;
        }

        InquiryListRequest request = new InquiryListRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                fromDateForApi,
                toDateForApi,
                new HashMap<>()
        );

        Log.d("FINAL_REQUEST", new Gson().toJson(request));

        RetrofitClient.getApiService().getInquiryList(request)
                .enqueue(new Callback<InquiryListResponse>() {

                    @Override
                    public void onResponse(Call<InquiryListResponse> call,
                                           Response<InquiryListResponse> response) {
                        loaderLayout.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("API_RESPONSE", new Gson().toJson(response.body()));

                            if (response.body().getInquiryList() != null) {
                                adapter.setData(response.body().getInquiryList());
                            } else {
                                adapter.setData(new ArrayList<>());
                            }

                            updateResultCount();

                        } else {
                            Toast.makeText(GetInquiryActivity.this,
                                    "Server Error", Toast.LENGTH_SHORT).show();
                            updateResultCount();
                        }
                    }

                    @Override
                    public void onFailure(Call<InquiryListResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(GetInquiryActivity.this,
                                "API Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Update the result count badge in the card header ──────────────────────
    private void updateResultCount() {
        if (tvResultCount == null || adapter == null) return;
        int count = adapter.getItemCount();
        tvResultCount.setText(count + " record" + (count == 1 ? "" : "s"));
    }
}