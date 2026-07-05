package com.example.androidproject.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                AdmissionDetail.class,
                CourseEntity.class,
                BatchEntity.class
        },
        version = 5,                    // ← bumped from 3 → 4 (new columns in CourseEntity)
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AdmissionDetailDao feeDetailDao();
    public abstract CourseDao courseDao();
    public abstract BatchDao batchDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "admission-db"
                            )
                            .fallbackToDestructiveMigration() // wipes old DB on schema change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}