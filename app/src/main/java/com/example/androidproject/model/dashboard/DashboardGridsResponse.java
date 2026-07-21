package com.example.androidproject.model.dashboard;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardGridsResponse {
    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("recentInquiries")
    private List<RecentEntry> recentInquiries;

    @SerializedName("recentAdmissions")
    private List<RecentEntry> recentAdmissions;

    @SerializedName("recentFees")
    private List<RecentEntry> recentFees;

    public boolean isSuccess() { return isSuccess; }
    public String getMessage() { return message; }
    public List<RecentEntry> getRecentInquiries() { return recentInquiries; }
    public List<RecentEntry> getRecentAdmissions() { return recentAdmissions; }
    public List<RecentEntry> getRecentFees() { return recentFees; }
}