package com.example.androidproject.model;

import java.util.List;

public class GetCoursesResponse {
    private String message;
    private List<Course> couseList;

    public GetCoursesResponse(String message, List<Course> couseList) {
        this.message = message;
        this.couseList = couseList;
    }

    public String getMessage() { return message; }
    public List<Course> getCouseList() { return couseList; }
}
