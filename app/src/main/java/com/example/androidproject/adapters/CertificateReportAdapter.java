package com.example.androidproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.summary.CertificateReportItem;

import java.util.ArrayList;
import java.util.List;

public class CertificateReportAdapter extends RecyclerView.Adapter<CertificateReportAdapter.ViewHolder> {

    private List<CertificateReportItem> allItems = new ArrayList<>();
    private List<CertificateReportItem> filteredItems = new ArrayList<>();

    public void setData(List<CertificateReportItem> items) {
        this.allItems = items != null ? items : new ArrayList<>();
        this.filteredItems = new ArrayList<>(this.allItems);
        notifyDataSetChanged();
    }

    /**
     * @param name          partial match on studentName
     * @param mobile        partial match on mobile
     * @param course        exact match on courseName, "" = any course
     * @param batch         exact match on batchName, "" = any batch
     * @param timing        exact match on timingDescription, "" = any timing
     * @param showDistributed true = show isDistributed==true rows (Distributed Certificates view),
     *                         false = show isDistributed==false rows (Pending Certificates view)
     */
    public void applyFilters(String name, String mobile, String course, String batch,
                             String timing, boolean showDistributed) {
        String n = name == null ? "" : name.trim().toLowerCase();
        String m = mobile == null ? "" : mobile.trim().toLowerCase();

        filteredItems.clear();
        for (CertificateReportItem item : allItems) {
            if (item.isDistributed != showDistributed) continue;

            if (!n.isEmpty()) {
                String sn = item.studentName == null ? "" : item.studentName.toLowerCase();
                if (!sn.contains(n)) continue;
            }
            if (!m.isEmpty()) {
                String mo = item.mobile == null ? "" : item.mobile.toLowerCase();
                if (!mo.contains(m)) continue;
            }
            if (course != null && !course.isEmpty()) {
                if (item.courseName == null || !item.courseName.equalsIgnoreCase(course)) continue;
            }
            if (batch != null && !batch.isEmpty()) {
                if (item.batchName == null || !item.batchName.equalsIgnoreCase(batch)) continue;
            }
            if (timing != null && !timing.isEmpty()) {
                if (item.timingDescription == null || !item.timingDescription.equalsIgnoreCase(timing)) continue;
            }
            filteredItems.add(item);
        }
        notifyDataSetChanged();
    }

    public List<CertificateReportItem> getFilteredList() {
        return filteredItems;
    }

    public int getFilteredCount() {
        return filteredItems.size();
    }

    public List<CertificateReportItem> getCheckedItems() {
        List<CertificateReportItem> checked = new ArrayList<>();
        for (CertificateReportItem item : filteredItems) {
            if (item.isChecked) checked.add(item);
        }
        return checked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_certificate_report, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CertificateReportItem item = filteredItems.get(position);

        holder.tvNo.setText(String.valueOf(position + 1));
        holder.tvAdmId.setText(String.valueOf(item.admissionID));
        holder.tvAdmDate.setText(safe(item.admissionDate));
        holder.tvStudentName.setText(safe(item.studentName));
        holder.tvMobile.setText(safe(item.mobile));
        holder.tvCourse.setText(safe(item.courseName));
        holder.tvBatch.setText(safe(item.batchName));
        holder.tvCertNo.setText(safe(item.certificateNumber));
        holder.tvResult.setText(safe(item.result));
        holder.tvPercentage.setText(safe(item.percentage));
        holder.tvHandedTo.setText(safe(item.issuerName));
        holder.tvContactInfo.setText(safe(item.issuerContact));
        holder.tvHandoverDate.setText(safe(item.handoverDate));

        if (item.isDistributed) {
            holder.tvStatus.setText("Distributed");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(Color.parseColor("#EF6C00"));
        }

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(item.isChecked);
        holder.cbSelect.setOnCheckedChangeListener((btn, checked) -> item.isChecked = checked);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvNo, tvAdmId, tvAdmDate, tvStudentName, tvMobile, tvCourse, tvBatch,
                tvStatus, tvCertNo, tvResult, tvPercentage, tvHandedTo, tvContactInfo, tvHandoverDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect       = itemView.findViewById(R.id.cbSelect);
            tvNo           = itemView.findViewById(R.id.tvNo);
            tvAdmId        = itemView.findViewById(R.id.tvAdmId);
            tvAdmDate      = itemView.findViewById(R.id.tvAdmDate);
            tvStudentName  = itemView.findViewById(R.id.tvStudentName);
            tvMobile       = itemView.findViewById(R.id.tvMobile);
            tvCourse       = itemView.findViewById(R.id.tvCourse);
            tvBatch        = itemView.findViewById(R.id.tvBatch);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
            tvCertNo       = itemView.findViewById(R.id.tvCertNo);
            tvResult       = itemView.findViewById(R.id.tvResult);
            tvPercentage   = itemView.findViewById(R.id.tvPercentage);
            tvHandedTo     = itemView.findViewById(R.id.tvHandedTo);
            tvContactInfo  = itemView.findViewById(R.id.tvContactInfo);
            tvHandoverDate = itemView.findViewById(R.id.tvHandoverDate);
        }
    }
}