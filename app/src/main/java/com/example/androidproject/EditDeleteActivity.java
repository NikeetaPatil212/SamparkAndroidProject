package com.example.androidproject;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.InquiryResponse;
import com.example.androidproject.model.UpdateStudentRequest;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDeleteActivity extends AppCompatActivity {

    EditText etFirstName, etMiddleName, etLastName, etFullName;
    EditText etMobile, etAlternateNo, etEmail, etAddress;

    String inquiryDate = "";

    Button btnSave, btnCancel;
    View loaderLayout;

    int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete);

        // ✅ INIT VIEWS
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etFullName = findViewById(R.id.etFullName);
        etMobile = findViewById(R.id.etMobile);
        etAlternateNo = findViewById(R.id.etAlternateNo);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loaderLayout = findViewById(R.id.loaderLayout);

        // ✅ TOOLBAR BACK
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ✅ GET DATA FROM INTENT
        studentId = getIntent().getIntExtra("studentId", 0);

        String fullName = getIntent().getStringExtra("name");

        etFullName.setText(fullName);
        etMobile.setText(getIntent().getStringExtra("mobile"));
        etAlternateNo.setText(getIntent().getStringExtra("altMobile"));
        etEmail.setText(getIntent().getStringExtra("email"));
        etAddress.setText(getIntent().getStringExtra("address"));
        inquiryDate = getIntent().getStringExtra("inquiry_date");


        // ✅ SPLIT FULL NAME → f_name, m_name, l_name
        if (fullName != null) {
            String[] parts = fullName.split(" ");

            etFirstName.setText(parts.length > 0 ? parts[0] : "");
            etMiddleName.setText(parts.length > 1 ? parts[1] : "");
            etLastName.setText(parts.length > 2 ? parts[2] : "");
        }

        // ✅ SAVE CLICK
        btnSave.setOnClickListener(v -> callUpdateApi());

        // ✅ CANCEL CLICK
        btnCancel.setOnClickListener(v -> finish());
    }

    private void callUpdateApi() {
        loaderLayout.setVisibility(VISIBLE);

        String userId = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        // ✅ BUILD FULL NAME
        String fName = etFirstName.getText().toString().trim();
        String mName = etMiddleName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();

        String fullName = fName + " " + mName + " " + lName;

        etFullName.setText(fullName); // optional

        UpdateStudentRequest request = new UpdateStudentRequest();

        request.fName = fName;
        request.mName = mName;
        request.lName = lName;
        request.fullName = fullName;
        request.mobile = etMobile.getText().toString().trim();
        request.alternateNo = etAlternateNo.getText().toString().trim();
        request.emailID = etEmail.getText().toString().trim();
        request.address = etAddress.getText().toString().trim();
        request.imgurl = "";
        request.inquiryDate = inquiryDate; // optional if not needed
        request.studentID = studentId;
        request.userID = Integer.parseInt(userId);
        request.instituteID = Integer.parseInt(instituteId);

        Log.d("UPDATE_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService().updateStudent(request)
                .enqueue(new Callback<InquiryResponse>() {

                    @Override
                    public void onResponse(Call<InquiryResponse> call,
                                           Response<InquiryResponse> response) {

                        loaderLayout.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {

                            Toast.makeText(EditDeleteActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                            finish();

                        } else {
                            Toast.makeText(EditDeleteActivity.this,
                                    "Update failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<InquiryResponse> call, Throwable t) {

                        loaderLayout.setVisibility(View.GONE);

                        Toast.makeText(EditDeleteActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}