package com.example.androidproject.model;

import java.util.Map;

public class GetAdmissionRequest {
    private int userID;
    private int instituteID;
    private String fromDate;
    private String toDate;
    private Map<String, String> filters;

    // Constructor + Getter Setter


    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getInstituteID() {
        return instituteID;
    }

    public void setInstituteID(int instituteID) {
        this.instituteID = instituteID;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }
}
