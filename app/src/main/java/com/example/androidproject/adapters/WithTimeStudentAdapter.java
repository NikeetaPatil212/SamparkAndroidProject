package com.example.androidproject.adapters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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
import com.example.androidproject.model.profile.WithTimeStudentResponse;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.utils.PrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import android.telephony.SmsManager;

public class WithTimeStudentAdapter
        extends RecyclerView.Adapter<WithTimeStudentAdapter.ViewHolder> {

    private static final String TAG        = "WithTimeAdapter";
    private static final String TIMING_URL = "http://160.187.87.113:8081/api/InstituteControllersV1/batch_time_test";
  //  private static final String CHANGE_URL = "http://160.187.87.113:8081/api/InstituteControllersV1/change_time";
    private static final String CHANGE_URL = "http://160.187.87.113:8081/api/InstituteControllersV1/allot_time";

    private final int userID;
    private final int instituteID;

    // ── ADD: store course name so it's available when sending message ──
    private String courseName = "";
    private String batchName = "";

    private List<WithTimeStudentResponse.StudentItem> studentList = new ArrayList<>();

    // ── Constructor ────────────────────────────────────────────────────────
    public WithTimeStudentAdapter(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }

    // ── ADD: setter so the host Activity/Fragment can pass course name in ──
    public void setCourseName(String courseName) {
        this.courseName = courseName != null ? courseName : "";
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName != null ? batchName : "";
    }

    public void setData(List<WithTimeStudentResponse.StudentItem> list) {
        this.studentList = list;
        notifyDataSetChanged();
    }

    // ── RecyclerView boilerplate ───────────────────────────────────────────
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_with_time_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WithTimeStudentResponse.StudentItem item = studentList.get(position);

        holder.tvAdmId.setText(String.valueOf(item.getAdmissionID()));
        holder.tvFullName.setText(item.getStudentName());
        holder.tvCurrentTiming.setText("⏰ " + item.getDescription());

        holder.btnChangeTiming.setOnClickListener(v -> fetchTimingsAndShowDialog(holder.itemView.getContext(), item));
    }

    @Override
    public int getItemCount() { return studentList.size(); }

    // ──────────────────────────────────────────────────────────────────────
    // STEP 1: Fetch available timings, then open dialog
    // ──────────────────────────────────────────────────────────────────────
    private void fetchTimingsAndShowDialog(Context context, WithTimeStudentResponse.StudentItem item) {

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

                    showChangeTimingDialog(context, item, resp.getBatchList());
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
    // STEP 2: Show dialog + on success → send WhatsApp + SMS
    // ──────────────────────────────────────────────────────────────────────
    private void showChangeTimingDialog(Context context,
                                        WithTimeStudentResponse.StudentItem item,
                                        List<BatchTimingResponse.BatchTimingItem> timings) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_batch_timing, null);

        TextView                     tvStudentName = view.findViewById(R.id.tvStudentName);
        MaterialAutoCompleteTextView spBatchTiming = view.findViewById(R.id.spBatchTiming);
        TextView                     tvCapacity    = view.findViewById(R.id.tvCapacity);
        TextView                     tvAvailable   = view.findViewById(R.id.tvAvailable);
        MaterialButton               btnAllot      = view.findViewById(R.id.btnAllotTiming);
        MaterialButton               btnClose      = view.findViewById(R.id.btnClose);

        tvStudentName.setText("👤  " + item.getStudentName()
                + "\n⏰ Current: " + item.getDescription());

        btnAllot.setText("Change Timing");
        btnAllot.setTextSize(12);
        spBatchTiming.setHint("Current: " + item.getDescription());

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
                Toast.makeText(context, "Please select a new timing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selected[0].getAvailableSeats() <= 0) {
                Toast.makeText(context, "This timing is full. Please choose another.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selected[0].getTimingID() == item.getTimeID()) {
                Toast.makeText(context, "Student is already in this slot!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog changingDialog = new AlertDialog.Builder(context)
                    .setMessage("Changing timing...")
                    .setCancelable(false)
                    .create();
            changingDialog.show();

            // Capture selected timing description for message use after thread
            final String newTimingDescription = selected[0].getTimingDescription();
            final int    newTimingID          = selected[0].getTimingID();


            new Thread(() -> {
                try {

                    JSONObject body = new JSONObject();
                    body.put("timeID", selected[0].getTimingID());
                    body.put("admissionID", item.getAdmissionID());
                    body.put("userID", userID);
                    body.put("instituteID", instituteID);

                    Log.d(TAG, "CHANGE_REQUEST: " + body.toString());
                    Log.d(TAG, "CHANGE_URL: " + CHANGE_URL);

                    URL url = new URL(CHANGE_URL);

                    HttpURLConnection conn =
                            (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type",
                            "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.toString().getBytes("UTF-8"));
                        os.flush();
                    }

                    int responseCode = conn.getResponseCode();

                    Log.d(TAG, "CHANGE_RESPONSE_CODE: " + responseCode);

                    String responseText = "";

                    InputStream inputStream;

                    if (responseCode >= 200 && responseCode < 300) {
                        inputStream = conn.getInputStream();
                    } else {
                        inputStream = conn.getErrorStream();
                    }

                    if (inputStream != null) {

                        Scanner scanner =
                                new Scanner(inputStream, "UTF-8");

                        StringBuilder sb = new StringBuilder();

                        while (scanner.hasNextLine()) {
                            sb.append(scanner.nextLine());
                        }

                        scanner.close();

                        responseText = sb.toString();
                    }

                    Log.d(TAG,
                            "CHANGE_RESPONSE: [" + responseText + "]");

                    boolean isSuccess;
                    String message;

                    // Handle empty response body
                    if (responseText == null
                            || responseText.trim().isEmpty()) {

                        isSuccess = (responseCode >= 200
                                && responseCode < 300);

                        message = isSuccess
                                ? "Timing changed successfully"
                                : "Server returned empty response";

                    } else {

                        JSONObject responseJson =
                                new JSONObject(responseText);

                        isSuccess =
                                responseJson.optBoolean(
                                        "isSuccess",
                                        responseCode >= 200
                                                && responseCode < 300);

                        message =
                                responseJson.optString(
                                        "message",
                                        isSuccess
                                                ? "Timing changed successfully"
                                                : "Failed to change timing");
                    }

                    final boolean finalSuccess = isSuccess;
                    final String finalMessage = message;

                    new Handler(Looper.getMainLooper()).post(() -> {

                        changingDialog.dismiss();

                        if (finalSuccess) {

                            dialog.dismiss();

                            // Update adapter item
                            item.setDescription(
                                    newTimingDescription);

                            item.setTimeID(
                                    newTimingID);

                            notifyDataSetChanged();

                            Toast.makeText(
                                    context,
                                    "✅ " + finalMessage,
                                    Toast.LENGTH_LONG
                            ).show();

                            // Send notification
                            sendBatchAllotmentNotification(
                                    context,
                                    item.getStudentName(),
                                    item.getMobile(),
                                    newTimingDescription
                            );

                        } else {

                            Toast.makeText(
                                    context,
                                    finalMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

                } catch (Exception e) {

                    Log.e(TAG,
                            "CHANGE_API_ERROR",
                            e);

                    new Handler(Looper.getMainLooper()).post(() -> {

                        changingDialog.dismiss();

                        Toast.makeText(
                                context,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }

            }).start();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ──────────────────────────────────────────────────────────────────────
    // Send WhatsApp message + SMS using template category "Batch Allotment"
    // ──────────────────────────────────────────────────────────────────────
    private void sendBatchAllotmentNotification(Context context,
                                                String studentName,
                                                String studentMobile,
                                                String batchTime) {
        TemplateRepository.getInstance(context)
                .getTemplateByCategory("Change Batch",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {

                                if (!template.isActive) {
                                    Log.d(TAG, "Template isActive=false, skipping message");
                                    Toast.makeText(context,
                                            "WhatsApp notifications are currently disabled.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(context);

                                // ── Build placeholder map ──────────────────────────────
                                Map<String, String> data = new HashMap<>();
                                data.put("StudentName", studentName);
                                data.put("course",      courseName);          // set via setCourseName()
                                data.put("BatchName",   batchName);         // if you have a separate batch name field, use it; fallback to timing description
                                data.put("BatchTime", batchTime);                                data.put("BatchTime",   batchTime);
                                data.put("institute",   pref.getInstituteName());
                                data.put("Authority",   pref.getStudentName());
                                data.put("mobile1",     pref.getInstituteMobile1());
                                data.put("mobile2",     pref.getInstituteMobile2());
                                data.put("email",       pref.getInstituteEmail());
                                data.put("address1",    pref.getInstituteAddress1());
                                data.put("address2",    pref.getInstituteAddress2());

                                // ── Pick language ──────────────────────────────────────
                                String lang = pref.getLanguage();
                                String templateText;
                                switch (lang) {
                                    case "MR": templateText = template.wa_MR; break;
                                    case "HI": templateText = template.wa_HI; break;
                                    default:   templateText = template.wa_EN; break;
                                }

                                String waMessage = TemplateRepository.fillTemplate(templateText, data);

                                // ── Pick SMS template ──────────────────────────────────
                                String smsTemplateText;
                                switch (lang) {
                                    case "MR": smsTemplateText = template.sms_MR; break;
                                    case "HI": smsTemplateText = template.sms_HI; break;
                                    default:   smsTemplateText = template.sms_EN; break;
                                }
                                String smsMessage = TemplateRepository.fillTemplate(smsTemplateText, data);

                                Log.d(TAG, "WA message: " + waMessage);
                                Log.d(TAG, "SMS message: " + smsMessage);

                                // ── Send SMS in background ─────────────────────────────
                                sendSmsInBackground(context, studentMobile, smsMessage);

                                // ── Open WhatsApp ──────────────────────────────────────
                                openWhatsApp(context, studentMobile, waMessage);
                            }

                            @Override
                            public void onError(String error) {
                                Log.w(TAG, "Batch Allotment template not found: " + error);
                                // No template — silently skip messaging
                            }
                        });
    }

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
        TextView tvAdmId, tvFullName, tvCurrentTiming, btnChangeTiming;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdmId         = itemView.findViewById(R.id.tvAdmId);
            tvFullName      = itemView.findViewById(R.id.tvFullName);
            tvCurrentTiming = itemView.findViewById(R.id.tvCurrentTiming);
            btnChangeTiming = itemView.findViewById(R.id.btnChangeTiming);
        }
    }
}