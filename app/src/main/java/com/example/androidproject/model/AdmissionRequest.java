package com.example.androidproject.model;

import java.util.List;

public class AdmissionRequest {

    private int userID;
    private int instituteID;
    private int studentID;
    private String admissionDate;
    private String imgUrl;
    private int advancePaid;
    private List<CourseList> courses;

    public AdmissionRequest(int userID,
                            int instituteID,
                            int studentID,
                            String admissionDate,
                            String imgUrl,
                            int advancePaid,
                            List<CourseList> courses) {

        this.userID = userID;
        this.instituteID = instituteID;
        this.studentID = studentID;
        this.admissionDate = admissionDate;
        this.imgUrl = imgUrl;
        this.advancePaid = advancePaid;
        this.courses = courses;
    }
}