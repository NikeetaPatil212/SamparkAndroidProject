package com.example.androidproject.adapters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.AdmissionActivity;
import com.example.androidproject.EditDeleteActivity;
import com.example.androidproject.ExtendActivity;
import com.example.androidproject.R;
import com.example.androidproject.model.InquiryItem;
import com.example.androidproject.model.template.TemplateEntity;
import com.example.androidproject.model.template.TemplateRepository;
import com.example.androidproject.utils.PrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    //    view.findViewById(R.id.tvSendMsg).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.tvSendMsg).setOnClickListener(v -> {
            dialog.dismiss();
            sendInquiryFollowUpMessage(item);
        });

        view.findViewById(R.id.tvAbort).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.tvExtend).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, ExtendActivity.class);
            intent.putExtra("studentId", item.getStudentId());
            intent.putExtra("mobile",       item.getMobile());
            intent.putExtra("studentName",       item.getStudentName());
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

    private void sendInquiryFollowUpMessage(InquiryItem item) {
        String mobile = item.getMobile();
        if (mobile == null || mobile.trim().isEmpty()) {
            Toast.makeText(context, "Mobile number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        TemplateRepository.getInstance(context)
                .getTemplateByCategory("Inquiry Follow Up",
                        new TemplateRepository.SingleTemplateCallback() {

                            @Override
                            public void onSuccess(TemplateEntity template) {

                                // ── Respect isActive flag ─────────────────
                                if (!template.isActive) {
                                    Log.d("InquiryAdapter", "Template isActive=false, skipping");
                                    Toast.makeText(context,
                                            "WhatsApp notifications are currently disabled.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                PrefManager pref = PrefManager.getInstance(context);

                                // ── Build placeholder map ─────────────────
                                // Derive first name from full name
                                String fullName  = item.getStudentName() != null
                                        ? item.getStudentName() : "";
                                String firstName = fullName.contains(" ")
                                        ? fullName.substring(0, fullName.indexOf(" "))
                                        : fullName;

                                // Today's date as inquiry date fallback
                                String inquiryDate = item.getInquiry_date() != null
                                        && !item.getInquiry_date().isEmpty()
                                        ? item.getInquiry_date()
                                        : new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(new Date());

                                Map<String, String> data = new HashMap<>();
                                data.put("FirstName",      firstName);
                                data.put("StudentName",    fullName);
                                data.put("InquiryDate",    inquiryDate);
                                data.put("InquiryCourses", item.getAbout() != null
                                        ? item.getAbout() : "");
                                data.put("institute",      pref.getInstituteName());
                                data.put("Authority",      pref.getOwnerName());
                                data.put("mobile1",        pref.getInstituteMobile1());
                                data.put("mobile2",        pref.getInstituteMobile2());
                                data.put("email",          pref.getInstituteEmail());
                                data.put("address1",       pref.getInstituteAddress1());
                                data.put("address2",       pref.getInstituteAddress2());
                                data.put("ownerName",       pref.getOwnerName());

                                // ── Pick language ─────────────────────────
                                String lang = pref.getLanguage();
                                String templateText;
                                switch (lang) {
                                    case "MR": templateText = template.wa_MR; break;
                                    case "HI": templateText = template.wa_HI; break;
                                    default:   templateText = template.wa_EN; break;
                                }

                                String message = TemplateRepository.fillTemplate(templateText, data);

                                Log.d("InquiryAdapter", "lang=" + lang);
                                Log.d("InquiryAdapter", "mobile=" + mobile);
                                Log.d("InquiryAdapter", "message=" + message);
                                Log.d("InquiryAdapter", "ownerName=" + pref.getOwnerName());

                                // ── SMS (background, silent fail) ─────────
                                sendSms(mobile, message);

                                // ── WhatsApp ──────────────────────────────
                                openWhatsApp(mobile, message);
                            }

                            @Override
                            public void onError(String error) {
                                Log.w("InquiryAdapter", "Template not found: " + error);
                                Toast.makeText(context,
                                        "Message template not found", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // ── SMS helper ────────────────────────────────────────────────────────────
    private void sendSms(String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("InquiryAdapter", "SMS permission not granted");
                return;
            }
            android.telephony.SmsManager sms = android.telephony.SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);
         //   sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);

            String formattedNumber = phoneNumber.startsWith("+91") ? phoneNumber
                    : phoneNumber.startsWith("91") ? "+" + phoneNumber
                    : "+91" + phoneNumber;
            sms.sendMultipartTextMessage(formattedNumber, null, parts, null, null);
            Log.d("InquiryAdapter", "SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e("InquiryAdapter", "SMS failed: " + e.getMessage());
        }
    }

    // ── WhatsApp helper ───────────────────────────────────────────────────────
    private void openWhatsApp(String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            /*intent.setData(Uri.parse("https://wa.me/" + phoneNumber
                    + "?text=" + Uri.encode(message)));*/
            String formattedNumber = phoneNumber.startsWith("+91") ? phoneNumber.substring(1)
                    : phoneNumber.startsWith("91") ? phoneNumber
                    : "91" + phoneNumber;
            intent.setData(Uri.parse("https://wa.me/" + formattedNumber
                    + "?text=" + Uri.encode(message)));

            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
}