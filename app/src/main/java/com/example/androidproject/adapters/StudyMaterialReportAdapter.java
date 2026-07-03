package com.example.androidproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.summary.StudyMaterialReportItem;

import java.util.ArrayList;
import java.util.List;

public class StudyMaterialReportAdapter extends RecyclerView.Adapter<StudyMaterialReportAdapter.ViewHolder> {

    private List<StudyMaterialReportItem> allItems = new ArrayList<>();
    private List<StudyMaterialReportItem> filteredItems = new ArrayList<>();

    public void setData(List<StudyMaterialReportItem> items) {
        this.allItems = items != null ? items : new ArrayList<>();
        this.filteredItems = new ArrayList<>(this.allItems);
        notifyDataSetChanged();
    }

    /**
     * @param course        exact match on courseName, "" = any course
     * @param batch         exact match on batchName, "" = any batch
     * @param timing        exact match on timingDescription, "" = any timing
     * @param showDistributed true = Distributed view, false = Pending view
     */
    public void applyFilters(String course, String batch, String timing, boolean showDistributed) {
        filteredItems.clear();
        for (StudyMaterialReportItem item : allItems) {
            if (item.isDistributed != showDistributed) continue;

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

    public List<StudyMaterialReportItem> getFilteredList() {
        return filteredItems;
    }

    public int getFilteredCount() {
        return filteredItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_material_report, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyMaterialReportItem item = filteredItems.get(position);

        holder.tvAdmId.setText(String.valueOf(item.admissionID));
        holder.tvAdmDate.setText(safe(item.admissionDate));
        holder.tvStudentName.setText(safe(item.studentName));
        holder.tvMobile.setText(safe(item.mobile));
        holder.tvLocation.setText(safe(item.location));
        holder.tvCourse.setText(safe(item.courseName));
        holder.tvBatch.setText(safe(item.batchName));
        holder.tvStudyMaterial.setText(safe(item.studyMaterial));
        holder.tvDistributionDate.setText(safe(item.distributionDate));
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdmId, tvAdmDate, tvStudentName, tvMobile, tvLocation,
                tvCourse, tvBatch, tvStudyMaterial, tvDistributionDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdmId            = itemView.findViewById(R.id.tvAdmId);
            tvAdmDate          = itemView.findViewById(R.id.tvAdmDate);
            tvStudentName      = itemView.findViewById(R.id.tvStudentName);
            tvMobile           = itemView.findViewById(R.id.tvMobile);
            tvLocation         = itemView.findViewById(R.id.tvLocation);
            tvCourse           = itemView.findViewById(R.id.tvCourse);
            tvBatch            = itemView.findViewById(R.id.tvBatch);
            tvStudyMaterial    = itemView.findViewById(R.id.tvStudyMaterial);
            tvDistributionDate = itemView.findViewById(R.id.tvDistributionDate);
        }
    }
}