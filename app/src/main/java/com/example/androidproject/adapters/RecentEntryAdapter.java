package com.example.androidproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidproject.R;
import com.example.androidproject.model.dashboard.RecentEntry;

import java.util.List;

public class RecentEntryAdapter extends RecyclerView.Adapter<RecentEntryAdapter.RowVH> {

    private final List<RecentEntry> items;
    private final boolean showAmount;

    public RecentEntryAdapter(List<RecentEntry> items, boolean showAmount) {
        this.items = items;
        this.showAmount = showAmount;
    }

    @NonNull
    @Override
    public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_row, parent, false);
        return new RowVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowVH holder, int position) {
        RecentEntry item = items.get(position);
        holder.tvName.setText(item.getStudentName());
        holder.tvCourse.setText(item.getCourseName());

        if (showAmount && item.getAmount() != null) {
            holder.tvAmount.setVisibility(View.VISIBLE);
            holder.tvAmount.setText("₹" + item.getAmount());
        } else {
            holder.tvAmount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    static class RowVH extends RecyclerView.ViewHolder {
        TextView tvName, tvCourse, tvAmount;
        RowVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}