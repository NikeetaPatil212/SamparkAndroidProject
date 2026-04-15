package com.example.androidproject.model;

public class AddReceiptResponse {

    private boolean isSuccess;
    private String message;
    private String receiptNo;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public String getReceiptNo() {
        return receiptNo;
    }
}