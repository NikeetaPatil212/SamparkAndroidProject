package com.example.androidproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

import com.bumptech.glide.Glide;
import com.example.androidproject.adapters.CertificateStudentAdapter;
import com.example.androidproject.model.Batch;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.certificate.CertificateRequest;
import com.example.androidproject.model.certificate.CertificateResponse;
import com.example.androidproject.model.certificate.CertificateStudent;
import com.example.androidproject.model.certificate.UpdateCertificateRequest;
import com.example.androidproject.model.certificate.UpdateCertificateResponse;
import com.example.androidproject.model.certificate.UploadImageResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.room.BatchEntity;
import com.example.androidproject.room.CourseBatchRepository;
import com.example.androidproject.room.CourseEntity;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CertificateHandOverActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private Spinner        spCourse, spBatch;
    private ImageView      ivBatchIcon;
    private EditText       etStudentName;
    private ImageView      ivClearSearch;
    private MaterialButton btnViewStudents;
    private CardView       cardStudentList;
    private RecyclerView   rvStudents;
    private TextView       tvStudentCount, tvCount;
    private FrameLayout    loaderLayout;

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<Course> courseList = new ArrayList<>();
    private List<Batch>  batchList  = new ArrayList<>();
    private List<CertificateStudent> allStudents = new ArrayList<>();

    private int selectedCourseId = -1;
    private String selectedBatchName = ""; // batch is optional filter — filter client-side

    // ── Spinner ready flags ───────────────────────────────────────────────────
    private boolean courseSpinnerReady = false;
    private boolean batchSpinnerReady  = false;

    private CertificateStudentAdapter adapter;

    private static final int REQUEST_CAMERA  = 101;
    private static final int REQUEST_GALLERY = 102;
    private Uri      capturedImageUri;
    private String   uploadedImageUrl = "";
    private AlertDialog currentDialog;
    private int      editingPosition  = -1;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_certificate_hand_over);

        initViews();
        setupBackButton();
        setupRowClick();
        fetchCourses();
    }

    // ── Init Views ────────────────────────────────────────────────────────────
    private void initViews() {
        spCourse        = findViewById(R.id.spCourse);
        spBatch         = findViewById(R.id.spBatch);
        ivBatchIcon     = findViewById(R.id.ivBatchIcon);
        etStudentName   = findViewById(R.id.etStudentName);
        ivClearSearch   = findViewById(R.id.ivClearSearch);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        cardStudentList = findViewById(R.id.cardStudentList);
        rvStudents      = findViewById(R.id.rvStudents);
        tvStudentCount  = findViewById(R.id.tvStudentCount);
        tvCount         = findViewById(R.id.tvCount);
        loaderLayout    = findViewById(R.id.loaderLayout);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CertificateStudentAdapter(this);
        rvStudents.setAdapter(adapter);

        spBatch.setEnabled(false);
        btnViewStudents.setEnabled(false);

        // Live name search
        etStudentName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                ivClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                adapter.filterByName(query);
                updateCount();
            }
        });

        // Clear search button
        ivClearSearch.setOnClickListener(v -> etStudentName.setText(""));

        // View Students button
        btnViewStudents.setOnClickListener(v -> fetchStudents());
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
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
                    selectedCourseId = -1;
                    resetBatch();
                    hideStudentList();
                    btnViewStudents.setEnabled(false);
                    return;
                }
                selectedCourseId = courseList.get(pos - 1).getCouseID();
                resetBatch();
                hideStudentList();
                btnViewStudents.setEnabled(true);
                fetchBatchesFromRoom(selectedCourseId);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Fetch Batches from Room DB ────────────────────────────────────────────
    private void fetchBatchesFromRoom(int courseId) {
        CourseBatchRepository.getInstance(this).getBatchesByCourse(courseId, batches -> {
            batchList.clear();
            for (BatchEntity e : batches) {
                batchList.add(new Batch(e.batchId, courseId, e.batchName, "", "", ""));
            }
            setupBatchSpinner();
        });
    }

    // ── Batch Spinner (optional filter) ──────────────────────────────────────
    private void setupBatchSpinner() {
        List<String> names = new ArrayList<>();
        names.add("All Batches");
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
                    selectedBatchName = ""; // All batches
                } else {
                    selectedBatchName = batchList.get(pos - 1).getBatchName();
                }
                // If students already loaded, filter client-side immediately
                if (!allStudents.isEmpty()) applyBatchFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Fetch Students from API ───────────────────────────────────────────────
    private void fetchStudents() {
        if (selectedCourseId == -1) {
            toast("Please select a course");
            return;
        }

        loaderLayout.setVisibility(View.VISIBLE);
        hideStudentList();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        CertificateRequest request = new CertificateRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                selectedCourseId
        );

        Log.d("CERT_REQUEST", new Gson().toJson(request));

        RetrofitClient.getApiService()
                .getCertificateStudents(request)
                .enqueue(new Callback<CertificateResponse>() {

                    @Override
                    public void onResponse(Call<CertificateResponse> call,
                                           Response<CertificateResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("CERT_RESPONSE", new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {

                            allStudents = response.body().studentList;

                            if (allStudents == null || allStudents.isEmpty()) {
                                toast("No students found");
                                return;
                            }

                            // Reset name search
                            etStudentName.setText("");

                            adapter.setData(allStudents);
                            applyBatchFilter(); // apply batch filter if selected

                            cardStudentList.setVisibility(View.VISIBLE);
                            updateCount();

                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().message
                                    : "Failed to fetch students";
                            toast(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<CertificateResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.e("CERT_ERROR", t.getMessage(), t);
                        toast("API Failed: " + t.getMessage());
                    }
                });
    }

    // ── Apply batch filter client-side ────────────────────────────────────────
    private void applyBatchFilter() {
        adapter.filterByBatch(selectedBatchName);
        updateCount();
    }

    // ── Update count in toolbar and card header ───────────────────────────────
    private void updateCount() {
        int count = adapter.getFilteredCount();
        String text = count + " Students";
        tvCount.setText(text);
        tvStudentCount.setText(text);
        tvStudentCount.setVisibility(View.VISIBLE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void hideStudentList() {
        cardStudentList.setVisibility(View.GONE);
        allStudents.clear();
        tvStudentCount.setVisibility(View.GONE);
    }

    private void resetBatch() {
        batchList.clear();
        selectedBatchName  = "";
        batchSpinnerReady  = false;

        ArrayAdapter<String> empty = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        empty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBatch.setAdapter(empty);
        spBatch.setEnabled(false);

        ivBatchIcon.setColorFilter(
                getResources().getColor(android.R.color.darker_gray, getTheme()));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ── Fields needed ─────────────────────────────────────────────────────────────


    // ── Call this in onCreate after setting adapter ───────────────────────────────
    private void setupRowClick() {
        adapter.setOnRowClickListener((student, position) -> {
            editingPosition = position;
            showCertificateDialog(student);
        });
    }

    // ── Show Dialog ───────────────────────────────────────────────────────────────
    private void showCertificateDialog(CertificateStudent student) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_certificate_edit, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        currentDialog = builder.create();
        currentDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        currentDialog.show();

        // ── Bind views ────────────────────────────────────────────────
        TextView        tvName        = dialogView.findViewById(R.id.tvDialogStudentName);
        TextView        tvAdmInfo     = dialogView.findViewById(R.id.tvDialogAdmInfo);
        EditText        etCertId      = dialogView.findViewById(R.id.etCertificateId);
        EditText        etIssuer      = dialogView.findViewById(R.id.etIssuer);
        EditText        etIssuerContact = dialogView.findViewById(R.id.etIssuerContact);
        EditText        etIssueDate   = dialogView.findViewById(R.id.etIssueDate);
        AutoCompleteTextView etResult = dialogView.findViewById(R.id.etResult);
        EditText        etPercentage  = dialogView.findViewById(R.id.etPercentage);
        ImageView       ivIssuerImg   = dialogView.findViewById(R.id.ivIssuerImg);
        TextView        tvImageStatus = dialogView.findViewById(R.id.tvImageStatus);
        MaterialButton  btnCapture    = dialogView.findViewById(R.id.btnCaptureImage);
        MaterialButton  btnGallery    = dialogView.findViewById(R.id.btnPickImage);
        MaterialButton  btnSave       = dialogView.findViewById(R.id.btnSaveCertificate);
        FrameLayout     dialogLoader  = dialogView.findViewById(R.id.dialogLoader);
        ImageButton     btnClose      = dialogView.findViewById(R.id.btnCloseDialog);

        // ── Pre-fill existing data ────────────────────────────────────
        tvName.setText(student.studentName);
        tvAdmInfo.setText("Adm ID: " + student.admissionID
                + "  |  " + student.course + "  |  " + student.batch);

        if (student.certificateID != null && !student.certificateID.isEmpty())
            etCertId.setText(student.certificateID);
        if (student.issuer != null && !student.issuer.isEmpty())
            etIssuer.setText(student.issuer);
        if (student.issuerContact != null && !student.issuerContact.isEmpty())
            etIssuerContact.setText(student.issuerContact);
        if (student.issueDate != null && !student.issueDate.isEmpty())
            etIssueDate.setText(student.issueDate);
        if (student.result != null && !student.result.isEmpty())
            etResult.setText(student.result);
        if (student.percentage != null)
            etPercentage.setText(String.valueOf(student.percentage.intValue()));

        // Load existing image if available
        uploadedImageUrl = student.issuerImg != null ? student.issuerImg : "";
        if (!uploadedImageUrl.isEmpty()) {
            Glide.with(this).load(uploadedImageUrl).into(ivIssuerImg);
            tvImageStatus.setText("✅ Image uploaded");
            tvImageStatus.setVisibility(View.VISIBLE);
        }

        // ── Result dropdown ───────────────────────────────────────────
        String[] results = {"Pass", "Fail", "Distinction", "First Class", "Second Class"};
        ArrayAdapter<String> resultAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, results);
        etResult.setAdapter(resultAdapter);
        etResult.setOnClickListener(v -> etResult.showDropDown());

        // ── Issue Date picker ─────────────────────────────────────────
        etIssueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                etIssueDate.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── Camera ────────────────────────────────────────────────────
        btnCapture.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "CertIssuer_" + System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            capturedImageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        });

        // ── Gallery ───────────────────────────────────────────────────
        btnGallery.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(pickIntent, REQUEST_GALLERY);
        });

        // ── Close ─────────────────────────────────────────────────────
        btnClose.setOnClickListener(v -> currentDialog.dismiss());

        // ── Save ──────────────────────────────────────────────────────
        btnSave.setOnClickListener(v -> {
            String certId   = etCertId.getText().toString().trim();
            String issuer   = etIssuer.getText().toString().trim();
            String contact  = etIssuerContact.getText().toString().trim();
            String issueDate= etIssueDate.getText().toString().trim();
            String result   = etResult.getText().toString().trim();
            String percStr  = etPercentage.getText().toString().trim();

            if (certId.isEmpty())   { etCertId.setError("Required");    return; }
            if (issuer.isEmpty())   { etIssuer.setError("Required");    return; }
            if (issueDate.isEmpty()){ etIssueDate.setError("Required"); return; }
            if (result.isEmpty())   { etResult.setError("Required");    return; }

            double percentage = percStr.isEmpty() ? 0 : Double.parseDouble(percStr);

            // ISO date
            String isoDate = issueDate + "T00:00:00.000Z";

            PrefManager pref = PrefManager.getInstance(this);

            dialogLoader.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            UpdateCertificateRequest request = new UpdateCertificateRequest(
                    student.admissionID, certId, issuer, isoDate, result,
                    percentage, contact, uploadedImageUrl,
                    Integer.parseInt(pref.getUserId()),
                    Integer.parseInt(pref.getInstituteId()),
                    Integer.parseInt(pref.getOperatorId())
            );

            Log.d("CERT_UPDATE_REQ", new Gson().toJson(request));

            RetrofitClient.getApiService()
                    .updateCertificate(request)
                    .enqueue(new Callback<UpdateCertificateResponse>() {

                        @Override
                        public void onResponse(Call<UpdateCertificateResponse> call,
                                               Response<UpdateCertificateResponse> response) {
                            dialogLoader.setVisibility(View.GONE);
                            btnSave.setEnabled(true);

                            Log.d("CERT_UPDATE_RAW", "HTTP=" + response.code());
                            Log.d("CERT_UPDATE_RAW", "body=" + new Gson().toJson(response.body()));

                           /* if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess) {

                                Toast.makeText(CertificateHandOverActivity.this,
                                        "✅ Certificate updated!", Toast.LENGTH_SHORT).show();

                                // ── Update local model so list reflects immediately ──
                                student.certificateID  = certId;
                                student.issuer         = issuer;
                                student.issuerContact  = contact;
                                student.issueDate      = issueDate;
                                student.result         = result;
                                student.percentage     = percentage;
                                student.issuerImg      = uploadedImageUrl;
                                student.certificate    = true;
                                adapter.notifyItemChanged(editingPosition);

                                currentDialog.dismiss();

                            } */

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess) {

                                Toast.makeText(CertificateHandOverActivity.this,
                                        "✅ Certificate updated!", Toast.LENGTH_SHORT).show();

                                // ── Update local model so list reflects immediately ──
                                student.certificateID  = certId;
                                student.issuer         = issuer;
                                student.issuerContact  = contact;
                                student.issueDate      = issueDate;
                                student.result         = result;
                                student.percentage     = percentage;
                                student.issuerImg      = uploadedImageUrl;
                                student.certificate    = true;
                                adapter.notifyItemChanged(editingPosition);

                                currentDialog.dismiss();

                                // ── Send WhatsApp + SMS via template ─────────────────
                                sendCertificateMessage(student, certId);

                            } else {
                                String msg = response.body() != null
                                        ? response.body().message : "Update failed";
                                Toast.makeText(CertificateHandOverActivity.this,
                                        "❌ " + msg, Toast.LENGTH_SHORT).show();
                            }

                           /* else {
                                String msg = response.body() != null
                                        ? response.body().message : "Update failed";
                                Toast.makeText(CertificateHandOverActivity.this,
                                        "❌ " + msg, Toast.LENGTH_SHORT).show();
                            }*/
                        }

                        @Override
                        public void onFailure(Call<UpdateCertificateResponse> call, Throwable t) {
                            dialogLoader.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(CertificateHandOverActivity.this,
                                    "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // ── Certificate WhatsApp + SMS ────────────────────────────────────────────────
    private void sendCertificateMessage(CertificateStudent student, String certId) {

        String mobile = student.mobile;
        if (mobile == null || mobile.trim().isEmpty()) {
            Log.w("CERT_MSG", "No mobile, skipping message");
            return;
        }

        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Certificate",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {

                                if (!template.isActive) {
                                    Log.d("CERT_MSG", "Template isActive=false, skipping");
                                    Toast.makeText(CertificateHandOverActivity.this,
                                            "Notifications are currently disabled.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(
                                        CertificateHandOverActivity.this);

                                // ── Fill placeholder map ──────────────────────
                                Map<String, String> data = new HashMap<>();
                                data.put("StudentName",       student.studentName != null
                                        ? student.studentName : "");
                                data.put("course",            student.course != null
                                        ? student.course : "");
                                data.put("CertificateNumber", certId);
                                data.put("receiver",          student.issuer != null
                                        ? student.issuer : "");
                                data.put("institute",         pref.getInstituteName());
                                data.put("Authority",         pref.getOwnerName());
                                data.put("mobile1",           pref.getInstituteMobile1());
                                data.put("mobile2",           pref.getInstituteMobile2());
                                data.put("email",             pref.getInstituteEmail());
                                data.put("address1",          pref.getInstituteAddress1());
                                data.put("address2",          pref.getInstituteAddress2());

                                // ── Pick language ─────────────────────────────
                                String lang = pref.getLanguage();
                                String waText, smsText;
                                switch (lang) {
                                    case "MR":
                                        waText  = template.wa_MR;
                                        smsText = template.sms_MR;
                                        break;
                                    case "HI":
                                        waText  = template.wa_HI;
                                        smsText = template.sms_HI;
                                        break;
                                    default:
                                        waText  = template.wa_EN;
                                        smsText = template.sms_EN;
                                        break;
                                }

                                String waMessage  = TemplateRepository.fillTemplate(waText,  data);
                                String smsMessage = TemplateRepository.fillTemplate(smsText, data);

                                Log.d("CERT_MSG", "mobile="     + mobile);
                                Log.d("CERT_MSG", "waMessage="  + waMessage);
                                Log.d("CERT_MSG", "smsMessage=" + smsMessage);

                                // ── Format mobile ─────────────────────────────
                                String formattedMobile = mobile.replaceAll("[^0-9]", "");
                                if (!formattedMobile.startsWith("91")
                                        && formattedMobile.length() == 10) {
                                    formattedMobile = "91" + formattedMobile;
                                }

                                // ── SMS ───────────────────────────────────────
                                sendCertSms(formattedMobile, smsMessage);

                                // ── WhatsApp ──────────────────────────────────
                                openCertWhatsApp(formattedMobile, waMessage);
                            }

                            @Override
                            public void onError(String error) {
                                Log.w("CERT_MSG", "Template not found: " + error);
                                Toast.makeText(CertificateHandOverActivity.this,
                                        "Message template not found", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // ── SMS helper ────────────────────────────────────────────────────────────────
    private void sendCertSms(String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("CERT_MSG", "SMS permission not granted");
                return;
            }
            android.telephony.SmsManager sms =
                    android.telephony.SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);
            sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            Log.d("CERT_MSG", "✅ SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e("CERT_MSG", "SMS failed: " + e.getMessage());
        }
    }

    // ── WhatsApp helper ───────────────────────────────────────────────────────────
    private void openCertWhatsApp(String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + phoneNumber
                    + "?text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Handle image pick/capture result ─────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || currentDialog == null
                || !currentDialog.isShowing()) return;

        Uri imageUri = null;
        if (requestCode == REQUEST_CAMERA) {
            imageUri = capturedImageUri;
        } else if (requestCode == REQUEST_GALLERY && data != null) {
            imageUri = data.getData();
        }

        if (imageUri == null) return;

        // Show preview
        ImageView ivIssuerImg = currentDialog.findViewById(R.id.ivIssuerImg);
        if (ivIssuerImg != null)
            Glide.with(this).load(imageUri).into(ivIssuerImg);

        // Upload to server
        uploadImageToServer(imageUri);
    }

    // ── Upload image ──────────────────────────────────────────────────────────────
    private void uploadImageToServer(Uri imageUri) {
        TextView tvImageStatus = currentDialog != null
                ? currentDialog.findViewById(R.id.tvImageStatus) : null;

        if (tvImageStatus != null) {
            tvImageStatus.setText("⏳ Uploading image...");
            tvImageStatus.setVisibility(View.VISIBLE);
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("image/jpeg"), bytes);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "file", "issuer_" + System.currentTimeMillis() + ".jpg", requestBody);

            RetrofitClient.getApiService()
                    .uploadCertificateImage(filePart)
                    .enqueue(new Callback<UploadImageResponse>() {

                        @Override
                        public void onResponse(Call<UploadImageResponse> call,
                                               Response<UploadImageResponse> response) {
                            Log.d("IMG_UPLOAD", "HTTP=" + response.code());
                            try {
                                Log.d("IMG_UPLOAD", "errorBody=" +
                                        (response.errorBody() != null ? response.errorBody().string() : "null"));
                            } catch (Exception e) { e.printStackTrace(); }
                            Log.d("IMG_UPLOAD", "body=" + new Gson().toJson(response.body()));

                            if (response.isSuccessful() && response.body() != null) {
                                // ← Accept any 200 response — store whatever URL field comes back
                                uploadedImageUrl = response.body().imageUrl != null
                                        ? response.body().imageUrl : "";
                                if (tvImageStatus != null) {
                                    tvImageStatus.setText("✅ Image uploaded successfully");
                                    tvImageStatus.setTextColor(Color.parseColor("#2E7D32"));
                                    tvImageStatus.setVisibility(View.VISIBLE);
                                }
                            } else {
                                uploadedImageUrl = "";
                                if (tvImageStatus != null) {
                                    tvImageStatus.setText("❌ Image upload failed (HTTP " + response.code() + ")");
                                    tvImageStatus.setTextColor(Color.parseColor("#E53935"));
                                    tvImageStatus.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UploadImageResponse> call, Throwable t) {
                            uploadedImageUrl = "";
                            if (tvImageStatus != null) {
                                tvImageStatus.setText("❌ Upload error: " + t.getMessage());
                                tvImageStatus.setTextColor(Color.parseColor("#E53935"));
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e("IMG_UPLOAD", "Failed: " + e.getMessage());
            if (tvImageStatus != null) {
                tvImageStatus.setText("❌ Could not read image");
                tvImageStatus.setTextColor(Color.parseColor("#E53935"));
            }
        }
    }
}