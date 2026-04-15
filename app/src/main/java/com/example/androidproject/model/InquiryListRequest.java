package com.example.androidproject.model;

import java.util.Map;

public class InquiryListRequest {
    private int userID;
    private int instituteID;
    private String fromDate;
    private String toDate;
    private Map<String, String> filters;

    public InquiryListRequest(int userID, int instituteID,
                              String fromDate, String toDate,
                              Map<String, String> filters) {
        this.userID = userID;
        this.instituteID = instituteID;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.filters = filters;
    }
}
