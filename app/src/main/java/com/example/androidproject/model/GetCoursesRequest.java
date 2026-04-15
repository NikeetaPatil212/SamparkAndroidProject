package com.example.androidproject.model;

public class GetCoursesRequest {
    private int userID;
    private int instituteID;
    private Filters filters;

    public GetCoursesRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
        this.filters = new Filters();
    }

    public static class Filters {
        // Add filter fields if required
    }
}
