package com.example.androidproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.androidproject.model.ExtendInquiryRequest;
import com.example.androidproject.model.InquiryResponse;
import com.example.androidproject.utils.ApiService;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExtendActivity extends AppCompatActivity {

    EditText etExtendDate,etFeedback;
    Button btnSave, btnCancel;

    FrameLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extend);


        etExtendDate = findViewById(R.id.etExtendDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loader = findViewById(R.id.loaderLayout);
        etFeedback = findViewById(R.id.etFeedback);

        etExtendDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                        etExtendDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });

    btnSave.setOnClickListener(v ->

    {

        String date = etExtendDate.getText().toString();

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();
            return;
        }

        callExtendApi(date);
    });
}


    private void callExtendApi(String selectedDate) {

        // ✅ SHOW LOADER
        loader.setVisibility(View.VISIBLE);

        String userId = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        String operatorId = PrefManager.getInstance(this).getOperatorId();

        int studentId = getIntent().getIntExtra("studentId", 0);

        // ✅ Convert date to API format
        String reminderDate = selectedDate + "T00:00:00.000Z";

        ExtendInquiryRequest request = new ExtendInquiryRequest(
                studentId,
                etFeedback.getText().toString(),
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                Integer.parseInt(operatorId),
                reminderDate
        );

        Log.d("EXTEND_REQ", new Gson().toJson(request));

        ApiService apiService = RetrofitClient.getApiService();

        apiService.extendInquiry(request).enqueue(new Callback<InquiryResponse>() {

            @Override
            public void onResponse(Call<InquiryResponse> call,
                                   Response<InquiryResponse> response) {

                // ✅ HIDE LOADER
                loader.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {

                    InquiryResponse res = response.body();

                    if (res.isSuccess()) {

                        Toast.makeText(ExtendActivity.this,
                                res.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        finish(); // go back

                    } else {
                        Toast.makeText(ExtendActivity.this,
                                res.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ExtendActivity.this,
                            "Server Error",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InquiryResponse> call, Throwable t) {

                // ✅ HIDE LOADER
                loader.setVisibility(View.GONE);

                Toast.makeText(ExtendActivity.this,
                        "API Failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}