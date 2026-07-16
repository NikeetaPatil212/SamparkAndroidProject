package com.example.androidproject.utils;

import com.example.androidproject.model.AbortInquiryRequest;
import com.example.androidproject.model.AbortInquiryResponse;
import com.example.androidproject.model.AddReceiptRequest;
import com.example.androidproject.model.AddReceiptResponse;
import com.example.androidproject.model.AddStudentRequest;
import com.example.androidproject.model.AdmissionRequest;
import com.example.androidproject.model.AdmissionResponse;
import com.example.androidproject.model.BatchRequest;
import com.example.androidproject.model.BatchResponse;
import com.example.androidproject.model.CancelAdmissionRequest;
import com.example.androidproject.model.CancelAdmissionResponse;
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
import com.example.androidproject.model.TransactionListRequest;
import com.example.androidproject.model.TransactionListResponse;
import com.example.androidproject.model.UpdateStudentRequest;
import com.example.androidproject.model.UpdateTransactionRequest;
import com.example.androidproject.model.UpdateTransactionResponse;
import com.example.androidproject.model.certificate.BirthdayRequest;
import com.example.androidproject.model.certificate.BirthdayResponse;
import com.example.androidproject.model.certificate.CertificateRequest;
import com.example.androidproject.model.certificate.CertificateResponse;
import com.example.androidproject.model.certificate.UpdateCertificateRequest;
import com.example.androidproject.model.certificate.UpdateCertificateResponse;
import com.example.androidproject.model.certificate.UploadImageResponse;
import com.example.androidproject.model.course.BatchAddRequest;
import com.example.androidproject.model.course.BatchAddResponse;
import com.example.androidproject.model.course.BatchTimeAddRequest;
import com.example.androidproject.model.course.BatchTimeAddResponse;
import com.example.androidproject.model.course.BatchTimeListRequest;
import com.example.androidproject.model.course.BatchTimeListResponse;
import com.example.androidproject.model.course.CourseRequest;
import com.example.androidproject.model.course.CourseResponse;
import com.example.androidproject.model.dashboard.DashboardCardsResponse;
import com.example.androidproject.model.dashboard.DashboardChartsResponse;
import com.example.androidproject.model.dashboard.DashboardRequest;
import com.example.androidproject.model.notification.NotificationStudentRequest;
import com.example.androidproject.model.notification.NotificationStudentResponse;
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
import com.example.androidproject.model.queue.SmsQueueRequest;
import com.example.androidproject.model.queue.SmsQueueResponse;
import com.example.androidproject.model.queue.WhatsAppQueueRequest;
import com.example.androidproject.model.queue.WhatsAppQueueResponse;

