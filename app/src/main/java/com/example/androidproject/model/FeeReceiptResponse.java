package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeeReceiptResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("detailList")
    private List<PaymentHistory> detailList;

    @SerializedName("summary")
    private Summary summary;

    @SerializedName("suggestedReceiptNo")
    private String suggestedReceiptNo;

    public boolean isSuccess() { return isSuccess; }
    public String getMessage() { return message; }
    public List<PaymentHistory> getDetailList() { return detailList; }
    public Summary getSummary() { return summary; }
    public String getSuggestedReceiptNo() { return suggestedReceiptNo; }

    // ✅ INNER CLASS
    public static class Summary {

        @SerializedName("studentID")
        private int studentID;

        @SerializedName("admissionID")
        private int admissionID;

        @SerializedName("studentName")
        private String studentName;

        @SerializedName("mobile")
        private String mobile;

        @SerializedName("courseName")
        private String courseName;

        @SerializedName("batchName")
        private String batchName;

        @SerializedName("fees")
        private double fees;

        @SerializedName("totalPaid")
        private double totalPaid;

        @SerializedName("remaining")
        private double remaining;

        public int getStudentID() { return studentID; }
        public int getAdmissionID() { return admissionID; }
        public String getStudentName() { return studentName; }
        public String getMobile() { return mobile; }
        public String getCourseName() { return courseName; }
        public String getBatchName() { return batchName; }
        public double getFees() { return fees; }
        public double getTotalPaid() { return totalPaid; }
        public double getRemaining() { return remaining; }
    }
}