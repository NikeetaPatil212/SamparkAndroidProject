package com.example.androidproject.adapters;

import android.graphics.Color;
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

    public interface OnKpiClick { void onClick(KpiCard.Type type); }

    private final List<KpiCard> items;
    private final OnKpiClick listener;

    public KpiCardAdapter(List<KpiCard> items, OnKpiClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kpi_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        KpiCard c = items.get(pos);
        h.tvIconEmoji.setText(c.emoji);
        h.tvLabel.setText(c.label);
        h.tvValue.setText(c.value);
        h.tvValue.setTextColor(c.valueColor);
        h.tvSubtitle.setText(c.subtitle);

        // Circular colored icon background
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(c.iconBgColor);
        h.iconBg.setBackground(bg);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(c.type);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    /** Update the value shown on a specific card type without full re-bind churn */
    public void updateValue(KpiCard.Type type, String newValue) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).type == type) {
                items.get(i).value = newValue;
                notifyItemChanged(i);
                return;
            }
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        FrameLayout iconBg;
        TextView tvIconEmoji, tvLabel, tvValue, tvSubtitle;
        VH(View v) {
            super(v);
            iconBg      = v.findViewById(R.id.iconBg);
            tvIconEmoji = v.findViewById(R.id.tvIconEmoji);
            tvLabel     = v.findViewById(R.id.tvLabel);
            tvValue     = v.findViewById(R.id.tvValue);
            tvSubtitle  = v.findViewById(R.id.tvSubtitle);
        }
    }
}