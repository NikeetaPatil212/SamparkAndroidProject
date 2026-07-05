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
import com.example.androidproject.model.course.CourseItem;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.VH> {

    public interface OnCourseClickListener {
        void onCourseClick(CourseItem item);
    }

    private List<CourseItem>      list             = new ArrayList<>();
    private OnCourseClickListener listener;
    private int                   selectedPosition = -1;

    public void setOnCourseClickListener(OnCourseClickListener l) { this.listener = l; }

    public void setData(List<CourseItem> data) {
        list.clear();
        if (data != null) list.addAll(data);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void updateItem(CourseItem updated) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).courseID == updated.courseID) {
                list.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void addItem(CourseItem item) {
        list.add(0, item);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CourseItem item = list.get(position);

        // ── Sr No ─────────────────────────────────────────────────
        h.tvSn.setText(String.valueOf(position + 1));  // ← THIS was missing

        // ── Row background ────────────────────────────────────────
        if (position == selectedPosition) {
            h.rowContainer.setBackgroundColor(Color.parseColor("#C8E6C9"));
        } else {
            h.rowContainer.setBackgroundColor(
                    position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));
        }

        // ── Fields ────────────────────────────────────────────────
        h.tvCourseName.setText(item.courseName != null ? item.courseName : "-");
        h.tvFees.setText("₹" + (item.fees == (long) item.fees
                ? String.valueOf((long) item.fees) : String.valueOf(item.fees)));
        h.tvScheme.setText(item.scheme != null ? item.scheme : "-");
        h.tvCertificate.setText(item.certificate == 1 ? "Yes" : "No");
        h.tvCertificate.setTextColor(item.certificate == 1
                ? Color.parseColor("#2E7D32")   // green
                : Color.parseColor("#D32F2F"));  // red
        h.tvDuration.setText(item.duration + " Mo");

        // ── Click ─────────────────────────────────────────────────
        h.rowContainer.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = h.getAdapterPosition();
            if (prev != -1) notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onCourseClick(item);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    // ── ViewHolder ─────────────────────────────────────────────────
    static class VH extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        TextView tvSn, tvCourseName, tvFees, tvScheme, tvCertificate, tvDuration;

        VH(View v) {
            super(v);
            rowContainer  = v.findViewById(R.id.rowContainer);
            tvSn          = v.findViewById(R.id.tvSn);           // ← tvSn added
            tvCourseName  = v.findViewById(R.id.tvCourseName);
            tvFees        = v.findViewById(R.id.tvFees);
            tvScheme      = v.findViewById(R.id.tvScheme);
            tvCertificate = v.findViewById(R.id.tvCertificate);
            tvDuration    = v.findViewById(R.id.tvDuration);
        }
    }
}