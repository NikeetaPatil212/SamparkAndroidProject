package com.example.androidproject.model;

import java.util.Map;

public class BatchRequest {

    private int userID;
    private int instituteID;
    private int courseID;

    public BatchRequest(int userID, int instituteID, int courseID) {
        this.userID = userID;
        this.instituteID = instituteID;
        this.courseID = courseID;
    }

    public int getUserID() {
        return userID;
    }

    public int getInstituteID() {
        return instituteID;
    }

    public int getCourseID() {
        return courseID;
    }
}
