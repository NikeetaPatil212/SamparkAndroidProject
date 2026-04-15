package com.example.androidproject.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.AdmissionDetails;

import java.util.ArrayList;
import java.util.List;

public class AdmissionAdapter extends RecyclerView.Adapter<AdmissionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AdmissionDetails item);
    }

    private List<AdmissionDetails> list;
    private final OnItemClickListener listener;

    public AdmissionAdapter(List<AdmissionDetails> list, OnItemClickListener listener) {
        this.list     = new ArrayList<>(list);
        this.listener = listener;
    }

    public void updateList(List<AdmissionDetails> newList) {
        this.list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_get_admissionlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AdmissionDetails item = list.get(position);

        // Alternating row tint
        h.itemView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : 0xFFF5FBF5);

        // ID
        h.tvAdmId.setText(String.valueOf(item.getAdm_id()));

        // Date  2026-03-18 → 18-03-26
        String date = item.getAdmDate();
        if (date != null && date.length() >= 10) {
            String[] p = date.substring(0, 10).split("-");
            if (p.length == 3) date = p[2] + "-" + p[1] + "-" + p[0].substring(2);
        }
        h.tvDate.setText(date != null ? date : "-");

        // Name
        String name = item.getStudent_Name();
        h.tvName.setText(name != null && !name.isEmpty() ? name : "N/A");

        // Course
        String course = item.getCourseName();
        h.tvCourse.setText(course != null && !course.isEmpty() ? course : "-");

        // Scheme
        String scheme = item.getScheme();
        h.tvScheme.setText(scheme != null && !scheme.isEmpty() ? scheme : "-");

        // Status pill — set color programmatically on the rounded drawable
        String status = item.getStatus();
        if (status == null || status.isEmpty()) status = "-";
        h.tvStatus.setText(status);

        int color;
        switch (status.toLowerCase()) {
            case "active":    color = 0xFF1565C0; break;  // blue
            case "paid":      color = 0xFF2E7D32; break;  // green
            case "pending":   color = 0xFFB71C1C; break;  // red
            case "cancelled":
            case "inactive":  color = 0xFF2E2334; break;  // green
            default:          color = 0xFF757575; break;  // grey
        }

        // Apply color to the pill shape while keeping the rounded corners
        GradientDrawable pill = new GradientDrawable();
        pill.setShape(GradientDrawable.RECTANGLE);
        pill.setCornerRadius(28f);   // fully pill-shaped
        pill.setColor(color);
        h.tvStatus.setBackground(pill);

        h.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdmId, tvDate, tvName, tvCourse, tvStatus, tvScheme;

        ViewHolder(@NonNull View v) {
            super(v);
            tvAdmId  = v.findViewById(R.id.tvAdmId);
            tvDate   = v.findViewById(R.id.tvDate);
            tvName   = v.findViewById(R.id.tvName);
            tvCourse = v.findViewById(R.id.tvCourse);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvScheme = v.findViewById(R.id.tvScheme);
        }
    }
}