package com.example.androidproject.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.certificate.CertificateStudent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CertificateStudentAdapter
        extends RecyclerView.Adapter<CertificateStudentAdapter.VH> {

    private final Context context;
    private List<CertificateStudent> fullList     = new ArrayList<>();
    private List<CertificateStudent> filteredList = new ArrayList<>();

    public CertificateStudentAdapter(Context context) { this.context = context; }

    public void setData(List<CertificateStudent> list) {
        fullList.clear();
        if (list != null) fullList.addAll(list);
        filteredList.clear();
        filteredList.addAll(fullList);
        notifyDataSetChanged();
    }

    public void filterByName(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lower = query.trim().toLowerCase();
            for (CertificateStudent s : fullList) {
                if (s.studentName != null && s.studentName.toLowerCase().contains(lower))
                    filteredList.add(s);
            }
        }
        notifyDataSetChanged();
    }

    public void filterByBatch(String batchName) {
        filteredList.clear();
        if (batchName == null || batchName.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            for (CertificateStudent s : fullList) {
                if (batchName.equalsIgnoreCase(s.batch)) filteredList.add(s);
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() { return filteredList.size(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_certificate_student, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CertificateStudent s = filteredList.get(position);

        h.rowContainer.setBackgroundColor(
                position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));

        h.tvAdmId.setText(String.valueOf(s.admissionID));
        h.tvStudentId.setText(String.valueOf(s.studentID));
        h.tvAdmDate.setText(s.admissionDate != null ? s.admissionDate : "");
        h.tvStudentName.setText(s.studentName != null ? s.studentName : "");
        h.tvMobile.setText(s.mobile != null ? s.mobile : "");
        h.tvLocation.setText(s.location != null ? s.location : "-");
        h.tvCourse.setText(s.course != null ? s.course : "");
        h.tvScheme.setText(s.scheme != null ? s.scheme : "");
        h.tvBatch.setText(s.batch != null ? s.batch : "");
        h.tvFee.setText(String.format(Locale.getDefault(), "₹%.0f", s.fee));
        h.tvPaid.setText(String.format(Locale.getDefault(), "₹%.0f", s.paid));

        // Outstanding — red if > 0, green if cleared
        h.tvOutstanding.setText(String.format(Locale.getDefault(), "₹%.0f", s.outstanding));
        h.tvOutstanding.setTextColor(s.outstanding > 0
                ? Color.parseColor("#E53935") : Color.parseColor("#2E7D32"));

        // Fee Status
        h.tvStatus.setText(s.status != null ? s.status : "");
        h.tvStatus.setTextColor("Pending".equalsIgnoreCase(s.status)
                ? Color.parseColor("#E65100") : Color.parseColor("#2E7D32"));

        // Certificate
        if (s.certificate) {
            h.tvCertificate.setText("✅ Yes");
            h.tvCertificate.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            h.tvCertificate.setText("❌ No");
            h.tvCertificate.setTextColor(Color.parseColor("#E53935"));
        }

        h.tvCertificateId.setText(s.certificateID != null && !s.certificateID.isEmpty()
                ? s.certificateID : "-");
        h.tvIssuer.setText(s.issuer != null && !s.issuer.isEmpty() ? s.issuer : "-");
        h.tvIssuerContact.setText(s.issuerContact != null && !s.issuerContact.isEmpty()
                ? s.issuerContact : "-");
        h.tvCertificateNo.setText(s.certificateID != null && !s.certificateID.isEmpty()
                ? s.certificateID : "-");
        h.tvIssueDate.setText(s.issueDate != null && !s.issueDate.isEmpty()
                ? s.issueDate : "-");

        // Result — green for Pass, red for Fail
        h.tvResult.setText(s.result != null && !s.result.isEmpty() ? s.result : "-");
        h.tvResult.setTextColor("Pass".equalsIgnoreCase(s.result)
                ? Color.parseColor("#2E7D32") : Color.parseColor("#E53935"));

        // Percentage
        h.tvPercentage.setText(s.percentage != null
                ? String.format(Locale.getDefault(), "%.0f%%", s.percentage) : "-");

        h.rowContainer.setOnClickListener(v -> {
            if (rowClickListener != null) {
                rowClickListener.onRowClick(s, position);
            }
        });
    }

    @Override
    public int getItemCount() { return filteredList.size(); }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        TextView tvAdmId, tvStudentId, tvAdmDate, tvStudentName, tvMobile,
                tvLocation, tvCourse, tvScheme, tvBatch, tvFee, tvPaid,
                tvOutstanding, tvStatus, tvCertificate, tvCertificateId,
                tvIssuer, tvIssuerContact, tvCertificateNo, tvIssueDate,
                tvResult, tvPercentage;

        VH(View v) {
            super(v);
            rowContainer    = v.findViewById(R.id.rowContainer);
            tvAdmId         = v.findViewById(R.id.tvAdmId);
            tvStudentId     = v.findViewById(R.id.tvStudentId);
            tvAdmDate       = v.findViewById(R.id.tvAdmDate);
            tvStudentName   = v.findViewById(R.id.tvStudentName);
            tvMobile        = v.findViewById(R.id.tvMobile);
            tvLocation      = v.findViewById(R.id.tvLocation);
            tvCourse        = v.findViewById(R.id.tvCourse);
            tvScheme        = v.findViewById(R.id.tvScheme);
            tvBatch         = v.findViewById(R.id.tvBatch);
            tvFee           = v.findViewById(R.id.tvFee);
            tvPaid          = v.findViewById(R.id.tvPaid);
            tvOutstanding   = v.findViewById(R.id.tvOutstanding);
            tvStatus        = v.findViewById(R.id.tvStatus);
            tvCertificate   = v.findViewById(R.id.tvCertificate);
            tvCertificateId = v.findViewById(R.id.tvCertificateId);
            tvIssuer        = v.findViewById(R.id.tvIssuer);
            tvIssuerContact = v.findViewById(R.id.tvIssuerContact);
            tvCertificateNo = v.findViewById(R.id.tvCertificateNo);
            tvIssueDate     = v.findViewById(R.id.tvIssueDate);
            tvResult        = v.findViewById(R.id.tvResult);
            tvPercentage    = v.findViewById(R.id.tvPercentage);
        }
    }

    // ── Callback interface ────────────────────────────────────────────────────────
    public interface OnRowClickListener {
        void onRowClick(CertificateStudent student, int position);
    }

    private OnRowClickListener rowClickListener;

    public void setOnRowClickListener(OnRowClickListener listener) {
        this.rowClickListener = listener;
    }
}