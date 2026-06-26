package com.example.androidproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.androidproject.model.ExtendInquiryRequest;
import com.example.androidproject.model.InquiryResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.utils.ApiService;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.telephony.SmsManager;

public class ExtendActivity extends AppCompatActivity {

    EditText etExtendDate, etFeedback;
    Button btnSave, btnCancel;
    FrameLayout loader;

    // ── Passed via Intent ─────────────────────────────────────────
    private int    studentId    = 0;
    private String studentName  = "";
    private String studentMobile= "";
    private String studentCourses = "";

    private boolean whatsAppOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extend);

        etExtendDate = findViewById(R.id.etExtendDate);
        btnSave      = findViewById(R.id.btnSave);
        btnCancel    = findViewById(R.id.btnCancel);
        loader       = findViewById(R.id.loaderLayout);
        etFeedback   = findViewById(R.id.etFeedback);

        // ── Read intent extras ────────────────────────────────────
        studentId     = getIntent().getIntExtra("studentId",    0);
        studentName   = getIntent().getStringExtra("studentName") != null
                ? getIntent().getStringExtra("studentName") : "";
        studentMobile = getIntent().getStringExtra("mobile") != null
                ? getIntent().getStringExtra("mobile") : "";
        studentCourses= getIntent().getStringExtra("about") != null
                ? getIntent().getStringExtra("about") : "";

        etExtendDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                        etExtendDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> {
            String date = etExtendDate.getText().toString().trim();
            if (date.isEmpty()) {
                Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();
                return;
            }
            callExtendApi(date);
        });
    }

    // ── Extend API ────────────────────────────────────────────────
    private void callExtendApi(String selectedDate) {
        loader.setVisibility(View.VISIBLE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        String operatorId  = PrefManager.getInstance(this).getOperatorId();

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

        RetrofitClient.getApiService().extendInquiry(request)
                .enqueue(new retrofit2.Callback<InquiryResponse>() {

                    @Override
                    public void onResponse(retrofit2.Call<InquiryResponse> call,
                                           retrofit2.Response<InquiryResponse> response) {
                        loader.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            InquiryResponse res = response.body();

                            if (res.isSuccess()) {
                                Toast.makeText(ExtendActivity.this,
                                        res.getMessage(), Toast.LENGTH_SHORT).show();

                                // ── Send template message after success ───
                                sendExtendMessage(selectedDate);

                            } else {
                                Toast.makeText(ExtendActivity.this,
                                        res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ExtendActivity.this,
                                    "Server Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<InquiryResponse> call, Throwable t) {
                        loader.setVisibility(View.GONE);
                        Toast.makeText(ExtendActivity.this,
                                "API Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Template → fill → SMS + WhatsApp ─────────────────────────
    private void sendExtendMessage(String extendedDate) {
        if (studentMobile.isEmpty()) {
            Log.w("ExtendActivity", "No mobile number, skipping message");
            finish();
            return;
        }

        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Inquiry Extend",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {

                                if (!template.isActive) {
                                    Log.d("ExtendActivity", "Template isActive=false, skipping");
                                    Toast.makeText(ExtendActivity.this,
                                            "WhatsApp notifications are currently disabled.",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(ExtendActivity.this);

                                // First name from full name
                                String firstName = studentName.contains(" ")
                                        ? studentName.substring(0, studentName.indexOf(" "))
                                        : studentName;

                                // Format date for display: yyyy-M-d → dd/MM/yyyy
                                String displayDate = formatDateForDisplay(extendedDate);

                                // ── Placeholder map ───────────────────────
                                Map<String, String> data = new HashMap<>();
                                data.put("FirstName",      firstName);
                                data.put("StudentName",    studentName);
                                data.put("InquiryDate",    displayDate);
                                data.put("InquiryCourses", studentCourses);
                                data.put("institute",      pref.getInstituteName());
                                data.put("Authority",      pref.getOwnerName());
                                data.put("mobile1",        pref.getInstituteMobile1());
                                data.put("mobile2",        pref.getInstituteMobile2());
                                data.put("email",          pref.getInstituteEmail());
                                data.put("address1",       pref.getInstituteAddress1());
                                data.put("address2",       pref.getInstituteAddress2());
                                data.put("ownerName",       pref.getOwnerName());

                                // ── Pick language ─────────────────────────
                                String lang = pref.getLanguage();
                                String templateText;
                                switch (lang) {
                                    case "MR": templateText = template.wa_MR; break;
                                    case "HI": templateText = template.wa_HI; break;
                                    default:   templateText = template.wa_EN; break;
                                }

                                String message = TemplateRepository.fillTemplate(templateText, data);

                                Log.d("ExtendActivity", "lang=" + lang);
                                Log.d("ExtendActivity", "mobile=" + studentMobile);
                                Log.d("ExtendActivity", "message=" + message);
                                Log.d("ExtendActivity", "message11=" + firstName + "--" + studentName);


                                // ── SMS ───────────────────────────────────
                                sendSms(studentMobile, message);

                                // ── WhatsApp ──────────────────────────────
                                openWhatsApp(studentMobile, message);
                            }

                            @Override
                            public void onError(String error) {
                                Log.w("ExtendActivity", "Template not found: " + error);
                                Toast.makeText(ExtendActivity.this,
                                        "Message template not found", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
    }

    // ── onResume: finish after returning from WhatsApp ────────────
    @Override
    protected void onResume() {
        super.onResume();
        if (whatsAppOpened) {
            whatsAppOpened = false;
            finish();
        }
    }

    // ── SMS helper ────────────────────────────────────────────────
    private void sendSms(String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("ExtendActivity", "SMS permission not granted");
                return;
            }
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);
        //    sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);

            String formattedNumber = phoneNumber.startsWith("+91") ? phoneNumber
                    : phoneNumber.startsWith("91") ? "+" + phoneNumber
                    : "+91" + phoneNumber;
            sms.sendMultipartTextMessage(formattedNumber, null, parts, null, null);

            Log.d("ExtendActivity", "SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e("ExtendActivity", "SMS failed: " + e.getMessage());
        }
    }

    // ── WhatsApp helper ───────────────────────────────────────────
    private void openWhatsApp(String phoneNumber, String message) {
        whatsAppOpened = true;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
         /*   intent.setData(Uri.parse("https://wa.me/" + phoneNumber
                    + "?text=" + Uri.encode(message)));*/
            String formattedNumber = phoneNumber.startsWith("+91") ? phoneNumber.substring(1)
                    : phoneNumber.startsWith("91") ? phoneNumber
                    : "91" + phoneNumber;
            intent.setData(Uri.parse("https://wa.me/" + formattedNumber
                    + "?text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ── Date formatter: yyyy-M-d → dd/MM/yyyy ────────────────────
    private String formatDateForDisplay(String date) {
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return out.format(in.parse(date));
        } catch (Exception e) {
            return date; // fallback: return as-is
        }
    }
}