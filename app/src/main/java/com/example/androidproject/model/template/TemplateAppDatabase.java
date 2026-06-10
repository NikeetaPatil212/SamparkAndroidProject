package com.example.androidproject.model.template;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Dedicated Room database for templates only.
 * Completely separate from your existing "admission-db".
 */
@Database(entities = {TemplateEntity.class}, version = 1, exportSchema = false)
public abstract class TemplateAppDatabase extends RoomDatabase {

    private static volatile TemplateAppDatabase INSTANCE;

    // ── This is the ONLY place TemplateDao is declared ────────────────────────
    public abstract TemplateDao templateDao();

    public static TemplateAppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TemplateAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TemplateAppDatabase.class,
                                    "template-db"   // separate .db file from admission-db
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}