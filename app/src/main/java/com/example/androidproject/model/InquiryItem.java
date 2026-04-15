package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class InquiryItem {

    @SerializedName("studentID")
    private int studentId;
    private String studentName;
    @SerializedName("mobile")
    private String mobile;
    private String alternateNo;
    private String emailID;
    private String gender;
    private String dob;
    private String inquiry_date;
    private String school_name;
    private String about;
    private String source_of_inquiry;
    private String reminderDate;
    private String reminderStatus;
    private String status;
    private String lastAction;
    private String address;
    private String type;
    private String courses;
    private String followUp;
    private String feedback;

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() { return studentName; }
    public String getMobile() { return mobile; }
    public String getAlternateNo() { return alternateNo; }
    public String getEmailID() { return emailID; }
    public String getGender() { return gender; }
    public String getDob() { return dob; }
    public String getInquiry_date() { return inquiry_date; }
    public String getSchool_name() { return school_name; }
    public String getAbout() { return about; }
    public String getSource_of_inquiry() { return source_of_inquiry; }
    public String getReminderDate() { return reminderDate; }
    public String getReminderStatus() { return reminderStatus; }
    public String getStatus() { return status; }
    public String getLastAction() { return lastAction; }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public String getCourses() {
        return courses;
    }

    public String getFollowUp() {
        return followUp;
    }

    public String getFeedback() {
        return feedback;
    }
}
