package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OutstandingResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("studentList")
    private List<StudentItem> studentList;

    public boolean isSuccess() { return isSuccess; }
    public String getMessage() { return message; }
    public List<StudentItem> getStudentList() { return studentList; }

    public static class StudentItem {
        @SerializedName("admissionID")    private int admissionID;
        @SerializedName("admissionDate")  private String admissionDate;
        @SerializedName("studentName")    private String studentName;
        @SerializedName("mobile")         private String mobile;
        @SerializedName("location")       private String location;
        @SerializedName("courseID")       private int courseID;
        @SerializedName("courseName")     private String courseName;
        @SerializedName("batchID")        private int batchID;
        @SerializedName("batchName")      private String batchName;
        @SerializedName("timingID")       private Integer timingID;
        @SerializedName("timingDescription") private String timingDescription;
        @SerializedName("fees")           private double fees;
        @SerializedName("paid")           private double paid;
        @SerializedName("outstanding")    private double outstanding;
        @SerializedName("reminderDate")   private String reminderDate;

        // checkbox selection state (not from API, used in adapter/UI)
        private transient boolean isSelected = false;

        public int getAdmissionID() { return admissionID; }
        public String getAdmissionDate() { return admissionDate; }
        public String getStudentName() { return studentName; }
        public String getMobile() { return mobile; }
        public String getLocation() { return location; }
        public int getCourseID() { return courseID; }
        public String getCourseName() { return courseName; }
        public int getBatchID() { return batchID; }
        public String getBatchName() { return batchName; }
        public Integer getTimingID() { return timingID; }
        public String getTimingDescription() { return timingDescription; }
        public double getFees() { return fees; }
        public double getPaid() { return paid; }
        public double getOutstanding() { return outstanding; }
        public String getReminderDate() { return reminderDate; }

        public boolean isSelected() { return isSelected; }
        public void setSelected(boolean selected) { isSelected = selected; }
    }
}