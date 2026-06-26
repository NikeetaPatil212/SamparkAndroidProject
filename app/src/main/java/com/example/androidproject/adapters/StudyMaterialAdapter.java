package com.example.androidproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.profile.StudyMaterialDistributionResponse;

import java.util.ArrayList;
import java.util.List;

public class StudyMaterialAdapter
        extends RecyclerView.Adapter<StudyMaterialAdapter.VH> {

    private List<StudyMaterialDistributionResponse.StudentItem> list = new ArrayList<>();

    public void setData(List<StudyMaterialDistributionResponse.StudentItem> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    /** Returns full student items that are checked */
    public List<StudyMaterialDistributionResponse.StudentItem> getCheckedStudents() {
        List<StudyMaterialDistributionResponse.StudentItem> checked = new ArrayList<>();
        for (StudyMaterialDistributionResponse.StudentItem s : list) {
            if (s.isChecked()) checked.add(s);
        }
        return checked;
    }

    /** Select / deselect all */
    public void setAllChecked(boolean checked) {
        for (StudyMaterialDistributionResponse.StudentItem s : list) {
            s.setChecked(checked);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_distribution_student, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        StudyMaterialDistributionResponse.StudentItem item = list.get(position);

        holder.tvAdmId.setText(String.valueOf(item.getAdmissionID()));
        holder.tvName.setText(item.getStudentName());

        // Grey out already-distributed students
        if (item.isDistributed()) {
            holder.itemView.setAlpha(0.5f);
            holder.cbStudent.setEnabled(false);
        } else {
            holder.itemView.setAlpha(1f);
            holder.cbStudent.setEnabled(true);
        }

        holder.cbStudent.setOnCheckedChangeListener(null);
        holder.cbStudent.setChecked(item.isChecked());
        holder.cbStudent.setOnCheckedChangeListener((btn, isChecked) ->
                item.setChecked(isChecked));

        holder.itemView.setOnClickListener(v -> {
            if (!item.isDistributed()) {
                item.setChecked(!item.isChecked());
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbStudent;
        TextView tvAdmId, tvName;

        VH(View v) {
            super(v);
            cbStudent = v.findViewById(R.id.cbStudent);
            tvAdmId   = v.findViewById(R.id.tvAdmId);
            tvName    = v.findViewById(R.id.tvStudentName);
        }
    }
}