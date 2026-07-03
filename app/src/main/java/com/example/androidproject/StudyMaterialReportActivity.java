package com.example.androidproject;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidproject.adapters.StudyMaterialReportAdapter;
import com.example.androidproject.model.summary.StudyMaterialReportItem;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.model.summary.StudyMaterialReportRequest;
import com.example.androidproject.model.summary.StudyMaterialReportResponse;
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
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudyMaterialReportActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────
    private Spinner        spCourse, spBatch, spTiming;
    private RadioGroup     rgView;
    private android.widget.RadioButton rbPending, rbDistributed;
    private MaterialButton btnViewStudents, btnGeneratePdf;
    private CardView       cardStudentList;
    private RecyclerView   rvStudyMaterial;
    private TextView       tvStudentCount, tvCount;
    private FrameLayout    loaderLayout;

    // ── Data ──────────────────────────────────────────────────────
    private List<StudyMaterialReportItem> allItems = new ArrayList<>();
    private StudyMaterialReportAdapter adapter;

    // ── PDF layout constants ─────────────────────────────────────
    private static final int PAGE_WIDTH    = 842;
    private static final int PAGE_HEIGHT   = 595;
    private static final int MARGIN        = 24;
    private static final int ROW_HEIGHT    = 22;
    private static final int HEADER_HEIGHT = 30;

    // 9 columns — widths must sum ≤ (PAGE_WIDTH - 2*MARGIN) = 794
    private static final int[] COL_WIDTHS = {
            50,   // Adm ID
            70,   // Adm Date
            130,  // Student Name
            80,   // Mobile
            75,   // Location
            75,   // Course
            75,   // Batch
            110,  // Study Material
            90    // Distribution Date
    }; // total = 755 ✔

    private static final String[] COL_HEADERS = {
            "Adm ID",
            "Adm Date",
            "Student Name",
            "Mobile",
            "Location",
            "Course",
            "Batch",
            "Study Material",
            "Distribution Date"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_material_report);

        initViews();
        setupBackButton();
        setupViewToggle();
        fetchStudyMaterial();
    }

    // ── Init Views ─────────────────────────────────────────────────
    private void initViews() {
        spCourse        = findViewById(R.id.spCourse);
        spBatch         = findViewById(R.id.spBatch);
        spTiming        = findViewById(R.id.spTiming);
        rgView          = findViewById(R.id.rgView);
        rbPending       = findViewById(R.id.rbPending);
        rbDistributed   = findViewById(R.id.rbDistributed);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnGeneratePdf  = findViewById(R.id.btnGeneratePdf);
        cardStudentList = findViewById(R.id.cardStudentList);
        rvStudyMaterial = findViewById(R.id.rvStudyMaterial);
        tvStudentCount  = findViewById(R.id.tvStudentCount);
        tvCount         = findViewById(R.id.tvCount);
        loaderLayout    = findViewById(R.id.loaderLayout);

        rvStudyMaterial.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyMaterialReportAdapter();
        rvStudyMaterial.setAdapter(adapter);

        btnViewStudents.setOnClickListener(v -> fetchStudyMaterial());
        btnGeneratePdf.setOnClickListener(v -> exportToPdf());

        AdapterViewSelectionWatcher spinnerWatcher = new AdapterViewSelectionWatcher();
        spCourse.setOnItemSelectedListener(spinnerWatcher);
        spBatch.setOnItemSelectedListener(spinnerWatcher);
        spTiming.setOnItemSelectedListener(spinnerWatcher);
    }

    private void setupViewToggle() {
        rbPending.setChecked(true);
        rgView.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private class AdapterViewSelectionWatcher implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            applyFilters();
        }
        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }

    private void exportToPdf() {
        List<StudyMaterialReportItem> data = adapter.getFilteredList();
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
    private File buildPdf(List<StudyMaterialReportItem> data) throws Exception {

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
        String viewLabel = rbDistributed.isChecked() ? "Distributed" : "Pending";

        int dataIndex = 0;

        for (int p = 0; p < pageCount; p++) {

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, p + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas c = page.getCanvas();

            int y = MARGIN;

            if (p == 0) {
                c.drawText("Sampark IM — Study Material Distribution Report (" + viewLabel + ")", MARGIN, y + 12, paintTitle);
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
                StudyMaterialReportItem item = data.get(dataIndex);

                c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT,
                        dataIndex % 2 == 0 ? paintRowEven : paintRowOdd);

                String[] cells = {
                        String.valueOf(item.admissionID),   // Adm ID
                        safe(item.admissionDate),           // Adm Date
                        safe(item.studentName),             // Student Name
                        safe(item.mobile),                  // Mobile
                        safe(item.location),                // Location
                        safe(item.courseName),              // Course
                        safe(item.batchName),               // Batch
                        safe(item.studyMaterial),           // Study Material
                        safe(item.distributionDate)         // Distribution Date
                };

                x = MARGIN;
                for (int col = 0; col < cells.length; col++) {
                    String txt = clipText(cells[col], COL_WIDTHS[col] - 4, paintCellText);
                    c.drawText(txt, x + 3, y + ROW_HEIGHT - 6, paintCellText);
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

        String fileName = "StudyMaterialReport_"
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

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    // ── Course / Batch / Timing spinners ────────────────────────────
    // Populated dynamically from whatever data came back, same approach
    // as CertificateReportActivity — keeps filter options in sync with
    // what's actually in the report.
    private void populateSpinnersFromData() {
        Set<String> courses = new LinkedHashSet<>();
        Set<String> batches = new LinkedHashSet<>();
        Set<String> timings = new LinkedHashSet<>();
        for (StudyMaterialReportItem item : allItems) {
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

    // ── Apply all filters ──────────────────────────────────────────
    private void applyFilters() {
        if (allItems.isEmpty()) return; // don't filter before data loads

        String course = spCourse.getAdapter() == null || spCourse.getSelectedItemPosition() == 0 ? ""
                : spCourse.getSelectedItem().toString();
        String batch = spBatch.getAdapter() == null || spBatch.getSelectedItemPosition() == 0 ? ""
                : spBatch.getSelectedItem().toString();
        String timing = spTiming.getAdapter() == null || spTiming.getSelectedItemPosition() == 0 ? ""
                : spTiming.getSelectedItem().toString();

        boolean showDistributed = rbDistributed.isChecked();

        adapter.applyFilters(course, batch, timing, showDistributed);
        updateCount();
    }

    // ── Fetch from API ─────────────────────────────────────────────
    private void fetchStudyMaterial() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStudentList.setVisibility(View.GONE);
        btnGeneratePdf.setVisibility(View.GONE);
        allItems.clear();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        StudyMaterialReportRequest request = new StudyMaterialReportRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        Log.d("STUDYMAT_REQ", new Gson().toJson(request));

        // NOTE: add getStudyMaterialReport(StudyMaterialReportRequest) to ApiService,
        // pointing at POST /api/InstituteControllersV1/StudyMaterialReport
        RetrofitClient.getApiService()
                .getStudyMaterialReport(request)
                .enqueue(new Callback<StudyMaterialReportResponse>() {

                    @Override
                    public void onResponse(Call<StudyMaterialReportResponse> call,
                                           Response<StudyMaterialReportResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("STUDYMAT_REPORT", "HTTP=" + response.code());
                        Log.d("STUDYMAT_REPORT", "body=" + new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {

                            allItems = response.body().studentList;

                            if (allItems == null || allItems.isEmpty()) {
                                toast("No study material records found");
                                return;
                            }

                            adapter.setData(allItems);
                            populateSpinnersFromData();
                            applyFilters();
                            cardStudentList.setVisibility(View.VISIBLE);
                            btnGeneratePdf.setVisibility(View.VISIBLE);
                            updateCount();

                        } else {
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "Unknown error";
                                Log.e("STUDYMAT_REPORT", "errorBody=" + err);
                                toast("Failed: " + err);
                            } catch (Exception e) {
                                toast("Failed to load study material report");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StudyMaterialReportResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.e("STUDYMAT_REPORT", "onFailure: " + t.getMessage());
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

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}