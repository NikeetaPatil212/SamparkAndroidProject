package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeeResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("studentList")
    private List<StudentOutstanding> studentList;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public List<StudentOutstanding> getStudentList() {
        return studentList;
    }

    public static class StudentOutstanding {

        @SerializedName("admissionID")
        private int admissionID;

        @SerializedName("admissionDate")
        private String admissionDate;

        @SerializedName("studentName")
        private String studentName;

        @SerializedName("mobile")
        private String mobile;

        @SerializedName("location")
        private String location;

        @SerializedName("courseID")
        private int courseID;

        @SerializedName("courseName")
        private String courseName;

        @SerializedName("batchID")
        private int batchID;

        @SerializedName("batchName")
        private String batchName;

        @SerializedName("timingID")
        private int timingID;

        @SerializedName("timingDescription")
        private String timingDescription;

        @SerializedName("fees")
        private double fees;

        @SerializedName("paid")
        private double paid;

        @SerializedName("outstanding")
        private double outstanding;

        @SerializedName("reminderDate")
        private String reminderDate;

        public int getAdmissionID() {
            return admissionID;
        }

        public String getAdmissionDate() {
            return admissionDate;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getMobile() {
            return mobile;
        }

        public String getLocation() {
            return location;
        }

        public int getCourseID() {
            return courseID;
        }

        public String getCourseName() {
            return courseName;
        }

        public int getBatchID() {
            return batchID;
        }

        public String getBatchName() {
            return batchName;
        }

        public int getTimingID() {
            return timingID;
        }

        public String getTimingDescription() {
            return timingDescription;
        }

        public double getFees() {
            return fees;
        }

        public double getPaid() {
            return paid;
        }

        public double getOutstanding() {
            return outstanding;
        }

        public String getReminderDate() {
            return reminderDate;
        }
    }
}