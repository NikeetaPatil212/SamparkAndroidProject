package com.example.androidproject.adapters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.profile.BatchTimingResponse;
import com.example.androidproject.model.profile.TimingLessStudentResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.utils.PrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TimingLessStudentAdapter extends RecyclerView.Adapter<TimingLessStudentAdapter.ViewHolder> {

    private static final String TAG        = "TimingLessAdapter";
    private static final String TIMING_URL = "http://160.187.87.113:8081/api/InstituteControllersV1/batch_time_test";
    private static final String ALLOT_URL  = "http://160.187.87.113:8081/api/InstituteControllersV1/allot_time";

    private final int userID;
    private final int instituteID;

    // ── Optional: course name passed from Activity ─────────────────────────
    private String courseName = "";
    private String batchName = "";
    public void setCourseName(String courseName) {
        this.courseName = courseName != null ? courseName : "";
    }
    public void setBatchName(String batchName) {
        this.batchName = batchName != null ? batchName : "";
    }

    // ── Callback so Activity can refresh tiles after allotment ─────────────
    public interface OnTimingsFetchedListener {
        void onTimingsFetched(List<BatchTimingResponse.BatchTimingItem> timings);
    }
    private OnTimingsFetchedListener timingsFetchedListener;

    public void setOnTimingsFetchedListener(OnTimingsFetchedListener l) {
        this.timingsFetchedListener = l;
    }

    // ── Data ───────────────────────────────────────────────────────────────
    private List<TimingLessStudentResponse.StudentItem> studentList = new ArrayList<>();

    // ── Constructor ────────────────────────────────────────────────────────
    public TimingLessStudentAdapter(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }

    public void setData(List<TimingLessStudentResponse.StudentItem> list) {
        this.studentList = list;
        notifyDataSetChanged();
    }

    // ── RecyclerView boilerplate ───────────────────────────────────────────
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_allot_batch_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimingLessStudentResponse.StudentItem item = studentList.get(position);

        holder.tvAdmId.setText(String.valueOf(item.getAdmissionID()));
        holder.tvStudentName.setText(item.getStudentName());

        holder.itemView.setOnClickListener(v ->
                fetchTimingsAndShowDialog(holder.itemView.getContext(), item));
    }

    @Override
    public int getItemCount() { return studentList.size(); }

    // ──────────────────────────────────────────────────────────────────────
    // STEP 1: Fetch batch timings from API, then open dialog
    // ──────────────────────────────────────────────────────────────────────
    private void fetchTimingsAndShowDialog(Context context, TimingLessStudentResponse.StudentItem item) {

        AlertDialog loadingDialog = new AlertDialog.Builder(context)
                .setMessage("Loading batch timings…")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("userID",      userID);
                body.put("instituteID", instituteID);

                URL url = new URL(TIMING_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15_000);
                conn.setReadTimeout(15_000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();
                Scanner scanner = new Scanner(
                        code == 200 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8");
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) sb.append(scanner.nextLine());
                scanner.close();

                com.google.gson.Gson gson = new com.google.gson.Gson();
                BatchTimingResponse resp  = gson.fromJson(sb.toString(), BatchTimingResponse.class);

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    loadingDialog.dismiss();

                    if (resp == null || !resp.isSuccess()
                            || resp.getBatchList() == null
                            || resp.getBatchList().isEmpty()) {

                        String msg = (resp != null && resp.getMessage() != null)
                                ? resp.getMessage() : "No batch timings found";
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<BatchTimingResponse.BatchTimingItem> timings = resp.getBatchList();

                    if (timingsFetchedListener != null) {
                        timingsFetchedListener.onTimingsFetched(timings);
                    }

                    showBatchTimingDialog(context, item, timings);
                });

            } catch (Exception e) {
                Log.e(TAG, "fetchTimings error: " + e.getMessage(), e);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(context,
                            "Failed to load timings: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ──────────────────────────────────────────────────────────────────────
    // STEP 2: Show dialog — on success call WhatsApp + SMS
    // ──────────────────────────────────────────────────────────────────────
    private void showBatchTimingDialog(Context context,
                                       TimingLessStudentResponse.StudentItem item,
                                       List<BatchTimingResponse.BatchTimingItem> timings) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_batch_timing, null);

        TextView                     tvStudentName = view.findViewById(R.id.tvStudentName);
        MaterialAutoCompleteTextView spBatchTiming = view.findViewById(R.id.spBatchTiming);
        TextView                     tvCapacity    = view.findViewById(R.id.tvCapacity);
        TextView                     tvAvailable   = view.findViewById(R.id.tvAvailable);
        MaterialButton               btnAllot      = view.findViewById(R.id.btnAllotTiming);
        MaterialButton               btnClose      = view.findViewById(R.id.btnClose);

        tvStudentName.setText("👤  " + item.getStudentName());
        tvCapacity.setText("—");
        tvAvailable.setText("—");

        List<String> labels = new ArrayList<>();
        for (BatchTimingResponse.BatchTimingItem t : timings) {
            labels.add(t.dropdownLabel());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_dropdown_item_1line, labels);
        spBatchTiming.setThreshold(0);
        spBatchTiming.setAdapter(spinnerAdapter);
        spBatchTiming.setOnClickListener(v -> spBatchTiming.showDropDown());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        final BatchTimingResponse.BatchTimingItem[] selected = {null};

        spBatchTiming.setOnItemClickListener((parent, v, pos, id) -> {
            selected[0] = timings.get(pos);
            tvCapacity.setText(String.valueOf(selected[0].getCapacity()));
            tvAvailable.setText(String.valueOf(selected[0].getAvailableSeats()));

            if (selected[0].getAvailableSeats() <= 0) {
                tvAvailable.setTextColor(0xFFE53935);
                Toast.makeText(context, "⚠️ This timing slot is full!", Toast.LENGTH_SHORT).show();
            } else {
                tvAvailable.setTextColor(0xFF2E7D32);
            }
        });

        btnAllot.setOnClickListener(v -> {

            if (selected[0] == null) {
                Toast.makeText(context, "Please select a batch timing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selected[0].getAvailableSeats() <= 0) {
                Toast.makeText(context, "This timing is full. Please choose another.", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog allotLoadingDialog = new AlertDialog.Builder(context)
                    .setMessage("Allotting timing...")
                    .setCancelable(false)
                    .create();
            allotLoadingDialog.show();

            // Capture values before entering thread
            final String selectedTimingDescription = selected[0].getTimingDescription();

            new Thread(() -> {
                try {
                    JSONObject body = new JSONObject();
                    body.put("timeID",       selected[0].getTimingID());
                    body.put("admissionID",  item.getAdmissionID());
                    body.put("userID",       userID);
                    body.put("instituteID",  instituteID);

                    Log.d(TAG, "ALLOT_REQUEST: " + body);

                    URL url = new URL(ALLOT_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15_000);
                    conn.setReadTimeout(15_000);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.toString().getBytes("UTF-8"));
                    }

                    int responseCode = conn.getResponseCode();
                    Scanner scanner = new Scanner(
                            responseCode >= 200 && responseCode < 300
                                    ? conn.getInputStream()
                                    : conn.getErrorStream(), "UTF-8");
                    StringBuilder sb = new StringBuilder();
                    while (scanner.hasNextLine()) sb.append(scanner.nextLine());
                    scanner.close();

                    String responseText = sb.toString();
                    Log.d(TAG, "ALLOT_RESPONSE: " + responseText);

                    JSONObject responseJson = new JSONObject(responseText);
                    boolean isSuccess = responseJson.optBoolean("isSuccess", false);
                    String  message   = responseJson.optString("message", "Failed to allot timing");

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        allotLoadingDialog.dismiss();

                        if (isSuccess) {
                            dialog.dismiss();
                            Toast.makeText(context, "✅ " + message, Toast.LENGTH_LONG).show();

                            // ── SEND WHATSAPP + SMS ────────────────────────────────
                            sendBatchAllotmentNotification(
                                    context,
                                    item.getStudentName(),
                                    item.getMobile(),             // ← must exist in StudentItem model
                                    selectedTimingDescription
                            );

                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "ALLOT_API_ERROR: " + e.getMessage(), e);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        allotLoadingDialog.dismiss();
                        Toast.makeText(context, "Failed to allot timing: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ──────────────────────────────────────────────────────────────────────
    // STEP 3: Fetch template "Batch Allotment" → fill → send WA + SMS
    // ──────────────────────────────────────────────────────────────────────
    private void sendBatchAllotmentNotification(Context context,
                                                String studentName,
                                                String studentMobile,
                                                String batchTime) {

        TemplateRepository.getInstance(context)
                .getTemplateByCategory("Batch Allotment",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {

                                if (!template.isActive) {
                                    Log.d(TAG, "Template isActive=false — skipping message");
                                    Toast.makeText(context,
                                            "WhatsApp notifications are currently disabled.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(context);

                                // ── Placeholder map ────────────────────────────────────
                                Map<String, String> data = new HashMap<>();
                                data.put("StudentName", studentName);
                                data.put("course",      courseName);           // set via setCourseName()
                                data.put("BatchName",   batchName);           // same as course here; override if you have a separate batch name
                                data.put("BatchTime",   batchTime);
                                data.put("institute",   pref.getInstituteName());
                                data.put("Authority",   pref.getStudentName());
                                data.put("mobile1",     pref.getInstituteMobile1());
                                data.put("mobile2",     pref.getInstituteMobile2());
                                data.put("email",       pref.getInstituteEmail());
                                data.put("address1",    pref.getInstituteAddress1());
                                data.put("address2",    pref.getInstituteAddress2());

                                // ── WhatsApp message ───────────────────────────────────
                                String lang = pref.getLanguage();
                                String waTemplateText;
                                switch (lang) {
                                    case "MR": waTemplateText = template.wa_MR; break;
                                    case "HI": waTemplateText = template.wa_HI; break;
                                    default:   waTemplateText = template.wa_EN; break;
                                }
                                String waMessage = TemplateRepository.fillTemplate(waTemplateText, data);

                                // ── SMS message ────────────────────────────────────────
                                String smsTemplateText;
                                switch (lang) {
                                    case "MR": smsTemplateText = template.sms_MR; break;
                                    case "HI": smsTemplateText = template.sms_HI; break;
                                    default:   smsTemplateText = template.sms_EN; break;
                                }
                                String smsMessage = TemplateRepository.fillTemplate(smsTemplateText, data);

                                // ── Debug logs ─────────────────────────────────────────
                                Log.d(TAG, "LANG: " + lang);
                                Log.d(TAG, "StudentName: " + studentName);
                                Log.d(TAG, "Mobile: " + studentMobile);
                                Log.d(TAG, "Course: " + courseName);
                                Log.d(TAG, "BatchTime: " + batchTime);
                                Log.d(TAG, "WA message: " + waMessage);
                                Log.d(TAG, "SMS message: " + smsMessage);

                                // ── Send ───────────────────────────────────────────────
                                sendSmsInBackground(context, studentMobile, smsMessage);
                                openWhatsApp(context, studentMobile, waMessage);
                            }

                            @Override
                            public void onError(String error) {
                                Log.w(TAG, "Batch Allotment template not found: " + error);
                                // No template → silently skip, allotment already succeeded
                            }
                        });
    }

    // ── SMS ────────────────────────────────────────────────────────────────
    private void sendSmsInBackground(Context context, String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "SMS permission not granted");
                return;
            }
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            Log.d(TAG, "SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "SMS failed: " + e.getMessage());
        }
    }

    // ── WhatsApp ───────────────────────────────────────────────────────────
    private void openWhatsApp(Context context, String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + phoneNumber
                    + "?text=" + Uri.encode(message)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdmId, tvStudentName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdmId       = itemView.findViewById(R.id.tvAdmId);
            tvStudentName = itemView.findViewById(R.id.tvFullName);
        }
    }
}