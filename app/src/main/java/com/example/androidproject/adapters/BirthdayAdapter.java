package com.example.androidproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.model.certificate.BirthdayResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.ViewHolder> {

    public interface OnWishClickListener {
        void onWishClick(BirthdayResponse.BirthdayStudent student);
    }

    private List<BirthdayResponse.BirthdayStudent> list = new ArrayList<>();
    private final OnWishClickListener listener;

    public BirthdayAdapter(OnWishClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<BirthdayResponse.BirthdayStudent> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_birthday_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BirthdayResponse.BirthdayStudent s = list.get(position);

        holder.tvStudentName.setText(s.getStudentName());
        holder.tvMobile.setText("📞 " + s.getMobile());
        holder.tvDob.setText("🎂 " + s.getDob());

        // Avatar initial
        if (s.getStudentName() != null && !s.getStudentName().isEmpty()) {
            holder.tvAvatar.setText(
                    String.valueOf(s.getStudentName().charAt(0)).toUpperCase(Locale.getDefault()));
        }

        holder.btnSendWish.setOnClickListener(v -> listener.onWishClick(s));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvStudentName, tvMobile, tvDob;
        MaterialButton btnSendWish;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar      = itemView.findViewById(R.id.tvAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvMobile      = itemView.findViewById(R.id.tvMobile);
            tvDob         = itemView.findViewById(R.id.tvDob);
            btnSendWish   = itemView.findViewById(R.id.btnSendWish);
        }
    }
}