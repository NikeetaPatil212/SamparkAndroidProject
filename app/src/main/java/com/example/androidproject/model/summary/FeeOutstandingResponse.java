package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeeOutstandingResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("summaryList")
    private List<SummaryItem> summaryList;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public List<SummaryItem> getSummaryList() {
        return summaryList;
    }

    public static class SummaryItem {

        @SerializedName("courseID")
        private int courseID;

        @SerializedName("courseName")
        private String courseName;

        @SerializedName("totalFees")
        private double totalFees;

        @SerializedName("totalPaid")
        private double totalPaid;

        @SerializedName("totalRemaining")
        private double totalRemaining;

        @SerializedName("collectionPercentage")
        private double collectionPercentage;

        public int getCourseID() {
            return courseID;
        }

        public String getCourseName() {
            return courseName;
        }

        public double getTotalFees() {
            return totalFees;
        }

        public double getTotalPaid() {
            return totalPaid;
        }

        public double getTotalRemaining() {
            return totalRemaining;
        }

        public double getCollectionPercentage() {
            return collectionPercentage;
        }
    }
}
