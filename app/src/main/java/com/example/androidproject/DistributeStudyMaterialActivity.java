package com.example.androidproject;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.StudyMaterialAdapter;
import com.example.androidproject.model.Batch;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.profile.BatchTimingResponse;
import com.example.androidproject.model.profile.StudyMaterialDistributionResponse;
import com.example.androidproject.model.profile.StudyMaterialUpdateRequest;
import com.example.androidproject.model.profile.StudyMaterialUpdateResponse;
import com.example.androidproject.model.queue.SmsQueueRequest;
import com.example.androidproject.model.queue.SmsQueueResponse;
import com.example.androidproject.model.queue.WhatsAppQueueRequest;
import com.example.androidproject.model.queue.WhatsAppQueueResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.room.BatchEntity;
import com.example.androidproject.room.CourseBatchRepository;
import com.example.androidproject.room.CourseEntity;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DistributeStudyMaterialActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private Spinner        spCourse, spBatch, spTiming;
    private ImageView      ivBatchIcon, ivTimingIcon;
    private TextView       tvDate, tvStudentCount;
    private CardView       cardStudentList;
    private RecyclerView   rvStudents;
    private FrameLayout    loaderLayout;
    private MaterialButton btnViewStudents, btnUpdateRecord;
    private CheckBox       cbSelectAll;

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<Course> courseList  = new ArrayList<>();
    private List<Batch>  batchList   = new ArrayList<>();
    private List<BatchTimingResponse.BatchTimingItem> timingList = new ArrayList<>();
    private List<StudyMaterialDistributionResponse.StudentItem> allStudents = new ArrayList<>();

    private int selectedCourseId = -1;
    private int selectedBatchId  = -1;
    private int selectedTimingId = -1;

    // ── Spinner ready flags ───────────────────────────────────────────────────
    private boolean courseSpinnerReady = false;
    private boolean batchSpinnerReady  = false;
    private boolean timingSpinnerReady = false;

    // ── WhatsApp queue ────────────────────────────────────────────────────────
    private List<String> whatsAppQueue   = new ArrayList<>();
    private List<String> whatsAppMobiles = new ArrayList<>();
    private boolean      sendingWhatsApp = false;

    private StudyMaterialAdapter adapter;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_distribute_study_material);

        initViews();
        setupBackButton();
        setTodayDate();
        fetchCourses();
    }

    // ── Init Views ────────────────────────────────────────────────────────────
    private void initViews() {
        spCourse        = findViewById(R.id.spCourse);
        spBatch         = findViewById(R.id.spBatch);
        spTiming        = findViewById(R.id.spTiming);
        ivBatchIcon     = findViewById(R.id.ivBatchIcon);
        ivTimingIcon    = findViewById(R.id.ivTimingIcon);
        tvDate          = findViewById(R.id.tvDate);
        tvStudentCount  = findViewById(R.id.tvStudentCount);
        cardStudentList = findViewById(R.id.cardStudentList);
        rvStudents      = findViewById(R.id.rvStudents);
        loaderLayout    = findViewById(R.id.loaderLayout);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnUpdateRecord = findViewById(R.id.btnUpdateRecord);
        cbSelectAll     = findViewById(R.id.cbSelectAll);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyMaterialAdapter();
        rvStudents.setAdapter(adapter);

        spBatch.setEnabled(false);
        spTiming.setEnabled(false);
        btnViewStudents.setEnabled(false);

        cbSelectAll.setOnCheckedChangeListener((btn, isChecked) ->
                adapter.setAllChecked(isChecked));

        btnViewStudents.setOnClickListener(v -> fetchStudents());

        btnUpdateRecord.setOnClickListener(v -> performUpdate());
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
    }

    // ── Fetch Courses from Room DB ────────────────────────────────────────────
    private void fetchCourses() {
        CourseBatchRepository.getInstance(this).getCourses(courses -> {
            courseList.clear();
            for (CourseEntity e : courses) {
                courseList.add(new Course(e.courseId, e.courseName, "", "", 0, 0));
            }
            setupCourseSpinner();
        });
    }

    // ── Fetch Batches from Room DB ────────────────────────────────────────────
    private void fetchBatches(int courseId) {
        CourseBatchRepository.getInstance(this).getBatchesByCourse(courseId, batches -> {
            batchList.clear();
            for (BatchEntity e : batches) {
                batchList.add(new Batch(e.batchId, courseId, e.batchName, "", "", ""));
            }
            setupBatchSpinner();
        });
    }

    // ── Course Spinner ────────────────────────────────────────────────────────
    private void setupCourseSpinner() {
        List<String> names = new ArrayList<>();
        names.add("Select Course --");
        for (Course c : courseList) names.add(c.getCouse_Name());

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCourse.setAdapter(aa);

        courseSpinnerReady = false;

        spCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (!courseSpinnerReady) { courseSpinnerReady = true; return; }
                if (pos == 0) {
                    resetBatch();
                    resetTiming();
                    hideStudentList();
                    return;
                }
                selectedCourseId = courseList.get(pos - 1).getCouseID();
                resetBatch();
                resetTiming();
                hideStudentList();
                fetchBatches(selectedCourseId);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Batch Spinner ─────────────────────────────────────────────────────────
    private void setupBatchSpinner() {
        List<String> names = new ArrayList<>();
        names.add("Select Batch --");
        for (Batch b : batchList) names.add(b.getBatchName());

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBatch.setAdapter(aa);
        spBatch.setEnabled(true);

        ivBatchIcon.setColorFilter(
                getResources().getColor(android.R.color.holo_green_dark, getTheme()));

        batchSpinnerReady = false;

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (!batchSpinnerReady) { batchSpinnerReady = true; return; }
                if (pos == 0) {
                    resetTiming();
                    hideStudentList();
                    btnViewStudents.setEnabled(false);
                    return;
                }
                selectedBatchId = batchList.get(pos - 1).getBatchID();
                resetTiming();
                hideStudentList();
                btnViewStudents.setEnabled(true);
                setupTimingSpinner();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Timing Spinner ────────────────────────────────────────────────────────
    private void setupTimingSpinner() {
        List<String> names = new ArrayList<>();
        names.add("All Timings");
        for (BatchTimingResponse.BatchTimingItem t : timingList) {
            names.add(t.getTimingDescription());
        }

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTiming.setAdapter(aa);
        spTiming.setEnabled(true);

        ivTimingIcon.setColorFilter(
                getResources().getColor(android.R.color.holo_green_dark, getTheme()));

        timingSpinnerReady = false;

        spTiming.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (!timingSpinnerReady) { timingSpinnerReady = true; return; }
                if (pos == 0) {
                    selectedTimingId = -1;
                } else {
                    selectedTimingId = timingList.get(pos - 1).getTimingID();
                }
                if (!allStudents.isEmpty()) applyTimingFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Fetch Students ────────────────────────────────────────────────────────
    private void fetchStudents() {
        if (selectedCourseId == -1 || selectedBatchId == -1) {
            toast("Please select Course and Batch");
            return;
        }

        loaderLayout.setVisibility(View.VISIBLE);
        hideStudentList();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        Log.d("API_REQUEST_PARAMS",
                "userID=" + userId +
                        ", instituteID=" + instituteId +
                        ", courseID=" + selectedCourseId +
                        ", batchID=" + selectedBatchId);

        RetrofitClient.getApiService()
                .getDistributionList(
                        Integer.parseInt(userId),
                        Integer.parseInt(instituteId),
                        selectedCourseId,
                        selectedBatchId)
                .enqueue(new Callback<StudyMaterialDistributionResponse>() {

                    @Override
                    public void onResponse(Call<StudyMaterialDistributionResponse> call,
                                           Response<StudyMaterialDistributionResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("API_RESPONSE_CODE", String.valueOf(response.code()));
                        Log.d("RESPONSE_BODY", new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            allStudents = response.body().getStudents();

                            if (allStudents == null || allStudents.isEmpty()) {
                                toast("No students found");
                                return;
                            }
                            applyTimingFilter();

                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().getMessage()
                                    : "Failed to fetch students";
                            toast(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StudyMaterialDistributionResponse> call,
                                          Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.e("API_ERROR", t.getMessage(), t);
                        toast("API Failed: " + t.getMessage());
                    }
                });
    }

    // ── Timing filter ─────────────────────────────────────────────────────────
    private void applyTimingFilter() {
        List<StudyMaterialDistributionResponse.StudentItem> filtered;

        if (selectedTimingId == -1) {
            filtered = allStudents;
        } else {
            filtered = new ArrayList<>();
            for (StudyMaterialDistributionResponse.StudentItem s : allStudents) {
                filtered.add(s); // add timingID field filter here when API supports it
            }
        }

        cardStudentList.setVisibility(View.VISIBLE);
        tvStudentCount.setText(filtered.size() + " Students");
        adapter.setData(filtered);
        cbSelectAll.setChecked(false);
    }

    // ── Update Record → then queue WhatsApp + SMS ─────────────────────────────
    private void performUpdate() {
        List<StudyMaterialDistributionResponse.StudentItem> checkedStudents =
                adapter.getCheckedStudents();

        if (checkedStudents.isEmpty()) {
            toast("Please select at least one student");
            return;
        }

        List<Integer> checkedIds = new ArrayList<>();
        for (StudyMaterialDistributionResponse.StudentItem s : checkedStudents) {
            checkedIds.add(s.getAdmissionID());
        }

        loaderLayout.setVisibility(View.VISIBLE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        SimpleDateFormat iso = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        String materialDate = iso.format(new Date());

        StudyMaterialUpdateRequest request = new StudyMaterialUpdateRequest(
                checkedIds,
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                materialDate
        );

        RetrofitClient.getApiService()
                .updateStudyMaterial(request)
                .enqueue(new Callback<StudyMaterialUpdateResponse>() {

                    @Override
                    public void onResponse(Call<StudyMaterialUpdateResponse> call,
                                           Response<StudyMaterialUpdateResponse> response) {
                        loaderLayout.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            toast("✅ " + response.body().getMessage());
                            buildAndSendQueue(checkedStudents);

                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().getMessage()
                                    : "Update failed";
                            toast(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StudyMaterialUpdateResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        toast("Failed: " + t.getMessage());
                    }
                });
    }

    // ── Build queue and fire SMS for all, then start WhatsApp one-by-one ──────
/*    private void buildAndSendQueue(
            List<StudyMaterialDistributionResponse.StudentItem> students) {

        whatsAppQueue.clear();
        whatsAppMobiles.clear();

        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date());

        for (StudyMaterialDistributionResponse.StudentItem s : students) {
            String mobile = s.getMobile();
            if (mobile == null || mobile.trim().isEmpty()) continue;

            mobile = mobile.replaceAll("[^0-9]", "");
            if (!mobile.startsWith("91") && mobile.length() == 10) {
                mobile = "91" + mobile;
            }

            String message =
                    "Dear " + s.getStudentName() + ",\n\n" +
                            "📚 Study material has been distributed to you on " + today + ".\n\n" +
                            "Please collect it from the institute.\n\n" +
                            "Thank you!";

            // SMS fires silently for every student right away
            sendSmsInBackground(mobile, message);

            // WhatsApp queue — opened one-by-one via onResume
            whatsAppMobiles.add(mobile);
            whatsAppQueue.add(message);
        }

        if (!whatsAppQueue.isEmpty()) {
            sendingWhatsApp = true;
            sendNextWhatsApp();
        } else {
            // No valid numbers — just refresh
            fetchStudents();
        }
    }*/


    private void buildAndSendQueue(
            List<StudyMaterialDistributionResponse.StudentItem> students) {

        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Study Material",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {

                                if (!template.isActive) {
                                    Log.d("StudyMaterial", "Template isActive=false, skipping");
                                    toast("Notifications are currently disabled.");
                                    fetchStudents();
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(
                                        DistributeStudyMaterialActivity.this);
                                String lang = pref.getLanguage();

                                List<WhatsAppQueueRequest.WhatsAppItem> waItems  = new ArrayList<>();
                                List<SmsQueueRequest.SmsItem>           smsItems = new ArrayList<>();

                                for (StudyMaterialDistributionResponse.StudentItem s : students) {
                                    String mobile = s.getMobile();
                                    if (mobile == null || mobile.trim().isEmpty()) continue;

                                    mobile = mobile.replaceAll("[^0-9]", "");
                                    if (!mobile.startsWith("91") && mobile.length() == 10) {
                                        mobile = "91" + mobile;
                                    }

                                    // ── Fill placeholder map ──────────────────
                                    Map<String, String> data = new HashMap<>();
                                    data.put("StudentName", s.getStudentName() != null
                                            ? s.getStudentName() : "");
                                  /*  data.put("course",     s.getCourseName()   != null
                                            ? s.getCourseName()   : "");*/
                                    data.put("institute",  pref.getInstituteName());
                                    data.put("Authority",  pref.getStudentName());
                                    data.put("mobile1",    pref.getInstituteMobile1());
                                    data.put("mobile2",    pref.getInstituteMobile2());
                                    data.put("email",      pref.getInstituteEmail());
                                    data.put("address1",   pref.getInstituteAddress1());
                                    data.put("address2",   pref.getInstituteAddress2());

                                    // ── Pick WA text by language ──────────────
                                    String waText;
                                    switch (lang) {
                                        case "MR": waText = template.wa_MR; break;
                                        case "HI": waText = template.wa_HI; break;
                                        default:   waText = template.wa_EN; break;
                                    }

                                    // ── Pick SMS text by language ─────────────
                                    String smsText;
                                    switch (lang) {
                                        case "MR": smsText = template.sms_MR; break;
                                        case "HI": smsText = template.sms_HI; break;
                                        default:   smsText = template.sms_EN; break;
                                    }

                                    String waMessage  = TemplateRepository.fillTemplate(waText,  data);
                                    String smsMessage = TemplateRepository.fillTemplate(smsText, data);

                                    Log.d("StudyMaterial", "mobile="     + mobile);
                                    Log.d("StudyMaterial", "waMessage="  + waMessage);
                                    Log.d("StudyMaterial", "smsMessage=" + smsMessage);

                                    // WhatsApp queue item
                                    waItems.add(new WhatsAppQueueRequest.WhatsAppItem(
                                            mobile,
                                            waMessage,
                                            "Study Material",
                                            template.accessToken != null ? template.accessToken : "",
                                            template.instanceID  != null ? template.instanceID  : ""
                                    ));

                                    // SMS queue item
                                    smsItems.add(new SmsQueueRequest.SmsItem(
                                            mobile,
                                            smsMessage,
                                            "Study Material"
                                    ));
                                }

                                if (waItems.isEmpty() && smsItems.isEmpty()) {
                                    toast("No valid mobile numbers found");
                                    fetchStudents();
                                    return;
                                }

                                // Post both queues — WA first, then SMS
                                postToWhatsAppQueue(waItems, smsItems);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("StudyMaterial", "Template not found: " + error);
                                toast("Message template not found");
                                fetchStudents();
                            }
                        });
    }

    // ── POST queue items to API ───────────────────────────────────────────────────
    private void postToWhatsAppQueue(
            List<WhatsAppQueueRequest.WhatsAppItem> waItems,
            List<SmsQueueRequest.SmsItem>           smsItems) {

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        WhatsAppQueueRequest request = new WhatsAppQueueRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                waItems
        );

        Log.d("WA_QUEUE_REQ", new Gson().toJson(request));
        loaderLayout.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService()
                .sendWhatsAppQueue(request)
                .enqueue(new Callback<WhatsAppQueueResponse>() {

                    @Override
                    public void onResponse(Call<WhatsAppQueueResponse> call,
                                           Response<WhatsAppQueueResponse> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {
                            Log.d("WA_QUEUE", "✅ WA queued: "
                                    + response.body().insertedCount);
                            toast("✅ " + response.body().message);
                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().message : "WA Queue API failed";
                            Log.e("WA_QUEUE", "❌ " + msg);
                            toast("WA Queue failed: " + msg);
                        }

                        // Always chain SMS queue regardless of WA result
                        postToSmsQueue(smsItems);
                    }

                    @Override
                    public void onFailure(Call<WhatsAppQueueResponse> call, Throwable t) {
                        Log.e("WA_QUEUE", "❌ " + t.getMessage());
                        toast("WA Queue error: " + t.getMessage());
                        // Still attempt SMS queue
                        postToSmsQueue(smsItems);
                    }
                });
    }

    private void postToSmsQueue(List<SmsQueueRequest.SmsItem> smsItems) {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        SmsQueueRequest request = new SmsQueueRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                smsItems
        );

        Log.d("SMS_QUEUE_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService()
                .sendSmsQueue(request)
                .enqueue(new Callback<SmsQueueResponse>() {

                    @Override
                    public void onResponse(Call<SmsQueueResponse> call,
                                           Response<SmsQueueResponse> response) {
                        loaderLayout.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {
                            Log.d("SMS_QUEUE", "✅ SMS queued: "
                                    + response.body().insertedCount);
                            toast("✅ " + response.body().message);
                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().message : "SMS Queue API failed";
                            Log.e("SMS_QUEUE", "❌ " + msg);
                            toast("SMS Queue failed: " + msg);
                        }

                        fetchStudents();
                    }

                    @Override
                    public void onFailure(Call<SmsQueueResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.e("SMS_QUEUE", "❌ " + t.getMessage());
                        toast("SMS Queue error: " + t.getMessage());
                        fetchStudents();
                    }
                });
    }

    // ── Open WhatsApp for the next student in queue ───────────────────────────
    private void sendNextWhatsApp() {
        if (whatsAppQueue.isEmpty()) {
            sendingWhatsApp = false;
            fetchStudents();
            toast("✅ All messages sent");
            return;
        }

        String message = whatsAppQueue.remove(0);
        String mobile  = whatsAppMobiles.remove(0);

        try {
            Uri uri = Uri.parse(
                    "https://api.whatsapp.com/send?phone=" + mobile
                            + "&text=" + Uri.encode(message));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (Exception e) {
            // WhatsApp not installed — try browser fallback
            try {
                Uri uri = Uri.parse(
                        "https://api.whatsapp.com/send?phone=" + mobile
                                + "&text=" + Uri.encode(message));
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception ex) {
                Log.e("WHATSAPP", "Failed for " + mobile + ": " + ex.getMessage());
                // Skip this student, continue queue immediately
                sendNextWhatsApp();
            }
        }
        // onResume will pick up the next entry when user returns from WhatsApp
    }

    // ── SMS — fires silently in background ────────────────────────────────────
    private void sendSmsInBackground(String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("SMS", "SEND_SMS permission not granted");
                return;
            }
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);

            PendingIntent sentIntent = PendingIntent.getBroadcast(
                    this, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("SMS_DEBUG", getResultCode() == Activity.RESULT_OK
                            ? "✅ SMS sent to " + phoneNumber
                            : "❌ SMS failed: " + getResultCode());
                }
            }, new IntentFilter("SMS_SENT"));

            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) sentIntents.add(sentIntent);

            smsManager.sendMultipartTextMessage(
                    phoneNumber, null, parts, sentIntents, null);

        } catch (Exception e) {
            Log.e("SMS", "SMS failed for " + phoneNumber + ": " + e.getMessage());
        }
    }

    // ── onResume — called when user returns from WhatsApp ────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        if (sendingWhatsApp) {
            sendNextWhatsApp();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void hideStudentList() {
        cardStudentList.setVisibility(View.GONE);
        allStudents.clear();
    }

    private void resetBatch() {
        batchList.clear();
        selectedBatchId   = -1;
        batchSpinnerReady = false;

        ArrayAdapter<String> empty = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        empty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBatch.setAdapter(empty);
        spBatch.setEnabled(false);

        ivBatchIcon.setColorFilter(
                getResources().getColor(android.R.color.darker_gray, getTheme()));
        btnViewStudents.setEnabled(false);
    }

    private void resetTiming() {
        selectedTimingId   = -1;
        timingSpinnerReady = false;

        ArrayAdapter<String> empty = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        empty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTiming.setAdapter(empty);
        spTiming.setEnabled(false);

        ivTimingIcon.setColorFilter(
                getResources().getColor(android.R.color.darker_gray, getTheme()));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}