import com.example.androidproject.model.summary.AdmissionReportRequest;
import com.example.androidproject.model.summary.AdmissionReportResponse;
import com.example.androidproject.model.summary.AdmissionSummaryRequest;
import com.example.androidproject.model.summary.AdmissionSummaryResponse;
import com.example.androidproject.model.summary.AttendanceReportRequest;
import com.example.androidproject.model.summary.AttendanceReportResponse;
import com.example.androidproject.model.summary.CertificateReportRequest;
import com.example.androidproject.model.summary.CertificateReportResponse;
import com.example.androidproject.model.summary.CollectionSummaryRequest;
import com.example.androidproject.model.summary.CollectionSummaryResponse;
import com.example.androidproject.model.summary.FeeOutstandingRequest;
import com.example.androidproject.model.summary.FeeOutstandingResponse;
import com.example.androidproject.model.summary.InquiryReportRequest;
import com.example.androidproject.model.summary.InquiryReportResponse;
import com.example.androidproject.model.summary.InquirySummaryRequest;
import com.example.androidproject.model.summary.InquirySummaryResponse;
import com.example.androidproject.model.summary.OutstandingRequest;
import com.example.androidproject.model.summary.OutstandingResponse;
import com.example.androidproject.model.summary.StudyMaterialReportRequest;
import com.example.androidproject.model.summary.StudyMaterialReportResponse;
import com.example.androidproject.model.template.InstituteProfileResponse;
import com.example.androidproject.model.template.InstituteRequest;
import com.example.androidproject.model.template.SettingsResponse;
import com.example.androidproject.model.template.TemplateRequest;
import com.example.androidproject.model.template.TemplateResponse;

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

    @POST("templates")
    Call<TemplateResponse> getTemplates(@Body TemplateRequest request);

    @POST("InstituteProfile")
    Call<InstituteProfileResponse> getInstituteProfile(@Body InstituteRequest request);

    @POST("get_Settings")
    Call<SettingsResponse> getSettings(@Body InstituteRequest request);

    @POST("WhatsApp_que_bulk")
    Call<WhatsAppQueueResponse> sendWhatsAppQueue(@Body WhatsAppQueueRequest request);

    @POST("Sms_que_bulk")
    Call<SmsQueueResponse> sendSmsQueue(@Body SmsQueueRequest request);

    @POST("Get_Certificate_Students")
    Call<CertificateResponse> getCertificateStudents(@Body CertificateRequest request);

    @POST("UpdateCertificate")
    Call<UpdateCertificateResponse> updateCertificate(@Body UpdateCertificateRequest request);

    @Multipart
    @POST("Upload_Certificate_Image")
    Call<UploadImageResponse> uploadCertificateImage(@Part MultipartBody.Part file);

    @POST("BirthDayList")
    Call<BirthdayResponse> getBirthdayList(@Body BirthdayRequest request);

    @POST("NotificationStudents")
    Call<NotificationStudentResponse> getNotificationStudents(@Body NotificationStudentRequest request);

    @POST("GetInquirySummary")
    Call<InquirySummaryResponse> getInquirySummary(@Body InquirySummaryRequest request);

    @POST("GetInquiryReport")
    Call<InquiryReportResponse> getInquiryReport(@Body InquiryReportRequest request);

    @POST("OutstandingSummary")
    Call<FeeOutstandingResponse> getOutstandingSummary(@Body FeeOutstandingRequest request);

    @POST("OutstandingDetail")
    Call<OutstandingResponse> getOutstandingDetail(@Body OutstandingRequest request);

    @POST("CollectionSummary")
    Call<CollectionSummaryResponse> getCollectionSummary(@Body CollectionSummaryRequest request);

    @POST("AdmissionReportDetail")
    Call<AdmissionReportResponse> getAdmissionReport(@Body AdmissionReportRequest request);

    @POST("AttendanceReport")
    Call<AttendanceReportResponse> getAttendanceReport(@Body AttendanceReportRequest request);

    @POST("CertificateReport")
    Call<CertificateReportResponse> getCertificateReport(@Body CertificateReportRequest request);

    @POST("StudyMaterialReport")
    Call<StudyMaterialReportResponse> getStudyMaterialReport(@Body StudyMaterialReportRequest request);

    @POST("abort_Inquiry")
    Call<AbortInquiryResponse> abortInquiry(@Body AbortInquiryRequest request);

    @POST("Add_Courses")
    Call<CourseResponse> saveCourse(@Body CourseRequest request);

    @POST("Add_Batch")
    Call<BatchAddResponse> addBatch(@Body BatchAddRequest request);

    @POST("Add_BatchTime")
    Call<BatchTimeAddResponse> addBatchTime(@Body BatchTimeAddRequest request);

    @POST("batch_time_test")
    Call<BatchTimeListResponse> getBatchTimes(@Body BatchTimeListRequest request);

    @POST("AdmissionSummary")
    Call<AdmissionSummaryResponse> getAdmissionSummary(@Body AdmissionSummaryRequest request);

    @POST("TransactionList")
    Call<TransactionListResponse> getTransactionList(@Body TransactionListRequest request);

    @POST("UpdateTransaction")
    Call<UpdateTransactionResponse> updateTransaction(@Body UpdateTransactionRequest request);

    @POST("CancelAdmission")
    Call<CancelAdmissionResponse> cancelAdmission(@Body CancelAdmissionRequest request);

    @POST("DashboardCards")
    Call<DashboardCardsResponse> getDashboardCards(@Body DashboardRequest request);

    @POST("DashboardCharts")
    Call<DashboardChartsResponse> getDashboardCharts(@Body DashboardRequest request);
}
