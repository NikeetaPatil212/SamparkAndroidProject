package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudyMaterialDistributionResponse {

    @SerializedName("isSuccess")  private boolean success;
    @SerializedName("message")    private String message;
    @SerializedName("studentlist")   private List<StudentItem> students;

    public boolean isSuccess()           { return success; }
    public String getMessage()           { return message; }
    public List<StudentItem> getStudents() { return students; }

    public static class StudentItem {
        @SerializedName("admissionID")   private int admissionID;
        @SerializedName("studentName")   private String studentName;
        @SerializedName("mobile")        private String mobile;
        @SerializedName("isDistributed") private boolean isDistributed;

        // runtime checkbox state
        private boolean checked = false;

        public int getAdmissionID()      { return admissionID; }
        public String getStudentName()   { return studentName; }
        public String getMobile()        { return mobile; }
        public boolean isDistributed()   { return isDistributed; }
        public boolean isChecked()       { return checked; }
        public void setChecked(boolean c){ this.checked = c; }
    }
}