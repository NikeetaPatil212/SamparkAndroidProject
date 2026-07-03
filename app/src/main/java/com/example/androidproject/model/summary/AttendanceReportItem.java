package com.example.androidproject.model.summary;

public class AttendanceReportItem {

    public int admissionID;
    public String admissionDate;
    public String studentName;
    public String mobile;
    public int courseID;
    public String courseName;
    public int batchID;
    public String batchName;
    public int timingID;
    public String timingDescription;
    public String attendanceStatus; // "Yes" / "No" / "Leave" etc.
    public String classDate;

    // ── UI-only field, not part of API payload ──────────────────────
    // Used by the adapter to track checkbox selection state in the RecyclerView.
    public transient boolean isChecked = false;
}