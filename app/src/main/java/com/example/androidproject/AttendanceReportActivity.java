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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.AttendanceReportAdapter;
import com.example.androidproject.model.summary.AttendanceReportItem;
import com.example.androidproject.model.summary.AttendanceReportRequest;
import com.example.androidproject.model.summary.AttendanceReportResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceReportActivity extends AppCompatActivity {

    private EditText etClassDate;
    private Spinner spCourse, spBatch, spTime;
    private RadioButton rbAllStudents, rbAbsent;
    private MaterialButton btnViewStudents, btnReset, btnGeneratePdf;
    private CardView cardStudentList;
    private RecyclerView rvAttendance;
    private TextView tvStudentCount, tvCount;
    private FrameLayout loaderLayout;

    private final Calendar selectedDate = Calendar.getInstance();
    private final List<AttendanceReportItem> allAttendance = new ArrayList<>();
    private final List<AttendanceReportItem> filteredAttendance = new ArrayList<>();
    private AttendanceReportAdapter adapter;
    private boolean bindingSpinners = false;

    private static final int PAGE_WIDTH = 842;
    private static final int PAGE_HEIGHT = 595;
    private static final int MARGIN = 24;
    private static final int ROW_HEIGHT = 22;
    private static final int HEADER_HEIGHT = 30;

    private static final int[] COL_WIDTHS = {28, 55, 75, 150, 85, 85, 85, 105, 60, 120};
    private static final String[] COL_HEADERS = {
            "SN", "Adm ID", "Adm Date", "Student Name", "Contact",
            "Course", "Batch", "Batch Time", "Present", "Class Date"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_report);

        initViews();
        setupBackButton();
        setupDatePicker();
        setupFilterListeners();
        setupEmptySpinners();
        fetchAttendance();
    }
    private void initViews() {
        etClassDate = findViewById(R.id.etClassDate);
        spCourse = findViewById(R.id.spCourse);
        spBatch = findViewById(R.id.spBatch);
        spTime = findViewById(R.id.spTime);
        rbAllStudents = findViewById(R.id.cbAll);
        rbAbsent = findViewById(R.id.cbAbsent);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnReset = findViewById(R.id.btnReset);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
        cardStudentList = findViewById(R.id.cardStudentList);
        rvAttendance = findViewById(R.id.rvAttendance);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvCount = findViewById(R.id.tvCount);
        loaderLayout = findViewById(R.id.loaderLayout);

        adapter = new AttendanceReportAdapter();
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        rvAttendance.setAdapter(adapter);

        btnViewStudents.setOnClickListener(v -> fetchAttendance());
        btnReset.setOnClickListener(v -> resetFilters());
        btnGeneratePdf.setOnClickListener(v -> exportToPdf());
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
    private void setupDatePicker() {
        updateDateText();
        etClassDate.setFocusable(false);
        etClassDate.setOnClickListener(v -> new android.app.DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateText();
                    fetchAttendance();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show());
    }

    private void setupFilterListeners() {
        rbAllStudents.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) applyAttendanceFilters();
        });

        rbAbsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) applyAttendanceFilters();
        });

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!bindingSpinners) {
                    applyAttendanceFilters();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spCourse.setOnItemSelectedListener(spinnerListener);
        spBatch.setOnItemSelectedListener(spinnerListener);
        spTime.setOnItemSelectedListener(spinnerListener);
    }
    private void setupEmptySpinners() {
        setSpinnerItems(spCourse, singleItem("All Courses"));
        setSpinnerItems(spBatch, singleItem("All Batches"));
        setSpinnerItems(spTime, singleItem("All Times"));
    }

    private List<String> singleItem(String value) {
        List<String> list = new ArrayList<>();
        list.add(value);
        return list;
    }

    private void updateDateText() {
        SimpleDateFormat display = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etClassDate.setText(display.format(selectedDate.getTime()));
    }

    private String buildClassDateIso() {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        return iso.format(selectedDate.getTime());
    }

    private void fetchAttendance() {
        loaderLayout.setVisibility(View.VISIBLE);
        cardStudentList.setVisibility(View.GONE);
        btnGeneratePdf.setVisibility(View.GONE);

        String userId = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        AttendanceReportRequest request = new AttendanceReportRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                buildClassDateIso()
        );

        Log.d("ATT_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService()
                .getAttendanceReport(request)
                .enqueue(new Callback<AttendanceReportResponse>() {
                    @Override
                    public void onResponse(Call<AttendanceReportResponse> call,
                                           Response<AttendanceReportResponse> response) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d("ATT_REPORT", "HTTP=" + response.code());
                        Log.d("ATT_REPORT", "body=" + new Gson().toJson(response.body()));

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess) {

                            allAttendance.clear();
                            if (response.body().studentList != null) {
                                allAttendance.addAll(response.body().studentList);
                            }

                            bindFilterSpinnersFromAttendance();
                            applyAttendanceFilters();

                            if (allAttendance.isEmpty()) {
                                toast("No attendance records found");
                            } else {
                                cardStudentList.setVisibility(View.VISIBLE);
                                btnGeneratePdf.setVisibility(View.VISIBLE);
                            }
                        } else {
                            showResponseError(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<AttendanceReportResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.e("ATT_REPORT", "onFailure: " + t.getMessage());
                        toast("Error: " + t.getMessage());
                    }
                });
    }
    private void bindFilterSpinnersFromAttendance() {
        bindingSpinners = true;

        Set<String> courses = new LinkedHashSet<>();
        Set<String> batches = new LinkedHashSet<>();
        Set<String> times = new LinkedHashSet<>();

        courses.add("All Courses");
        batches.add("All Batches");
        times.add("All Times");

        for (AttendanceReportItem item : allAttendance) {
            addIfNotEmpty(courses, item.courseName);
            addIfNotEmpty(batches, item.batchName);
            addIfNotEmpty(times, item.timingDescription);
        }

        setSpinnerItems(spCourse, new ArrayList<>(courses));
        setSpinnerItems(spBatch, new ArrayList<>(batches));
        setSpinnerItems(spTime, new ArrayList<>(times));

        bindingSpinners = false;
    }

    private void setSpinnerItems(Spinner spinner, List<String> items) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void addIfNotEmpty(Set<String> set, String value) {
        if (value != null && !value.trim().isEmpty()) {
            set.add(value.trim());
        }
    }

    private void applyAttendanceFilters() {
        filteredAttendance.clear();

        String selectedCourse = selectedSpinnerValue(spCourse, "All Courses");
        String selectedBatch = selectedSpinnerValue(spBatch, "All Batches");
        String selectedTime = selectedSpinnerValue(spTime, "All Times");
        boolean onlyAbsent = rbAbsent.isChecked();

        for (AttendanceReportItem item : allAttendance) {
            if (!selectedCourse.isEmpty() && !same(item.courseName, selectedCourse)) continue;
            if (!selectedBatch.isEmpty() && !same(item.batchName, selectedBatch)) continue;
            if (!selectedTime.isEmpty() && !same(item.timingDescription, selectedTime)) continue;
            if (onlyAbsent && !isAbsent(item.attendanceStatus)) continue;

            filteredAttendance.add(item);
        }

        adapter.setData(filteredAttendance);
        updateCount();
    }

    private String selectedSpinnerValue(Spinner spinner, String allLabel) {
        if (spinner.getSelectedItem() == null) return "";
        String value = spinner.getSelectedItem().toString().trim();
        return value.equalsIgnoreCase(allLabel) ? "" : value;
    }

    private boolean same(String left, String right) {
        return left != null && left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean isAbsent(String status) {
        if (status == null) return false;
        String value = status.trim();
        return value.equalsIgnoreCase("No")
                || value.equalsIgnoreCase("Absent")
                || value.equalsIgnoreCase("False")
                || value.equals("0");
    }

    private void resetFilters() {
        rbAllStudents.setChecked(true);
        selectedDate.setTime(new Date());
        updateDateText();
        fetchAttendance();
    }
    private void updateCount() {
        String text = filteredAttendance.size() + " Records";
        tvCount.setText(text);
        tvStudentCount.setText(text);
        tvStudentCount.setVisibility(View.VISIBLE);
    }

    private void exportToPdf() {
        if (filteredAttendance.isEmpty()) {
            toast("No data to export");
            return;
        }

        loaderLayout.setVisibility(View.VISIBLE);
        try {
            File pdfFile = buildPdf(filteredAttendance);
            loaderLayout.setVisibility(View.GONE);
            openPdf(pdfFile);
        } catch (Exception e) {
            loaderLayout.setVisibility(View.GONE);
            Log.e("PDF_EXPORT", "Error generating PDF", e);
            toast("Failed to generate PDF: " + e.getMessage());
        }
    }
    private File buildPdf(List<AttendanceReportItem> data) throws Exception {
        PdfDocument document = new PdfDocument();

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#2E7D32"));
        titlePaint.setTextSize(14f);
        titlePaint.setFakeBoldText(true);

        Paint smallPaint = new Paint();
        smallPaint.setColor(Color.parseColor("#555555"));
        smallPaint.setTextSize(9f);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.parseColor("#2E7D32"));
        headerBgPaint.setStyle(Paint.Style.FILL);

        Paint headerTextPaint = new Paint();
        headerTextPaint.setColor(Color.WHITE);
        headerTextPaint.setTextSize(8f);
        headerTextPaint.setFakeBoldText(true);

        Paint cellPaint = new Paint();
        cellPaint.setColor(Color.parseColor("#1A1A1A"));
        cellPaint.setTextSize(8f);

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#DDDDDD"));
        gridPaint.setStrokeWidth(0.5f);

        int rowsPerPage = (PAGE_HEIGHT - (MARGIN * 2) - 70 - HEADER_HEIGHT) / ROW_HEIGHT;
        int pageCount = (int) Math.ceil(data.size() / (double) rowsPerPage);
        int dataIndex = 0;

        for (int p = 0; p < pageCount; p++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, p + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int y = MARGIN;
            canvas.drawText("Attendance Report", MARGIN, y + 12, titlePaint);
            y += 24;
            canvas.drawText("Date: " + etClassDate.getText().toString()
                    + "   Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm",
                    Locale.getDefault()).format(new Date()), MARGIN, y, smallPaint);
            y += 18;

            int x = MARGIN;
            canvas.drawRect(x, y, PAGE_WIDTH - MARGIN, y + HEADER_HEIGHT, headerBgPaint);
            for (int i = 0; i < COL_HEADERS.length; i++) {
                canvas.drawText(COL_HEADERS[i], x + 3, y + 20, headerTextPaint);
                x += COL_WIDTHS[i];
            }
            y += HEADER_HEIGHT;

            int rowsDrawn = 0;
            while (dataIndex < data.size() && rowsDrawn < rowsPerPage) {
                AttendanceReportItem item = data.get(dataIndex);
                String[] cells = {
                        String.valueOf(dataIndex + 1),
                        safe(getFirstFieldValue(item, "admissionID", "admissionId", "admId", "admID")),
                        safe(item.admissionDate),
                        safe(item.studentName),
                        safe(item.mobile),
                        safe(item.courseName),
                        safe(item.batchName),
                        safe(item.timingDescription),
                        safe(item.attendanceStatus),
                        safe(item.classDate)
                };

                x = MARGIN;
                for (int i = 0; i < cells.length; i++) {
                    canvas.drawText(clipText(cells[i], COL_WIDTHS[i] - 4, cellPaint),
                            x + 3, y + 15, cellPaint);
                    canvas.drawLine(x, y - ROW_HEIGHT + 2, x, y + ROW_HEIGHT, gridPaint);
                    x += COL_WIDTHS[i];
                }

                y += ROW_HEIGHT;
                dataIndex++;
                rowsDrawn++;
            }

            document.finishPage(page);
        }

        File pdfDir = new File(getCacheDir(), "pdfs");
        if (!pdfDir.exists()) pdfDir.mkdirs();

        File pdfFile = new File(pdfDir, "AttendanceReport_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date())
                + ".pdf");

        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();
        return pdfFile;
    }
    private void openPdf(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(Intent.createChooser(intent, "Open PDF with"));
        } catch (Exception e) {
            toast("No PDF viewer installed");
        }
    }

    private void showResponseError(Response<AttendanceReportResponse> response) {
        try {
            String err = response.errorBody() != null
                    ? response.errorBody().string()
                    : "Unknown error";
            Log.e("ATT_REPORT", "errorBody=" + err);
            toast("Failed: " + err);
        } catch (Exception e) {
            toast("Failed to load attendance");
        }
    }

    private String safe(Object value) {
        if (value == null) return "-";
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "-" : text;
    }

    private Object getFirstFieldValue(Object target, String... fieldNames) {
        if (target == null) return null;
        for (String fieldName : fieldNames) {
            try {
                java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(target);
                if (value != null && !String.valueOf(value).trim().isEmpty()) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String clipText(String text, int maxWidth, Paint paint) {
        if (paint.measureText(text) <= maxWidth) return text;
        while (text.length() > 1 && paint.measureText(text + "...") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}