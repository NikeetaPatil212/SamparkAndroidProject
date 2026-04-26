package com.example.androidproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.StudentBasicResponse;

import java.util.ArrayList;
import java.util.List;


public class AllotBatchAdapter extends RecyclerView.Adapter<AllotBatchAdapter.StudentViewHolder> {

    private List<StudentBasicResponse.StudentItem> studentList = new ArrayList<>();

    public void setData(List<StudentBasicResponse.StudentItem> list) {
        this.studentList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_allot_batch_row, parent, false);

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {

        StudentBasicResponse.StudentItem item = studentList.get(position);

        holder.tvStudentName.setText(item.getStudentName());
        holder.tvAdmissionId.setText("Admission ID: " + item.getAdmissionID());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView tvStudentName, tvAdmissionId;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStudentName = itemView.findViewById(R.id.tvStudentName);
           // tvAdmissionId = itemView.findViewById(R.id.tvAdmissionId);
        }
    }
}