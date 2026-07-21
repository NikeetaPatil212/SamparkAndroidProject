package com.example.androidproject.adapters;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;

import java.util.List;

public class KpiCardAdapter extends RecyclerView.Adapter<KpiCardAdapter.VH> {

    public interface OnCardClick { void onClick(KpiCard.Type type); }

    private final List<KpiCard>  cards;
    private final OnCardClick    listener;

    // Per-card gradient pairs  [start, end]
    /*private static final int[][] GRADIENTS = {
            { 0xFF1565C0,  0xFF43A047},  // Total Inquiries  — green
            { 0xFFE65100,  0xFFFF7043},  // Pending          — deep orange
            { 0xFF2E7D32,  0xFF1E88E5},  // Admissions       — blue
            { 0xFFB71C1C, 0xFFE53935 },  // Aborted          — red
            { 0xFF00695C, 0xFF00897B },  // Fee Collected    — teal
            { 0xFF6A1B9A, 0xFF8E24AA },  // Refunded         — purple
            { 0xFF37474F, 0xFF546E7A },  // Non-Refunded     — blue-grey
            { 0xFFF57F17, 0xFFFFB300 },  // Expenses         — amber
    };*/


    // Gradient Colors (Start, End)
    private final int[][] GRADIENTS = {
            // Green
            { 0xB82E7D32, 0xB881C784 },
            // Red
            { 0xB8C62828, 0xB8E57373 },
            // Orange
            { 0xB8E65100, 0xB8FFB74D },
            // Blue
            { 0xB81565C0, 0xB864B5F6 },
            // Teal
            { 0xB800695C, 0xB84DB6AC },
            // Purple
            { 0xB86A1B9A, 0xB8CE93D8 },
            // Blue Grey
            { 0xB837474F, 0xB8B0BEC5 },
            // Amber
            { 0xB8F57F17, 0xB8FFE082 },
    };

    public KpiCardAdapter(List<KpiCard> cards, OnCardClick listener) {
        this.cards    = cards;
        this.listener = listener;
    }

    // Update a single card's value by type
    public void updateValue(KpiCard.Type type, String value) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).type == type) {
                cards.get(i).value = value;
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kpi_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        KpiCard card = cards.get(pos);
        int[]   grad = GRADIENTS[pos % GRADIENTS.length];

        // Gradient background
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{ grad[0], grad[1] });
        gd.setCornerRadius(56f);
        h.flBg.setBackground(gd);

        h.tvWatermark.setText(card.emoji);
        h.tvPill.setText(card.pillLabel);
        h.tvValue.setText(card.value);
        h.tvTitle.setText(card.title);
        h.tvSubtitle.setText(card.subtitle);

        // Position label: "3 of 8"
        h.tvPosition.setText((pos + 1) + " of " + cards.size());

        h.itemView.setOnClickListener(v -> listener.onClick(card.type));
    }

    @Override public int getItemCount() { return cards.size(); }

    static class VH extends RecyclerView.ViewHolder {
        FrameLayout flBg;
        TextView tvWatermark, tvPill, tvValue, tvTitle, tvSubtitle, tvPosition;
        VH(@NonNull View v) {
            super(v);
            flBg        = v.findViewById(R.id.flGradientBg);
            tvWatermark = v.findViewById(R.id.tvWatermark);
            tvPill      = v.findViewById(R.id.tvPill);
            tvValue     = v.findViewById(R.id.tvValue);
            tvTitle     = v.findViewById(R.id.tvTitle);
            tvSubtitle  = v.findViewById(R.id.tvSubtitle);
            tvPosition  = v.findViewById(R.id.tvPosition);
        }
    }
}