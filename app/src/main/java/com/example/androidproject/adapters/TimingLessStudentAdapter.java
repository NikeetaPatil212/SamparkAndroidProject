package com.example.androidproject.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.profile.TimingLessStudentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class TimingLessStudentAdapter extends RecyclerView.Adapter<TimingLessStudentAdapter.ViewHolder> {

    private List<TimingLessStudentResponse.StudentItem> studentList = new ArrayList<>();

    public void setData(List<TimingLessStudentResponse.StudentItem> list) {
        this.studentList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_allot_batch_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimingLessStudentResponse.StudentItem item = studentList.get(position);

        holder.tvAdmId.setText(String.valueOf(item.getAdmissionID()));
        holder.tvStudentName.setText(item.getStudentName());

        // ✅ row click opens dialog — context from itemView
        holder.itemView.setOnClickListener(v ->
                showBatchTimingDialog(holder.itemView.getContext(), item));

        //    holder.tvAdmDate.setText(item.getAdmDate());
      /*  holder.tvMobile.setText(item.getMobile());
        holder.tvCourse.setText(item.getCourseName());
        holder.tvScheme.setText(item.getScheme());
        holder.tvBatch.setText(item.getBatchName());*/
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // ✅ context from itemView — no Activity reference needed
    private void showBatchTimingDialog(Context context,
                                       TimingLessStudentResponse.StudentItem item) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_batch_timing, null);

        TextView tvStudentName                     = view.findViewById(R.id.tvStudentName);
        MaterialAutoCompleteTextView spBatchTiming = view.findViewById(R.id.spBatchTiming);
        TextView tvCapacity                        = view.findViewById(R.id.tvCapacity);
        TextView tvAvailable                       = view.findViewById(R.id.tvAvailable);
        MaterialButton btnAllotTiming              = view.findViewById(R.id.btnAllotTiming);
        MaterialButton btnClose                    = view.findViewById(R.id.btnClose);

        tvStudentName.setText("👤  " + item.getStudentName());
        tvCapacity.setText("—");
        tvAvailable.setText("—");

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        // Static timing slots — replace with API later
        String[] timingNames = {"Morning 9", "Morning 10", "Evening 5", "Evening 6"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                timingNames);

        spBatchTiming.setThreshold(0);
        spBatchTiming.setAdapter(spinnerAdapter);
        spBatchTiming.setOnClickListener(v -> spBatchTiming.showDropDown());

        // Timing selected — capacity/available from API later, show — for now
        spBatchTiming.setOnItemClickListener((parent, v, pos, id) -> {
            tvCapacity.setText("—");
            tvAvailable.setText("—");
        });

        btnAllotTiming.setOnClickListener(v -> {
            String selected = spBatchTiming.getText().toString().trim();
            if (selected.isEmpty()) {
                Toast.makeText(context,
                        "Please select a batch timing", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(context,
                    "✅ Timing \"" + selected + "\" allotted to "
                            + item.getStudentName(),
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAdmId, tvAdmDate, tvStudentName, tvMobile,
                tvCourse, tvScheme, tvBatch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAdmId       = itemView.findViewById(R.id.tvAdmId);
            tvStudentName = itemView.findViewById(R.id.tvFullName);
           /* tvAdmDate = itemView.findViewById(R.id.tvAdmDate);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvScheme = itemView.findViewById(R.id.tvScheme);
            tvBatch = itemView.findViewById(R.id.tvBatch);*/
        }
    }
}