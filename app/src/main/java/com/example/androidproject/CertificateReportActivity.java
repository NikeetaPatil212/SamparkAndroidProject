package com.example.androidproject;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.CertificateReportAdapter;
import com.example.androidproject.model.outstanding.OutstandingItem;
import com.example.androidproject.model.summary.CertificateReportItem;
import com.example.androidproject.model.summary.CertificateReportRequest;
import com.example.androidproject.model.summary.CertificateReportResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CertificateReportActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private EditText       etStudentName, etContactNo;
    private Spinner        spCourse, spBatch, spTiming;
    private RadioGroup     rgView;
    private android.widget.RadioButton rbPending, rbDistributed;
    private MaterialButton btnViewStudents, btnReset, btnSendWhatsappMessage, btnGeneratePdf;
    private CardView       cardStudentList;
    private RecyclerView   rvCertificates;
    private TextView       tvStudentCount, tvCount;
    private FrameLayout    loaderLayout;

    // ── Data ──────────────────────────────────────────────────────
    private List<CertificateReportItem> allCertificates = new ArrayList<>();
    private CertificateReportAdapter adapter;

    // ── PDF layout constants ─────────────────────────────────────
    private static final int PAGE_WIDTH    = 842;
    private static final int PAGE_HEIGHT   = 595;
    private static final int MARGIN        = 24;
    private static final int ROW_HEIGHT    = 22;
    private static final int HEADER_HEIGHT = 30;

    // 14 columns — widths must sum ≤ (PAGE_WIDTH - 2*MARGIN) = 794
    private static final int[] COL_WIDTHS = {
            22,   // SN
            40,   // Adm ID
            58,   // Adm Date
            90,   // Student Name
            65,   // Contact
            55,   // Course Name
            60,   // Batch
            55,   // Status
            65,   // Certificate No
            45,   // Result
            55,   // Percentage
            65,   // Handed To
            55,   // Contact Info
            60    // Handover Date
    }; // total = 790 ✔

    private static final String[] COL_HEADERS = {
            "SN",
            "Adm ID",
            "Adm Date",
            "Student Name",
            "Contact",
            "Course",
            "Batch",
            "Status",
            "Cert No",
            "Result",
            "Percentage",
            "Handed To",
            "Contact Info",
            "Handover Date"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_certificate_report);

        initViews();
        setupBackButton();
        setupViewToggle();
        setupLiveFilters();
        fetchCertificates();
    }

    // ── Init Views ─────────────────────────────────────────────────
    private void initViews() {
        etStudentName   = findViewById(R.id.etStudentName);
        etContactNo     = findViewById(R.id.etContactNo);
        spCourse        = findViewById(R.id.spCourse);
        spBatch         = findViewById(R.id.spBatch);
        spTiming        = findViewById(R.id.spTiming);
        rgView          = findViewById(R.id.rgView);
        rbPending       = findViewById(R.id.rbPending);
        rbDistributed   = findViewById(R.id.rbDistributed);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnReset        = findViewById(R.id.btnReset);
        btnSendWhatsappMessage = findViewById(R.id.btnSendWhatsappMessage);
        btnGeneratePdf  = findViewById(R.id.btnGeneratePdf);
        cardStudentList = findViewById(R.id.cardStudentList);
        rvCertificates  = findViewById(R.id.rvCertificates);
        tvStudentCount  = findViewById(R.id.tvStudentCount);
        tvCount         = findViewById(R.id.tvCount);
        loaderLayout    = findViewById(R.id.loaderLayout);

        rvCertificates.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CertificateReportAdapter();
        rvCertificates.setAdapter(adapter);

        btnViewStudents.setOnClickListener(v -> fetchCertificates());
        btnReset.setOnClickListener(v -> resetFilters());

        btnSendWhatsappMessage.setOnClickListener(v -> {
            List<CertificateReportItem> selected = adapter.getCheckedItems();
            if (selected.isEmpty()) {
                toast("Please select at least one student");
                return;
            }
            sendWhatsAppBulk(selected);
        });

        btnGeneratePdf.setOnClickListener(v -> exportToPdf());
    }

    private void setupViewToggle() {
        rbPending.setChecked(true);
        rgView.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private void exportToPdf() {
        List<CertificateReportItem> data = adapter.getFilteredList();
        if (data == null || data.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }
        loaderLayout.setVisibility(View.VISIBLE);
        try {
            File pdfFile = buildPdf(data);
            loaderLayout.setVisibility(View.GONE);
            openPdf(pdfFile);
        } catch (Exception e) {
            loaderLayout.setVisibility(View.GONE);
            Log.e("PDF_EXPORT", "Error generating PDF", e);
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── Build PDF ─────────────────────────────────────────────────
    private File buildPdf(List<CertificateReportItem> data) throws Exception {

        PdfDocument document = new PdfDocument();

        Paint paintTitle = new Paint();
        paintTitle.setColor(Color.parseColor("#2E7D32"));
        paintTitle.setTextSize(14f);
        paintTitle.setFakeBoldText(true);

        Paint paintSubtitle = new Paint();
        paintSubtitle.setColor(Color.parseColor("#555555"));
        paintSubtitle.setTextSize(9f);

        Paint paintHeaderBg = new Paint();
        paintHeaderBg.setColor(Color.parseColor("#2E7D32"));
        paintHeaderBg.setStyle(Paint.Style.FILL);

        Paint paintHeaderText = new Paint();
        paintHeaderText.setColor(Color.WHITE);
        paintHeaderText.setTextSize(8f);
        paintHeaderText.setFakeBoldText(true);

        Paint paintCellText = new Paint();
        paintCellText.setColor(Color.parseColor("#1A1A1A"));
        paintCellText.setTextSize(8f);

        Paint paintCellGreen = new Paint();
        paintCellGreen.setColor(Color.parseColor("#2E7D32"));
        paintCellGreen.setTextSize(8f);
        paintCellGreen.setFakeBoldText(true);

        Paint paintCellAmber = new Paint();
        paintCellAmber.setColor(Color.parseColor("#EF6C00"));
        paintCellAmber.setTextSize(8f);
        paintCellAmber.setFakeBoldText(true);

        Paint paintRowEven = new Paint();
        paintRowEven.setColor(Color.WHITE);
        paintRowEven.setStyle(Paint.Style.FILL);

        Paint paintRowOdd = new Paint();
        paintRowOdd.setColor(Color.parseColor("#F5FBF5"));
        paintRowOdd.setStyle(Paint.Style.FILL);

        Paint paintGrid = new Paint();
        paintGrid.setColor(Color.parseColor("#DDDDDD"));
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(0.5f);

        int TITLE_BLOCK_HEIGHT = 52;
        int usableHeight       = PAGE_HEIGHT - MARGIN * 2;
        int rowsPerFirstPage   = (usableHeight - TITLE_BLOCK_HEIGHT - HEADER_HEIGHT) / ROW_HEIGHT;
        int rowsPerOtherPage   = (usableHeight - HEADER_HEIGHT) / ROW_HEIGHT;

        int pageCount = 1;
        int remaining = data.size() - rowsPerFirstPage;
        if (remaining > 0)
            pageCount += (int) Math.ceil((double) remaining / rowsPerOtherPage);

        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm",
                Locale.getDefault()).format(new Date());
        String viewLabel = rbDistributed.isChecked() ? "Distributed Certificates" : "Pending Certificates";

        int dataIndex = 0;

        for (int p = 0; p < pageCount; p++) {

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, p + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas c = page.getCanvas();

            int y = MARGIN;

            if (p == 0) {
                c.drawText("Sampark IM — Certificate Hand-Over Report (" + viewLabel + ")", MARGIN, y + 12, paintTitle);
                y += 18;
                c.drawText("Generated: " + timestamp
                                + "   |   Total Records: " + data.size(),
                        MARGIN, y + 8, paintSubtitle);
                y += 24;
            } else {
                String pageLabel = "Page " + (p + 1) + " of " + pageCount;
                c.drawText(pageLabel, PAGE_WIDTH - MARGIN - 60, y + 8, paintSubtitle);
                y += 16;
            }

            int x = MARGIN;
            c.drawRect(x, y, PAGE_WIDTH - MARGIN, y + HEADER_HEIGHT, paintHeaderBg);
            for (int col = 0; col < COL_HEADERS.length; col++) {
                c.drawText(COL_HEADERS[col], x + 3, y + HEADER_HEIGHT - 8, paintHeaderText);
                x += COL_WIDTHS[col];
            }
            y += HEADER_HEIGHT;

            int rowsThisPage = (p == 0) ? rowsPerFirstPage : rowsPerOtherPage;
            int rowsDrawn    = 0;

            while (dataIndex < data.size() && rowsDrawn < rowsThisPage) {
                CertificateReportItem item = data.get(dataIndex);

                c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT,
                        dataIndex % 2 == 0 ? paintRowEven : paintRowOdd);

                String statusText = item.isDistributed ? "Distributed" : "Pending";

                String[] cells = {
                        String.valueOf(dataIndex + 1),      // SN
                        String.valueOf(item.admissionID),   // Adm ID
                        safe(item.admissionDate),           // Adm Date
                        safe(item.studentName),             // Student Name
                        safe(item.mobile),                  // Contact
                        safe(item.courseName),              // Course
                        safe(item.batchName),               // Batch
                        statusText,                         // Status
                        safe(item.certificateNumber),       // Cert No
                        safe(item.result),                  // Result
                        safe(item.percentage),              // Percentage
                        safe(item.issuerName),              // Handed To
                        safe(item.issuerContact),           // Contact Info
                        safe(item.handoverDate)             // Handover Date
                };

                x = MARGIN;
                for (int col = 0; col < cells.length; col++) {
                    Paint cellPaint;
                    if (col == 7) {
                        cellPaint = item.isDistributed ? paintCellGreen : paintCellAmber;
                    } else {
                        cellPaint = paintCellText;
                    }

                    String txt = clipText(cells[col], COL_WIDTHS[col] - 4, cellPaint);
                    c.drawText(txt, x + 3, y + ROW_HEIGHT - 6, cellPaint);
                    c.drawLine(x, y, x, y + ROW_HEIGHT, paintGrid);
                    x += COL_WIDTHS[col];
                }
                c.drawLine(x, y, x, y + ROW_HEIGHT, paintGrid);
                c.drawLine(MARGIN, y + ROW_HEIGHT, x, y + ROW_HEIGHT, paintGrid);

                y += ROW_HEIGHT;
                dataIndex++;
                rowsDrawn++;
            }

            document.finishPage(page);
        }

        File pdfDir = new File(getCacheDir(), "pdfs");
        if (!pdfDir.exists()) pdfDir.mkdirs();

        String fileName = "CertificateReport_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date())
                + ".pdf";
        File pdfFile = new File(pdfDir, fileName);
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();

        return pdfFile;
    }

    // ── Open PDF ──────────────────────────────────────────────────
    private void openPdf(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(Intent.createChooser(intent, "Open PDF with"));
        } catch (Exception e) {
            Toast.makeText(this,
                    "No PDF viewer installed. Please install one to open this file.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private String clipText(String text, int maxWidth, Paint paint) {
        if (paint.measureText(text) <= maxWidth) return text;
        while (text.length() > 1 && paint.measureText(text + "…") > maxWidth)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    // ── Bulk WhatsApp ─────────────────────────────────────────────
    // Uses the same TemplateRepository pattern as inquiries/attendance,
    // pointed at a "Certificate Notification" template category — rename
    // to match whatever category you configure for this message type.
    private void sendWhatsAppBulk(List<CertificateReportItem> items) {
        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Certificate Pendings",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {
                                if (!template.isActive) {
                                    toast("WhatsApp notifications are currently disabled.");
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(CertificateReportActivity.this);
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

                                for (CertificateReportItem s : items) {
                                    if (s.mobile == null || s.mobile.isEmpty()) continue;

                                    String fullName  = s.studentName != null ? s.studentName : "";
                                    String firstName = fullName.contains(" ")
                                            ? fullName.substring(0, fullName.indexOf(" "))
                                            : fullName;

                                    Map<String, String> data = new HashMap<>();
                                    data.put("FirstName",         firstName);
                                    data.put("StudentName",       s.studentName);
                                    data.put("Course",            s.courseName != null ? s.courseName : "");
                                    data.put("Batch",              s.batchName != null ? s.batchName : "");
                                    data.put("CertificateNumber", s.certificateNumber != null ? s.certificateNumber : "");
                                    data.put("institute",         pref.getInstituteName());
                                    data.put("Authority",         pref.getOwnerName());
                                    data.put("mobile1",           pref.getInstituteMobile1());
                                    data.put("mobile2",           pref.getInstituteMobile2());
                                    data.put("email",             pref.getInstituteEmail());
                                    data.put("address1",          pref.getInstituteAddress1());
                                    data.put("address2",          pref.getInstituteAddress2());
                                    data.put("ownerName",         pref.getOwnerName());

                                    String message = TemplateRepository.fillTemplate(templateText, data);
                                    Log.d("Certificate msg--", "onSuccess: " + message);

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
                                                    adapter.removeItems(items);
                                                    allCertificates.removeAll(items);
                                                    updateCount();

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

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    // ── Course / Batch / Timing spinners ────────────────────────────
    // Populated dynamically from whatever data came back so the filter
    // options always match what's actually in the report (mirrors the
    // desktop screen's dropdowns). "All" sits at position 0 in each.
    private void populateSpinnersFromData() {
        Set<String> courses = new LinkedHashSet<>();
        Set<String> batches = new LinkedHashSet<>();
        Set<String> timings = new LinkedHashSet<>();
        for (CertificateReportItem item : allCertificates) {
            if (item.courseName != null && !item.courseName.isEmpty()) courses.add(item.courseName);
            if (item.batchName != null && !item.batchName.isEmpty()) batches.add(item.batchName);
            if (item.timingDescription != null && !item.timingDescription.isEmpty()) timings.add(item.timingDescription);
        }
        setSpinnerOptions(spCourse, "All Courses", courses);
        setSpinnerOptions(spBatch, "All Batches", batches);
        setSpinnerOptions(spTiming, "All Timings", timings);
    }

    private void setSpinnerOptions(Spinner spinner, String allLabel, Set<String> values) {
        List<String> options = new ArrayList<>();
        options.add(allLabel);
        options.addAll(values);
        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, options);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);
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

        AdapterViewSelectionWatcher spinnerWatcher = new AdapterViewSelectionWatcher();
        spCourse.setOnItemSelectedListener(spinnerWatcher);
        spBatch.setOnItemSelectedListener(spinnerWatcher);
        spTiming.setOnItemSelectedListener(spinnerWatcher);
    }

    private class AdapterViewSelectionWatcher implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            applyFilters();
        }
        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }

    // ── Apply all filters ──────────────────────────────────────────
    private void applyFilters() {
        if (allCertificates.isEmpty()) return; // don't filter before data loads

        String course = spCourse.getSelectedItemPosition() == 0 ? ""
                : spCourse.getSelectedItem().toString();
        String batch = spBatch.getSelectedItemPosition() == 0 ? ""
                : spBatch.getSelectedItem().toString();
        String timing = spTiming.getSelectedItemPosition() == 0 ? ""
                : spTiming.getSelectedItem().toString();

        boolean showDistributed = rbDistributed.isChecked();

        adapter.applyFilters(
                etStudentName.getText().toString(),
                etContactNo.getText().toString(),
                course,
                batch,
                timing,
                showDistributed
        );
        updateCount();
    }

    // ── Fetch from API ─────────────────────────────────────────────
    private void fetchCertificates() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStudentList.setVisibility(View.GONE);
        btnGeneratePdf.setVisibility(View.GONE);
        btnSendWhatsappMessage.setVisibility(View.GONE);
        allCertificates.clear();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        CertificateReportRequest request = new CertificateReportRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        Log.d("CERT_REQ", new Gson().toJson(request));

        // NOTE: add getCertificateReport(CertificateReportRequest) to ApiService,
        // pointing at POST /api/InstituteControllersV1/CertificateReport
        RetrofitClient.getApiService()
                .getCertificateReport(request)
                .enqueue(new Callback<CertificateReportResponse>() {

                    @Override
                    public void onResponse(Call<CertificateReportResponse> call,
                                           Response<CertificateReportResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("CERT_REPORT", "HTTP=" + response.code());
                        Log.d("CERT_REPORT", "body=" + new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {

                            allCertificates = response.body().studentList;

                            if (allCertificates == null || allCertificates.isEmpty()) {
                                toast("No certificate records found");
                                return;
                            }

                            adapter.setData(allCertificates);
                            populateSpinnersFromData();
                            applyFilters();
                            cardStudentList.setVisibility(View.VISIBLE);
                            btnGeneratePdf.setVisibility(View.VISIBLE);
                            btnSendWhatsappMessage.setVisibility(View.VISIBLE);
                            updateCount();

                        } else {
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "Unknown error";
                                Log.e("CERT_REPORT", "errorBody=" + err);
                                toast("Failed: " + err);
                            } catch (Exception e) {
                                toast("Failed to load certificate report");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CertificateReportResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.e("CERT_REPORT", "onFailure: " + t.getMessage());
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
        if (spCourse.getAdapter() != null) spCourse.setSelection(0);
        if (spBatch.getAdapter() != null) spBatch.setSelection(0);
        if (spTiming.getAdapter() != null) spTiming.setSelection(0);
        rbPending.setChecked(true);
        applyFilters();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}