package com.example.androidproject.model;

import java.util.List;

public class InquiryListResponse {
    private String message;
    private List<InquiryItem> inquiryList;

    public List<InquiryItem> getInquiryList() {
        return inquiryList;
    }
}
