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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.InquiryReportAdapter;
import com.example.androidproject.model.summary.InquiryReportItem;
import com.example.androidproject.model.summary.InquiryReportRequest;
import com.example.androidproject.model.summary.InquiryReportResponse;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailedReportActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private EditText       etStudentName, etContactNo, etLocation;
    private Spinner        spCourse;
    private CheckBox       cbAll, cbActive, cbConverted, cbCancelled;
    private MaterialButton btnViewStudents, btnReset, btnSendWhatsappMessage,btnSendSMSMessage,btnGeneratePdf;
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
    private static final int PAGE_WIDTH    = 842;
    private static final int PAGE_HEIGHT   = 595;
    private static final int MARGIN        = 24;
    private static final int ROW_HEIGHT    = 22;
    private static final int HEADER_HEIGHT = 30;

    // 14 columns — widths must sum ≤ (PAGE_WIDTH - 2*MARGIN) = 794
    private static final int[] COL_WIDTHS = {
            22,   // No
            38,   // Inquiry No
            58,   // Inquiry Date
            105,  // Student Name
            70,   // Mobile
            70,   // Alt No
            58,   // Location
            62,   // Course        ← added
            58,   // Reminder Date
            58,   // Status
            55,   // Feedback
            35,   // Gender
            48,   // School
            47    // Reference
    };  // total = 764 ✔

    private static final String[] COL_HEADERS = {
            "No",
            "Inq Date",
            "Student Name",
            "Mobile",
            "Alt No",
            "Location",
            "Course",        // ← added
            "Reminder",
            "Status",
            "Feedback",
            "Gender",
            "School",
            "Reference"
    };

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
        btnGeneratePdf  = findViewById(R.id.btnGeneratePdf);
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

        btnGeneratePdf.setOnClickListener(v -> exportToPdf());  // ← wired here

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

    private void exportToPdf() {
        List<InquiryReportItem> data = adapter.getFilteredList();
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
    private File buildPdf(List<InquiryReportItem> data) throws Exception {

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
     /*   double totalFees = 0, totalPaid = 0, totalOut = 0;
        for (InquiryReportItem item : data) {
            totalFees += item.fees;
            totalPaid += item.paid;
            totalOut  += item.outstanding;
        }*/

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
                InquiryReportItem item = data.get(dataIndex);

                c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT,
                        dataIndex % 2 == 0 ? paintRowEven : paintRowOdd);

                // ── cells[] — note reminderDate uses safe(), NOT fmt() ──
                String[] cells = {
                        String.valueOf(dataIndex + 1),          // No
                      //  safe(item.nu),               // Adm Date  ← safe()
                        safe(item.inquiryDate),               // Adm Date  ← safe()
                        safe(item.fullName),                 // Student Name
                        safe(item.mobile),                      // Mobile
                        safe(item.alternateNo),                      // Mobile
                        safe(item.location),                    // Location
                        safe(item.about),                  // Course
                        safe(item.reminderDate),                   // Batch
                        safe(item.reminderStatus),                   // Batch
                        safe(item.feedback),
                        safe(item.gender),
                        safe(item.schoolName),
                        safe(item.reference)
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
           /* int footerY = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT;
            c.drawRect(MARGIN, footerY, PAGE_WIDTH - MARGIN,
                    footerY + FOOTER_HEIGHT, paintFooterBg);
            c.drawText(
                    "Total Fees: " + fmt(totalFees)
                            + "   |   Total Paid: " + fmt(totalPaid)
                            + "   |   Outstanding: " + fmt(totalOut)
                            + "   |   Page " + (p + 1) + "/" + pageCount,
                    MARGIN + 6, footerY + FOOTER_HEIGHT - 8, paintFooterText);*/

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
                                                    adapter.removeItems(items);
                                                    allInquiries.removeAll(items);
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
                                                    adapter.removeItems(items);
                                                    allInquiries.removeAll(items);
                                                    updateCount();
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
        btnGeneratePdf.setVisibility(View.GONE);
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
                            btnGeneratePdf.setVisibility(View.VISIBLE);
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