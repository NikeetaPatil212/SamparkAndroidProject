package com.example.androidproject.model.summary;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InquirySummaryResponse {

    @SerializedName("isSuccess")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("summaryList")
    private List<SummaryItem> summaryList;

    public boolean isSuccess()           { return success; }
    public String getMessage()           { return message; }
    public List<SummaryItem> getSummaryList() { return summaryList; }

    public static class SummaryItem {

        @SerializedName("courseName")
        private String courseName;

        @SerializedName("totalInquiries")
        private int totalInquiries;

        @SerializedName("active")
        private int active;

        @SerializedName("converted")
        private int converted;

        @SerializedName("cancelled")
        private int cancelled;

        public String getCourseName()     { return courseName; }
        public int getTotalInquiries()    { return totalInquiries; }
        public int getActive()            { return active; }
        public int getConverted()         { return converted; }
        public int getCancelled()         { return cancelled; }
    }
}