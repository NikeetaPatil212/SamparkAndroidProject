package com.example.androidproject.adapters;

public class KpiCard {

    public enum Type {
        TOTAL_INQUIRIES, PENDING_INQUIRIES, INQUIRY_ABORTED, TOTAL_ADMISSIONS,
        FEE_COLLECTED, REFUNDED, NON_REFUNDED, EXPENSES
    }

    public final Type type;
    public final String emoji;   // icon emoji
    public final String label;   // e.g. "Total Inquiries"
    public String value;         // e.g. "11" or "₹25.7K"
    public final String subtitle;// e.g. "All inquiries"
    public final int iconBgColor;
    public final int valueColor;

    public KpiCard(Type type, String emoji, String label, String subtitle,
                   int iconBgColor, int valueColor) {
        this.type = type;
        this.emoji = emoji;
        this.label = label;
        this.subtitle = subtitle;
        this.iconBgColor = iconBgColor;
        this.valueColor = valueColor;
        this.value = "0";
    }
}

