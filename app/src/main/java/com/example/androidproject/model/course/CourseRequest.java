package com.example.androidproject.model.course;

public class CourseRequest {
    private int    courseID;
    private String courseName;
    private double fees;
    private String scheme;
    private int    certificate;
    private int    duration;
    private int    userID;
    private int    instituteID;

    public CourseRequest(int courseID, String courseName, double fees,
                         String scheme, int certificate, int duration,
                         int userID, int instituteID) {
        this.courseID    = courseID;
        this.courseName  = courseName;
        this.fees        = fees;
        this.scheme      = scheme;
        this.certificate = certificate;
        this.duration    = duration;
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}