package com.example.androidproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.StudyMaterialAdapter;
import com.example.androidproject.model.Batch;
import com.example.androidproject.model.BatchRequest;
import com.example.androidproject.model.BatchResponse;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.model.profile.BatchTimingResponse;
import com.example.androidproject.model.profile.StudyMaterialDistributionResponse;
import com.example.androidproject.model.profile.StudyMaterialUpdateRequest;
import com.example.androidproject.model.profile.StudyMaterialUpdateResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DistributeStudyMaterialActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private AutoCompleteTextView spCourse, spBatch, spTiming;
    private ImageView ivBatchArrow, ivBatchIcon, ivTimingArrow, ivTimingIcon;
    private TextView tvDate, tvStudentCount;
    private CardView cardStudentList;
    private RecyclerView rvStudents;
    private FrameLayout loaderLayout;
    private MaterialButton btnViewStudents, btnUpdateRecord, btnUpdateWithMsg;
    private CheckBox cbSelectAll;

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<Course> courseList = new ArrayList<>();
    private List<Batch>  batchList  = new ArrayList<>();
    private List<BatchTimingResponse.BatchTimingItem> timingList = new ArrayList<>();
    private List<StudyMaterialDistributionResponse.StudentItem> allStudents = new ArrayList<>();

    private int selectedCourseId = -1;
    private int selectedBatchId  = -1;
    private int selectedTimingId = -1; // -1 = no filter

    private StudyMaterialAdapter adapter;
    private boolean pendingWhatsAppReturn = false;

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

    private void initViews() {
        spCourse        = findViewById(R.id.spCourse);
        spBatch         = findViewById(R.id.spBatch);
        spTiming        = findViewById(R.id.spTiming);
        ivBatchArrow    = findViewById(R.id.ivBatchArrow);
        ivBatchIcon     = findViewById(R.id.ivBatchIcon);
        ivTimingArrow   = findViewById(R.id.ivTimingArrow);
        ivTimingIcon    = findViewById(R.id.ivTimingIcon);
        tvDate          = findViewById(R.id.tvDate);
        tvStudentCount  = findViewById(R.id.tvStudentCount);
        cardStudentList = findViewById(R.id.cardStudentList);
        rvStudents      = findViewById(R.id.rvStudents);
        loaderLayout    = findViewById(R.id.loaderLayout);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnUpdateRecord = findViewById(R.id.btnUpdateRecord);
        cbSelectAll     = findViewById(R.id.cbSelectAll);
        btnUpdateWithMsg = findViewById(R.id.btnUpdateWithMsg);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyMaterialAdapter();
        rvStudents.setAdapter(adapter);

        adapter.setSendMsgListener(student -> sendWhatsAppToStudent(student));

        spBatch.setEnabled(false);
        spTiming.setEnabled(false);
        btnViewStudents.setEnabled(false);

        // Select All toggle
        cbSelectAll.setOnCheckedChangeListener((btn, isChecked) -> {
            adapter.setAllChecked(isChecked);
        });

        // View Students button
        btnViewStudents.setOnClickListener(v -> fetchStudents());

        // Update Record button
       /* btnUpdateRecord.setOnClickListener(v -> updateRecord());*/

        btnUpdateRecord.setOnClickListener(v -> {
            // Turn OFF msg mode if it was on
            adapter.setMsgMode(false);
            performUpdate(false);
        });

// Update + Send Msg — save then show Send Msg button per row
        btnUpdateWithMsg.setOnClickListener(v -> {
            performUpdate(true);
        });

    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
    }

    // ── Fetch courses ─────────────────────────────────────────────────────────
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
                        } else {
                            toast("Failed to fetch courses");
                        }
                    }
                    @Override
                    public void onFailure(Call<GetCoursesResponse> call, Throwable t) {
                        toast("Course error: " + t.getMessage());
                    }
                });
    }

    private void setupCourseDropdown() {
        List<String> names = new ArrayList<>();
        for (Course c : courseList) names.add(c.getCouse_Name());

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spCourse.setAdapter(aa);
        spCourse.setOnClickListener(v -> spCourse.showDropDown());

        spCourse.setOnItemClickListener((parent, view, position, id) -> {
            selectedCourseId = courseList.get(position).getCouseID();
            resetBatch();
            resetTiming();
            hideStudentList();
            fetchBatches(selectedCourseId);
        });
    }

    // ── Fetch batches ─────────────────────────────────────────────────────────
    private void fetchBatches(int courseId) {
        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        BatchRequest request = new BatchRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId), courseId);

        RetrofitClient.getApiService().getBatch(request)
                .enqueue(new Callback<BatchResponse>() {
                    @Override
                    public void onResponse(Call<BatchResponse> call,
                                           Response<BatchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            batchList = response.body().getBatchList();
                            setupBatchDropdown();
                        } else {
                            toast("Failed to fetch batches");
                        }
                    }
                    @Override
                    public void onFailure(Call<BatchResponse> call, Throwable t) {
                        toast("Batch error: " + t.getMessage());
                    }
                });
    }

    private void setupBatchDropdown() {
        List<String> names = new ArrayList<>();
        for (Batch b : batchList) names.add(b.getBatchName());

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spBatch.setAdapter(aa);
        spBatch.setEnabled(true);
        spBatch.setOnClickListener(v -> spBatch.showDropDown());

        ivBatchArrow.setColorFilter(
                getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        ivBatchIcon.setColorFilter(
                getResources().getColor(android.R.color.holo_green_dark, getTheme()));

        spBatch.setOnItemClickListener((parent, view, position, id) -> {
            selectedBatchId = batchList.get(position).getBatchID();
            resetTiming();
            hideStudentList();
            btnViewStudents.setEnabled(true);
            // Timing dropdown is optional — enable it after batch selected
            setupTimingDropdown();
        });
    }

    // ── Timing dropdown (optional filter) ────────────────────────────────────
    private void setupTimingDropdown() {
        // Re-use your existing batch timing fetch if you have it,
        // or just enable with the timings already fetched for this batch.
        // For now wire it from your existing TIMING_URL call if needed.
        // Minimal: just enable it so user can optionally pick.
        spTiming.setEnabled(true);
        ivTimingArrow.setColorFilter(
                getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        ivTimingIcon.setColorFilter(
                getResources().getColor(android.R.color.holo_green_dark, getTheme()));

        // You can load real timings here via your existing batch_time_test API
        // For now just add an "All Timings" placeholder:
        List<String> timingNames = new ArrayList<>();
        timingNames.add("All Timings");
        for (BatchTimingResponse.BatchTimingItem t : timingList) {
            timingNames.add(t.getTimingDescription());
        }

        ArrayAdapter<String> aa = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, timingNames);
        spTiming.setAdapter(aa);
        spTiming.setOnClickListener(v -> spTiming.showDropDown());

        spTiming.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedTimingId = -1; // All
            } else {
                selectedTimingId = timingList.get(position - 1).getTimingID();
            }
            // If students already loaded, re-filter
            if (!allStudents.isEmpty()) applyTimingFilter();
        });
    }


    private void fetchStudents() {

        if (selectedCourseId == -1 || selectedBatchId == -1) {
            toast("Please select Course and Batch");
            return;
        }

        loaderLayout.setVisibility(View.VISIBLE);
        hideStudentList();

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        // 🔥 LOG REQUEST HERE
        Log.d("API_REQUEST_PARAMS",
                "userID=" + userId +
                        ", instituteID=" + instituteId +
                        ", courseID=" + selectedCourseId +
                        ", batchID=" + selectedBatchId);

        String fullUrl = "http://160.25.62.225:8081/api/InstituteControllersV1/list_of_distribution"
                + "?userID=" + userId
                + "&instituteID=" + instituteId
                + "&CourseID=" + selectedCourseId
                + "&batchID=" + selectedBatchId;

        Log.d("API_FULL_URL", fullUrl);

        RetrofitClient.getApiService()
                .getDistributionList(
                        Integer.parseInt(userId),
                        Integer.parseInt(instituteId),
                        selectedCourseId,
                        1)
                .enqueue(new Callback<StudyMaterialDistributionResponse>() {

                    @Override
                    public void onResponse(Call<StudyMaterialDistributionResponse> call,
                                           Response<StudyMaterialDistributionResponse> response) {

                        loaderLayout.setVisibility(View.GONE);

                        // 🔥 ALSO LOG RAW RESPONSE
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

    // ── Filter by timing if selected ──────────────────────────────────────────
    private void applyTimingFilter() {
        List<StudyMaterialDistributionResponse.StudentItem> filtered;

        if (selectedTimingId == -1) {
            filtered = allStudents;
        } else {
            filtered = new ArrayList<>();
            for (StudyMaterialDistributionResponse.StudentItem s : allStudents) {
                // Filter by timing — add timingID field to your model if API returns it
                filtered.add(s);
            }
        }

        cardStudentList.setVisibility(View.VISIBLE);
        tvStudentCount.setText(filtered.size() + " Students");
        adapter.setData(filtered);
        cbSelectAll.setChecked(false);
    }

    // ── Update Record ─────────────────────────────────────────────────────────
  /*  private void updateRecord() {
        List<StudyMaterialDistributionResponse.StudentItem> checkedStudents =
                adapter.getCheckedStudents();

        if (checkedStudents.isEmpty()) {
            toast("Please select at least one student");
            return;
        }

        // Build admissionID list for API
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

                            // ── Send WhatsApp to all checked students ──────────
                            sendWhatsAppToStudents(checkedStudents);

                            // Refresh list
                            fetchStudents();

                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().getMessage()
                                    : "Update failed";
                            toast(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StudyMaterialUpdateResponse> call,
                                          Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        toast("Failed: " + t.getMessage());
                    }
                });
    }*/

    private void performUpdate(boolean withMsg) {
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

                            if (withMsg) {
                                // Show Send Msg button on every row
                                adapter.setMsgMode(true);
                                toast("Tap 'Send Msg' on each student to send WhatsApp");
                            } else {
                                // Normal update — just refresh
                                fetchStudents();
                            }

                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().getMessage()
                                    : "Update failed";
                            toast(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StudyMaterialUpdateResponse> call,
                                          Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        toast("Failed: " + t.getMessage());
                    }
                });
    }

    // ── Single student WhatsApp (called from row Send Msg button) ─────────────

    // ── Add onResume() — auto-refresh when user comes back from WhatsApp ───────
    @Override
    protected void onResume() {
        super.onResume();
        if (pendingWhatsAppReturn) {
            pendingWhatsAppReturn = false;

            // Turn off msg mode — hide all Send Msg buttons
            adapter.setMsgMode(false);

            // Refresh student list
            fetchStudents();

            toast("List refreshed");
        }
    }

    // ── Replace sendWhatsAppToStudent() with this ─────────────────────────────
    private void sendWhatsAppToStudent(
            StudyMaterialDistributionResponse.StudentItem student) {

        String mobile = student.getMobile();
        if (mobile == null || mobile.trim().isEmpty()) {
            toast("No mobile number for: " + student.getStudentName());
            return;
        }

        mobile = mobile.replaceAll("[^0-9]", "");
        if (!mobile.startsWith("91") && mobile.length() == 10) {
            mobile = "91" + mobile;
        }

        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date());

        String message =
                "Dear " + student.getStudentName() + ",\n\n" +
                        "📚 Study material has been distributed to you on " + today + ".\n\n" +
                        "Please collect it from the institute.\n\n" +
                        "Thank you!";

        try {
            Uri uri = Uri.parse(
                    "https://api.whatsapp.com/send?phone=" + mobile
                            + "&text=" + Uri.encode(message));

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.whatsapp");

            // ── Set flag BEFORE launching WhatsApp ────────────────────────────
            pendingWhatsAppReturn = true;

            startActivity(intent);

        } catch (Exception e) {
            pendingWhatsAppReturn = false;
            Log.e("WHATSAPP", "Failed: " + e.getMessage());

            // Fallback — try without forcing WhatsApp package
            try {
                Uri uri = Uri.parse(
                        "https://api.whatsapp.com/send?phone=" + mobile
                                + "&text=" + Uri.encode(message));
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                pendingWhatsAppReturn = true;
            } catch (Exception ex) {
                toast("WhatsApp not installed for: " + student.getStudentName());
            }
        }
    }

   /* private void sendWhatsAppToStudents(
            List<StudyMaterialDistributionResponse.StudentItem> students) {

        // Compose the message — customize as needed
        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date());

        for (StudyMaterialDistributionResponse.StudentItem student : students) {

            String mobile = student.getMobile();
            if (mobile == null || mobile.trim().isEmpty()) continue;

            // Clean number — remove spaces, dashes, brackets
            mobile = mobile.replaceAll("[^0-9]", "");

            // Add India country code if not present
            if (!mobile.startsWith("91") && mobile.length() == 10) {
                mobile = "91" + mobile;
            }

            String message =
                    "Dear " + student.getStudentName() + ",\n\n" +
                            "📚 Study material has been distributed to you on " + today + ".\n\n" +
                            "Please collect it from the institute.\n\n" +
                            "Thank you!";

            try {
                // ── Option 1: Direct WhatsApp chat (no need for contact saved) ──
                Uri uri = Uri.parse(
                        "https://api.whatsapp.com/send?phone=" + mobile
                                + "&text=" + Uri.encode(message));

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.whatsapp"); // force WhatsApp app
                startActivity(intent);

            } catch (Exception e) {
                Log.e("WHATSAPP", "Failed for " + mobile + ": " + e.getMessage());
                toast("WhatsApp not installed or number invalid for: "
                        + student.getStudentName());
            }
        }
    }*/


    // ── Helpers ───────────────────────────────────────────────────────────────
    private void hideStudentList() {
        cardStudentList.setVisibility(View.GONE);
        allStudents.clear();
    }

    private void resetBatch() {
        batchList.clear();
        selectedBatchId = -1;
        spBatch.setText("");
        spBatch.setAdapter(null);
        spBatch.setEnabled(false);
        ivBatchArrow.setColorFilter(
                getResources().getColor(android.R.color.darker_gray, getTheme()));
        ivBatchIcon.setColorFilter(
                getResources().getColor(android.R.color.darker_gray, getTheme()));
        btnViewStudents.setEnabled(false);
    }

    private void resetTiming() {
        selectedTimingId = -1;
        spTiming.setText("");
        spTiming.setEnabled(false);
        ivTimingArrow.setColorFilter(
                getResources().getColor(android.R.color.darker_gray, getTheme()));
        ivTimingIcon.setColorFilter(
                getResources().getColor(android.R.color.darker_gray, getTheme()));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}