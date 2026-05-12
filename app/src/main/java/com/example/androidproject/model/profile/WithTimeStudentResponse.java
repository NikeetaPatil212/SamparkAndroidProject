package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WithTimeStudentResponse {

    @SerializedName("isSuccess")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("students")
    private List<StudentItem> students;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<StudentItem> getStudents() { return students; }

    public static class StudentItem {
        @SerializedName("admissionID")  private int admissionID;
        @SerializedName("studentName")  private String studentName;
        @SerializedName("mobile")       private String mobile;
        @SerializedName("timeID")       private int timeID;
        @SerializedName("description")  private String description;

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

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public int getTimeID() {
            return timeID;
        }

        public void setTimeID(int timeID) {
            this.timeID = timeID;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}