package com.example.androidproject.model.summary;

public class StudyMaterialReportItem {

    // ── Fields exactly as returned in your sample ──────────────────
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
    public String certificateStatus;   // unused on this screen — likely leftover from a shared DTO
    public String certificateNumber;   // unused on this screen — likely leftover from a shared DTO
    public String result;              // unused on this screen — likely leftover from a shared DTO
    public String percentage;          // unused on this screen — likely leftover from a shared DTO
    public String issuerName;          // unused on this screen — likely leftover from a shared DTO
    public String issuerContact;       // unused on this screen — likely leftover from a shared DTO
    public String issuerImage;         // unused on this screen — likely leftover from a shared DTO
    public String handoverDate;        // unused on this screen — likely leftover from a shared DTO
    public boolean isDistributed;      // drives the Pending / Distributed toggle

    // ── Best-guess fields for the columns the desktop screen actually shows ──
    // TODO: confirm real property names against a live API response.
    public String location;
    public String studyMaterial;
    public String distributionDate;

    // ── UI-only field, not part of API payload ──────────────────────
    public transient boolean isChecked = false;
}