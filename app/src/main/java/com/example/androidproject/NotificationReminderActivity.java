package com.example.androidproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.NotificationAdapter;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.model.notification.NotificationStudent;
import com.example.androidproject.model.notification.NotificationStudentRequest;
import com.example.androidproject.model.notification.NotificationStudentResponse;
import com.example.androidproject.model.queue.SmsQueueRequest;
import com.example.androidproject.model.queue.SmsQueueResponse;
import com.example.androidproject.model.queue.WhatsAppQueueRequest;
import com.example.androidproject.model.queue.WhatsAppQueueResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationReminderActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView spSendTo, spCourse, spBatch, spTiming;
    private MaterialButton btnViewStudents;
    private LinearLayout layoutSendButtons;
    private CheckBox cbSelectAll;
    private RecyclerView rvStudents;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private androidx.cardview.widget.CardView cardStudentList;
    private TextInputEditText etHeader, etMessage;

    private NotificationAdapter adapter;

    // All students fetched from API
    private List<NotificationStudent> allStudents = new ArrayList<>();

    // Unique filter values extracted from allStudents
    private List<String> courseNames  = new ArrayList<>();
    private List<String> batchNames   = new ArrayList<>();
    private List<String> timingNames  = new ArrayList<>();

    // Selected filter values (null = All)
    private String selectedSendTo = "All";
    private Integer selectedCourseID  = null;
    private String  selectedBatchName = null;
    private String  selectedTimingName= null;

    private List<Course> courseList = new ArrayList<>();
    private LinearLayout layoutSelectFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_reminder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        spSendTo        = findViewById(R.id.spSendTo);
        spCourse        = findViewById(R.id.spCourse);
        spBatch         = findViewById(R.id.spBatch);
        spTiming        = findViewById(R.id.spTiming);
        btnViewStudents = findViewById(R.id.btnViewStudents);
    //    btnSendMessage  = findViewById(R.id.btnSendMessage);
        cbSelectAll     = findViewById(R.id.cbSelectAll);
        rvStudents      = findViewById(R.id.rvStudents);
        progressBar     = findViewById(R.id.progressBar);
        tvEmpty         = findViewById(R.id.tvEmpty);
        cardStudentList = findViewById(R.id.cardStudentList);
        etHeader        = findViewById(R.id.etHeader);
        etMessage       = findViewById(R.id.etMessage);
        layoutSelectFilters = findViewById(R.id.layoutSelectFilters);

        layoutSendButtons = findViewById(R.id.layoutSendButtons);
        MaterialButton btnSendWhatsApp = findViewById(R.id.btnSendWhatsApp);
        MaterialButton btnSendSMS      = findViewById(R.id.btnSendSMS);
        MaterialButton btnSendBoth     = findViewById(R.id.btnSendBoth);

        btnSendWhatsApp.setOnClickListener(v -> handleSend(true,  false));
        btnSendSMS.setOnClickListener(     v -> handleSend(false, true));
        btnSendBoth.setOnClickListener(    v -> handleSend(true,  true));

        adapter = new NotificationAdapter();
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);
        rvStudents.setNestedScrollingEnabled(false);


        // In onCreate, after finding views:
        spBatch.setEnabled(false);
        spTiming.setEnabled(false);


        setupSendToDropdown();
        fetchCourses();

        btnViewStudents.setOnClickListener(v -> fetchStudents());

        cbSelectAll.setOnCheckedChangeListener((btn, checked) -> adapter.selectAll(checked));


        btnSendWhatsApp.setOnClickListener(v -> handleSend(true,  false));
        btnSendSMS.setOnClickListener(     v -> handleSend(false, true));
        btnSendBoth.setOnClickListener(    v -> handleSend(true,  true));
    }


    // ── Remove pendingSmsMessage from NotificationStudent — not needed ──
