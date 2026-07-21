package com.example.androidproject.adapters;

public class KpiCard {

    public enum Type {
        TOTAL_INQUIRIES, PENDING_INQUIRIES, INQUIRY_ABORTED,
        TOTAL_ADMISSIONS, FEE_COLLECTED, REFUNDED, NON_REFUNDED, EXPENSES
    }

    public Type   type;
    public String emoji;
    public String title;
    public String subtitle;
    public String value;
    public int    gradientStart;
    public int    gradientEnd;
    public String pillLabel;

    public KpiCard(Type type, String emoji, String title,
                   String subtitle, int gradStart, int gradEnd) {
        this.type          = type;
        this.emoji         = emoji;
        this.title         = title;
        this.subtitle      = subtitle;
        this.value         = "—";
        this.gradientStart = gradStart;
        this.gradientEnd   = gradEnd;
        this.pillLabel     = title.toUpperCase();
    }
}