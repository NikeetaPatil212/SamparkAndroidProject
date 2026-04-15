package com.example.androidproject.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.AdmissionActivity;
import com.example.androidproject.EditDeleteActivity;
import com.example.androidproject.ExtendActivity;
import com.example.androidproject.R;
import com.example.androidproject.model.InquiryItem;

import java.util.ArrayList;
import java.util.List;

public class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.ViewHolder> {

    private Context context;
    private List<InquiryItem> originalList = new ArrayList<>();
    private List<InquiryItem> filteredList = new ArrayList<>();

    // Track active filters so they can be combined
    private String activeNameFilter    = "";
    private String activeContactFilter = "";

    public InquiryAdapter(Context context) {
        this.context = context;
    }

    // ── Called after API success ──────────────────────────────────────────────
    public void setData(List<InquiryItem> list) {
        originalList.clear();
        originalList.addAll(list);

        // Reset filters when new data arrives
        activeNameFilter    = "";
        activeContactFilter = "";

        filteredList.clear();
        filteredList.addAll(list);

        notifyDataSetChanged();
    }

    // ── Filter by student name (live search) ──────────────────────────────────
    public void filterByName(String text) {
        activeNameFilter = (text == null) ? "" : text.trim();
        applyFilters();
    }

    // ── Filter by contact number (live search) ────────────────────────────────
    public void filterByContact(String text) {
        activeContactFilter = (text == null) ? "" : text.trim();
        applyFilters();
    }

    // ── Apply both filters together ───────────────────────────────────────────
    private void applyFilters() {
        filteredList.clear();

        String nameLower    = activeNameFilter.toLowerCase();
        String contactLower = activeContactFilter.toLowerCase();

        for (InquiryItem item : originalList) {
            boolean nameMatches = nameLower.isEmpty()
                    || (item.getStudentName() != null
                    && item.getStudentName().toLowerCase().contains(nameLower));

            boolean contactMatches = contactLower.isEmpty()
                    || (item.getMobile() != null
                    && item.getMobile().toLowerCase().contains(contactLower))
                    || (item.getAlternateNo() != null
                    && item.getAlternateNo().toLowerCase().contains(contactLower));

            if (nameMatches && contactMatches) {
                filteredList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    // ── RecyclerView boilerplate ──────────────────────────────────────────────
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_inquiry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        InquiryItem item = filteredList.get(position);

        h.tvInquiryId.setText(String.valueOf(item.getStudentId()));
        h.tvName.setText(item.getStudentName());
        h.tvMobile.setText(item.getMobile());
        h.tvInquiryDate.setText(item.getInquiry_date());
        h.tvAddress.setText(item.getAddress());
        h.tvType.setText("Inquiry");
        h.tvCourses.setText(item.getAbout());
        h.tvFollowUp.setText(item.getReminderDate());
        h.tvFeedback.setText(item.getFeedback());

        h.tvStatus.setOnClickListener(v -> showActionDialog(item));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvMobile, tvInquiryDate, tvInquiryId,
                tvAddress, tvType, tvCourses, tvFollowUp, tvFeedback;
        Button tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvName        = itemView.findViewById(R.id.tvName);
            tvInquiryId   = itemView.findViewById(R.id.tvInquiryId);
            tvMobile      = itemView.findViewById(R.id.tvMobile);
            tvInquiryDate = itemView.findViewById(R.id.tvInquiryDate);
            tvStatus      = itemView.findViewById(R.id.btnStatus);
            tvAddress     = itemView.findViewById(R.id.tvAddress);
            tvType        = itemView.findViewById(R.id.tvType);
            tvCourses     = itemView.findViewById(R.id.tvCourses);
            tvFollowUp    = itemView.findViewById(R.id.tvFollowUp);
            tvFeedback    = itemView.findViewById(R.id.tvFeedback);
        }
    }

    // ── Action dialog ─────────────────────────────────────────────────────────
    private void showActionDialog(InquiryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_inquiry_actions, null);

        AlertDialog dialog = builder.setView(view).create();

        view.findViewById(R.id.tvSendMsg).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvAbort).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.tvExtend).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, ExtendActivity.class);
            intent.putExtra("studentId", item.getStudentId());
            Log.d("InquiryAdapter", "Extend studentId: " + item.getStudentId());
            context.startActivity(intent);
        });

        view.findViewById(R.id.tvEditDelete).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, EditDeleteActivity.class);
            intent.putExtra("studentId",    item.getStudentId());
            intent.putExtra("name",         item.getStudentName());
            intent.putExtra("mobile",       item.getMobile());
            intent.putExtra("altMobile",    item.getAlternateNo());
            intent.putExtra("email",        item.getEmailID());
            intent.putExtra("address",      item.getAddress());
            intent.putExtra("about",        item.getAbout());
            intent.putExtra("inquiry_date", item.getInquiry_date());
            context.startActivity(intent);
        });

        view.findViewById(R.id.tvAdmission).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, AdmissionActivity.class);
            intent.putExtra("studentId",    item.getStudentId());
            intent.putExtra("student_name", item.getStudentName());
            intent.putExtra("mobile",       item.getMobile());
            Log.d("InquiryAdapter", "Admission studentId: " + item.getStudentId());
            context.startActivity(intent);
        });

        dialog.show();
    }
}