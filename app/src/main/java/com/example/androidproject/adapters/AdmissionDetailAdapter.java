package com.example.androidproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.room.AdmissionDetail;

import java.util.ArrayList;
import java.util.List;

public class AdmissionDetailAdapter extends RecyclerView.Adapter<AdmissionDetailAdapter.ViewHolder> {
    private List<AdmissionDetail> list = new ArrayList<>();
    public void setItems(List<AdmissionDetail> newItems) {
        list.clear();
        list.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(AdmissionDetail detail) {
        list.add(detail);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_admission_detail, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        AdmissionDetail d = list.get(pos);
        h.tvCourseName.setText(d.getCourseName());
        h.tvCourseId.setText(d.getCourseId());
        h.tvBatchName.setText(d.getBatchName());
        h.tvTotalFee.setText(String.valueOf(d.getTotalFee()));
        h.tvPaidFee.setText(String.valueOf(d.getPaidFee()));
        h.tvRemainingFee.setText(String.valueOf(d.getRemainingFee()));
        h.tvBatchId.setText(d.getBatchId());
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvCourseId, tvBatchName, tvTotalFee, tvPaidFee, tvRemainingFee, tvBatchId;
        ViewHolder(View v) {
            super(v);
            tvCourseName = v.findViewById(R.id.tvCourseName);
            tvCourseId = v.findViewById(R.id.tvCourseId);
            tvBatchName = v.findViewById(R.id.tvBatchName);
            tvTotalFee = v.findViewById(R.id.tvTotalFee);
            tvPaidFee = v.findViewById(R.id.tvPaidFee);
            tvRemainingFee = v.findViewById(R.id.tvRemainingFee);
            tvBatchId = v.findViewById(R.id.tvBatchId);
        }
    }
}
