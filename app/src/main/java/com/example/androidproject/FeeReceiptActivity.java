package com.example.androidproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.AddReceiptRequest;
import com.example.androidproject.model.AddReceiptResponse;
import com.example.androidproject.model.FeeReceiptRequest;
import com.example.androidproject.model.FeeReceiptResponse;
import com.example.androidproject.model.PaymentHistory;
import com.example.androidproject.model.SuggestReceiptRequest;
import com.example.androidproject.model.SuggestReceiptResponse;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeeReceiptActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private EditText        etAmountReceived, etReceiptNo;
    private Button          btnSubmit, btnCancel;
    private ImageButton     btnBack, btnDatePicker;
    private TextView        tvReceiptDate, tvReceiptNo;
    private TextView        tvStudentId, tvAdmissionIdResult, tvStudentName, tvPhone, tvAvatarInitial;
    private TextView        tvCourseName, tvBatch, tvFee, tvPaidFee, tvRemainingFeeRight, tvRemainingFee;
    private LinearLayout    layoutResult, layoutPaymentHistory;
    private ProgressBar     progressBar;

    // ── Config ────────────────────────────────────────────────────
    private final int USER_ID      = 2181;
    private final int INSTITUTE_ID = 1;

    // ── State ─────────────────────────────────────────────────────
    private int    passedStudentId   = -1;
    private int    passedAdmissionId = -1;
    private double remainingFeeAmount = 0.0;

    // ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_receipt2);

        initViews();
        setTodayDate();
        getIntentData();
        setupListeners();
        getSuggestedReceiptNo();
        callApi();
    }

    // ── Init Views ────────────────────────────────────────────────
    private void initViews() {
        etAmountReceived    = findViewById(R.id.etAmountReceived);
        etReceiptNo         = findViewById(R.id.etReceiptNo);
        btnSubmit           = findViewById(R.id.btnSubmit);
        btnCancel           = findViewById(R.id.btnCancel);
        btnBack             = findViewById(R.id.btnBack);
        btnDatePicker       = findViewById(R.id.btnDatePicker);
        tvReceiptDate       = findViewById(R.id.tvReceiptDate);
        tvReceiptNo         = findViewById(R.id.tvReceiptNo);
        tvStudentId         = findViewById(R.id.tvStudentId);
        tvAdmissionIdResult = findViewById(R.id.tvAdmissionIdResult);
        tvStudentName       = findViewById(R.id.tvStudentName);
        tvPhone             = findViewById(R.id.tvPhone);
        tvAvatarInitial     = findViewById(R.id.tvAvatarInitial);
        tvCourseName        = findViewById(R.id.tvCourseName);
        tvBatch             = findViewById(R.id.tvBatch);
        tvFee               = findViewById(R.id.tvFee);
        tvPaidFee           = findViewById(R.id.tvPaidFee);
        tvRemainingFeeRight = findViewById(R.id.tvRemainingFeeRight);
        tvRemainingFee      = findViewById(R.id.tvRemainingFee);
        layoutResult        = findViewById(R.id.layoutResult);
        layoutPaymentHistory= findViewById(R.id.layoutPaymentHistory);
        progressBar         = findViewById(R.id.progressBar);
    }

    // ── Today Date ────────────────────────────────────────────────
    private void setTodayDate() {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        tvReceiptDate.setText(today);
    }

    // ── Intent Data ───────────────────────────────────────────────
    private void getIntentData() {
        if (getIntent() != null) {
            passedStudentId   = getIntent().getIntExtra("student_id",   -1);
            passedAdmissionId = getIntent().getIntExtra("admission_id", -1);
        }
        Log.d("INTENT", "Student: " + passedStudentId + " Admission: " + passedAdmissionId);
    }

    // ── Listeners ─────────────────────────────────────────────────
    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnDatePicker.setOnClickListener(v -> showDatePicker());
        tvReceiptDate.setOnClickListener(v -> showDatePicker());

        btnSubmit.setOnClickListener(v -> submitFeeReceipt());
        btnCancel.setOnClickListener(v -> resetForm());

        // Live remaining fee update as user types amount
        etAmountReceived.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    tvRemainingFee.setText("₹ " + String.format(Locale.getDefault(),
                            "%.2f", remainingFeeAmount));
                    return;
                }
                try {
                    double entered  = Double.parseDouble(s.toString());
                    double updated  = remainingFeeAmount - entered;
                    tvRemainingFee.setText("₹ " + String.format(Locale.getDefault(),
                            "%.2f", Math.max(updated, 0)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ── Date Picker ───────────────────────────────────────────────
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) -> tvReceiptDate.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ── Fetch Receipt Data ────────────────────────────────────────
    private void callApi() {
        FeeReceiptRequest request = new FeeReceiptRequest(
                USER_ID, INSTITUTE_ID, passedStudentId, passedAdmissionId);

        Log.d("FEE_REQUEST", new Gson().toJson(request));
        showLoading(true);

        RetrofitClient.getApiService().getReceiptRecord(request)
                .enqueue(new Callback<FeeReceiptResponse>() {

                    @Override
                    public void onResponse(Call<FeeReceiptResponse> call,
                                           Response<FeeReceiptResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            FeeReceiptResponse res = response.body();
                            Log.d("FEE_RESPONSE", new Gson().toJson(res));

                            if (res.isSuccess()) {
                                populateData(res);
                            } else {
                                Toast.makeText(FeeReceiptActivity.this,
                                        res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(FeeReceiptActivity.this,
                                    "Server Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FeeReceiptResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(FeeReceiptActivity.this,
                                t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Populate UI ───────────────────────────────────────────────
    private void populateData(FeeReceiptResponse data) {
        layoutResult.setVisibility(View.VISIBLE);

        FeeReceiptResponse.Summary s = data.getSummary();

        // Store remaining for validation
        remainingFeeAmount = s.getRemaining();

        // Toolbar receipt no badge
        tvReceiptNo.setVisibility(View.VISIBLE);
        tvReceiptNo.setText("Receipt No : " + data.getSuggestedReceiptNo());

        // Clear amount field — never show 0.0 on open
        etAmountReceived.setText("");

        // ── Student Details ──
        // Show only the number in the chip — no label prefix
        tvStudentId.setText(String.valueOf(s.getStudentID()));
        tvAdmissionIdResult.setText(String.valueOf(s.getAdmissionID()));
        tvStudentName.setText(s.getStudentName());
        tvPhone.setText(s.getMobile());

        // Avatar initial — first letter of name
        String name = s.getStudentName();
        if (name != null && !name.isEmpty()) {
            tvAvatarInitial.setText(
                    String.valueOf(name.charAt(0)).toUpperCase(Locale.getDefault()));
        }

        // ── Course & Fee Details ──
        tvCourseName.setText(s.getCourseName());
        tvBatch.setText("Batch : " + s.getBatchName());

        // Always 2 decimal places
        tvFee.setText("₹ " + String.format(Locale.getDefault(), "%.2f", s.getFees()));
        tvPaidFee.setText("₹ " + String.format(Locale.getDefault(), "%.2f", s.getTotalPaid()));
        tvRemainingFeeRight.setText("₹ " + String.format(Locale.getDefault(), "%.2f", s.getRemaining()));
        tvRemainingFee.setText("₹ " + String.format(Locale.getDefault(), "%.2f", s.getRemaining()));

        // ── Payment History ──
        buildPaymentHistory(data.getDetailList());
    }

    // ── Payment History Rows ──────────────────────────────────────
    private void buildPaymentHistory(List<PaymentHistory> list) {
        layoutPaymentHistory.removeAllViews();

        if (list == null || list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No payment history");
            empty.setTextColor(Color.parseColor("#AAAAAA"));
            empty.setTextSize(13f);
            empty.setPadding(32, 20, 32, 20);
            layoutPaymentHistory.addView(empty);
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            PaymentHistory item = list.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(32, 14, 32, 14);
            // Alternate row tint
            row.setBackgroundColor(i % 2 == 0
                    ? Color.WHITE
                    : Color.parseColor("#F5FBF5"));

            TextView tvDate = new TextView(this);
            tvDate.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvDate.setText(item.getTrDate());
            tvDate.setTextColor(Color.parseColor("#555555"));
            tvDate.setTextSize(13f);

            TextView tvAmt = new TextView(this);
            tvAmt.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            // Format amount to 2dp
            try {
                double amt = Double.parseDouble(item.getAmount());
                tvAmt.setText(String.format(Locale.getDefault(), "%.2f", amt));
            } catch (Exception e) {
                tvAmt.setText(item.getAmount());
            }
            tvAmt.setTextColor(Color.parseColor("#2E7D32"));
            tvAmt.setTextSize(13f);
            tvAmt.setTypeface(null, android.graphics.Typeface.BOLD);

            row.addView(tvDate);
            row.addView(tvAmt);
            layoutPaymentHistory.addView(row);
        }
    }

    // ── Submit ────────────────────────────────────────────────────
    private void submitFeeReceipt() {
        String amountStr = etAmountReceived.getText().toString().trim();
        String receiptNo = etReceiptNo.getText().toString().trim();
        String date      = tvReceiptDate.getText().toString();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter Amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        /*if (amount <= 0) {
            Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            return;
        }*/
        if (amount > remainingFeeAmount) {
            Toast.makeText(this, "Amount exceeds remaining fee ₹"
                            + String.format(Locale.getDefault(), "%.2f", remainingFeeAmount),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String isoDate = convertToISO(date);

        AddReceiptRequest request = new AddReceiptRequest(
                USER_ID, INSTITUTE_ID,
                passedStudentId, passedAdmissionId,
                amount, isoDate, receiptNo,
                USER_ID, "Fee Received");

        Log.d("ADD_RECEIPT_REQ", new Gson().toJson(request));
        showLoading(true);

        RetrofitClient.getApiService().addReceipt(request)
                .enqueue(new Callback<AddReceiptResponse>() {

                    @Override
                    public void onResponse(Call<AddReceiptResponse> call,
                                           Response<AddReceiptResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            AddReceiptResponse res = response.body();
                            if (res.isSuccess()) {
                                Toast.makeText(FeeReceiptActivity.this,
                                        "Receipt Added Successfully",
                                        Toast.LENGTH_LONG).show();

                                // Go back to Dashboard
                                Intent intent = new Intent(
                                        FeeReceiptActivity.this, DashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(FeeReceiptActivity.this,
                                        res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(FeeReceiptActivity.this,
                                    "Server Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AddReceiptResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(FeeReceiptActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Suggested Receipt No ──────────────────────────────────────
    private void getSuggestedReceiptNo() {
        SuggestReceiptRequest request = new SuggestReceiptRequest(USER_ID, INSTITUTE_ID);
        Log.d("SUGGEST_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().getSuggestedReceipt(request)
                .enqueue(new Callback<SuggestReceiptResponse>() {

                    @Override
                    public void onResponse(Call<SuggestReceiptResponse> call,
                                           Response<SuggestReceiptResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SuggestReceiptResponse res = response.body();
                            if (res.isSuccess()) {
                                Log.d("SUGGEST_NO", res.getSuggestedReceiptNo());
                                etReceiptNo.setText(res.getSuggestedReceiptNo());
                            } else {
                                Toast.makeText(FeeReceiptActivity.this,
                                        res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SuggestReceiptResponse> call, Throwable t) {
                        Toast.makeText(FeeReceiptActivity.this,
                                "Receipt No Failed: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void resetForm() {
        etAmountReceived.setText("");
        layoutResult.setVisibility(View.GONE);
        tvReceiptNo.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String convertToISO(String date) {
        try {
            SimpleDateFormat in  = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            return out.format(in.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}