package com.example.androidproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private static final String PREF_NAME = "app_prefs";

    // Keys
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_INSTITUTE_ID = "key_institute_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_OPERATOR_ID = "operator_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_COURSE_ID = "course_id";
    private static final String KEY_BATCH_ID = "batch_id";

    // Student Keys
    private static final String KEY_STUDENT_NAME = "student_name";
    private static final String KEY_STUDENT_MOBILE = "student_mobile";
    private static final String KEY_STUDENT_EMAIL = "student_email";
    private static final String KEY_STUDENT_ADDRESS = "student_address";

    // Institute Keys
    private static final String KEY_INSTITUTE_NAME = "institute_name";
    private static final String KEY_INSTITUTE_MOBILE1 = "institute_mobile1";
    private static final String KEY_INSTITUTE_MOBILE2 = "institute_mobile2";
    private static final String KEY_INSTITUTE_EMAIL = "institute_email";
    private static final String KEY_INSTITUTE_ADDRESS1 = "institute_address1";
    private static final String KEY_INSTITUTE_ADDRESS2 = "institute_address2";
    private static final String KEY_INSTITUTE_OWNER_NAME = "institute_ownerName";
    private static final String KEY_LAST_INSTITUTE_NAME = "last_institute_name";

    // App Preferences
    private static final String KEY_APP_LANGUAGE = "app_language";
    private static final String KEY_PROFILE_IMAGE = "KEY_PROFILE_IMAGE";

    // Authentication / Remember Me Keys
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_REMEMBER_PHONE = "remember_phone";
    private static final String KEY_REMEMBER_USERNAME = "remember_username";
    private static final String KEY_REMEMBER_PASSWORD = "remember_password";
    private static final String KEY_LAST_PHONE = "last_phone";
    private static final String KEY_LAST_USERNAME = "last_username";

    private static PrefManager instance;
    private final SharedPreferences prefs;

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

    // ── User Session ──────────────────────────────────────────────
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

    public void saveUserRole(String role) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply();
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "");
    }

    public void saveOperatorId(String operatorId) {
        prefs.edit().putString(KEY_OPERATOR_ID, operatorId).apply();
    }

    public String getOperatorId() {
        return prefs.getString(KEY_OPERATOR_ID, "");
    }

    public void saveUserName(String userName) {
        prefs.edit().putString(KEY_USER_NAME, userName).apply();
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

    // ── Student Details ───────────────────────────────────────────
    public void saveStudentName(String studentName) {
        prefs.edit().putString(KEY_STUDENT_NAME, studentName).apply();
    }

    public String getStudentName() {
        return prefs.getString(KEY_STUDENT_NAME, "");
    }

    public void saveStudentMobile(String mobile) {
        prefs.edit().putString(KEY_STUDENT_MOBILE, mobile).apply();
    }

    public String getStudentMobile() {
        return prefs.getString(KEY_STUDENT_MOBILE, "");
    }

    public void saveStudentEmail(String email) {
        prefs.edit().putString(KEY_STUDENT_EMAIL, email).apply();
    }

    public String getStudentEmail() {
        return prefs.getString(KEY_STUDENT_EMAIL, "");
    }

    public void saveStudentAddress(String address) {
        prefs.edit().putString(KEY_STUDENT_ADDRESS, address).apply();
    }

    public String getStudentAddress() {
        return prefs.getString(KEY_STUDENT_ADDRESS, "");
    }

    // ── Institute Details ─────────────────────────────────────────
    public void saveInstituteName(String instituteName) {
        prefs.edit().putString(KEY_INSTITUTE_NAME, instituteName).apply();
    }

    public String getInstituteName() {
        return prefs.getString(KEY_INSTITUTE_NAME, "");
    }

    public void saveInstituteProfile(String name, String mobile1, String mobile2,
                                     String email, String address1, String address2, String ownerName) {
        prefs.edit()
                .putString(KEY_INSTITUTE_NAME, name)
                .putString(KEY_INSTITUTE_MOBILE1, mobile1)
                .putString(KEY_INSTITUTE_MOBILE2, mobile2)
                .putString(KEY_INSTITUTE_EMAIL, email)
                .putString(KEY_INSTITUTE_ADDRESS1, address1)
                .putString(KEY_INSTITUTE_ADDRESS2, address2)
                .putString(KEY_INSTITUTE_OWNER_NAME, ownerName)
                .apply();
    }

    public String getInstituteMobile1() { return prefs.getString(KEY_INSTITUTE_MOBILE1, ""); }
    public String getInstituteMobile2() { return prefs.getString(KEY_INSTITUTE_MOBILE2, ""); }
    public String getInstituteEmail()   { return prefs.getString(KEY_INSTITUTE_EMAIL, ""); }
    public String getInstituteAddress1(){ return prefs.getString(KEY_INSTITUTE_ADDRESS1, ""); }
    public String getInstituteAddress2(){ return prefs.getString(KEY_INSTITUTE_ADDRESS2, ""); }
    public String getOwnerName()        { return prefs.getString(KEY_INSTITUTE_OWNER_NAME, ""); }

    public void saveLastInstituteName(String instituteName) {
        prefs.edit().putString(KEY_LAST_INSTITUTE_NAME, instituteName).apply();
    }

    public String getLastInstituteName() {
        return prefs.getString(KEY_LAST_INSTITUTE_NAME, "");
    }

    // ── Preferences & Settings ────────────────────────────────────
    public void saveLanguage(String language) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language).apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_APP_LANGUAGE, "EN");
    }

    public void saveProfileImage(String url) {
        prefs.edit().putString(KEY_PROFILE_IMAGE, url).apply();
    }

    public String getProfileImage() {
        return prefs.getString(KEY_PROFILE_IMAGE, "");
    }



    // ── Authentication & Remember Me ──────────────────────────────
    public void setRememberMe(boolean remember) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void saveLoginCredentials(String phone, String username, String password) {
        prefs.edit()
                .putString(KEY_REMEMBER_PHONE, phone)
                .putString(KEY_REMEMBER_USERNAME, username)
                .putString(KEY_REMEMBER_PASSWORD, password)
                .apply();
    }

    public String getRememberPhone() {
        return prefs.getString(KEY_REMEMBER_PHONE, "");
    }

    public String getRememberUsername() {
        return prefs.getString(KEY_REMEMBER_USERNAME, "");
    }

    public String getRememberPassword() {
        return prefs.getString(KEY_REMEMBER_PASSWORD, "");
    }

    public void saveLastPhone(String phone) {
        prefs.edit().putString(KEY_LAST_PHONE, phone).apply();
    }

    public String getLastPhone() {
        return prefs.getString(KEY_LAST_PHONE, "");
    }

    public void saveLastUsername(String username) {
        prefs.edit().putString(KEY_LAST_USERNAME, username).apply();
    }

    public String getLastUsername() {
        return prefs.getString(KEY_LAST_USERNAME, "");
    }

    public void savePassword(String password) {
        prefs.edit().putString(KEY_REMEMBER_PASSWORD, password).apply();
    }

    public String getPassword() {
        return prefs.getString(KEY_REMEMBER_PASSWORD, "");
    }

    public void clearPassword() {
        prefs.edit().remove(KEY_REMEMBER_PASSWORD).apply();
    }

    public void clearRememberMe() {
        prefs.edit()
                .remove(KEY_REMEMBER_PHONE)
                .remove(KEY_REMEMBER_USERNAME)
                .remove(KEY_REMEMBER_PASSWORD)
                .putBoolean(KEY_REMEMBER_ME, false)
                .apply();
    }

    // ── Cleanup ───────────────────────────────────────────────────
    public void clearSession() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_ROLE)
                .remove(KEY_OPERATOR_ID)
                .apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}