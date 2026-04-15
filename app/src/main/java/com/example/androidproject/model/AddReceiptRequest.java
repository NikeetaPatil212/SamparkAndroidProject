package com.example.androidproject.model;

public class AddReceiptRequest {

    private int userID;
    private int instituteID;
    private int studentID;
    private int admissionID;
    private double amount;
    private String trDate;
    private String receiptNo;
    private int operatorID;
    private String description;

    public AddReceiptRequest(int userID, int instituteID, int studentID,
                             int admissionID, double amount, String trDate,
                             String receiptNo, int operatorID, String description) {
        this.userID = userID;
        this.instituteID = instituteID;
        this.studentID = studentID;
        this.admissionID = admissionID;
        this.amount = amount;
        this.trDate = trDate;
        this.receiptNo = receiptNo;
        this.operatorID = operatorID;
        this.description = description;
    }
}