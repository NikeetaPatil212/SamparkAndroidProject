package com.example.androidproject.model;

public class ExtendInquiryRequest {

    private int studentID;
    private String feedback;
    private int userID;
    private int instituteID;
    private int operatorID;
    private String reminderDate;


    public ExtendInquiryRequest(int studentID, String feedback, int userID, int instituteID,
                                int operatorID, String reminderDate) {
        this.studentID = studentID;
        this.feedback = feedback;
        this.userID = userID;
        this.instituteID = instituteID;
        this.operatorID = operatorID;
        this.reminderDate = reminderDate;
    }
}