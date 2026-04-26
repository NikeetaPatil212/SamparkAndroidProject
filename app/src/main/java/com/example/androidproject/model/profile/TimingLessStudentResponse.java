package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TimingLessStudentResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("students")
    private List<StudentItem> students;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public List<StudentItem> getStudents() {
        return students;
    }

    public static class StudentItem {

        @SerializedName("admissionID")
        private int admissionID;

        @SerializedName("admDate")
        private String admDate;

        @SerializedName("studentName")
        private String studentName;

        @SerializedName("mobile")
        private String mobile;

        @SerializedName("courseName")
        private String courseName;

        @SerializedName("scheme")
        private String scheme;

        @SerializedName("batchName")
        private String batchName;

        public int getAdmissionID() { return admissionID; }
        public String getAdmDate() { return admDate; }
        public String getStudentName() { return studentName; }
        public String getMobile() { return mobile; }
        public String getCourseName() { return courseName; }
        public String getScheme() { return scheme; }
        public String getBatchName() { return batchName; }
    }
}