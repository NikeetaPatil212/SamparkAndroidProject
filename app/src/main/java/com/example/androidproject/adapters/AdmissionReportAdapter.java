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
import com.example.androidproject.model.summary.AdmissionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdmissionReportAdapter
        extends RecyclerView.Adapter<AdmissionReportAdapter.VH> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private List<AdmissionItem> fullList     = new ArrayList<>();
    private List<AdmissionItem> filteredList = new ArrayList<>();
    private OnSelectionChangedListener selectionListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener l) {
        this.selectionListener = l;
    }

    // ── Data ──────────────────────────────────────────────────────
    public void setData(List<AdmissionItem> list) {
        fullList.clear();
        if (list != null) fullList.addAll(list);
        filteredList.clear();
        filteredList.addAll(fullList);
        notifyDataSetChanged();
    }

    // ── Filter ────────────────────────────────────────────────────
    public void applyFilters(String course, String batch, String time,
                             String name,   String contact, String location) {
        filteredList.clear();

        String courseSel   = course   != null ? course   : "All Courses";
        String batchSel    = batch    != null ? batch    : "All Batches";
        String timeSel     = time     != null ? time     : "All Time Slots";
        String nameLower   = name     != null ? name.trim().toLowerCase(Locale.ROOT)     : "";
        String contactTrim = contact  != null ? contact.trim()                            : "";
        String locLower    = location != null ? location.trim().toLowerCase(Locale.ROOT) : "";

        for (AdmissionItem item : fullList) {

            boolean courseOk  = courseSel.equals("All Courses")
                    || courseSel.equals(item.courseName);

            boolean batchOk   = batchSel.equals("All Batches")
                    || batchSel.equals(item.batchName);

            boolean timeOk    = timeSel.equals("All Time Slots")
                    || timeSel.equals(item.timingDescription);

            boolean nameOk    = nameLower.isEmpty()
                    || (item.studentName != null
                    && item.studentName.toLowerCase(Locale.ROOT).contains(nameLower));

            boolean contactOk = contactTrim.isEmpty()
                    || (item.mobile != null && item.mobile.contains(contactTrim));

            boolean locOk     = locLower.isEmpty()
                    || (item.location != null
                    && item.location.toLowerCase(Locale.ROOT).contains(locLower));

            if (courseOk && batchOk && timeOk && nameOk && contactOk && locOk)
                filteredList.add(item);
        }
        notifyDataSetChanged();
    }

    // ── Selection helpers ─────────────────────────────────────────
    public List<AdmissionItem> getCheckedItems() {
        List<AdmissionItem> checked = new ArrayList<>();
        for (AdmissionItem item : filteredList)
            if (item.isSelected()) checked.add(item);
        return checked;
    }

    public List<AdmissionItem> getFilteredList() { return filteredList; }
    public int                 getFilteredCount() { return filteredList.size(); }

    public void selectAll(boolean checked) {
        for (AdmissionItem item : filteredList) item.setSelected(checked);
        notifyDataSetChanged();
    }

    public boolean isAllSelected() {
        if (filteredList.isEmpty()) return false;
        for (AdmissionItem item : filteredList)
            if (!item.isSelected()) return false;
        return true;
    }

    // ── RecyclerView ──────────────────────────────────────────────
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_admission_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdmissionItem item = filteredList.get(position);

        // Alternating row colour — same as InquiryReportAdapter
        h.rowContainer.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        h.tvNo.setText(String.valueOf(position + 1));
        h.tvAdmDate.setText(item.admissionDate != null ? item.admissionDate : "-");
        h.tvAdmId.setText(String.valueOf(item.admissionID));
        h.tvStudentName.setText(item.studentName != null ? item.studentName : "-");
        h.tvMobile.setText(item.mobile != null ? item.mobile : "-");
        h.tvLocation.setText(!isEmpty(item.location) ? item.location : "-");
        h.tvCourse.setText(item.courseName != null ? item.courseName : "-");
        h.tvBatch.setText(item.batchName != null ? item.batchName : "-");
        h.tvBatchTime.setText(!isEmpty(item.timingDescription) ? item.timingDescription : "-");
        h.tvFees.setText(fmt(item.fees));
        h.tvPaid.setText(fmt(item.paid));
        h.tvOutstanding.setText(fmt(item.outstanding));

        // Checkbox — same guard pattern as InquiryReportAdapter
        h.cbSelect.setOnCheckedChangeListener(null);
        h.cbSelect.setChecked(item.isSelected());
        h.cbSelect.setOnCheckedChangeListener((btn, checked) -> {
            item.setSelected(checked);
            if (selectionListener != null) selectionListener.onSelectionChanged();
        });

        // Tap the whole row to toggle
        h.rowContainer.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            h.cbSelect.setChecked(item.isSelected());
            if (selectionListener != null) selectionListener.onSelectionChanged();
        });
    }

    @Override
    public int getItemCount() { return filteredList.size(); }

    // ── Helpers ───────────────────────────────────────────────────
    private String fmt(double v) {
        return "₹" + (v == (long) v ? String.valueOf((long) v) : String.valueOf(v));
    }

    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }

    // ── ViewHolder ────────────────────────────────────────────────
    static class VH extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        TextView tvNo, tvAdmDate, tvAdmId, tvStudentName, tvMobile,
                tvLocation, tvCourse, tvBatch, tvBatchTime,
                tvFees, tvPaid, tvOutstanding;
        CheckBox cbSelect;

        VH(View v) {
            super(v);
            rowContainer  = v.findViewById(R.id.rowContainer);
            tvNo          = v.findViewById(R.id.tvNo);
            tvAdmDate     = v.findViewById(R.id.tvAdmDate);
            tvAdmId       = v.findViewById(R.id.tvAdmId);
            tvStudentName = v.findViewById(R.id.tvStudentName);
            tvMobile      = v.findViewById(R.id.tvMobile);
            tvLocation    = v.findViewById(R.id.tvLocation);
            tvCourse      = v.findViewById(R.id.tvCourse);
            tvBatch       = v.findViewById(R.id.tvBatch);
            tvBatchTime   = v.findViewById(R.id.tvBatchTime);
            tvFees        = v.findViewById(R.id.tvFees);
            tvPaid        = v.findViewById(R.id.tvPaid);
            tvOutstanding = v.findViewById(R.id.tvOutstanding);
            cbSelect      = v.findViewById(R.id.cbSelect);
        }
    }
}