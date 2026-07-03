package com.example.androidproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.summary.AttendanceReportItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.ViewHolder> {

    private List<AttendanceReportItem> allItems = new ArrayList<>();
    private List<AttendanceReportItem> filteredItems = new ArrayList<>();

    public void setData(List<AttendanceReportItem> items) {
        allItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        filteredItems = new ArrayList<>(allItems);
        notifyDataSetChanged();
    }

    public void applyFilters(String name, String mobile, String course, List<String> statuses) {
        applyFilters(name, mobile, course, "", "", statuses);
    }

    public void applyFilters(String name, String mobile, String course, String batch,
                             String timing, List<String> statuses) {
        String searchName = safe(name).toLowerCase();
        String searchMobile = safe(mobile).toLowerCase();
        String selectedCourse = safe(course);
        String selectedBatch = safe(batch);
        String selectedTiming = safe(timing);

        filteredItems.clear();

        for (AttendanceReportItem item : allItems) {
            if (!searchName.isEmpty()
                    && !safe(item.studentName).toLowerCase().contains(searchName)) {
                continue;
            }

            if (!searchMobile.isEmpty()
                    && !safe(item.mobile).toLowerCase().contains(searchMobile)) {
                continue;
            }

            if (!selectedCourse.isEmpty()
                    && !safe(item.courseName).equalsIgnoreCase(selectedCourse)) {
                continue;
            }

            if (!selectedBatch.isEmpty()
                    && !safe(item.batchName).equalsIgnoreCase(selectedBatch)) {
                continue;
            }

            if (!selectedTiming.isEmpty()
                    && !safe(item.timingDescription).equalsIgnoreCase(selectedTiming)) {
                continue;
            }

            if (statuses != null && !statuses.isEmpty() && !containsIgnoreCase(statuses, "All")) {
                String status = safe(item.attendanceStatus);
                boolean matched = false;
                for (String s : statuses) {
                    if (status.equalsIgnoreCase(safe(s))) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) continue;
            }

            filteredItems.add(item);
        }

        notifyDataSetChanged();
    }
    public List<AttendanceReportItem> getFilteredList() {
        return new ArrayList<>(filteredItems);
    }

    public int getFilteredCount() {
        return filteredItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceReportItem item = filteredItems.get(position);

        holder.rowContainer.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        holder.tvSN.setText(String.valueOf(position + 1));
        holder.tvAdmId.setText(safe(getFirstFieldValue(item,
                "admissionID", "admissionId", "admId", "admID")));
        holder.tvAdmDate.setText(safe(item.admissionDate));
        holder.tvStudentName.setText(safe(item.studentName));
        holder.tvContact.setText(safe(item.mobile));
        holder.tvCourseName.setText(safe(item.courseName));
        holder.tvBatch.setText(safe(item.batchName));
        holder.tvBatchTiming.setText(safe(item.timingDescription));
        holder.tvPresent.setText(safe(item.attendanceStatus));
        holder.tvClassDate.setText(safe(item.classDate));
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSN, tvAdmId, tvAdmDate, tvStudentName, tvContact;
        TextView tvCourseName, tvBatch, tvBatchTiming, tvPresent, tvClassDate;
        LinearLayout rowContainer;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSN = itemView.findViewById(R.id.tvSN);
            tvAdmId = itemView.findViewById(R.id.tvAdmId);
            tvAdmDate = itemView.findViewById(R.id.tvAdmDate);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvBatch = itemView.findViewById(R.id.tvBatch);
            tvBatchTiming = itemView.findViewById(R.id.tvBatchTiming);
            tvPresent = itemView.findViewById(R.id.tvPresent);
            tvClassDate = itemView.findViewById(R.id.tvClassDate);
            rowContainer = itemView.findViewById(R.id.rowContainer);
        }
    }
    private static boolean containsIgnoreCase(List<String> list, String value) {
        for (String item : list) {
            if (safe(item).equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    private static String safe(Object value) {
        if (value == null) return "-";
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "-" : text;
    }

    private static Object getFirstFieldValue(Object target, String... fieldNames) {
        if (target == null) return null;
        for (String fieldName : fieldNames) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
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
}