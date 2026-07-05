package com.example.androidproject;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.BatchTimeAdapter;
import com.example.androidproject.model.course.BatchTimeAddRequest;
import com.example.androidproject.model.course.BatchTimeAddResponse;
import com.example.androidproject.model.course.BatchTimeItem;
import com.example.androidproject.model.course.BatchTimeListRequest;
import com.example.androidproject.model.course.BatchTimeListResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BatchTimeActivity extends AppCompatActivity {

    private EditText etDescription, etCapacity, etStartTime, etEndTime;
    private MaterialButton btnSave, btnUpdate, btnCancelEdit;
    private TextView tvFormTitle, tvTimeCount;
    private RecyclerView rvBatchTimes;
    private FrameLayout loaderLayout;

    private BatchTimeAdapter adapter;
    private int editingTimeId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_time);

        initViews();
        setupRecyclerView();
        loadBatchTimes();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());

        etDescription = findViewById(R.id.etDescription);
        etCapacity = findViewById(R.id.etCapacity);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnSave = findViewById(R.id.btnSave);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvTimeCount = findViewById(R.id.tvTimeCount);
        rvBatchTimes = findViewById(R.id.rvBatchTimes);
        loaderLayout = findViewById(R.id.loaderLayout);

        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                editingTimeId = 0;
                saveBatchTime();
            }
        });

        btnUpdate.setOnClickListener(v -> {
            if (validateForm()) saveBatchTime();
        });

        btnCancelEdit.setOnClickListener(v -> cancelEditMode());
    }

    private void setupRecyclerView() {
        adapter = new BatchTimeAdapter();
        rvBatchTimes.setLayoutManager(new LinearLayoutManager(this));
        rvBatchTimes.setAdapter(adapter);
        rvBatchTimes.setNestedScrollingEnabled(false);

        adapter.setOnBatchTimeClickListener(item -> {
            editingTimeId = item.timingID;
            populateForm(item);
            switchToEditMode();
        });
    }

    private void saveBatchTime() {
        hideKeyboard();
        showLoader(true);

        PrefManager pref = PrefManager.getInstance(this);
        int userID = Integer.parseInt(pref.getUserId());
        int instituteID = Integer.parseInt(pref.getInstituteId());

        BatchTimeAddRequest request = new BatchTimeAddRequest(
                editingTimeId,
                etDescription.getText().toString().trim(),
                etStartTime.getText().toString().trim(),
                etEndTime.getText().toString().trim(),
                userID,
                instituteID,
                Integer.parseInt(etCapacity.getText().toString().trim()));

        Log.d("BATCH_TIME_SAVE_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().addBatchTime(request)
                .enqueue(new Callback<BatchTimeAddResponse>() {
                    @Override
                    public void onResponse(Call<BatchTimeAddResponse> call,
                                           Response<BatchTimeAddResponse> response) {
                        Log.d("BATCH_TIME_SAVE_RESP", "code=" + response.code()
                                + " body=" + new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {
                            toast(response.body().message);
                            cancelEditMode();
                            loadBatchTimes();
                        } else {
                            showLoader(false);
                            String msg = response.body() != null && response.body().message != null
                                    ? response.body().message : "Save failed";
                            toast(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<BatchTimeAddResponse> call, Throwable t) {
                        showLoader(false);
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    private void loadBatchTimes() {
        showLoader(true);

        PrefManager pref = PrefManager.getInstance(this);
        BatchTimeListRequest request = new BatchTimeListRequest(
                Integer.parseInt(pref.getUserId()),
                Integer.parseInt(pref.getInstituteId()));

        RetrofitClient.getApiService().getBatchTimes(request)
                .enqueue(new Callback<BatchTimeListResponse>() {
                    @Override
                    public void onResponse(Call<BatchTimeListResponse> call,
                                           Response<BatchTimeListResponse> response) {
                        showLoader(false);
                        Log.d("BATCH_TIME_LIST_RESP", "code=" + response.code()
                                + " body=" + new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().batchList != null) {
                            adapter.setData(response.body().batchList);
                            tvTimeCount.setText(response.body().batchList.size() + " Timings");
                            tvTimeCount.setVisibility(response.body().batchList.isEmpty()
                                    ? View.GONE : View.VISIBLE);
                        } else {
                            adapter.setData(null);
                            tvTimeCount.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<BatchTimeListResponse> call, Throwable t) {
                        showLoader(false);
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    private boolean validateForm() {
        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Batch time is required");
            etDescription.requestFocus();
            return false;
        }
        if (etCapacity.getText().toString().trim().isEmpty()) {
            etCapacity.setError("Capacity is required");
            etCapacity.requestFocus();
            return false;
        }
        if (etStartTime.getText().toString().trim().isEmpty()) {
            toast("Please select start time");
            return false;
        }
        if (etEndTime.getText().toString().trim().isEmpty()) {
            toast("Please select end time");
            return false;
        }
        return true;
    }

    private void populateForm(BatchTimeItem item) {
        etDescription.setText(item.timingDescription != null ? item.timingDescription : "");
        etCapacity.setText(String.valueOf(item.capacity));
        etStartTime.setText(item.startTime != null ? item.startTime : "");
        etEndTime.setText(item.endTime != null ? item.endTime : "");
    }

    private void switchToEditMode() {
        tvFormTitle.setText("Edit Batch Time");
        btnSave.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.VISIBLE);
        btnCancelEdit.setVisibility(View.VISIBLE);
    }

    private void cancelEditMode() {
        editingTimeId = 0;
        tvFormTitle.setText("Add Batch Time");
        etDescription.setText("");
        etCapacity.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
        btnSave.setVisibility(View.VISIBLE);
        btnUpdate.setVisibility(View.GONE);
        btnCancelEdit.setVisibility(View.GONE);
        adapter.clearSelection();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String amPm = hourOfDay >= 12 ? "PM" : "AM";
            int hour12 = hourOfDay % 12;
            if (hour12 == 0) hour12 = 12;
            target.setText(String.format(Locale.getDefault(), "%02d.%02d %s", hour12, minute, amPm));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void showLoader(boolean show) {
        loaderLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View focus = getCurrentFocus();
            if (focus != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
