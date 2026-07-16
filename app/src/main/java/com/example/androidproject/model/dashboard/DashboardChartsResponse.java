package com.example.androidproject.model.dashboard;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DashboardChartsResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("monthlySummary")
    private List<MonthlySummaryItem> monthlySummary;

    @SerializedName("feeSummary")
    private List<FeeSummaryItem> feeSummary;

    @SerializedName("courseTrend")
    private List<CourseTrendItem> courseTrend;

    public boolean isSuccess() { return isSuccess; }
    public String getMessage() { return message; }
    public List<MonthlySummaryItem> getMonthlySummary() { return monthlySummary; }
    public List<FeeSummaryItem> getFeeSummary() { return feeSummary; }
    public List<CourseTrendItem> getCourseTrend() { return courseTrend; }

    public static class MonthlySummaryItem {
        @SerializedName("monthYear")
        private String monthYear;   // e.g. "2026-07"

        @SerializedName("inquiries")
        private int inquiries;

        @SerializedName("admissions")
        private int admissions;

        public String getMonthYear() { return monthYear; }
        public int getInquiries() { return inquiries; }
        public int getAdmissions() { return admissions; }
    }

    public static class FeeSummaryItem {
        @SerializedName("category")
        private String category;   // "Total Fee" | "Paid Fee" | "Remaining Fee" | "Waived/Refunded Fee"

        @SerializedName("amount")
        private double amount;

        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }

    public static class CourseTrendItem {
        @SerializedName("courseName")
        private String courseName;

        @SerializedName("monthYear")
        private String monthYear;

        @SerializedName("admissionsCount")
        private int admissionsCount;

        public String getCourseName() { return courseName; }
        public String getMonthYear() { return monthYear; }
        public int getAdmissionsCount() { return admissionsCount; }
    }
}