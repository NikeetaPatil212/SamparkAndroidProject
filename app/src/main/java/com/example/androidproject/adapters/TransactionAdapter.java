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
import com.example.androidproject.model.TransactionItem;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    public interface OnRowClickListener {
        void onClick(TransactionItem item);
    }

    private final List<TransactionItem> data     = new ArrayList<>();
    private OnRowClickListener          listener;
    private int                         selectedPosition = -1;

    public void setOnRowClickListener(OnRowClickListener l) {
        this.listener = l;
    }

    public void setData(List<TransactionItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TransactionItem item = data.get(position);

        // ── Date ──────────────────────────────────────────────────
        h.tvDate.setText(item.transactionDate != null ? item.transactionDate : "-");

        // ── UTNO ──────────────────────────────────────────────────
        h.tvUtno.setText(String.valueOf(item.utno));

        // ── Transaction Type with color ────────────────────────────
        h.tvType.setText(item.transactionType != null ? item.transactionType : "-");
        if ("Admission".equalsIgnoreCase(item.transactionType)) {
            h.tvType.setTextColor(Color.parseColor("#1565C0")); // blue
        } else {
            h.tvType.setTextColor(Color.parseColor("#2E7D32")); // green
        }

        // ── DR (Debit) ─────────────────────────────────────────────
        if (item.debit > 0) {
            h.tvDr.setText(formatAmt(item.debit));
            h.tvDr.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            h.tvDr.setText("0");
            h.tvDr.setTextColor(Color.parseColor("#AAAAAA"));
        }

        // ── CR (Credit) ────────────────────────────────────────────
        if (item.credit > 0) {
            h.tvCr.setText(formatAmt(item.credit));
            h.tvCr.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            h.tvCr.setText("0");
            h.tvCr.setTextColor(Color.parseColor("#AAAAAA"));
        }

        // ── Row background: selected = blue highlight, else alternate
        if (position == selectedPosition) {
            h.rowContainer.setBackgroundColor(Color.parseColor("#C8E6C9"));
        } else {
            h.rowContainer.setBackgroundColor(
                    position % 2 == 0 ? Color.WHITE : Color.parseColor("#F5FBF5"));
        }

        // ── Click ──────────────────────────────────────────────────
        h.rowContainer.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = h.getAdapterPosition();
            if (prev != -1) notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String formatAmt(double v) {
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }

    // ── ViewHolder ─────────────────────────────────────────────────
    static class VH extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        TextView     tvDate, tvUtno, tvType, tvDr, tvCr;

        VH(@NonNull View v) {
            super(v);
            rowContainer = v.findViewById(R.id.rowContainer);
            tvDate       = v.findViewById(R.id.tvDate);
            tvUtno       = v.findViewById(R.id.tvUtno);
            tvType       = v.findViewById(R.id.tvType);
            tvDr         = v.findViewById(R.id.tvDr);
            tvCr         = v.findViewById(R.id.tvCr);
        }
    }
}