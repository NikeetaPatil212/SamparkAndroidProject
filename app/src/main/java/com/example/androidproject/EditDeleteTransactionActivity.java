package com.example.androidproject;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.TransactionAdapter;
import com.example.androidproject.model.TransactionItem;
import com.example.androidproject.model.TransactionListRequest;
import com.example.androidproject.model.TransactionListResponse;
import com.example.androidproject.model.UpdateTransactionRequest;
import com.example.androidproject.model.UpdateTransactionResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDeleteTransactionActivity extends AppCompatActivity {

    private static final String TAG = "EDIT_TXN";

    // ── Views: Results wrapper ─────────────────────────────────────
    private LinearLayout layoutResults;

    // ── Views: Course & Fee card ───────────────────────────────────
    private TextView tvCourseName, tvBatchName;
    private TextView tvTotalFees, tvPaidFees, tvRemainingFees;

    // ── Views: Student card ────────────────────────────────────────
    private TextView tvStudentId, tvDisplayAdmId, tvStudentName, tvMobile;

    // ── Views: Payment history ─────────────────────────────────────
    private RecyclerView       rvTransactions;
    private TransactionAdapter adapter;

    // ── Views: Edit transaction card ───────────────────────────────
    private CardView       cardEditTransaction;
    private TextView       tvTrno, tvTransactionType;
    private EditText       etTrDate, etTransactionAmt, etNarration;
    private MaterialButton btnUpdate;

    // ── Loader ─────────────────────────────────────────────────────
    private FrameLayout loaderLayout;

    // ── State ──────────────────────────────────────────────────────
    private int    selectedUtno   = 0;
    private int    selectedTrno   = 0;
    private String selectedTrType = "";

    private final Calendar         trDateCal  = Calendar.getInstance();
    private final SimpleDateFormat displayFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiFmt     = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    private int passedStudentId   = -1;
    private int passedAdmissionId = -1;

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete_transaction);

        initViews();
        setupBackButton();
        setupRecyclerView();
        setupListeners();
        fetchTransactionList();
    }

    // ── Bind views ─────────────────────────────────────────────────
    private void initViews() {
        layoutResults       = findViewById(R.id.layoutResults);
        loaderLayout        = findViewById(R.id.loaderLayout);

        tvCourseName        = findViewById(R.id.tvCourseName);
        tvBatchName         = findViewById(R.id.tvBatchName);
        tvTotalFees         = findViewById(R.id.tvTotalFees);
        tvPaidFees          = findViewById(R.id.tvPaidFees);
        tvRemainingFees     = findViewById(R.id.tvRemainingFees);

        tvStudentId         = findViewById(R.id.tvStudentId);
        tvDisplayAdmId      = findViewById(R.id.tvAdmissionId);
        tvStudentName       = findViewById(R.id.tvStudentName);
        tvMobile            = findViewById(R.id.tvMobile);

        rvTransactions      = findViewById(R.id.rvTransactions);

        cardEditTransaction = findViewById(R.id.cardEditTransaction);
        tvTrno              = findViewById(R.id.tvTrno);
        tvTransactionType   = findViewById(R.id.tvTransactionType);
        etTrDate            = findViewById(R.id.etTrDate);
        etTransactionAmt    = findViewById(R.id.etTransactionAmt);
        etNarration         = findViewById(R.id.etNarration);
        btnUpdate           = findViewById(R.id.btnUpdate);

        // ✅ Get IDs from intent
        if (getIntent() != null) {
            passedStudentId   = getIntent().getIntExtra("studentId",   -1);
            passedAdmissionId = getIntent().getIntExtra("admissionId", -1);
        }
        Log.d(TAG, "Student: " + passedStudentId + " Admission: " + passedAdmissionId);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── RecyclerView ───────────────────────────────────────────────
    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
        rvTransactions.setNestedScrollingEnabled(false);

        adapter.setOnRowClickListener(item -> {
            selectedUtno   = item.utno;
            selectedTrno   = item.trno;
            selectedTrType = item.transactionType != null ? item.transactionType : "";

            tvTrno.setText(String.valueOf(item.trno));
            tvTransactionType.setText(nvl(item.transactionType));

            // Parse transaction date
            try {
                // Try ISO format first
                Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .parse(item.transactionDate);
                if (d == null) d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(item.transactionDate);
                if (d != null) {
                    trDateCal.setTime(d);
                    etTrDate.setText(displayFmt.format(d));
                }
            } catch (Exception e) {
                // Try simple date format
                try {
                    Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .parse(item.transactionDate);
                    if (d != null) {
                        trDateCal.setTime(d);
                        etTrDate.setText(displayFmt.format(d));
                    }
                } catch (Exception ex) {
                    etTrDate.setText(nvl(item.transactionDate));
                }
            }

            // ✅ Show debit or credit as amount
            double amt = item.debit > 0 ? item.debit : item.credit;
            etTransactionAmt.setText(formatAmt(amt));
            etNarration.setText("");

            cardEditTransaction.setVisibility(View.VISIBLE);
            cardEditTransaction.post(() ->
                    cardEditTransaction.requestFocus());
        });
    }

    // ── Listeners ──────────────────────────────────────────────────
    private void setupListeners() {
        etTrDate.setOnClickListener(v ->
                new DatePickerDialog(this,
                        (dp, y, m, d) -> {
                            trDateCal.set(y, m, d);
                            etTrDate.setText(displayFmt.format(trDateCal.getTime()));
                        },
                        trDateCal.get(Calendar.YEAR),
                        trDateCal.get(Calendar.MONTH),
                        trDateCal.get(Calendar.DAY_OF_MONTH)).show());

        btnUpdate.setOnClickListener(v -> {
            if (validateEditForm()) updateTransaction();
        });
    }

    // ── API: TransactionList ───────────────────────────────────────
    private void fetchTransactionList() {
        showLoader(true);
        cardEditTransaction.setVisibility(View.GONE);
        adapter.clearSelection();

        PrefManager pref = PrefManager.getInstance(this);
        int userID       = Integer.parseInt(pref.getUserId());
        int instituteID  = Integer.parseInt(pref.getInstituteId());

        TransactionListRequest request = new TransactionListRequest(
                passedStudentId,
                passedAdmissionId,
                userID,
                instituteID);

        Log.d(TAG, "fetchTransactionList req=" + new Gson().toJson(request));

        RetrofitClient.getApiService()
                .getTransactionList(request)
                .enqueue(new Callback<TransactionListResponse>() {
                    @Override
                    public void onResponse(Call<TransactionListResponse> call,
                                           Response<TransactionListResponse> response) {
                        showLoader(false);
                        Log.d(TAG, "resp code=" + response.code()
                                + " body=" + new Gson().toJson(response.body()));

                        if (!response.isSuccessful() || response.body() == null) {
                            toast("❌ Server error: " + response.code());
                            return;
                        }

                        TransactionListResponse body = response.body();
                        if (!body.isSuccess) {
                            toast("❌ " + body.message);
                            layoutResults.setVisibility(View.GONE);
                            return;
                        }

                        bindStudentInfo(body);
                        bindTransactions(body.transactionHistory);
                        layoutResults.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<TransactionListResponse> call, Throwable t) {
                        showLoader(false);
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    // ── Bind student info ──────────────────────────────────────────
    private void bindStudentInfo(TransactionListResponse body) {
        if (body.studentInfo == null) return;

        tvCourseName.setText(nvl(body.studentInfo.courseName));
        tvBatchName.setText(nvl(body.studentInfo.batchName));
        tvTotalFees.setText("₹ " + formatAmt(body.studentInfo.totalFees));
        tvPaidFees.setText("₹ " + formatAmt(body.studentInfo.paidFees));
        tvRemainingFees.setText("₹ " + formatAmt(body.studentInfo.remainingFees));
        tvRemainingFees.setTextColor(body.studentInfo.remainingFees > 0
                ? Color.parseColor("#C62828")   // red = still owes money
                : Color.parseColor("#2E7D32")); // green = fully paid

        tvStudentId.setText(String.valueOf(body.studentInfo.studentID));
        tvDisplayAdmId.setText(String.valueOf(body.studentInfo.admissionID));
        tvStudentName.setText(nvl(body.studentInfo.studentName));
        tvMobile.setText(nvl(body.studentInfo.mobile));
    }

    // ── Bind transactions ──────────────────────────────────────────
    private void bindTransactions(List<TransactionItem> list) {
        if (list == null || list.isEmpty()) {
            toast("No transactions found");
            return;
        }
        adapter.setData(list);
    }

    // ── Validate edit form ─────────────────────────────────────────
    private boolean validateEditForm() {
        if (selectedUtno == 0) {
            toast("Please select a transaction row first");
            return false;
        }
        if (etTransactionAmt.getText().toString().trim().isEmpty()) {
            etTransactionAmt.setError("Amount required");
            etTransactionAmt.requestFocus();
            return false;
        }
        if (etTrDate.getText().toString().trim().isEmpty()) {
            toast("Please select a transaction date");
            return false;
        }
        return true;
    }

    // ── API: UpdateTransaction (new API) ───────────────────────────
    private void updateTransaction() {
        hideKeyboard();
        showLoader(true);

        PrefManager pref = PrefManager.getInstance(this);
        int userID       = Integer.parseInt(pref.getUserId());
        int instituteID  = Integer.parseInt(pref.getInstituteId());
        int operatorID   = Integer.parseInt(pref.getOperatorId());

        double amt       = Double.parseDouble(
                etTransactionAmt.getText().toString().trim());
        String trDate    = apiFmt.format(trDateCal.getTime());
        String narration = etNarration.getText().toString().trim();

        // ✅ New API request — only utno, amount, transactionDate, comments, operatorID, userID, instituteID
        UpdateTransactionRequest request = new UpdateTransactionRequest(
                selectedUtno,
                amt,
                trDate,
                narration,
                operatorID,
                userID,
                instituteID);

        Log.d(TAG, "updateTransaction req=" + new Gson().toJson(request));

        RetrofitClient.getApiService()
                .updateTransaction(request)
                .enqueue(new Callback<UpdateTransactionResponse>() {
                    @Override
                    public void onResponse(Call<UpdateTransactionResponse> call,
                                           Response<UpdateTransactionResponse> response) {
                        showLoader(false);
                        Log.d(TAG, "update resp=" + new Gson().toJson(response.body()));

                        if (!response.isSuccessful() || response.body() == null) {
                            toast("❌ Server error: " + response.code());
                            return;
                        }

                        UpdateTransactionResponse body = response.body();
                        if (body.isSuccess) {
                            toast("✅ " + body.message);
                            cardEditTransaction.setVisibility(View.GONE);
                            adapter.clearSelection();
                            selectedUtno = 0;
                            fetchTransactionList(); // ✅ refresh list
                        } else {
                            toast("❌ " + body.message);
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateTransactionResponse> call, Throwable t) {
                        showLoader(false);
                        Log.e(TAG, "update onFailure: " + t.getMessage());
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String formatAmt(double v) {
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }

    private String nvl(String s) { return s != null ? s : ""; }

    private void showLoader(boolean show) {
        loaderLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(INPUT_METHOD_SERVICE);
            View focus = getCurrentFocus();
            if (focus != null)
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}