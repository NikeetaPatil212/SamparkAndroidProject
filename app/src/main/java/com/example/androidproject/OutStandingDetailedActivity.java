package com.example.androidproject;


import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.OutstandingFeesAdapter;
import com.example.androidproject.model.outstanding.OutstandingItem;

import com.example.androidproject.model.summary.InquiryReportItem;
import com.example.androidproject.model.summary.OutstandingRequest;
import com.example.androidproject.model.summary.OutstandingResponse;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutStandingDetailedActivity extends AppCompatActivity {

    private Spinner spinnerCourse, spinnerBatch, spinnerTime;
    private EditText etStudentName, etContactNo, etLocation;
    private Button btnReset, btnApplyFilter, btnExportExcel, btnSendMessage;
    private CheckBox cbSelectAll;
    private MaterialButton btnSendWhatsappMessage,btnSendSMSMessage,btnExportToPdf;
    private RecyclerView rvOutstanding;
    private LinearLayout llFooter;
    private CardView cardTable;
    private TextView tvEmptyState, tvTotalBadge, tvTotalFees, tvTotalPaid, tvTotalOutstanding;
    private FrameLayout loaderLayout;

    private OutstandingFeesAdapter adapter;
    private List<OutstandingItem> fullList = new ArrayList<>();

    private static final int PAGE_WIDTH    = 842;   // A4 landscape width  (pt)
    private static final int PAGE_HEIGHT   = 595;   // A4 landscape height (pt)
    private static final int MARGIN        = 24;
    private static final int ROW_HEIGHT    = 22;
    private static final int HEADER_HEIGHT = 30;

    // Column widths (must sum ≤ PAGE_WIDTH - 2*MARGIN)
    private static final int[] COL_WIDTHS = { 28, 72, 90, 65, 80, 68, 68, 68, 68, 52, 52, 60 };
    private static final String[] COL_HEADERS = {
            "No", "Adm Date", "Student Name",
            "Mobile", "Location", "Course", "Batch",
            "Time", "Fees", "Paid", "Outstanding", "Due Date"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_standing_detailed);

        initViews();
        setupBackButton();
        setupRecyclerView();
        setupListeners();
        fetchOutstandingData();
    }

    private void initViews() {
        spinnerCourse  = findViewById(R.id.spinnerCourse);
        spinnerBatch   = findViewById(R.id.spinnerBatch);
        spinnerTime    = findViewById(R.id.spinnerTime);
        etStudentName  = findViewById(R.id.etStudentName);
        etContactNo    = findViewById(R.id.etContactNo);
        etLocation     = findViewById(R.id.etLocation);
        btnReset       = findViewById(R.id.btnReset);
        btnApplyFilter = findViewById(R.id.btnViewStudents);
        btnSendWhatsappMessage  = findViewById(R.id.btnSendWhatsappMessage);
        btnSendSMSMessage  = findViewById(R.id.btnSendSMSMessage);
        btnExportToPdf  = findViewById(R.id.btnExportToPdf);
        cbSelectAll    = findViewById(R.id.cbSelectAll);
        rvOutstanding  = findViewById(R.id.rvOutstanding);
        llFooter       = findViewById(R.id.llFooter);
        cardTable      = findViewById(R.id.cardTable);
        tvEmptyState   = findViewById(R.id.tvEmptyState);
        tvTotalBadge   = findViewById(R.id.tvTotalBadge);
        tvTotalFees    = findViewById(R.id.tvTotalFees);
        tvTotalPaid    = findViewById(R.id.tvTotalPaid);
        tvTotalOutstanding = findViewById(R.id.tvTotalOutstanding);
        loaderLayout   = findViewById(R.id.loaderLayout);
    }

    private void removeQueuedStudentsFromList(List<OutstandingItem> items) {
        adapter.removeItems(items);
        fullList.removeAll(items);
        cbSelectAll.setChecked(false);

        boolean empty = adapter.getFilteredCount() == 0;
        cardTable.setVisibility(empty ? View.GONE : View.VISIBLE);
        llFooter.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

        updateTotalsAndBadge();
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new OutstandingFeesAdapter();
        rvOutstanding.setLayoutManager(new LinearLayoutManager(this));
        rvOutstanding.setAdapter(adapter);
        adapter.setOnSelectionChangedListener(() -> {
            cbSelectAll.setChecked(adapter.isAllSelected());
            updateTotalsAndBadge();
        });
    }

    private void setupListeners() {
        btnApplyFilter.setOnClickListener(v -> applyFilters());
        btnReset.setOnClickListener(v -> resetFilters());
        cbSelectAll.setOnClickListener(v -> {
            adapter.selectAll(cbSelectAll.isChecked());
            updateTotalsAndBadge();
        });
        btnSendWhatsappMessage.setOnClickListener(v -> {
            List<OutstandingItem> selected = adapter.getCheckedItems();
            if (selected.isEmpty()) {
                toast("Please select at least one student");
                return;
            }
            sendWhatsAppBulk(selected);
        });

        btnSendSMSMessage.setOnClickListener(v -> {
            List<OutstandingItem> selected = adapter.getCheckedItems();
            if (selected.isEmpty()) {
                toast("Please select at least one student");
                return;
            }
            sendSmsBulk(selected);
        });

        btnExportToPdf.setOnClickListener(v -> exportToPdf());  // ← wired here
    }

    // ── Entry point ───────────────────────────────────────────────
    private void exportToPdf() {
        List<OutstandingItem> data = adapter.getFilteredList();
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
    private File buildPdf(List<OutstandingItem> data) throws Exception {

        PdfDocument document = new PdfDocument();

        // Paints
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

        Paint paintCellBlue = new Paint();
        paintCellBlue.setColor(Color.parseColor("#1565C0"));
        paintCellBlue.setTextSize(8f);
        paintCellBlue.setFakeBoldText(true);

        Paint paintCellGreen = new Paint();
        paintCellGreen.setColor(Color.parseColor("#2E7D32"));
        paintCellGreen.setTextSize(8f);
        paintCellGreen.setFakeBoldText(true);

        Paint paintCellRed = new Paint();
        paintCellRed.setColor(Color.parseColor("#C62828"));
        paintCellRed.setTextSize(8f);
        paintCellRed.setFakeBoldText(true);

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

        Paint paintFooterBg = new Paint();
        paintFooterBg.setColor(Color.parseColor("#E8F5E9"));
        paintFooterBg.setStyle(Paint.Style.FILL);

        Paint paintFooterText = new Paint();
        paintFooterText.setColor(Color.parseColor("#2E7D32"));
        paintFooterText.setTextSize(9f);
        paintFooterText.setFakeBoldText(true);

        // Pagination
        int TITLE_BLOCK_HEIGHT = 52;
        int FOOTER_HEIGHT      = 28;
        int usableHeight       = PAGE_HEIGHT - MARGIN * 2 - FOOTER_HEIGHT;
        int rowsPerFirstPage   = (usableHeight - TITLE_BLOCK_HEIGHT - HEADER_HEIGHT) / ROW_HEIGHT;
        int rowsPerOtherPage   = (usableHeight - HEADER_HEIGHT) / ROW_HEIGHT;

        int pageCount = 1;
        int remaining = data.size() - rowsPerFirstPage;
        if (remaining > 0)
            pageCount += (int) Math.ceil((double) remaining / rowsPerOtherPage);

        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm",
                Locale.getDefault()).format(new Date());

        // Grand totals for footer
        double totalFees = 0, totalPaid = 0, totalOut = 0;
        for (OutstandingItem item : data) {
            totalFees += item.fees;
            totalPaid += item.paid;
            totalOut  += item.outstanding;
        }

        int dataIndex = 0;

        for (int p = 0; p < pageCount; p++) {

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, p + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas c = page.getCanvas();

            int y = MARGIN;

            // Title block — first page only
            if (p == 0) {
                c.drawText("Sampark IM — Outstanding Fees Report", MARGIN, y + 12, paintTitle);
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

            // Header row
            int x = MARGIN;
            c.drawRect(x, y, PAGE_WIDTH - MARGIN, y + HEADER_HEIGHT, paintHeaderBg);
            for (int col = 0; col < COL_HEADERS.length; col++) {
                c.drawText(COL_HEADERS[col], x + 3, y + HEADER_HEIGHT - 8, paintHeaderText);
                x += COL_WIDTHS[col];
            }
            y += HEADER_HEIGHT;

            // Data rows
            int rowsThisPage = (p == 0) ? rowsPerFirstPage : rowsPerOtherPage;
            int rowsDrawn    = 0;

            while (dataIndex < data.size() && rowsDrawn < rowsThisPage) {
                OutstandingItem item = data.get(dataIndex);

                c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT,
                        dataIndex % 2 == 0 ? paintRowEven : paintRowOdd);

                // ── cells[] — note reminderDate uses safe(), NOT fmt() ──
                String[] cells = {
                        String.valueOf(dataIndex + 1),          // No
                        safe(item.admissionDate),               // Adm Date  ← safe()
                        safe(item.studentName),                 // Student Name
                        safe(item.mobile),                      // Mobile
                        safe(item.location),                    // Location
                        safe(item.courseName),                  // Course
                        safe(item.batchName),                   // Batch
                        fmt(item.fees),                         // Fees       ← fmt()
                        fmt(item.paid),                         // Paid       ← fmt()
                        fmt(item.outstanding),                  // Outstanding← fmt()
                        safe(item.reminderDate)                 // Due Date   ← safe() ✔
                };

                x = MARGIN;
                for (int col = 0; col < cells.length; col++) {
                    Paint cellPaint;
                    if      (col == 8)  cellPaint = paintCellBlue;   // Fees
                    else if (col == 9)  cellPaint = paintCellGreen;  // Paid
                    else if (col == 10) cellPaint = paintCellRed;    // Outstanding
                    else                cellPaint = paintCellText;

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

            // Footer
            int footerY = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT;
            c.drawRect(MARGIN, footerY, PAGE_WIDTH - MARGIN,
                    footerY + FOOTER_HEIGHT, paintFooterBg);
            c.drawText(
                    "Total Fees: " + fmt(totalFees)
                            + "   |   Total Paid: " + fmt(totalPaid)
                            + "   |   Outstanding: " + fmt(totalOut)
                            + "   |   Page " + (p + 1) + "/" + pageCount,
                    MARGIN + 6, footerY + FOOTER_HEIGHT - 8, paintFooterText);

            document.finishPage(page);
        }

        // Save to cache
        File pdfDir = new File(getCacheDir(), "pdfs");
        if (!pdfDir.exists()) pdfDir.mkdirs();

        String fileName = "OutstandingReport_"
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

    private String fmt(double v) {
        return "\u20B9" + (v == (long) v ? String.valueOf((long) v) : String.valueOf(v));
    }

    private String clipText(String text, int maxWidth, Paint paint) {
        if (paint.measureText(text) <= maxWidth) return text;
        while (text.length() > 1 && paint.measureText(text + "…") > maxWidth)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    private void sendWhatsAppBulk(List<OutstandingItem> items) {
        // ── Fetch template first, then build messages ─────────────────────────
        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Payment Reminder",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {
                                if (!template.isActive) {
                                    toast("WhatsApp notifications are currently disabled.");
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(OutStandingDetailedActivity.this);
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

                                for (OutstandingItem s : items) {
                                    if (s.mobile == null || s.mobile.isEmpty()) continue;

                                    // ── Fill template placeholders ────────────
                                    String fullName  = s.studentName != null ? s.studentName : "";
                                    String firstName = fullName.contains(" ")
                                            ? fullName.substring(0, fullName.indexOf(" "))
                                            : fullName;

                                    Map<String, String> data = new HashMap<>();
                                    data.put("studentName",    s.studentName);
                                    data.put("due", String.valueOf(s.outstanding));
                                    data.put("dueDate", s.reminderDate != null ? s.reminderDate : "");
                                //    data.put("InquiryDate",    s.inquiryDate != null ? s.inquiryDate : "");
                                //    data.put("InquiryCourses", s.about != null ? s.about : "");
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
                                                    removeQueuedStudentsFromList(items);
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

    private void sendSmsBulk(List<OutstandingItem> items) {
        TemplateRepository.getInstance(this)
                .getTemplateByCategory("Payment Reminder",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {
                                if (!template.isActive) {
                                    toast("SMS notifications are currently disabled.");
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(OutStandingDetailedActivity.this);
                                String lang = pref.getLanguage();
                                String templateText;
                                switch (lang) {
                                    case "MR": templateText = template.sms_MR; break;
                                    case "HI": templateText = template.sms_HI; break;
                                    default:   templateText = template.sms_EN; break;
                                }

                                List<com.example.androidproject.model.queue.SmsQueueRequest.SmsItem>
                                        smsItems = new ArrayList<>();

                                for (OutstandingItem s : items) {
                                    if (s.mobile == null || s.mobile.isEmpty()) continue;

                                    String fullName  = s.studentName != null ? s.studentName : "";
                                    String firstName = fullName.contains(" ")
                                            ? fullName.substring(0, fullName.indexOf(" "))
                                            : fullName;

                                    Map<String, String> data = new HashMap<>();
                                //    data.put("FirstName",      firstName);
                                    data.put("studentName",    s.studentName);
                                    data.put("due", String.valueOf(s.outstanding));
                                    data.put("dueDate", s.reminderDate != null ? s.reminderDate : "");
                                //    data.put("InquiryDate",    s.inquiryDate != null ? s.inquiryDate : "");
                                //    data.put("InquiryCourses", s.about != null ? s.about : "");
                                    data.put("institute",      pref.getInstituteName());
                                    data.put("Authority",      pref.getOwnerName());
                                    data.put("mobile1",        pref.getInstituteMobile1());
                                    data.put("mobile2",        pref.getInstituteMobile2());
                                    data.put("email",          pref.getInstituteEmail());
                                    data.put("address1",       pref.getInstituteAddress1());
                                    data.put("address2",       pref.getInstituteAddress2());
                                    data.put("ownerName",      pref.getOwnerName());

                                    String message = TemplateRepository.fillTemplate(templateText, data);

                                    Log.d("Msg-----", "onSuccess: " + message);

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
                                                    removeQueuedStudentsFromList(items);
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

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    // ── API CALL ─────────────────────────────────────────────────
    private void fetchOutstandingData() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardTable.setVisibility(View.GONE);
        llFooter.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        OutstandingRequest request = new OutstandingRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getOutstandingDetail(request)
                .enqueue(new Callback<OutstandingResponse>() {
                    @Override
                    public void onResponse(Call<OutstandingResponse> call,
                                           Response<OutstandingResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("OUTSTANDING_RAW", new Gson().toJson(response.body()));

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {

                            fullList = mapToItems(response.body().getStudentList());

                            if (fullList.isEmpty()) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                return;
                            }

                            setupFilterSpinners();
                            adapter.setData(fullList);
                            cardTable.setVisibility(View.VISIBLE);
                            llFooter.setVisibility(View.VISIBLE);
                            btnSendSMSMessage.setVisibility(View.VISIBLE);
                            btnSendWhatsappMessage.setVisibility(View.VISIBLE);
                            updateTotalsAndBadge();

                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Failed to load outstanding report";
                            Toast.makeText(OutStandingDetailedActivity.this, msg, Toast.LENGTH_SHORT).show();
                            tvEmptyState.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<OutstandingResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        Toast.makeText(OutStandingDetailedActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private List<OutstandingItem> mapToItems(List<OutstandingResponse.StudentItem> raw) {
        List<OutstandingItem> result = new ArrayList<>();
        if (raw == null) return result;
        for (OutstandingResponse.StudentItem s : raw) {
            OutstandingItem item = new OutstandingItem();
            item.admissionID       = s.getAdmissionID();
            item.admissionDate     = s.getAdmissionDate();
            item.studentName       = s.getStudentName();
            item.mobile             = s.getMobile();
            item.location           = s.getLocation();
            item.courseID           = s.getCourseID();
            item.courseName         = s.getCourseName();
            item.batchID            = s.getBatchID();
            item.batchName          = s.getBatchName();
            item.timingID           = s.getTimingID();
            item.timingDescription  = s.getTimingDescription();
            item.fees                = s.getFees();
            item.paid                = s.getPaid();
            item.outstanding         = s.getOutstanding();
            item.reminderDate        = s.getReminderDate();
            result.add(item);
        }
        return result;
    }

    // ── FILTER SPINNERS (built from loaded data) ───────────────────
    private void setupFilterSpinners() {
        LinkedHashSet<String> courses = new LinkedHashSet<>();
        LinkedHashSet<String> batches = new LinkedHashSet<>();
        LinkedHashSet<String> times   = new LinkedHashSet<>();
        courses.add("All Courses");
        batches.add("All Batches");
        times.add("All Time Slots");

        for (OutstandingItem item : fullList) {
            if (!TextUtils.isEmpty(item.courseName)) courses.add(item.courseName);
            if (!TextUtils.isEmpty(item.batchName)) batches.add(item.batchName);
            if (!TextUtils.isEmpty(item.timingDescription)) times.add(item.timingDescription);
        }

        spinnerCourse.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(courses)));
        spinnerBatch.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(batches)));
        spinnerTime.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(times)));
    }

    private void applyFilters() {
        String course = (String) spinnerCourse.getSelectedItem();
        String batch  = (String) spinnerBatch.getSelectedItem();
        String time   = (String) spinnerTime.getSelectedItem();
        String name   = etStudentName.getText().toString();
        String contact = etContactNo.getText().toString();
        String location = etLocation.getText().toString();

        adapter.applyFilters(course, batch, time, name, contact, location);

        boolean empty = adapter.getFilteredCount() == 0;
        cardTable.setVisibility(empty ? View.GONE : View.VISIBLE);
        llFooter.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        cbSelectAll.setChecked(false);
        updateTotalsAndBadge();
    }

    private void resetFilters() {
        spinnerCourse.setSelection(0);
        spinnerBatch.setSelection(0);
        spinnerTime.setSelection(0);
        etStudentName.setText("");
        etContactNo.setText("");
        etLocation.setText("");
        adapter.setData(fullList);
        cardTable.setVisibility(View.VISIBLE);
        llFooter.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        cbSelectAll.setChecked(false);
        updateTotalsAndBadge();
    }

    // ── TOTALS ───────────────────────────────────────────────────
    private void updateTotalsAndBadge() {
        double totalFees = 0, totalPaid = 0, totalOutstanding = 0;
        for (OutstandingItem item : adapter.getFilteredList()) {
            totalFees        += item.fees;
            totalPaid         += item.paid;
            totalOutstanding += item.outstanding;
        }
        tvTotalFees.setText("Total Fees " + formatAmount(totalFees));
        tvTotalPaid.setText("Total Paid " + formatAmount(totalPaid));
        tvTotalOutstanding.setText("Outstanding " + formatAmount(totalOutstanding));
        tvTotalBadge.setText(adapter.getFilteredCount() + " Records");
        tvTotalBadge.setVisibility(View.VISIBLE);
    }

    private String formatAmount(double amount) {
        return "₹" + (amount == (long) amount ? String.valueOf((long) amount) : String.valueOf(amount));
    }

    // ── SEND MESSAGE ─────────────────────────────────────────────
    private void sendMessageToSelected() {
        List<OutstandingItem> selected = adapter.getCheckedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one student", Toast.LENGTH_SHORT).show();
            return;
        }

        OutstandingItem first = selected.get(0);
        String message = "Dear " + first.studentName
                + ", your outstanding fee for " + first.courseName
                + " (" + first.batchName + ") is " + formatAmount(first.outstanding)
                + ". Please clear it at the earliest. - Sampark IM";

        String mobile = first.mobile != null ? first.mobile.replaceAll("[^0-9]", "") : "";
        if (mobile.isEmpty()) {
            Toast.makeText(this, "No valid mobile number found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.length() == 10) mobile = "91" + mobile;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + mobile
                    + "&text=" + Uri.encode(message)));
            startActivity(intent);

            if (selected.size() > 1) {
                Toast.makeText(this, "Opened WhatsApp for " + first.studentName
                                + ". Repeat for remaining " + (selected.size() - 1) + " student(s).",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
}