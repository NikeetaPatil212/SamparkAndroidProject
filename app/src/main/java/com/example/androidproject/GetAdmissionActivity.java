package com.example.androidproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.AdmissionAdapter;
import com.example.androidproject.model.AdmissionDetails;
import com.example.androidproject.model.GetAdmissionRequest;
import com.example.androidproject.model.GetAdmissionResponse;
import com.example.androidproject.utils.ApiService;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAdmissionActivity extends AppCompatActivity {

    private RecyclerView    recyclerView;
    private AdmissionAdapter adapter;
    private EditText        etSearchName, etSearchMobile;
    private Button          btnSearch;
    private ImageButton     btnBack;
    private TextView        tvCount, tvFromDate, tvToDate;
    private LinearLayout    layoutFromDate, layoutToDate;

    // 3 state views
    private LinearLayout    layoutLoading, layoutEmpty;
    private CardView        layoutTable;

    private List<AdmissionDetails> fullList = new ArrayList<>();

    private String fromDateApi = "2025-01-10T00:00:00.000Z";
    private String toDateApi   = getTodayIso();



    private final SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_admission);
        initViews();
        setupListeners();
        callAdmissionApi();
    }

    private void initViews() {
        recyclerView   = findViewById(R.id.recyclerView);
        etSearchName   = findViewById(R.id.etSearchName);
        etSearchMobile = findViewById(R.id.etSearchMobile);
        btnSearch      = findViewById(R.id.btnSearch);
        btnBack        = findViewById(R.id.btnBack);
        tvCount        = findViewById(R.id.tvCount);
        tvFromDate     = findViewById(R.id.tvFromDate);
        tvToDate       = findViewById(R.id.tvToDate);
        layoutFromDate = findViewById(R.id.layoutFromDate);
        layoutToDate   = findViewById(R.id.layoutToDate);
        layoutLoading  = findViewById(R.id.layoutLoading);
        layoutEmpty    = findViewById(R.id.layoutEmpty);
        layoutTable    = findViewById(R.id.layoutTable);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        layoutFromDate.setOnClickListener(v -> showDatePicker(true));
        layoutToDate.setOnClickListener(v -> showDatePicker(false));
        btnSearch.setOnClickListener(v -> callAdmissionApi());

        etSearchName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) { filterList(); }
        });

        etSearchMobile.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) { filterList(); }
        });
    }

    // ── 3 UI states ───────────────────────────────────────────────
    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        layoutTable.setVisibility(View.GONE);
        tvCount.setVisibility(View.GONE);
        btnSearch.setEnabled(false);
    }

    private void showTable() {
        layoutLoading.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        layoutTable.setVisibility(View.VISIBLE);
        tvCount.setVisibility(View.VISIBLE);
        btnSearch.setEnabled(true);
    }

    private void showEmpty() {
        layoutLoading.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        layoutTable.setVisibility(View.GONE);
        tvCount.setVisibility(View.VISIBLE);
        tvCount.setText("0 records");
        btnSearch.setEnabled(true);
    }

    // ── Date Picker ───────────────────────────────────────────────
    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(year, month, day, 0, 0, 0);
            sel.set(Calendar.MILLISECOND, 0);
            String display = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
            String iso = isoFmt.format(sel.getTime());
            if (isFrom) {
                fromDateApi = iso;
                tvFromDate.setText(display);
                tvFromDate.setTextColor(0xFF1A1A1A);
            } else {
                toDateApi = iso;
                tvToDate.setText(display);
                tvToDate.setTextColor(0xFF1A1A1A);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ── API Call ──────────────────────────────────────────────────
    private void callAdmissionApi() {
        showLoading();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        String operatorId  = PrefManager.getInstance(this).getOperatorId();

        GetAdmissionRequest request = new GetAdmissionRequest();
        request.setUserID(Integer.parseInt(userId));
        request.setInstituteID(Integer.parseInt(instituteId));
        request.setFromDate(fromDateApi);
        request.setToDate(toDateApi);
        request.setFilters(new HashMap<>());

        Log.d("ADD_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService()
                .getAdmissionDetails(request)
                .enqueue(new Callback<GetAdmissionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GetAdmissionResponse> call,
                                           @NonNull Response<GetAdmissionResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getDetails() != null
                                && !response.body().getDetails().isEmpty()) {
                            fullList = response.body().getDetails();
                            filterList();
                        } else {
                            fullList.clear();
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetAdmissionResponse> call,
                                          @NonNull Throwable t) {
                        showEmpty();
                        Toast.makeText(GetAdmissionActivity.this,
                                "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Filter name + mobile ──────────────────────────────────────
    private void filterList() {
        String nameQ   = etSearchName.getText().toString().trim().toLowerCase();
        String mobileQ = etSearchMobile.getText().toString().trim();

        List<AdmissionDetails> filtered = new ArrayList<>();
        for (AdmissionDetails item : fullList) {
            boolean nm = nameQ.isEmpty()
                    || (item.getStudent_Name() != null
                    && item.getStudent_Name().toLowerCase().contains(nameQ));
            boolean mm = mobileQ.isEmpty()
                    || (item.getMobile() != null
                    && item.getMobile().contains(mobileQ));
            if (nm && mm) filtered.add(item);
        }

        if (filtered.isEmpty()) {
            showEmpty();
            return;
        }

        showTable();
        tvCount.setText(filtered.size() + " records");

        if (adapter == null) {
            adapter = new AdmissionAdapter(filtered, this::showPopup);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(filtered);
        }
    }

    private void showPopup(AdmissionDetails item) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_admission_actions, null);

        ImageView ivStudent = view.findViewById(R.id.ivStudent);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvMobile = view.findViewById(R.id.tvMobile);
        TextView tvAltMobile = view.findViewById(R.id.tvAltMobile);
        TextView tvAddress = view.findViewById(R.id.tvAddress);
        LinearLayout tvViewDetails = view.findViewById(R.id.tvViewDetails);


// Set data
        tvName.setText(item.getStudent_Name());
        tvMobile.setText("Mobile: " + item.getMobile());
       /* tvAltMobile.setText("Alt: " + item.getMobile());
        tvAddress.setText("Address: " + item.getMobile());*/

        Log.d("studeidd----", "showPopup: " + item.getStudID());


        AlertDialog dialog = builder.setView(view).create();


        tvViewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On ViewDetails TextView click:
                Intent intent = new Intent(GetAdmissionActivity.this, StudentProfleActivity.class);
                intent.putExtra("studentId", item.getStudID());
                intent.putExtra("admissionId",item.getAdm_id());
                startActivity(intent);
            }
        });

        // Fee Receipt Click
        view.findViewById(R.id.tvFeeReceipt).setOnClickListener(v -> {
            dialog.dismiss();

            Log.d("DATA_PASS", "StudentID=" + item.getStudID()
                    + " AdmissionID=" + item.getAdm_id());

            Intent intent = new Intent(GetAdmissionActivity.this, FeeReceiptActivity.class);
            intent.putExtra("studentId", item.getStudID());
            intent.putExtra("admissionId", item.getAdm_id());
            startActivity(intent);
        });

        // Example: Student Details Activity
        view.findViewById(R.id.tvNewAdmission).setOnClickListener(v -> {
            dialog.dismiss();

            Intent intent = new Intent(GetAdmissionActivity.this, NewAdmissionActivity.class);
            intent.putExtra("studentId", item.getStudID());
            intent.putExtra("student_name", item.getStudent_Name());
            startActivity(intent);
        });


        view.findViewById(R.id.tvEditProfile).setOnClickListener(v -> {
            dialog.dismiss();

            Intent intent = new Intent(GetAdmissionActivity.this, EditStudentProfileActivity.class);
            intent.putExtra("studentId", item.getStudID());
            Log.d("GetSTudeID----", "showPopup: " + item.getStudID());
            startActivity(intent);
        });

        // Cancel
        view.findViewById(R.id.tvGoBack).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static String getTodayIso() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
    }
}