package com.example.androidproject.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room cache for a single batch row, keyed by the server's batchId.
 * Mirrors CourseEntity's shape/conventions so it drops into the same
 * database alongside courses.
 */
@Entity(tableName = "batches")
public class BatchEntity {

    @PrimaryKey
    @NonNull
    public int batchId;

    public int courseId;
    public String batchName;
    public String startDate;
    public String endDate;

    public BatchEntity() {}

    public BatchEntity(int batchId, int courseId, String batchName,
                       String startDate, String endDate) {
        this.batchId   = batchId;
        this.courseId  = courseId;
        this.batchName = batchName;
        this.startDate = startDate;
        this.endDate   = endDate;
    }
}