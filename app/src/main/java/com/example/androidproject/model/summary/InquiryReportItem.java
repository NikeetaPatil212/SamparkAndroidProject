package com.example.androidproject.model.summary;

import com.google.gson.annotations.SerializedName;

public class InquiryReportItem {
    @SerializedName("studentID")     public int    studentID;
    @SerializedName("inquiryDate")   public String inquiryDate;
    @SerializedName("fullName")      public String fullName;
    @SerializedName("mobile")        public String mobile;
    @SerializedName("alternateNo")   public String alternateNo;
    @SerializedName("location")      public String location;
    @SerializedName("about")         public String about;
    @SerializedName("reminderDate")  public String reminderDate;
    @SerializedName("reminderStatus")public String reminderStatus;
    @SerializedName("feedback")      public String feedback;
    @SerializedName("gender")        public String gender;
    @SerializedName("schoolName")    public String schoolName;
    @SerializedName("reference")     public String reference;

    // runtime selection state
    private boolean selected = false;
    public boolean isSelected()        { return selected; }
    public void setSelected(boolean s) { this.selected = s; }
}