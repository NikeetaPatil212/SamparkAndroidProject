package com.example.androidproject.model.summary;


import java.util.List;

/**
 * Response body for AttendanceReport.
 *
 * {
 *   "isSuccess": true,
 *   "message": "Attendance report loaded successfully.",
 *   "studentList": [ ... ]
 * }
 */
public class AttendanceReportResponse {
    public boolean isSuccess;
    public String message;
    public List<AttendanceReportItem> studentList;
}