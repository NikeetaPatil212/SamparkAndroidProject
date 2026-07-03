package com.example.androidproject.model.summary;

public class CertificateReportItem {

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
    public String certificateStatus;
    public String certificateNumber;
    public String result;
    public String percentage;
    public String issuerName;
    public String issuerContact;
    public String issuerImage;
    public String handoverDate; // null while pending
    public boolean isDistributed;

    // ── UI-only field, not part of API payload ──────────────────────
    public transient boolean isChecked = false;


    private boolean selected = false;
    public boolean isSelected()        { return selected; }
    public void setSelected(boolean s) { this.selected = s; }
}