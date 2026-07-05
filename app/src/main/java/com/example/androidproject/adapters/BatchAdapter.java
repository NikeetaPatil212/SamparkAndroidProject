package com.example.androidproject.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.course.BatchItem;

import java.util.ArrayList;
import java.util.List;

public class BatchAdapter extends RecyclerView.Adapter<BatchAdapter.ViewHolder> {

    private List<BatchItem> data = new ArrayList<>();
    private int selectedPosition = -1;

    public interface OnBatchClickListener {
        void onBatchClick(BatchItem item);
    }

    private OnBatchClickListener listener;

    public void setOnBatchClickListener(OnBatchClickListener l) {
        this.listener = l;
    }

    public void setData(List<BatchItem> list) {
        this.data = list != null ? list : new ArrayList<>();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_batch_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BatchItem item = data.get(position);

        holder.tvSrNo.setText(String.valueOf(position + 1));
        holder.tvCourseName.setText(safe(item.courseName));
        holder.tvBatchName.setText(safe(item.batchName));
        holder.tvStartDate.setText(safe(item.startDate));
        holder.tvEndDate.setText(safe(item.endDate));
        holder.tvCapacity.setText(item.capacity > 0 ? String.valueOf(item.capacity) : "—");
        holder.tvType.setText(safe(item.type));
        /*holder.tvBatchName.setText(item.batchName != null && !item.batchName.isEmpty()
                ? item.batchName : "—");*/
        // Alternate row colors

        Log.d("kjhgfdsasdfghjk", "onBindViewHolder: " + item.batchName);
        holder.rowLayout.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        // Highlight selected
        if (position == selectedPosition) {
            holder.rowLayout.setBackgroundColor(Color.parseColor("#C8E6C9"));
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onBatchClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "—" : s;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rowLayout;
        TextView tvSrNo, tvCourseName, tvBatchName,
                tvStartDate, tvEndDate, tvCapacity, tvType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowLayout    = itemView.findViewById(R.id.rowLayout);
            tvSrNo       = itemView.findViewById(R.id.tvSrNo);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvBatchName  = itemView.findViewById(R.id.tvBatchName);
            tvStartDate  = itemView.findViewById(R.id.tvStartDate);
            tvEndDate    = itemView.findViewById(R.id.tvEndDate);
            tvCapacity   = itemView.findViewById(R.id.tvCapacity);
            tvType       = itemView.findViewById(R.id.tvType);
        }
    }
}