package com.example.androidproject.utils;

import com.example.androidproject.model.AddReceiptRequest;
import com.example.androidproject.model.AddReceiptResponse;
import com.example.androidproject.model.AddStudentRequest;
import com.example.androidproject.model.AdmissionRequest;
import com.example.androidproject.model.AdmissionResponse;
import com.example.androidproject.model.BatchRequest;
import com.example.androidproject.model.BatchResponse;
import com.example.androidproject.model.ExtendInquiryRequest;
import com.example.androidproject.model.FeeReceiptRequest;
import com.example.androidproject.model.FeeReceiptResponse;
import com.example.androidproject.model.GetAdmissionRequest;
import com.example.androidproject.model.GetAdmissionResponse;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.model.ImageUploadResponse;
import com.example.androidproject.model.InquiryListRequest;
import com.example.androidproject.model.InquiryListResponse;
import com.example.androidproject.model.InquiryRequest;
import com.example.androidproject.model.InquiryResponse;
import com.example.androidproject.model.LoginRequest;
import com.example.androidproject.model.LoginResponse;
import com.example.androidproject.model.MobileRequest;
import com.example.androidproject.model.MobileResponse;
import com.example.androidproject.model.StudentBasicRequest;
import com.example.androidproject.model.StudentBasicResponse;
import com.example.androidproject.model.SuggestReceiptRequest;
import com.example.androidproject.model.SuggestReceiptResponse;
import com.example.androidproject.model.UpdateStudentRequest;
import com.example.androidproject.model.profile.EditProfileRequest;
import com.example.androidproject.model.profile.EditProfileResponse;
import com.example.androidproject.model.profile.ProfileDetailsRequest;
import com.example.androidproject.model.profile.ProfileDetailsResponse;
import com.example.androidproject.model.profile.StudentDetailsRequest;
import com.example.androidproject.model.profile.StudentDetailsResponse;
import com.example.androidproject.model.profile.StudyMaterialDistributionResponse;
import com.example.androidproject.model.profile.StudyMaterialUpdateRequest;
import com.example.androidproject.model.profile.StudyMaterialUpdateResponse;
import com.example.androidproject.model.profile.TimingLessStudentResponse;
import com.example.androidproject.model.profile.WithTimeStudentResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface ApiService {

    @POST("UserLogin")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("Add_Inquiry")
    Call<InquiryResponse> addInquiry(@Body InquiryRequest inquiryRequest);

    @POST("Get_Institutes")
    Call<MobileResponse> getInstitute(@Body MobileRequest request);

    @POST("Get_Courses")
    Call<GetCoursesResponse> getCourses(@Body GetCoursesRequest request);

    @POST("Get_Inquiry")
    Call<InquiryListResponse> getInquiryList(@Body InquiryListRequest request);

    @POST("Get_Batch")
    Call<BatchResponse> getBatch(@Body BatchRequest request);

    @POST("Add_Admission")
    Call<AdmissionResponse> getAdmission(@Body AdmissionRequest admissionRequest);

    @Multipart
    @POST("Updalod_Image")
    Call<ImageUploadResponse> uploadImage(
            @Part MultipartBody.Part file
    );
    @POST("Extend_Inquiry")
    Call<InquiryResponse> extendInquiry(@Body ExtendInquiryRequest request);

    @POST("Update Student")
    Call<InquiryResponse> updateStudent(@Body UpdateStudentRequest request);

    @POST("Add_Student")
    Call<InquiryResponse> addStudent(@Body AddStudentRequest request);

    @POST("Receipt_Form")
    Call<FeeReceiptResponse> getReceiptRecord(@Body FeeReceiptRequest request);

    @POST("Get_Admissions")
    Call<GetAdmissionResponse> getAdmissionDetails(@Body GetAdmissionRequest request);

    @POST("Suggest_ReceiptNo")
    Call<SuggestReceiptResponse> getSuggestedReceipt(@Body SuggestReceiptRequest request);

    @POST("Add_Receipt")
    Call<AddReceiptResponse> addReceipt(@Body AddReceiptRequest request);

    @POST("Get_ProfileDetails")
    Call<ProfileDetailsResponse> getProfileDetails(@Body ProfileDetailsRequest request);

    @POST("get_TimingLess")
    Call<TimingLessStudentResponse> getBatchAllotment(@Body StudentBasicRequest request);

    @POST("Get_Student_Basic")
    Call<StudentDetailsResponse> getStudentBasicDetails(@Body StudentDetailsRequest request);

    @POST("edit_profile")
    Call<EditProfileResponse> updateStudentProfile(@Body EditProfileRequest request);

    @POST("with_time")
    Call<WithTimeStudentResponse> getStudentsWithTime(@Body StudentBasicRequest request);
    @GET("list_of_distribution")
    Call<StudyMaterialDistributionResponse> getDistributionList(
            @Query("userID")     int userID,
            @Query("instituteID") int instituteID,
            @Query("CourseID")   int courseID,
            @Query("batchID")    int batchID
    );

    @POST("Study_Material")
    Call<StudyMaterialUpdateResponse> updateStudyMaterial(@Body StudyMaterialUpdateRequest request);
}
