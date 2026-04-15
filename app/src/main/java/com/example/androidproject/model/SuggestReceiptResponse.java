package com.example.androidproject.model;

public class SuggestReceiptResponse {

    private boolean isSuccess;
    private String message;
    private String suggestedReceiptNo;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public String getSuggestedReceiptNo() {
        return suggestedReceiptNo;
    }
}