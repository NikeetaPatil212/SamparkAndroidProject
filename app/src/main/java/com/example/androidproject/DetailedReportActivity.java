package com.example.androidproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.InquiryReportAdapter;
import com.example.androidproject.model.InquiryItem;
import com.example.androidproject.model.InquiryListRequest;
import com.example.androidproject.model.InquiryListResponse;
import com.example.androidproject.model.summary.InquiryReportItem;
import com.example.androidproject.model.summary.InquiryReportRequest;
import com.example.androidproject.model.summary.InquiryReportResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailedReportActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private EditText       etStudentName, etContactNo, etLocation;
    private Spinner        spCourse;
    private CheckBox       cbAll, cbActive, cbConverted, cbCancelled;
    private MaterialButton btnViewStudents, btnReset, btnSendWhatsappMessage,btnSendSMSMessage;
    private CardView       cardStudentList;
    private RecyclerView   rvInquiries;
    private TextView       tvStudentCount, tvCount;
    private FrameLayout    loaderLayout;

    // ── Data ──────────────────────────────────────────────────────
    // change from List<InquiryItem> to:
    private List<InquiryReportItem> allInquiries = new ArrayList<>();
    private InquiryReportAdapter adapter;

    // ADD these fields after existing fields:
    private List<InquiryReportItem> selectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detailed_report);

        initViews();
        setupBackButton();
        setupCourseSpinner();
        setupStatusCheckboxes();
        setupLiveFilters();
        fetchInquiries();
    }

    // ── Init Views ─────────────────────────────────────────────────
    private void initViews() {
        etStudentName   = findViewById(R.id.etStudentName);
        etContactNo     = findViewById(R.id.etContactNo);
        etLocation      = findViewById(R.id.etLocation);
        spCourse        = findViewById(R.id.spCourse);
        cbAll           = findViewById(R.id.cbAll);
        cbActive        = findViewById(R.id.cbActive);
        cbConverted     = findViewById(R.id.cbConverted);
        cbCancelled     = findViewById(R.id.cbCancelled);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnReset        = findViewById(R.id.btnReset);
        btnSendWhatsappMessage  = findViewById(R.id.btnSendWhatsappMessage);
        btnSendSMSMessage  = findViewById(R.id.btnSendSMSMessage);
        cardStudentList = findViewById(R.id.cardStudentList);
        rvInquiries     = findViewById(R.id.rvInquiries);
        tvStudentCount  = findViewById(R.id.tvStudentCount);
        tvCount         = findViewById(R.id.tvCount);
        loaderLayout    = findViewById(R.id.loaderLayout);

        rvInquiries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InquiryReportAdapter();
        rvInquiries.setAdapter(adapter);

        btnViewStudents.setOnClickListener(v -> fetchInquiries());
        btnReset.setOnClickListener(v -> resetFilters());

        btnSendWhatsappMessage.setOnClickListener(v -> {
            List<InquiryReportItem> selected = adapter.getCheckedItems();
            if (selected.isEmpty()) {
                toast("Please select at least one student");
                return;
            }
            sendWhatsAppBulk(selected);
        });

        btnSendSMSMessage.setOnClickListener(v -> {
            List<InquiryReportItem> selected = adapter.getCheckedItems();
            if (selected.isEmpty()) {
                toast("Please select at least one student");
                return;
            }
            sendSmsBulk(selected);
        });

       /* btnSendMessage.setOnClickListener(v -> {
            List<InquiryItem> selected = adapter.getCheckedItems();
            if (selected.isEmpty()) {
                toast("Please select at least one student");
                return;
            }
            toast("Selected: " + selected.size() + " students");
            // wire send message logic here
        });*/

    }

    private void sendWhatsAppBulk(List<InquiryReportItem> items) {
        // ── Fetch template first, then build messages ─────────────────────────
        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Inquiry Follow Up",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {
                                if (!template.isActive) {
                                    toast("WhatsApp notifications are currently disabled.");
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(DetailedReportActivity.this);
                                String lang = pref.getLanguage();
                                String templateText;
                                switch (lang) {
                                    case "MR": templateText = template.wa_MR; break;
                                    case "HI": templateText = template.wa_HI; break;
                                    default:   templateText = template.wa_EN; break;
                                }

                                String accessToken = template.accessToken;
                                String instanceID  = template.instanceID;

                                List<com.example.androidproject.model.queue.WhatsAppQueueRequest.WhatsAppItem>
                                        whatsappItems = new ArrayList<>();

                                for (InquiryReportItem s : items) {
                                    if (s.mobile == null || s.mobile.isEmpty()) continue;

                                    // ── Fill template placeholders ────────────
                                    String fullName  = s.fullName != null ? s.fullName : "";
                                    String firstName = fullName.contains(" ")
                                            ? fullName.substring(0, fullName.indexOf(" "))
                                            : fullName;

                                    Map<String, String> data = new HashMap<>();
                                    data.put("FirstName",      firstName);
                                    data.put("StudentName",    fullName);
                                    data.put("InquiryDate",    s.inquiryDate != null ? s.inquiryDate : "");
                                    data.put("InquiryCourses", s.about != null ? s.about : "");
                                    data.put("institute",      pref.getInstituteName());
                                    data.put("Authority",      pref.getOwnerName());
                                    data.put("mobile1",        pref.getInstituteMobile1());
                                    data.put("mobile2",        pref.getInstituteMobile2());
                                    data.put("email",          pref.getInstituteEmail());
                                    data.put("address1",       pref.getInstituteAddress1());
                                    data.put("address2",       pref.getInstituteAddress2());
                                    data.put("ownerName",      pref.getOwnerName());

                                    String message = TemplateRepository.fillTemplate(templateText, data);

                                    // ── Format mobile ─────────────────────────
                                    String mobile = s.mobile;
                                    String formattedMobile = mobile.startsWith("+91")
                                            ? mobile.substring(1)
                                            : mobile.startsWith("91") ? mobile
                                            : "91" + mobile;

                                    whatsappItems.add(
                                            new com.example.androidproject.model.queue.WhatsAppQueueRequest
                                                    .WhatsAppItem(formattedMobile, message,
                                                    "manualnotification", accessToken, instanceID));
                                }

                                if (whatsappItems.isEmpty()) {
                                    toast("No valid mobile numbers found");
                                    return;
                                }

                                // ── Call queue API ────────────────────────────
                                com.example.androidproject.model.queue.WhatsAppQueueRequest request =
                                        new com.example.androidproject.model.queue.WhatsAppQueueRequest(
                                                Integer.parseInt(pref.getUserId()),
                                                Integer.parseInt(pref.getInstituteId()),
                                                whatsappItems);

                                loaderLayout.setVisibility(View.VISIBLE);

                                RetrofitClient.getApiService().sendWhatsAppQueue(request)
                                        .enqueue(new Callback<com.example.androidproject.model.queue.WhatsAppQueueResponse>() {
                                            @Override
                                            public void onResponse(
                                                    Call<com.example.androidproject.model.queue.WhatsAppQueueResponse> call,
                                                    Response<com.example.androidproject.model.queue.WhatsAppQueueResponse> response) {
                                                loaderLayout.setVisibility(View.GONE);
                                                if (response.isSuccessful() && response.body() != null
                                                        && response.body().isSuccess) {
                                                    toast("✅ " + response.body().insertedCount
                                                            + " WhatsApp message(s) queued!");
                                                } else {
                                                    toast("❌ Failed to queue WhatsApp messages");
                                                }
                                            }
                                            @Override
                                            public void onFailure(
                                                    Call<com.example.androidproject.model.queue.WhatsAppQueueResponse> call,
                                                    Throwable t) {
                                                loaderLayout.setVisibility(View.GONE);
                                                toast("WhatsApp Failed: " + t.getMessage());
                                            }
                                        });
                            }

                            @Override
                            public void onError(String error) {
                                toast("Template not found: " + error);
                            }
                        });
    }

    private void sendSmsBulk(List<InquiryReportItem> items) {
        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Inquiry Follow Up",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {
                                if (!template.isActive) {
                                    toast("SMS notifications are currently disabled.");
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(DetailedReportActivity.this);
                                String lang = pref.getLanguage();
                                String templateText;
                                switch (lang) {
                                    case "MR": templateText = template.sms_MR; break;
                                    case "HI": templateText = template.sms_HI; break;
                                    default:   templateText = template.sms_EN; break;
                                }

                                List<com.example.androidproject.model.queue.SmsQueueRequest.SmsItem>
                                        smsItems = new ArrayList<>();

                                for (InquiryReportItem s : items) {
                                    if (s.mobile == null || s.mobile.isEmpty()) continue;

                                    String fullName  = s.fullName != null ? s.fullName : "";
                                    String firstName = fullName.contains(" ")
                                            ? fullName.substring(0, fullName.indexOf(" "))
                                            : fullName;

                                    Map<String, String> data = new HashMap<>();
                                    data.put("FirstName",      firstName);
                                    data.put("StudentName",    fullName);
                                    data.put("InquiryDate",    s.inquiryDate != null ? s.inquiryDate : "");
                                    data.put("InquiryCourses", s.about != null ? s.about : "");
                                    data.put("institute",      pref.getInstituteName());
                                    data.put("Authority",      pref.getOwnerName());
                                    data.put("mobile1",        pref.getInstituteMobile1());
                                    data.put("mobile2",        pref.getInstituteMobile2());
                                    data.put("email",          pref.getInstituteEmail());
                                    data.put("address1",       pref.getInstituteAddress1());
                                    data.put("address2",       pref.getInstituteAddress2());
                                    data.put("ownerName",      pref.getOwnerName());

                                    String message = TemplateRepository.fillTemplate(templateText, data);

                                    String mobile = s.mobile;
                                    String formattedMobile = mobile.startsWith("+91")
                                            ? mobile.substring(1)
                                            : mobile.startsWith("91") ? mobile
                                            : "91" + mobile;

                                    smsItems.add(
                                            new com.example.androidproject.model.queue.SmsQueueRequest
                                                    .SmsItem(formattedMobile, message,
                                                    "manualnotification"));
                                }

                                if (smsItems.isEmpty()) {
                                    toast("No valid mobile numbers found");
                                    return;
                                }

                                com.example.androidproject.model.queue.SmsQueueRequest request =
                                        new com.example.androidproject.model.queue.SmsQueueRequest(
                                                Integer.parseInt(pref.getUserId()),
                                                Integer.parseInt(pref.getInstituteId()),
                                                smsItems);

                                loaderLayout.setVisibility(View.VISIBLE);

                                RetrofitClient.getApiService().sendSmsQueue(request)
                                        .enqueue(new Callback<com.example.androidproject.model.queue.SmsQueueResponse>() {
                                            @Override
                                            public void onResponse(
                                                    Call<com.example.androidproject.model.queue.SmsQueueResponse> call,
                                                    Response<com.example.androidproject.model.queue.SmsQueueResponse> response) {
                                                loaderLayout.setVisibility(View.GONE);
                                                if (response.isSuccessful() && response.body() != null
                                                        && response.body().isSuccess) {
                                                    toast("✅ " + response.body().insertedCount
                                                            + " SMS message(s) queued!");
                                                } else {
                                                    toast("❌ Failed to queue SMS messages");
                                                }
                                            }
                                            @Override
                                            public void onFailure(
                                                    Call<com.example.androidproject.model.queue.SmsQueueResponse> call,
                                                    Throwable t) {
                                                loaderLayout.setVisibility(View.GONE);
                                                toast("SMS Failed: " + t.getMessage());
                                            }
                                        });
                            }

                            @Override
                            public void onError(String error) {
                                toast("Template not found: " + error);
                            }
                        });
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    // ── Course Spinner ─────────────────────────────────────────────
    private void setupCourseSpinner() {
        List<String> courses = new ArrayList<>();
        courses.add("All Courses");
        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, courses);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCourse.setAdapter(aa);
    }

    // ── Status checkboxes ──────────────────────────────────────────
    private void setupStatusCheckboxes() {
        cbAll.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                cbActive.setChecked(false);
                cbConverted.setChecked(false);
                cbCancelled.setChecked(false);
            }
            applyFilters();
        });
        cbActive.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) cbAll.setChecked(false);
            applyFilters();
        });
        cbConverted.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) cbAll.setChecked(false);
            applyFilters();
        });
        cbCancelled.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) cbAll.setChecked(false);
            applyFilters();
        });
    }

    // ── Live text filters ──────────────────────────────────────────
    private void setupLiveFilters() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                applyFilters();
            }
        };
        etStudentName.addTextChangedListener(watcher);
        etContactNo.addTextChangedListener(watcher);
        etLocation.addTextChangedListener(watcher);
    }

    // ── Apply all filters ──────────────────────────────────────────
    private void applyFilters() {
        if (allInquiries.isEmpty()) return; // don't filter before data loads

        List<String> statuses = new ArrayList<>();
        if (cbAll.isChecked())       statuses.add("All");
        if (cbActive.isChecked())    statuses.add("Active");
        if (cbConverted.isChecked()) statuses.add("Converted");
        if (cbCancelled.isChecked()) {
            statuses.add("Cancelled");
            statuses.add("Aborted");
        }

        String course = spCourse.getSelectedItemPosition() == 0 ? ""
                : spCourse.getSelectedItem().toString();

        adapter.applyFilters(
                etStudentName.getText().toString(),
                etContactNo.getText().toString(),
                etLocation.getText().toString(),
                course,
                statuses
        );
        updateCount();
    }

    // ── Fetch from API ─────────────────────────────────────────────
    private void fetchInquiries() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStudentList.setVisibility(View.GONE);
        btnSendSMSMessage.setVisibility(View.GONE);
        btnSendWhatsappMessage.setVisibility(View.GONE);
        allInquiries.clear();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        InquiryReportRequest request = new InquiryReportRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        Log.d("INQ_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService()
                .getInquiryReport(request)
                .enqueue(new Callback<InquiryReportResponse>() {

                    @Override
                    public void onResponse(Call<InquiryReportResponse> call,
                                           Response<InquiryReportResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("INQ_REPORT", "HTTP=" + response.code());
                        Log.d("INQ_REPORT", "body=" + new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {

                            allInquiries = response.body().inquiryList;

                            if (allInquiries == null || allInquiries.isEmpty()) {
                                toast("No inquiries found");
                                return;
                            }

                            adapter.setData(allInquiries);
                            applyFilters();
                            cardStudentList.setVisibility(View.VISIBLE);
                            btnSendSMSMessage.setVisibility(View.VISIBLE);
                            btnSendWhatsappMessage.setVisibility(View.VISIBLE);
                            updateCount();

                        } else {
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "Unknown error";
                                Log.e("INQ_REPORT", "errorBody=" + err);
                                toast("Failed: " + err);
                            } catch (Exception e) {
                                toast("Failed to load inquiries");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<InquiryReportResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.e("INQ_REPORT", "onFailure: " + t.getMessage());
                        toast("Error: " + t.getMessage());
                    }
                });
    }

    // ── Helpers ────────────────────────────────────────────────────
    private void updateCount() {
        int count = adapter.getFilteredCount();
        String text = count + " Records";
        tvCount.setText(text);
        tvStudentCount.setText(text);
        tvStudentCount.setVisibility(View.VISIBLE);
    }

    private void resetFilters() {
        etStudentName.setText("");
        etContactNo.setText("");
        etLocation.setText("");
        spCourse.setSelection(0);
        cbAll.setChecked(true);
        cbActive.setChecked(false);
        cbConverted.setChecked(false);
        cbCancelled.setChecked(false);
        applyFilters();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}