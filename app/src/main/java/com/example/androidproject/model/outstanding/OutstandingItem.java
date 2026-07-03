package com.example.androidproject.model.outstanding;

public class OutstandingItem {
    public int admissionID;
    public String admissionDate;
    public String studentName;
    public String mobile;
    public String location;
    public int courseID;
    public String courseName;
    public int batchID;
    public String batchName;
    public Integer timingID;
    public String timingDescription;
    public double fees;
    public double paid;
    public double outstanding;
    public String reminderDate;   // used as Due Date

    private boolean selected = false;

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}