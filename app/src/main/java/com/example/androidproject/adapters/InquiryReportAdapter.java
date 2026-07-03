package com.example.androidproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.outstanding.OutstandingItem;
import com.example.androidproject.model.summary.InquiryReportItem;

import java.util.ArrayList;
import java.util.List;

public class InquiryReportAdapter
        extends RecyclerView.Adapter<InquiryReportAdapter.VH> {

    private List<InquiryReportItem> fullList     = new ArrayList<>();
    private List<InquiryReportItem> filteredList = new ArrayList<>();

    public void setData(List<InquiryReportItem> list) {
        fullList.clear();
        if (list != null) fullList.addAll(list);
        filteredList.clear();
        filteredList.addAll(fullList);
        notifyDataSetChanged();
    }

    public List<InquiryReportItem> getFilteredList() {
        return filteredList;
    }

    public void applyFilters(String name, String contact,
                             String location, String course,
                             List<String> statuses) {
        filteredList.clear();
        String nameLower     = name     != null ? name.trim().toLowerCase()     : "";
        String contactLower  = contact  != null ? contact.trim().toLowerCase()  : "";
        String locationLower = location != null ? location.trim().toLowerCase() : "";
        String courseLower   = course   != null ? course.trim().toLowerCase()   : "";
        boolean allStatus    = statuses == null || statuses.isEmpty()
                || statuses.contains("All");

        for (InquiryReportItem item : fullList) {
            boolean nameOk = nameLower.isEmpty()
                    || (item.fullName != null
                    && item.fullName.toLowerCase().contains(nameLower));

            boolean contactOk = contactLower.isEmpty()
                    || (item.mobile != null
                    && item.mobile.toLowerCase().contains(contactLower))
                    || (item.alternateNo != null
                    && item.alternateNo.toLowerCase().contains(contactLower));

            boolean locationOk = locationLower.isEmpty()
                    || (item.location != null
                    && item.location.toLowerCase().contains(locationLower));

            boolean courseOk = courseLower.isEmpty()
                    || (item.about != null
                    && item.about.toLowerCase().contains(courseLower));

            boolean statusOk = allStatus
                    || (item.reminderStatus != null
                    && statuses.contains(item.reminderStatus));

            if (nameOk && contactOk && locationOk && courseOk && statusOk)
                filteredList.add(item);
        }
        notifyDataSetChanged();
    }

    public List<InquiryReportItem> getCheckedItems() {
        List<InquiryReportItem> checked = new ArrayList<>();
        for (InquiryReportItem item : filteredList)
            if (item.isSelected()) checked.add(item);
        return checked;
    }

    public int getFilteredCount() { return filteredList.size(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_inquiry_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        InquiryReportItem item = filteredList.get(position);

        h.rowContainer.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        h.tvNo.setText(String.valueOf(position + 1));
        h.tvInquiryDate.setText(item.inquiryDate   != null ? item.inquiryDate   : "-");
        h.tvStudentName.setText(item.fullName      != null ? item.fullName      : "-");
        h.tvMobile.setText(item.mobile             != null ? item.mobile        : "-");
        h.tvAltMobile.setText(item.alternateNo     != null
                && !item.alternateNo.isEmpty()     ? item.alternateNo           : "-");
        h.tvLocation.setText(item.location         != null
                && !item.location.isEmpty()        ? item.location              : "-");
        h.tvCourses.setText(item.about             != null ? item.about         : "-");
        h.tvReminderDate.setText(item.reminderDate != null ? item.reminderDate  : "-");
        h.tvFeedback.setText(item.feedback         != null
                && !item.feedback.isEmpty()        ? item.feedback              : "-");
        h.tvSchool.setText(item.schoolName         != null
                && !item.schoolName.isEmpty()      ? item.schoolName            : "-");
        h.tvReference.setText(item.reference       != null
                && !item.reference.isEmpty()       ? item.reference             : "-");

        // Status color
        String status = item.reminderStatus;
        h.tvStatus.setText(status != null ? status : "-");
        if ("Converted".equalsIgnoreCase(status)) {
            h.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("Active".equalsIgnoreCase(status)) {
            h.tvStatus.setTextColor(Color.parseColor("#1565C0"));
        } else if ("Cancelled".equalsIgnoreCase(status)
                || "Aborted".equalsIgnoreCase(status)) {
            h.tvStatus.setTextColor(Color.parseColor("#E53935"));
        } else {
            h.tvStatus.setTextColor(Color.parseColor("#555555"));
        }

        // Checkbox
        h.cbSelect.setOnCheckedChangeListener(null);
        h.cbSelect.setChecked(item.isSelected());
        h.cbSelect.setOnCheckedChangeListener((btn, checked) ->
                item.setSelected(checked));

        h.rowContainer.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            h.cbSelect.setChecked(item.isSelected());
        });
    }

    @Override
    public int getItemCount() { return filteredList.size(); }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        TextView tvNo, tvInquiryDate, tvStudentName, tvMobile, tvAltMobile,
                tvLocation, tvCourses, tvReminderDate, tvStatus,
                tvFeedback, tvSchool, tvReference;
        CheckBox cbSelect;

        VH(View v) {
            super(v);
            rowContainer   = v.findViewById(R.id.rowContainer);
            tvNo           = v.findViewById(R.id.tvNo);
            tvInquiryDate  = v.findViewById(R.id.tvInquiryDate);
            tvStudentName  = v.findViewById(R.id.tvStudentName);
            tvMobile       = v.findViewById(R.id.tvMobile);
            tvAltMobile    = v.findViewById(R.id.tvAltMobile);
            tvLocation     = v.findViewById(R.id.tvLocation);
            tvCourses      = v.findViewById(R.id.tvCourses);
            tvReminderDate = v.findViewById(R.id.tvReminderDate);
            tvStatus       = v.findViewById(R.id.tvStatus);
            tvFeedback     = v.findViewById(R.id.tvFeedback);
            tvSchool       = v.findViewById(R.id.tvSchool);
            tvReference    = v.findViewById(R.id.tvReference);
            cbSelect       = v.findViewById(R.id.cbSelect);
        }
    }
}