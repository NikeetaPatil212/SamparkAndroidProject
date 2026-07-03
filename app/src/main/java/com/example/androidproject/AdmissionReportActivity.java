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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.AdmissionReportAdapter;
import com.example.androidproject.model.summary.AdmissionItem;
import com.example.androidproject.model.summary.AdmissionReportRequest;
import com.example.androidproject.model.summary.AdmissionReportResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdmissionReportActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────
    private EditText       etStudentName, etContactNo, etLocation;
    private Spinner        spCourse, spBatch, spTime;
    private MaterialButton btnReset, btnViewStudents,
            btnSendWhatsappMessage, btnExportPdf;
    private CardView       cardStudentList;
    private RecyclerView   rvAdmissions;
    private TextView       tvStudentCount, tvCount;
    private FrameLayout    loaderLayout;

    private AdmissionReportAdapter adapter;
    private List<AdmissionItem>    fullList = new ArrayList<>();

    // ── PDF layout constants ───────────────────────────────────────
    private static final int PAGE_WIDTH    = 842;   // A4 landscape width  (pt)
    private static final int PAGE_HEIGHT   = 595;   // A4 landscape height (pt)
    private static final int MARGIN        = 24;
    private static final int ROW_HEIGHT    = 22;
    private static final int HEADER_HEIGHT = 30;

    // Column widths (must sum ≤ PAGE_WIDTH - 2*MARGIN)
    private static final int[] COL_WIDTHS = { 28, 72, 38, 108, 80, 68, 68, 68, 68, 52, 52, 60 };
    private static final String[] COL_HEADERS = {
            "No", "Adm Date", "ID", "Student Name",
            "Mobile", "Location", "Course", "Batch",
            "Time", "Fees", "Paid", "Outstanding"
    };

    // ── Lifecycle ──────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admission_report);

        initViews();
        setupBackButton();
        setupRecyclerView();
        setupListeners();
        fetchAdmissionData();
    }

    // ── Init ───────────────────────────────────────────────────────
    private void initViews() {
        etStudentName          = findViewById(R.id.etStudentName);
        etContactNo            = findViewById(R.id.etContactNo);
        etLocation             = findViewById(R.id.etLocation);
        spCourse               = findViewById(R.id.spCourse);
        spBatch                = findViewById(R.id.spBatch);
        spTime                 = findViewById(R.id.spTime);
        btnReset               = findViewById(R.id.btnReset);
        btnViewStudents        = findViewById(R.id.btnViewStudents);
        btnSendWhatsappMessage = findViewById(R.id.btnSendWhatsappMessage);
        btnExportPdf           = findViewById(R.id.btnExportExcel);   // ← your new button
        cardStudentList        = findViewById(R.id.cardStudentList);
        rvAdmissions           = findViewById(R.id.rvAdmissions);
        tvStudentCount         = findViewById(R.id.tvStudentCount);
        tvCount                = findViewById(R.id.tvCount);
        loaderLayout           = findViewById(R.id.loaderLayout);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new AdmissionReportAdapter();
        rvAdmissions.setLayoutManager(new LinearLayoutManager(this));
        rvAdmissions.setAdapter(adapter);
        adapter.setOnSelectionChangedListener(() -> {
            int sel = adapter.getCheckedItems().size();
            tvStudentCount.setText(sel > 0
                    ? sel + " Selected"
                    : adapter.getFilteredCount() + " Records");
        });
    }

    private void setupListeners() {
        btnViewStudents.setOnClickListener(v        -> applyFilters());
        btnReset.setOnClickListener(v               -> resetFilters());
        btnSendWhatsappMessage.setOnClickListener(v -> sendWhatsApp());
        btnExportPdf.setOnClickListener(v           -> exportToPdf());  // ← wired here
    }

    // ── API Call ───────────────────────────────────────────────────
    private void fetchAdmissionData() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStudentList.setVisibility(View.GONE);
        btnSendWhatsappMessage.setVisibility(View.GONE);
        btnExportPdf.setVisibility(View.GONE);
        tvStudentCount.setVisibility(View.GONE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        AdmissionReportRequest request = new AdmissionReportRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getAdmissionReport(request)
                .enqueue(new Callback<AdmissionReportResponse>() {
                    @Override
                    public void onResponse(Call<AdmissionReportResponse> call,
                                           Response<AdmissionReportResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("ADMISSION_RAW", new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<AdmissionItem> list = response.body().getStudentList();
                            fullList = (list != null) ? list : new ArrayList<>();

                            if (fullList.isEmpty()) {
                                Toast.makeText(AdmissionReportActivity.this,
                                        "No admission records found",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            setupFilterSpinners();

                            // ── AUTO-LOAD: show all records immediately ──
                            adapter.setData(fullList);
                            showTable(fullList.size());

                        } else {
                            String msg = response.body() != null
                                    && response.body().getMessage() != null
                                    ? response.body().getMessage()
                                    : "Failed to load admission report";
                            Toast.makeText(AdmissionReportActivity.this,
                                    msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdmissionReportResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(AdmissionReportActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── shows table card + action buttons + badge ─────────────────
    private void showTable(int count) {
        cardStudentList.setVisibility(View.VISIBLE);
        btnSendWhatsappMessage.setVisibility(View.VISIBLE);
        btnExportPdf.setVisibility(View.VISIBLE);
        tvCount.setText(count + " Students");
        tvStudentCount.setText(count + " Records");
        tvStudentCount.setVisibility(View.VISIBLE);
    }

    // ── Spinners ───────────────────────────────────────────────────
    private void setupFilterSpinners() {
        LinkedHashSet<String> courses = new LinkedHashSet<>();
        LinkedHashSet<String> batches = new LinkedHashSet<>();
        LinkedHashSet<String> times   = new LinkedHashSet<>();
        courses.add("All Courses");
        batches.add("All Batches");
        times.add("All Time Slots");

        for (AdmissionItem item : fullList) {
            if (!TextUtils.isEmpty(item.courseName))        courses.add(item.courseName);
            if (!TextUtils.isEmpty(item.batchName))         batches.add(item.batchName);
            if (!TextUtils.isEmpty(item.timingDescription)) times.add(item.timingDescription);
        }

        spCourse.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(courses)));
        spBatch.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(batches)));
        spTime.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(times)));
    }

    // ── Filter ─────────────────────────────────────────────────────
    private void applyFilters() {
        if (fullList.isEmpty()) {
            Toast.makeText(this, "Data not loaded yet, please wait",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.setData(fullList);
        adapter.applyFilters(
                (String) spCourse.getSelectedItem(),
                (String) spBatch.getSelectedItem(),
                (String) spTime.getSelectedItem(),
                etStudentName.getText().toString(),
                etContactNo.getText().toString(),
                etLocation.getText().toString()
        );

        int count = adapter.getFilteredCount();
        if (count == 0) {
            cardStudentList.setVisibility(View.GONE);
            btnSendWhatsappMessage.setVisibility(View.GONE);
            btnExportPdf.setVisibility(View.GONE);
            tvStudentCount.setVisibility(View.GONE);
            Toast.makeText(this, "No records match the selected filters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showTable(count);   // ← reuse helper
    }

    private void resetFilters() {
        spCourse.setSelection(0);
        spBatch.setSelection(0);
        spTime.setSelection(0);
        etStudentName.setText("");
        etContactNo.setText("");
        etLocation.setText("");

        if (!fullList.isEmpty()) {
            // ── restore full list instead of hiding table ──
            adapter.setData(fullList);
            showTable(fullList.size());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PDF EXPORT
    // ══════════════════════════════════════════════════════════════
    private void exportToPdf() {
        List<AdmissionItem> data = adapter.getFilteredList();
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

    private File buildPdf(List<AdmissionItem> data) throws Exception {

        PdfDocument document = new PdfDocument();

        // ── Paints ────────────────────────────────────────────────
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

        Paint paintCellTextBlue = new Paint();
        paintCellTextBlue.setColor(Color.parseColor("#1565C0"));
        paintCellTextBlue.setTextSize(8f);
        paintCellTextBlue.setFakeBoldText(true);

        Paint paintCellTextGreen = new Paint();
        paintCellTextGreen.setColor(Color.parseColor("#2E7D32"));
        paintCellTextGreen.setTextSize(8f);
        paintCellTextGreen.setFakeBoldText(true);

        Paint paintCellTextRed = new Paint();
        paintCellTextRed.setColor(Color.parseColor("#C62828"));
        paintCellTextRed.setTextSize(8f);
        paintCellTextRed.setFakeBoldText(true);

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

        // ── Pagination ────────────────────────────────────────────
        // How many rows fit per page after title block (first page) / header (subsequent)
        int TITLE_BLOCK_HEIGHT = 52;   // title + subtitle + spacing
        int FOOTER_HEIGHT      = 28;
        int usableHeight       = PAGE_HEIGHT - MARGIN * 2 - FOOTER_HEIGHT;

        int rowsPerFirstPage   = (usableHeight - TITLE_BLOCK_HEIGHT - HEADER_HEIGHT) / ROW_HEIGHT;
        int rowsPerOtherPage   = (usableHeight - HEADER_HEIGHT) / ROW_HEIGHT;

        int pageCount  = 1;
        int remaining  = data.size() - rowsPerFirstPage;
        if (remaining > 0)
            pageCount += (int) Math.ceil((double) remaining / rowsPerOtherPage);

        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm",
                Locale.getDefault()).format(new Date());

        int dataIndex = 0;   // tracks which AdmissionItem we're drawing

        for (int p = 0; p < pageCount; p++) {

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, p + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas c = page.getCanvas();

            int y = MARGIN;

            // ── Title block (first page only) ──────────────────
            if (p == 0) {
                c.drawText("Sampark IM — Admission Report", MARGIN, y + 12, paintTitle);
                y += 18;
                c.drawText("Generated: " + timestamp
                        + "   |   Total Records: " + data.size(), MARGIN, y + 8, paintSubtitle);
                y += 16 + 8;   // padding before header row
            } else {
                // Page number for subsequent pages
                String pageLabel = "Page " + (p + 1) + " of " + pageCount;
                c.drawText(pageLabel, PAGE_WIDTH - MARGIN - 60, y + 8, paintSubtitle);
                y += 16;
            }

            // ── Column header row ──────────────────────────────
            int x = MARGIN;
            c.drawRect(x, y, PAGE_WIDTH - MARGIN, y + HEADER_HEIGHT, paintHeaderBg);
            for (int col = 0; col < COL_HEADERS.length; col++) {
                c.drawText(COL_HEADERS[col],
                        x + 3, y + HEADER_HEIGHT - 8, paintHeaderText);
                x += COL_WIDTHS[col];
            }
            y += HEADER_HEIGHT;

            // ── Data rows ──────────────────────────────────────
            int rowsThisPage = (p == 0) ? rowsPerFirstPage : rowsPerOtherPage;
            int rowsDrawn    = 0;

            while (dataIndex < data.size() && rowsDrawn < rowsThisPage) {
                AdmissionItem item = data.get(dataIndex);
                boolean isEven    = (dataIndex % 2 == 0);

                // Row background
                c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN,
                        y + ROW_HEIGHT, isEven ? paintRowEven : paintRowOdd);

                // Cell values
                String[] cells = {
                        String.valueOf(dataIndex + 1),
                        safe(item.admissionDate),
                        String.valueOf(item.admissionID),
                        safe(item.studentName),
                        safe(item.mobile),
                        safe(item.location),
                        safe(item.courseName),
                        safe(item.batchName),
                        safe(item.timingDescription),
                        fmt(item.fees),
                        fmt(item.paid),
                        fmt(item.outstanding)
                };

                x = MARGIN;
                for (int col = 0; col < cells.length; col++) {
                    // Pick colour for financial columns
                    Paint cellPaint;
                    if      (col == 9)  cellPaint = paintCellTextBlue;   // Fees
                    else if (col == 10) cellPaint = paintCellTextGreen;  // Paid
                    else if (col == 11) cellPaint = paintCellTextRed;    // Outstanding
                    else                cellPaint = paintCellText;

                    // Clip text to column width
                    String txt = clipText(cells[col], COL_WIDTHS[col] - 4, cellPaint);
                    c.drawText(txt, x + 3, y + ROW_HEIGHT - 6, cellPaint);

                    // Vertical grid line
                    c.drawLine(x, y, x, y + ROW_HEIGHT, paintGrid);
                    x += COL_WIDTHS[col];
                }
                // Right border + horizontal grid line
                c.drawLine(x, y, x, y + ROW_HEIGHT, paintGrid);
                c.drawLine(MARGIN, y + ROW_HEIGHT, x, y + ROW_HEIGHT, paintGrid);

                y += ROW_HEIGHT;
                dataIndex++;
                rowsDrawn++;
            }

            // ── Footer ─────────────────────────────────────────
            int footerY = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT;
            c.drawRect(MARGIN, footerY, PAGE_WIDTH - MARGIN,
                    footerY + FOOTER_HEIGHT, paintFooterBg);

            // Totals in footer
            double totalFees = 0, totalPaid = 0, totalOut = 0;
            for (AdmissionItem item : data) {
                totalFees += item.fees;
                totalPaid += item.paid;
                totalOut  += item.outstanding;
            }

            c.drawText("Total Fees: " + fmt(totalFees)
                            + "   |   Total Paid: " + fmt(totalPaid)
                            + "   |   Outstanding: " + fmt(totalOut)
                            + "   |   " + (p + 1) + "/" + pageCount,
                    MARGIN + 6, footerY + FOOTER_HEIGHT - 8, paintFooterText);

            document.finishPage(page);
        }

        // ── Write file to cache/pdfs/ ─────────────────────────────
        File pdfDir = new File(getCacheDir(), "pdfs");
        if (!pdfDir.exists()) pdfDir.mkdirs();

        String fileName = "AdmissionReport_"
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

    /** Open the generated PDF with any installed PDF viewer */
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

    // ── PDF helpers ────────────────────────────────────────────────
    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private String fmt(double v) {
        return "\u20B9" + (v == (long) v ? String.valueOf((long) v) : String.valueOf(v));
    }

    /** Truncate text so it fits inside a column — avoids overflow */
    private String clipText(String text, int maxWidth, Paint paint) {
        if (paint.measureText(text) <= maxWidth) return text;
        while (text.length() > 1 && paint.measureText(text + "…") > maxWidth)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    // ── WhatsApp ───────────────────────────────────────────────────
    private void sendWhatsApp() {
        List<AdmissionItem> selected = adapter.getCheckedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one student",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AdmissionItem first = selected.get(0);
        String message = "Dear " + first.studentName
                + ", your admission for " + first.courseName
                + " (" + first.batchName + ") has been confirmed."
                + " Total Fees: \u20B9" + (long) first.fees
                + ", Paid: \u20B9"      + (long) first.paid
                + ", Outstanding: \u20B9" + (long) first.outstanding
                + ". - Sampark IM";

        String mobile = first.mobile != null
                ? first.mobile.replaceAll("[^0-9]", "") : "";
        if (mobile.isEmpty()) {
            Toast.makeText(this, "No valid mobile number for selected student",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.length() == 10) mobile = "91" + mobile;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + mobile
                    + "&text=" + Uri.encode(message)));
            startActivity(intent);

            if (selected.size() > 1) {
                Toast.makeText(this,
                        "Opened WhatsApp for " + first.studentName
                                + ". Repeat for remaining "
                                + (selected.size() - 1) + " student(s).",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
}