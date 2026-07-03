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
import java.util.Locale;

public class OutstandingFeesAdapter
        extends RecyclerView.Adapter<OutstandingFeesAdapter.VH> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private List<OutstandingItem> fullList     = new ArrayList<>();
    private List<OutstandingItem> filteredList = new ArrayList<>();
    private OnSelectionChangedListener selectionListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }
    public void removeItems(List<OutstandingItem> itemsToRemove) {
        if (itemsToRemove == null || itemsToRemove.isEmpty()) return;

        fullList.removeAll(itemsToRemove);
        filteredList.removeAll(itemsToRemove);

        for (OutstandingItem item : itemsToRemove) {
            item.setSelected(false);
        }

        notifyDataSetChanged();

        if (selectionListener != null) {
            selectionListener.onSelectionChanged();
        }
    }

    public void setData(List<OutstandingItem> list) {
        fullList.clear();
        if (list != null) fullList.addAll(list);
        filteredList.clear();
        filteredList.addAll(fullList);
        notifyDataSetChanged();
    }

    public void applyFilters(String course, String batch, String time,
                             String name, String contact, String location) {
        filteredList.clear();

        String courseSel   = course   != null ? course   : "All Courses";
        String batchSel    = batch    != null ? batch    : "All Batches";
        String timeSel     = time     != null ? time     : "All Time Slots";
        String nameLower   = name     != null ? name.trim().toLowerCase(Locale.ROOT)     : "";
        String contactTrim = contact  != null ? contact.trim()                            : "";
        String locLower    = location != null ? location.trim().toLowerCase(Locale.ROOT) : "";

        for (OutstandingItem item : fullList) {
            boolean courseOk = courseSel.equals("All Courses")
                    || courseSel.equals(item.courseName);
            boolean batchOk = batchSel.equals("All Batches")
                    || batchSel.equals(item.batchName);
            boolean timeOk = timeSel.equals("All Time Slots")
                    || timeSel.equals(item.timingDescription);
            boolean nameOk = nameLower.isEmpty()
                    || (item.studentName != null
                    && item.studentName.toLowerCase(Locale.ROOT).contains(nameLower));
            boolean contactOk = contactTrim.isEmpty()
                    || (item.mobile != null && item.mobile.contains(contactTrim));
            boolean locOk = locLower.isEmpty()
                    || (item.location != null
                    && item.location.toLowerCase(Locale.ROOT).contains(locLower));

            if (courseOk && batchOk && timeOk && nameOk && contactOk && locOk)
                filteredList.add(item);
        }
        notifyDataSetChanged();
    }


    public List<OutstandingItem> getFilteredList() {
        return filteredList;
    }

    public int getFilteredCount() { return filteredList.size(); }

    public void selectAll(boolean checked) {
        for (OutstandingItem item : filteredList) item.setSelected(checked);
        notifyDataSetChanged();
    }

    public boolean isAllSelected() {
        if (filteredList.isEmpty()) return false;
        for (OutstandingItem item : filteredList)
            if (!item.isSelected()) return false;
        return true;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_outstanding_fees, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OutstandingItem item = filteredList.get(position);

        h.rowContainer.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        h.tvNo.setText(String.valueOf(position + 1));
        h.tvAdmDate.setText(item.admissionDate != null ? item.admissionDate : "-");
        h.tvStudentName.setText(item.studentName != null ? item.studentName : "-");
        h.tvMobile.setText(item.mobile != null ? item.mobile : "-");
        h.tvLocation.setText(item.location != null && !item.location.isEmpty() ? item.location : "-");
        h.tvCourse.setText(item.courseName != null ? item.courseName : "-");
        h.tvBatch.setText(item.batchName != null ? item.batchName : "-");
        h.tvBatchTime.setText(item.timingDescription != null && !item.timingDescription.isEmpty()
                ? item.timingDescription : "-");
        h.tvFees.setText(formatAmount(item.fees));
        h.tvPaid.setText(formatAmount(item.paid));
        h.tvOutstanding.setText(formatAmount(item.outstanding));
        h.tvDueDate.setText(item.reminderDate != null ? item.reminderDate : "-");

        h.cbSelect.setOnCheckedChangeListener(null);
        h.cbSelect.setChecked(item.isSelected());
        h.cbSelect.setOnCheckedChangeListener((btn, checked) -> {
            item.setSelected(checked);
            if (selectionListener != null) selectionListener.onSelectionChanged();
        });

        h.rowContainer.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            h.cbSelect.setChecked(item.isSelected());
        });
    }

    @Override
    public int getItemCount() { return filteredList.size(); }

    private String formatAmount(double amount) {
        return "₹" + (amount == (long) amount ? String.valueOf((long) amount) : String.valueOf(amount));
    }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        TextView tvNo, tvAdmDate, tvStudentName, tvMobile, tvLocation,
                tvCourse, tvBatch, tvBatchTime, tvFees, tvPaid, tvOutstanding, tvDueDate;
        CheckBox cbSelect;

        VH(View v) {
            super(v);
            rowContainer  = v.findViewById(R.id.rowContainer);
            tvNo          = v.findViewById(R.id.tvNo);
            tvAdmDate     = v.findViewById(R.id.tvAdmDate);
            tvStudentName = v.findViewById(R.id.tvStudentName);
            tvMobile      = v.findViewById(R.id.tvMobile);
            tvLocation    = v.findViewById(R.id.tvLocation);
            tvCourse      = v.findViewById(R.id.tvCourse);
            tvBatch       = v.findViewById(R.id.tvBatch);
            tvBatchTime   = v.findViewById(R.id.tvBatchTime);
            tvFees        = v.findViewById(R.id.tvFees);
            tvPaid        = v.findViewById(R.id.tvPaid);
            tvOutstanding = v.findViewById(R.id.tvOutstanding);
            tvDueDate     = v.findViewById(R.id.tvDueDate);
            cbSelect      = v.findViewById(R.id.cbSelect);
        }
    }
    public List<OutstandingItem> getCheckedItems() {
        List<OutstandingItem> checked = new ArrayList<>();
        for (OutstandingItem item : filteredList)
            if (item.isSelected()) checked.add(item);
        return checked;
    }
}