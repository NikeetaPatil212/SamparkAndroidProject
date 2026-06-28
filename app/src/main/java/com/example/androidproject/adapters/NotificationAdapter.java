package com.example.androidproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.notification.NotificationStudent;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationStudent> displayList = new ArrayList<>();

    public void setData(List<NotificationStudent> list) {
        this.displayList = list;
        notifyDataSetChanged();
    }

    public List<NotificationStudent> getSelectedStudents() {
        List<NotificationStudent> selected = new ArrayList<>();
        for (NotificationStudent s : displayList)
            if (s.isSelected()) selected.add(s);
        return selected;
    }

    public void selectAll(boolean select) {
        for (NotificationStudent s : displayList) s.setSelected(select);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        NotificationStudent s = displayList.get(position);

        h.tvSrNo.setText(String.valueOf(position + 1));
        h.tvFullName.setText(s.getFullName());
        h.tvMobile.setText("📞 " + s.getMobile());
        h.tvCourse.setText(s.getCourseName());
        h.tvBatch.setText(s.getBatchName());
        h.tvTiming.setText(s.getTimingDescription());
        h.cbSelect.setChecked(s.isSelected());

        // Avatar initial
        if (s.getFullName() != null && !s.getFullName().isEmpty()) {
            h.tvAvatar.setText(
                    String.valueOf(s.getFullName().charAt(0)).toUpperCase(Locale.getDefault()));
        }

        // Alternate row background
        h.itemView.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        h.cbSelect.setOnCheckedChangeListener(null);
        h.cbSelect.setChecked(s.isSelected());
        h.cbSelect.setOnCheckedChangeListener((btn, checked) -> s.setSelected(checked));

        // Tap row = toggle checkbox
        h.itemView.setOnClickListener(v -> {
            s.setSelected(!s.isSelected());
            h.cbSelect.setChecked(s.isSelected());
        });
    }

    @Override public int getItemCount() { return displayList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrNo, tvAvatar, tvFullName, tvMobile, tvCourse, tvBatch, tvTiming;
        CheckBox cbSelect;

        ViewHolder(@NonNull View v) {
            super(v);
            tvSrNo    = v.findViewById(R.id.tvSrNo);
            tvAvatar  = v.findViewById(R.id.tvAvatar);
            tvFullName= v.findViewById(R.id.tvFullName);
            tvMobile  = v.findViewById(R.id.tvMobile);
            tvCourse  = v.findViewById(R.id.tvCourse);
            tvBatch   = v.findViewById(R.id.tvBatch);
            tvTiming  = v.findViewById(R.id.tvTiming);
            cbSelect  = v.findViewById(R.id.cbSelect);
        }
    }

    public void clearSelections() {
        for (NotificationStudent s : displayList) {
            s.setSelected(false);
        }
        notifyDataSetChanged(); // ← this is what was missing before
    }
}
