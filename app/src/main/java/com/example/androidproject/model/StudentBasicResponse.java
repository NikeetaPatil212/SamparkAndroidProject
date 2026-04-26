package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StudentBasicResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("students")
    private List<StudentItem> students;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<StudentItem> getStudents() {
        return students;
    }

    public void setStudents(List<StudentItem> students) {
        this.students = students;
    }


    // ================= INNER STUDENT CLASS =================
    public static class StudentItem {

        @SerializedName("admissionID")
        private int admissionID;

        @SerializedName("studentName")
        private String studentName;

        public int getAdmissionID() {
            return admissionID;
        }

        public void setAdmissionID(int admissionID) {
            this.admissionID = admissionID;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }
    }
}