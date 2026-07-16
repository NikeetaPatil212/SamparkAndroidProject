package com.example.androidproject.model.dashboard;

import com.google.gson.annotations.SerializedName;

public class DashboardCardsResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("dashboard")
    private DashboardCards dashboard;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public DashboardCards getDashboard() {
        return dashboard;
    }

    public static class DashboardCards {

        @SerializedName("totalInquiries")
        private int totalInquiries;

        @SerializedName("pendingInquiries")
        private int pendingInquiries;

        @SerializedName("inquiryAborted")
        private int inquiryAborted;

        @SerializedName("totalAdmissions")
        private int totalAdmissions;

        @SerializedName("totalFeeCollected")
        private double totalFeeCollected;

        @SerializedName("totalRefunded")
        private double totalRefunded;

        @SerializedName("nonRefunded")
        private double nonRefunded;

        @SerializedName("totalExpenses")
        private double totalExpenses;

        public int getTotalInquiries() { return totalInquiries; }
        public int getPendingInquiries() { return pendingInquiries; }
        public int getInquiryAborted() { return inquiryAborted; }
        public int getTotalAdmissions() { return totalAdmissions; }
        public double getTotalFeeCollected() { return totalFeeCollected; }
        public double getTotalRefunded() { return totalRefunded; }
        public double getNonRefunded() { return nonRefunded; }
        public double getTotalExpenses() { return totalExpenses; }
    }
}