// We build messages inline here instead

    private void handleSend(boolean sendWhatsApp, boolean sendSms) {

        // ── Validate message ──
        String header  = etHeader.getText()  != null
                ? etHeader.getText().toString().trim()  : "";
        String message = etMessage.getText() != null
                ? etMessage.getText().toString().trim() : "";

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Get selected students ──
        List<NotificationStudent> selected = adapter.getSelectedStudents();
        if (selected.isEmpty()) {
            Toast.makeText(this,
                    "Please select at least one student", Toast.LENGTH_SHORT).show();
            return;
        }

        PrefManager pref       = PrefManager.getInstance(this);
        String      accessToken = "678fa99d21c01";
        String      instanceID  = "67E4E625E5B52";

        // ── Build personalised lists ──
        List<WhatsAppQueueRequest.WhatsAppItem> whatsappItems = new ArrayList<>();
        List<SmsQueueRequest.SmsItem>           smsItems      = new ArrayList<>();

        for (NotificationStudent s : selected) {
            // "Dear FirstName, message" — with optional header on top
            String firstName    = (s.getFirstName() != null && !s.getFirstName().isEmpty())
                    ? s.getFirstName() : s.getFullName();
            String personalised = "Dear " + firstName + ", " + message;
            if (!header.isEmpty()) {
                personalised = header + "\n" + personalised;
            }

            Log.d("NOTIF_SEND", "To: " + s.getMobile() + " | Msg: " + personalised);

            if (sendWhatsApp) {
                whatsappItems.add(new WhatsAppQueueRequest.WhatsAppItem(
                        s.getMobile(), personalised,
                        "manualnotification", accessToken, instanceID));
            }
            if (sendSms) {
                smsItems.add(new SmsQueueRequest.SmsItem(
                        s.getMobile(), personalised, "manualnotification"));
            }
        }

        // ── Fire APIs ──
        if (sendWhatsApp && !whatsappItems.isEmpty()) {
            sendWhatsAppBulk(whatsappItems);
        }
        if (sendSms && !smsItems.isEmpty()) {
            sendSmsBulk(smsItems);
        }

        etHeader.setText("");
        etMessage.setText("");
        cbSelectAll.setChecked(false);

// ── Uncheck all students in allStudents list directly ──
        for (NotificationStudent s : allStudents) {
            s.setSelected(false);
        }
// ── Re-apply filter to refresh adapter with unchecked state ──
        applyFiltersAndShow();
    }

    // ── WhatsApp bulk API ─────────────────────────────────────────────
    private void sendWhatsAppBulk(List<WhatsAppQueueRequest.WhatsAppItem> items) {
        PrefManager pref = PrefManager.getInstance(this);

        WhatsAppQueueRequest request = new WhatsAppQueueRequest(
                Integer.parseInt(pref.getUserId()),
                Integer.parseInt(pref.getInstituteId()),
                items);

        Log.d("NOTIF_WA", "WA Bulk request: " + new Gson().toJson(request));

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Queuing WhatsApp messages...");
        pd.setCancelable(false);
        pd.show();

        RetrofitClient.getApiService().sendWhatsAppQueue(request)
                .enqueue(new Callback<WhatsAppQueueResponse>() {

                    @Override
                    public void onResponse(Call<WhatsAppQueueResponse> call,
                                           Response<WhatsAppQueueResponse> response) {
                        pd.dismiss();

                        if (response.isSuccessful() && response.body() != null) {
                            WhatsAppQueueResponse res = response.body();
                            Log.d("NOTIF_WA", "Response: " + res.message
                                    + " | inserted: " + res.insertedCount);

                            if (res.isSuccess) {
                                Toast.makeText(NotificationReminderActivity.this,
                                        "✅ " + res.insertedCount
                                                + " WhatsApp message(s) queued!",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(NotificationReminderActivity.this,
                                        "❌ " + res.message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(NotificationReminderActivity.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<WhatsAppQueueResponse> call, Throwable t) {
                        pd.dismiss();
                        Log.e("NOTIF_WA", "WA Failed: " + t.getMessage());
                        Toast.makeText(NotificationReminderActivity.this,
                                "WhatsApp Failed: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── SMS bulk API ──────────────────────────────────────────────────
    private void sendSmsBulk(List<SmsQueueRequest.SmsItem> items) {
        PrefManager pref = PrefManager.getInstance(this);

        SmsQueueRequest request = new SmsQueueRequest(
                Integer.parseInt(pref.getUserId()),
                Integer.parseInt(pref.getInstituteId()),
                items);

        Log.d("NOTIF_SMS", "SMS Bulk request: " + new Gson().toJson(request));

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Queuing SMS messages...");
        pd.setCancelable(false);
        pd.show();

        RetrofitClient.getApiService().sendSmsQueue(request)
                .enqueue(new Callback<SmsQueueResponse>() {

                    @Override
                    public void onResponse(Call<SmsQueueResponse> call,
                                           Response<SmsQueueResponse> response) {
                        pd.dismiss();

                        if (response.isSuccessful() && response.body() != null) {
                            SmsQueueResponse res = response.body();
                            Log.d("NOTIF_SMS", "Response: " + res.message
                                    + " | inserted: " + res.insertedCount);

                            if (res.isSuccess) {
                                Toast.makeText(NotificationReminderActivity.this,
                                        "✅ " + res.insertedCount
                                                + " SMS message(s) queued!",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(NotificationReminderActivity.this,
                                        "❌ " + res.message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(NotificationReminderActivity.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SmsQueueResponse> call, Throwable t) {
                        pd.dismiss();
                        Log.e("NOTIF_SMS", "SMS Failed: " + t.getMessage());
                        Toast.makeText(NotificationReminderActivity.this,
                                "SMS Failed: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // ── Send To dropdown (All / Select) ──────────────────────────
    private void setupSendToDropdown() {
        List<String> options = Arrays.asList("All", "Select");
        ArrayAdapter<String> a = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, options);
        spSendTo.setAdapter(a);
        spSendTo.setOnClickListener(v -> spSendTo.showDropDown());

        spSendTo.setOnItemClickListener((p, v, pos, id) -> {
            selectedSendTo = options.get(pos);

            if (selectedSendTo.equals("All")) {
                // ── "All" selected ──
                // Hide course/batch/timing filters
                layoutSelectFilters.setVisibility(View.GONE);

                // Reset all filter selections
                selectedCourseID   = null;
                selectedBatchName  = null;
                selectedTimingName = null;
                spCourse.setText("");
                spBatch.setText("");
                spTiming.setText("");

                // Show View Students directly
                btnViewStudents.setVisibility(View.VISIBLE);

            } else {
                // ── "Select" selected ──
                // Show course/batch/timing filters
                layoutSelectFilters.setVisibility(View.VISIBLE);

                // Show View Students
                btnViewStudents.setVisibility(View.VISIBLE);

                // Load courses if not loaded yet
                if (courseList.isEmpty()) {
                    fetchCourses();
                }
            }

            // Reset student list when Send To changes
            cardStudentList.setVisibility(View.GONE);
            layoutSendButtons.setVisibility(View.GONE);
            allStudents.clear();
            adapter.setData(new ArrayList<>());
            cbSelectAll.setChecked(false);
        });
    }

    // ── Fetch courses from API (same as AdmissionActivity) ───────
    private void fetchCourses() {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        GetCoursesRequest request = new GetCoursesRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getCourses(request)
                .enqueue(new Callback<GetCoursesResponse>() {
                    @Override
                    public void onResponse(Call<GetCoursesResponse> call,
                                           Response<GetCoursesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            courseList = response.body().getCouseList();
                            setupCourseDropdown();
                        }
                    }
                    @Override
                    public void onFailure(Call<GetCoursesResponse> call, Throwable t) {
                        Log.e("NOTIF", "Course fetch failed: " + t.getMessage());
                    }
                });
    }

    private void setupCourseDropdown() {
        List<String> names = new ArrayList<>();
        names.add("All Courses");
        for (Course c : courseList) names.add(c.getCouse_Name());

        ArrayAdapter<String> a = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spCourse.setAdapter(a);
        spCourse.setOnClickListener(v -> spCourse.showDropDown());

        spCourse.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedCourseID = null;
            } else {
                Course c = courseList.get(position - 1);
                selectedCourseID = c.getCouseID();
            }

            // ── Reset batch + timing since course changed ──
            selectedBatchName  = null;
            selectedTimingName = null;
            spBatch.setText("");
            spTiming.setText("");
            spTiming.setEnabled(false);

            // ── Rebuild batch dropdown for new course ──
            if (!allStudents.isEmpty()) {
                populateBatchDropdown();
                applyFiltersAndShow(); // re-filter with new course
            }
        });
    }

    // ── Fetch all students from API ───────────────────────────────
    private void fetchStudents() {
        cardStudentList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        layoutSendButtons.setVisibility(View.GONE);
        cbSelectAll.setChecked(false);
        allStudents.clear();
        adapter.setData(new ArrayList<>());

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        NotificationStudentRequest request = new NotificationStudentRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getNotificationStudents(request)
                .enqueue(new Callback<NotificationStudentResponse>() {

                    @Override
                    public void onResponse(Call<NotificationStudentResponse> call,
                                           Response<NotificationStudentResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {

                            List<NotificationStudent> fetched =
                                    response.body().getStudentList();

                            if (fetched == null || fetched.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                return;
                            }

                            allStudents.addAll(fetched);
                            Log.d("NOTIF", "Total fetched: " + allStudents.size());

                            // ── Rebuild batch dropdown based on current course filter ──
                            populateBatchDropdown();

                            // ── If batch already selected, rebuild timing too ──
                            if (selectedBatchName != null) {
                                populateTimingDropdown();
                            }

                            // ── Apply whatever filters are currently active ──
                            applyFiltersAndShow();

                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<NotificationStudentResponse> call,
                                          Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(NotificationReminderActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Build batch dropdown from loaded student data ─────────────
    private void populateBatchDropdown() {
        LinkedHashSet<String> batches = new LinkedHashSet<>();
        for (NotificationStudent s : allStudents) {
            if (selectedCourseID == null || s.getCourseID() == selectedCourseID) {
                batches.add(s.getBatchName());
            }
        }

        List<String> batchList = new ArrayList<>();
        batchList.add("All Batches");
        batchList.addAll(batches);

        spBatch.setEnabled(true);
        spBatch.setFocusable(false);          // keyboard fix
        spBatch.setFocusableInTouchMode(false);

        ArrayAdapter<String> a = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, batchList);
        spBatch.setAdapter(a);
        spBatch.setOnClickListener(v -> spBatch.showDropDown());

        spBatch.setOnItemClickListener((p, v, pos, id) -> {
            // ── Update selected batch ──
            selectedBatchName  = pos == 0 ? null : batchList.get(pos);
            selectedTimingName = null;
            spTiming.setText("");

            // ── Rebuild timing for selected batch ──
            populateTimingDropdown();

            // ── Apply filter immediately ──
            applyFiltersAndShow();
        });
    }

    private void populateTimingDropdown() {
        LinkedHashSet<String> timings = new LinkedHashSet<>();
        for (NotificationStudent s : allStudents) {
            boolean courseMatch = selectedCourseID == null
                    || s.getCourseID() == selectedCourseID;
            boolean batchMatch  = selectedBatchName == null
                    || s.getBatchName().equals(selectedBatchName);
            if (courseMatch && batchMatch) {
                timings.add(s.getTimingDescription());
            }
        }

        List<String> timingList = new ArrayList<>();
        timingList.add("All Timings");
        timingList.addAll(timings);

        spTiming.setEnabled(true);
        spTiming.setFocusable(false);          // keyboard fix
        spTiming.setFocusableInTouchMode(false);

        ArrayAdapter<String> a = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, timingList);
        spTiming.setAdapter(a);
        spTiming.setOnClickListener(v -> spTiming.showDropDown());

        spTiming.setOnItemClickListener((p, v, pos, id) -> {
            selectedTimingName = pos == 0 ? null : timingList.get(pos);
            applyFiltersAndShow();
        });
    }

    // ── Filter allStudents and update RecyclerView ────────────────
    private void applyFiltersAndShow() {
        List<NotificationStudent> filtered = new ArrayList<>();

        for (NotificationStudent s : allStudents) {
            boolean courseOk = selectedCourseID  == null
                    || s.getCourseID()           == selectedCourseID;
            boolean batchOk  = selectedBatchName == null
                    || s.getBatchName().equals(selectedBatchName);
            boolean timingOk = selectedTimingName== null
                    || s.getTimingDescription().equals(selectedTimingName);

            if (courseOk && batchOk && timingOk) filtered.add(s);
        }

        Log.d("NOTIF", "Filtered: " + filtered.size());

        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            layoutSendButtons.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            layoutSendButtons.setVisibility(View.VISIBLE);
        }

        cbSelectAll.setChecked(false);
        adapter.setData(filtered);
    }
}