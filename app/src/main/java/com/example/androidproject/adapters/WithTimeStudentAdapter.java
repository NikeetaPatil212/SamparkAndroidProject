package com.example.androidproject.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.profile.BatchTimingResponse;
import com.example.androidproject.model.profile.WithTimeStudentResponse;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WithTimeStudentAdapter
        extends RecyclerView.Adapter<WithTimeStudentAdapter.ViewHolder> {

    private static final String TAG        = "WithTimeAdapter";
    // Reuse same timing fetch URL as TimingLessStudentAdapter
    private static final String TIMING_URL = "http://160.25.62.225:8081/api/InstituteControllersV1/batch_time_test";
    // Change timing URL — replace with your real endpoint
    private static final String CHANGE_URL = "http://160.25.62.225:8081/api/InstituteControllersV1/change_time";

    private final int userID;
    private final int instituteID;

    private List<WithTimeStudentResponse.StudentItem> studentList = new ArrayList<>();

    // ── Constructor ────────────────────────────────────────────────────────
    public WithTimeStudentAdapter(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
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
        holder.tvCurrentTiming.setText("⏰ " + item.getDescription()); // e.g. "Morning 7 AM"

        // "Change" button tap → fetch timings → show dialog
        holder.btnChangeTiming.setOnClickListener(v ->
                fetchTimingsAndShowDialog(holder.itemView.getContext(), item));
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
    // STEP 2: Show SAME dialog design as TimingLessStudentAdapter
    //         but with "Change Timing" header + current slot shown
    // ──────────────────────────────────────────────────────────────────────
    private void showChangeTimingDialog(Context context,
                                        WithTimeStudentResponse.StudentItem item,
                                        List<BatchTimingResponse.BatchTimingItem> timings) {

        // ── Reuse exact same dialog layout ────────────────────────────────
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_batch_timing, null);

        TextView                     tvStudentName = view.findViewById(R.id.tvStudentName);
        MaterialAutoCompleteTextView spBatchTiming = view.findViewById(R.id.spBatchTiming);
        TextView                     tvCapacity    = view.findViewById(R.id.tvCapacity);
        TextView                     tvAvailable   = view.findViewById(R.id.tvAvailable);
        MaterialButton               btnAllot      = view.findViewById(R.id.btnAllotTiming);
        MaterialButton               btnClose      = view.findViewById(R.id.btnClose);

        // ── Customize for "Change" mode ───────────────────────────────────
        tvStudentName.setText("👤  " + item.getStudentName()
                + "\n⏰ Current: " + item.getDescription());

        // Change button label
        btnAllot.setText("Change Timing");
        btnAllot.setTextSize(12);

        // Pre-fill dropdownwith current slot hint
        spBatchTiming.setHint("Current: " + item.getDescription());

        // ── Build dropdown from live timings ──────────────────────────────
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
            Log.d("09876543we4r5t6yui", "showChangeTimingDialog: " + selected[0].getCapacity());
            tvAvailable.setText(String.valueOf(selected[0].getAvailableSeats()));

            if (selected[0].getAvailableSeats() <= 0) {
                tvAvailable.setTextColor(0xFFE53935);
                Toast.makeText(context,
                        "⚠️ This timing slot is full!", Toast.LENGTH_SHORT).show();
            } else {
                tvAvailable.setTextColor(0xFF2E7D32);
            }
        });

        // ── Confirm change ────────────────────────────────────────────────
        btnAllot.setOnClickListener(v -> {

            if (selected[0] == null) {
                Toast.makeText(context,
                        "Please select a new timing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selected[0].getAvailableSeats() <= 0) {
                Toast.makeText(context,
                        "This timing is full. Please choose another.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Prevent same slot selection
            if (selected[0].getTimingID() == item.getTimeID()) {
                Toast.makeText(context,
                        "Student is already in this slot!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog changingDialog = new AlertDialog.Builder(context)
                    .setMessage("Changing timing...")
                    .setCancelable(false)
                    .create();
            changingDialog.show();

            new Thread(() -> {
                try {
                    JSONObject body = new JSONObject();
                    body.put("timeID",       selected[0].getTimingID());
                    body.put("admissionID",  item.getAdmissionID());
                    body.put("userID",       userID);
                    body.put("instituteID",  instituteID);

                    Log.d(TAG, "CHANGE_REQUEST: " + body);

                    URL url = new URL(CHANGE_URL);
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
                    Log.d(TAG, "CHANGE_RESPONSE: " + responseText);

                    JSONObject responseJson = new JSONObject(responseText);
                    boolean isSuccess = responseJson.optBoolean("isSuccess", false);
                    String message    = responseJson.optString("message", "Failed to change timing");

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        changingDialog.dismiss();

                        if (isSuccess) {
                            dialog.dismiss();

                            // Update row immediately without re-fetching
                            item.setDescription(selected[0].getTimingDescription());
                            item.setTimeID(selected[0].getTimingID());
                            notifyDataSetChanged();

                            Toast.makeText(context,
                                    "✅ " + message, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "CHANGE_API_ERROR: " + e.getMessage(), e);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        changingDialog.dismiss();
                        Toast.makeText(context,
                                "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdmId, tvFullName, tvCurrentTiming, btnChangeTiming;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdmId        = itemView.findViewById(R.id.tvAdmId);
            tvFullName     = itemView.findViewById(R.id.tvFullName);
            tvCurrentTiming= itemView.findViewById(R.id.tvCurrentTiming);
            btnChangeTiming= itemView.findViewById(R.id.btnChangeTiming);
        }
    }
}