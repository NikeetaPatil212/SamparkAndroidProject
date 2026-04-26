package com.example.androidproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.androidproject.model.ImageUploadResponse;
import com.example.androidproject.model.profile.EditProfileRequest;
import com.example.androidproject.model.profile.EditProfileResponse;
import com.example.androidproject.model.profile.StudentDetailsRequest;
import com.example.androidproject.model.profile.StudentDetailsResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditStudentProfileActivity extends AppCompatActivity {

    // ── Fields ─────────────────────────────────────────────────────────────────
    private EditText etFullName, etMobile, etAltMobile, etEmail,
            etAddress, etDob, etGender, etSchool;

    private ImageView ivStudent;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private FrameLayout loaderLayout;

    // ── State ──────────────────────────────────────────────────────────────────
    private String uploadedImageUrl = ""; // holds URL after successful upload

    // ── Camera constants ───────────────────────────────────────────────────────
    private static final int CAMERA_PERMISSION_CODE = 201;
    private static final int CAMERA_REQUEST_CODE    = 202;
    private static final int GALLERY_REQUEST_CODE   = 203;
    private Uri imageUri; // URI of photo captured by camera

    // ── Photo source choice dialog options ─────────────────────────────────────
    private static final int SOURCE_CAMERA  = 0;
    private static final int SOURCE_GALLERY = 1;
    private int studentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_profile);

        initViews();

        studentID   = getIntent().getIntExtra("studentId",   -1);
        Log.d("EditStudentProfile", "onCreate: studentID=" + studentID);

        if (studentID == -1) {
            Toast.makeText(this, "Invalid Student ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchStudentDetails();
    }

    // ── Bind views and set listeners ───────────────────────────────────────────
    private void initViews() {
        ivStudent   = findViewById(R.id.ivStudent);
        etFullName  = findViewById(R.id.etFullName);
        etMobile    = findViewById(R.id.etMobile);
        etAltMobile = findViewById(R.id.etAltMobile);
        etEmail     = findViewById(R.id.etEmail);
        etAddress   = findViewById(R.id.etAddress);
        etDob       = findViewById(R.id.etDob);
        etGender    = findViewById(R.id.etGender);
        etSchool    = findViewById(R.id.etSchool);
        btnSave     = findViewById(R.id.btnSave);
        btnBack     = findViewById(R.id.btnBack);
        loaderLayout= findViewById(R.id.loaderLayout);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());

        // ── DOB: tap opens DatePickerDialog ────────────────────────────────────
        etDob.setFocusable(false);
        etDob.setClickable(true);
        etDob.setOnClickListener(v -> openDobDatePicker());

        // ── Photo: tap opens Camera / Gallery choice ───────────────────────────
        ivStudent.setOnClickListener(v -> showPhotoSourceDialog());
    }

    // ── DOB date picker ────────────────────────────────────────────────────────
    private void openDobDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Try to pre-select current DOB value if already set
        String currentDob = etDob.getText().toString().trim();
        if (!currentDob.isEmpty()) {
            try {
                // Support both yyyy/MM/dd and yyyy-MM-dd from API
                String normalised = currentDob.replace("-", "/");
                String[] parts = normalised.split("/");
                if (parts.length == 3) {
                    calendar.set(Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[2]));
                }
            } catch (Exception ignored) {}
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Format as yyyy/MM/dd to match API
                    String selected = String.format(Locale.getDefault(),
                            "%04d/%02d/%02d", year, month + 1, dayOfMonth);
                    etDob.setText(selected);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // DOB cannot be in the future
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.setTitle("Select Date of Birth");
        dialog.show();
    }

    // ── Photo source — Camera or Gallery ──────────────────────────────────────
    private void showPhotoSourceDialog() {
        String[] options = {"📷  Take Photo", "🖼️  Choose from Gallery"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Update Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == SOURCE_CAMERA) {
                        checkCameraPermissionAndOpen();
                    } else {
                        openGallery();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Camera permission check ────────────────────────────────────────────────
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── Open camera ────────────────────────────────────────────────────────────
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Profile Photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Captured by Camera");
        imageUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    // ── Open gallery ───────────────────────────────────────────────────────────
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    // ── Handle camera / gallery result ────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        Uri selectedUri = null;

        if (requestCode == CAMERA_REQUEST_CODE) {
            // Camera saves to imageUri we passed in
            selectedUri = imageUri;

        } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
            selectedUri = data.getData();
            imageUri = selectedUri; // keep reference for upload
        }

        if (selectedUri != null) {
            // Show preview immediately
            Glide.with(this)
                    .load(selectedUri)
                    .placeholder(R.drawable.ic_person_foreground)
                    .circleCrop()
                    .into(ivStudent);

            // Upload to server
            try {
                File file = uriToFile(selectedUri);
                uploadImage(file);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Image processing error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── Convert URI → File (same as AdmissionActivity) ────────────────────────
    private File uriToFile(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File file = new File(getCacheDir(), "profile_upload.jpg");
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return file;
    }

    // ── Upload image (same pattern as AdmissionActivity) ──────────────────────
    private void uploadImage(File imageFile) {
        loaderLayout.setVisibility(View.VISIBLE);

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/jpeg"), imageFile);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        RetrofitClient.getApiService().uploadImage(body)
                .enqueue(new Callback<ImageUploadResponse>() {

                    @Override
                    public void onResponse(Call<ImageUploadResponse> call,
                                           Response<ImageUploadResponse> response) {
                        loaderLayout.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ImageUploadResponse res = response.body();
                            Log.d("UPLOAD_RESPONSE", new Gson().toJson(res));

                            if (res.isSuccess()) {
                                uploadedImageUrl = res.getImageUrl();
                                Toast.makeText(EditStudentProfileActivity.this,
                                        "Photo uploaded successfully ✓",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("IMAGE_URL", uploadedImageUrl);
                            } else {
                                Toast.makeText(EditStudentProfileActivity.this,
                                        "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditStudentProfileActivity.this,
                                    "Server error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(EditStudentProfileActivity.this,
                                "Image upload failed", Toast.LENGTH_SHORT).show();
                        Log.e("UPLOAD_ERROR", t.getMessage());
                    }
                });
    }

    // ── Save profile (original logic, now passes uploadedImageUrl) ─────────────
    private void saveProfile() {
        String fullName  = etFullName.getText().toString().trim();
        String mobile    = etMobile.getText().toString().trim();
        String altMobile = etAltMobile.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String address   = etAddress.getText().toString().trim();
        String dob       = etDob.getText().toString().trim();
        String gender    = etGender.getText().toString().trim();
        String school    = etSchool.getText().toString().trim();

        if (fullName.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this,
                    "Name and Mobile are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Split full name into first / middle / last
        String[] parts = fullName.split(" ");
        String fName = parts.length > 0 ? parts[0] : "";
        String mName = parts.length > 1 ? parts[1] : "";
        String lName = parts.length > 2 ? parts[2] : "";

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();
        String operatorId = PrefManager.getInstance(this).getOperatorId();

        EditProfileRequest request = new EditProfileRequest(
                fName,
                mName,
                lName,
                fullName,
                mobile,
                altMobile,
                email,
                address,
                dob,
                gender,
                uploadedImageUrl,
                studentID,
                Integer.parseInt(userId),
                Integer.parseInt(instituteId),
                Integer.parseInt(operatorId)
        );

        Log.d("EDIT_PROFILE_REQUEST", new Gson().toJson(request));

        loaderLayout.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService()
                .updateStudentProfile(request)
                .enqueue(new Callback<EditProfileResponse>() {

                    @Override
                    public void onResponse(Call<EditProfileResponse> call,
                                           Response<EditProfileResponse> response) {
                        loaderLayout.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditStudentProfileActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            if (response.body().isSuccess()) {
                                finish();
                            }
                        } else {
                            Toast.makeText(EditStudentProfileActivity.this,
                                    "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<EditProfileResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(EditStudentProfileActivity.this,
                                "API Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Fetch existing student details to pre-fill form ────────────────────────
    private void fetchStudentDetails() {
        loaderLayout.setVisibility(View.VISIBLE);

        String userId      = PrefManager.getInstance(this).getUserId();
        String instituteId = PrefManager.getInstance(this).getInstituteId();

        StudentDetailsRequest request = new StudentDetailsRequest(
                studentID,
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        Log.d("STUDENT_DETAILS_REQ", new Gson().toJson(request));

        RetrofitClient.getApiService()
                .getStudentBasicDetails(request)
                .enqueue(new Callback<StudentDetailsResponse>() {

                    @Override
                    public void onResponse(Call<StudentDetailsResponse> call,
                                           Response<StudentDetailsResponse> response) {
                        loaderLayout.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccesss()) {
                            setStudentData(response.body().getDetails());
                        } else {
                            Toast.makeText(EditStudentProfileActivity.this,
                                    "Failed to fetch details",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StudentDetailsResponse> call, Throwable t) {
                        loaderLayout.setVisibility(View.GONE);
                        Toast.makeText(EditStudentProfileActivity.this,
                                t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Pre-fill all fields from API response (original logic) ────────────────
    private void setStudentData(StudentDetailsResponse.Details details) {
        etFullName.setText(details.getFullName());
        etMobile.setText(details.getMobile());
        etAltMobile.setText(details.getAlternateNo());
        etEmail.setText(details.getEmail());
        etAddress.setText(details.getAddress());

        // Normalise DOB from API (yyyy-MM-dd → yyyy/MM/dd for display)
        String dob = details.getDob();
        if (dob != null && !dob.isEmpty()) {
            etDob.setText(dob.replace("-", "/"));
        }

        etGender.setText(details.getGender());
        etSchool.setText(details.getSchool());

        // Load existing profile photo
        String imgUrl = details.getImgurl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            uploadedImageUrl = imgUrl; // keep existing URL unless user picks a new one
            Glide.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_person_foreground)
                    .circleCrop()
                    .into(ivStudent);
        } else {
            ivStudent.setImageResource(R.drawable.ic_person_foreground);
        }
    }
}