package com.example.androidproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.BirthdayAdapter;
import com.example.androidproject.model.certificate.BirthdayRequest;
import com.example.androidproject.model.certificate.BirthdayResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BirthdayReminderActivity extends AppCompatActivity {

    private RecyclerView    rvBirthdays;
    private ProgressBar     progressBar;
    private TextView        tvEmpty, tvSelectedDate;
    private MaterialButton  btnShow;
    private BirthdayAdapter adapter;

    private boolean      wishSent = false;
    private String       currentMobile;
    private List<String> whatsAppMessageQueue = new ArrayList<>();
    private Calendar     selectedCalendar     = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday_reminder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvEmpty        = findViewById(R.id.tvEmpty);
        progressBar    = findViewById(R.id.progressBar);
        rvBirthdays    = findViewById(R.id.rvBirthdays);
        btnShow        = findViewById(R.id.btnShow);

        rvBirthdays.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BirthdayAdapter(this::onWishClick);
        rvBirthdays.setAdapter(adapter);

        // Default = today
        updateDateDisplay();

        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        btnShow.setOnClickListener(v -> fetchBirthdays());
    }

    private void updateDateDisplay() {
        tvSelectedDate.setText(
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(selectedCalendar.getTime()));
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedCalendar.set(year, month, day);
                    updateDateDisplay();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ── Fetch ─────────────────────────────────────────────────────
    private void fetchBirthdays() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        adapter.setData(new ArrayList<>());

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        String isoDate = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(selectedCalendar.getTime());

        Log.d("BIRTHDAY", "Fetching for date: " + isoDate);

        BirthdayRequest request = new BirthdayRequest(
                isoDate,
                Integer.parseInt(userId),
                Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getBirthdayList(request)
                .enqueue(new Callback<BirthdayResponse>() {

                    @Override
                    public void onResponse(Call<BirthdayResponse> call,
                                           Response<BirthdayResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        Log.d("BIRTHDAY", "Response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            BirthdayResponse res = response.body();
                            Log.d("BIRTHDAY", "isSuccess: " + res.isSuccess()
                                    + " | message: " + res.getMessage());

                            if (res.isSuccess()) {
                                List<BirthdayResponse.BirthdayStudent> list =
                                        res.getStudentList();

                                if (list == null || list.isEmpty()) {
                                    tvEmpty.setVisibility(View.VISIBLE);
                                } else {
                                    Log.d("BIRTHDAY", "Students found: " + list.size());
                                    tvEmpty.setVisibility(View.GONE);
                                    adapter.setData(list);
                                }
                            } else {
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(BirthdayReminderActivity.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BirthdayResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("BIRTHDAY", "API failed: " + t.getMessage());
                        Toast.makeText(BirthdayReminderActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Individual wish ───────────────────────────────────────────
    private void onWishClick(BirthdayResponse.BirthdayStudent student) {
        currentMobile = student.getMobile();
        whatsAppMessageQueue.clear();

        PrefManager pref = PrefManager.getInstance(this);

        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Birthday",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {
                                if (!template.isActive) {
                                    Toast.makeText(BirthdayReminderActivity.this,
                                            "Birthday notifications are disabled.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Map<String, String> data = new HashMap<>();
                                data.put("studentName", student.getStudentName() != null
                                        ? student.getStudentName() : "");
                                data.put("institute",   pref.getInstituteName());
                                data.put("Authority",   pref.getOwnerName());
                                data.put("mobile1",     pref.getInstituteMobile1());
                                data.put("mobile2",     pref.getInstituteMobile2());
                                data.put("email",       pref.getInstituteEmail());
                                data.put("address1",    pref.getInstituteAddress1());
                                data.put("address2",    pref.getInstituteAddress2());

                                String lang = pref.getLanguage();
                                String templateText;
                                switch (lang) {
                                    case "MR": templateText = template.wa_MR; break;
                                    case "HI": templateText = template.wa_HI; break;
                                    default:   templateText = template.wa_EN; break;
                                }

                                String message = TemplateRepository
                                        .fillTemplate(templateText, data);

                                Log.d("BIRTHDAY_DEBUG", "Sending to: " + currentMobile);
                                Log.d("BIRTHDAY_DEBUG", "Message: " + message);

                                // SMS in background
                                sendSmsInBackground(currentMobile, message);

                                // WhatsApp
                                whatsAppMessageQueue.add(message);
                                wishSent = true;
                                sendNextQueuedWhatsApp();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(BirthdayReminderActivity.this,
                                        "Template not found: " + error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // ── WhatsApp ──────────────────────────────────────────────────
    private void sendNextQueuedWhatsApp() {
        if (whatsAppMessageQueue.isEmpty()) {
            wishSent = false;
            return;
        }
        String msg = whatsAppMessageQueue.remove(0);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + currentMobile
                    + "?text=" + Uri.encode(msg)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            wishSent = false;
            whatsAppMessageQueue.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wishSent) {
            if (!whatsAppMessageQueue.isEmpty()) {
                sendNextQueuedWhatsApp();
            } else {
                wishSent = false;
                Toast.makeText(this, "🎂 Wish sent!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── SMS ───────────────────────────────────────────────────────
    private void sendSmsInBackground(String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("SMS", "Permission not granted — skipping SMS");
                return;
            }
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            PendingIntent sentIntent = PendingIntent.getBroadcast(
                    this, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) sentIntents.add(sentIntent);
            smsManager.sendMultipartTextMessage(
                    phoneNumber, null, parts, sentIntents, null);
            Log.d("SMS_DEBUG", "Birthday SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e("SMS_DEBUG", "SMS failed: " + e.getMessage());
        }
    }
}