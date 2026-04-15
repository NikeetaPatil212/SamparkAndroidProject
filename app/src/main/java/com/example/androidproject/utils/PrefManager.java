package com.example.androidproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_INSTITUTE_ID = "key_institute_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_OPERATOR_ID = "operator_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_COURSE_ID = "course_id";
    private static final String KEY_BATCH_ID  = "batch_id";
    private static final String KEY_MOBILE_ID  = "mobile_id";


    private static PrefManager instance;
    private SharedPreferences prefs;


    private PrefManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new PrefManager(context);
        }
        return instance;
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }
    public void saveInstituteId(String instituteId) {
        prefs.edit().putString(KEY_INSTITUTE_ID, instituteId).apply();
    }

    public String getInstituteId() {
        return prefs.getString(KEY_INSTITUTE_ID, "0");
    }
    public void clear() {
        prefs.edit().clear().apply();
    }

    public void saveUserRole(String role) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply();
    }

    public void saveOperatorId(String operatorId) {
        prefs.edit().putString(KEY_OPERATOR_ID, operatorId).apply();
    }

    public void saveUserName(String userName) {
        prefs.edit().putString(KEY_USER_NAME, userName).apply();
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "");
    }

    public String getOperatorId() {
        return prefs.getString(KEY_OPERATOR_ID, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setCourseId(int courseId) {
        prefs.edit().putInt(KEY_COURSE_ID, courseId).apply();
    }

    public int getCourseId() {
        return prefs.getInt(KEY_COURSE_ID, 0);
    }

    public void setBatchId(int batchId) {
        prefs.edit().putInt(KEY_BATCH_ID, batchId).apply();
    }

    public int getBatchId() {
        return prefs.getInt(KEY_BATCH_ID, 0);
    }


   /* public void saveMobileNo(String mobileNo) {
        prefs.edit().putString(KEY_MOBILE_ID, mobileNo).apply();
    }

    public String getMobileNo() {
        return prefs.getString(KEY_MOBILE_ID, "");
    }*/


    public void setMobile(String mobile) {
        prefs.edit().putString("mobile", mobile);
        prefs.edit().apply();
    }

    public String getMobile() {
        return prefs.getString("mobile", "");
    }

}

