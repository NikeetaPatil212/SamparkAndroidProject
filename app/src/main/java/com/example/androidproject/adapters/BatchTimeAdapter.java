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
import com.example.androidproject.model.course.BatchTimeItem;

import java.util.ArrayList;
import java.util.List;

public class BatchTimeAdapter extends RecyclerView.Adapter<BatchTimeAdapter.ViewHolder> {

    private List<BatchTimeItem> data = new ArrayList<>();
    private int selectedPosition = -1;
    private OnBatchTimeClickListener listener;

    public interface OnBatchTimeClickListener {
        void onBatchTimeClick(BatchTimeItem item);
    }

    public void setOnBatchTimeClickListener(OnBatchTimeClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<BatchTimeItem> list) {
        data = list != null ? list : new ArrayList<>();
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
                .inflate(R.layout.item_batch_time_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BatchTimeItem item = data.get(position);

        holder.tvSrNo.setText(String.valueOf(position + 1));
        holder.tvDescription.setText(safe(item.timingDescription));
        holder.tvStartTime.setText(safe(item.startTime));
        holder.tvEndTime.setText(safe(item.endTime));
        holder.tvCapacity.setText(String.valueOf(item.capacity));
        holder.tvFilled.setText(String.valueOf(item.filled));
        holder.tvAvailable.setText(String.valueOf(item.availableSeats));

        holder.rowLayout.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        if (position == selectedPosition) {
            holder.rowLayout.setBackgroundColor(Color.parseColor("#C8E6C9"));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onBatchTimeClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rowLayout;
        TextView tvSrNo, tvDescription, tvStartTime, tvEndTime,
                tvCapacity, tvFilled, tvAvailable;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowLayout = itemView.findViewById(R.id.rowLayout);
            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvFilled = itemView.findViewById(R.id.tvFilled);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
        }
    }
}
