package com.example.androidproject.model;

public class InquiryRequest {

    private int studentID;
    private String f_name;
    private String m_name;
    private String l_name;
    private String full_name;
    private String mobile;
    private String alternateNo;
    private String emailID;
    private String address;
    private String type;
    private String about;
    private String inquiry_date;
    private String school_name;
    private String source_of_inquiry;
    private String dob;
    private String gender;
    private String feedback;
    private int userID;
    private int instituteID;
    private int operatorID;
    private String reminderDate;

    public InquiryRequest(String f_name, String m_name, String l_name,
                          String full_name, String mobile, String alternateNo,
                          String emailID, String address, String type, String about,
                          String inquiry_date, String school_name, String source_of_inquiry,
                          String dob, String gender, String feedback,
                          int userID, int instituteID, int operatorID, String reminderDate) {

    //    this.studentID = studentID;
        this.f_name = f_name;
        this.m_name = m_name;
        this.l_name = l_name;
        this.full_name = full_name;
        this.mobile = mobile;
        this.alternateNo = alternateNo;
        this.emailID = emailID;
        this.address = address;
        this.type = type;
        this.about = about;
        this.inquiry_date = inquiry_date;
        this.school_name = school_name;
        this.source_of_inquiry = source_of_inquiry;
        this.dob = dob;
        this.gender = gender;
        this.feedback = feedback;
        this.userID = userID;
        this.instituteID = instituteID;
        this.operatorID = operatorID;
        this.reminderDate = reminderDate;
    }
}
