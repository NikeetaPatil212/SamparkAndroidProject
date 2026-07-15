package com.example.androidproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapters.DrawerAdapter;
import com.example.androidproject.utils.NavMenuItem;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    private boolean isInquiryExpanded = false;
    private boolean isAdmissionExpanded = false;
    private boolean isCertificateExpanded = false;
    private boolean isNotificationExpanded = false;
    private boolean isReportsExpanded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(DashboardActivity.this);


        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                collapseAllMenus();
            }
        });
    }

    private void collapseAllMenus() {
        Menu menu = navigationView.getMenu();

        if (isInquiryExpanded) {
            isInquiryExpanded = false;
            menu.findItem(R.id.nav_add_inquiry).setVisible(false);
            menu.findItem(R.id.nav_get_inquiry).setVisible(false);
            menu.findItem(R.id.nav_batch).setVisible(false);
            menu.findItem(R.id.nav_summary_report).setVisible(false);
            menu.findItem(R.id.nav_detailed_report).setVisible(false);
            menu.findItem(R.id.nav_inquiry).setTitle("Inquiry");
        }

        if (isAdmissionExpanded) {
            isAdmissionExpanded = false;
            menu.findItem(R.id.nav_add_admission).setVisible(false);
            menu.findItem(R.id.nav_get_admission).setVisible(false);
            menu.findItem(R.id.nav_get_timeAllot).setVisible(false);
            menu.findItem(R.id.nav_change_timeAllot).setVisible(false);
            menu.findItem(R.id.nav_distribute_study_material).setVisible(false);
            menu.findItem(R.id.nav_birthdate).setVisible(false);
            menu.findItem(R.id.nav_notification_reminder).setVisible(false);
            menu.findItem(R.id.nav_add_summary_report).setVisible(false);
            menu.findItem(R.id.nav_edit_delete).setVisible(false);
            menu.findItem(R.id.nav_admissions).setTitle("Admissions");
        }

        if(isCertificateExpanded){
            isCertificateExpanded = false;
            menu.findItem(R.id.nav_certificate_handover).setVisible(false);
            menu.findItem(R.id.nav_certificates).setTitle("Certificates");
        }

        if(isReportsExpanded){
            isReportsExpanded = false;
            menu.findItem(R.id.nav_reports_fees).setVisible(false);
            menu.findItem(R.id.nav_outstanding_report).setVisible(false);
            menu.findItem(R.id.nav_collection_summary).setVisible(false);
            menu.findItem(R.id.nav_admission_report).setVisible(false);
            menu.findItem(R.id.nav_attendance_report).setVisible(false);
            menu.findItem(R.id.nav_certificate_report).setVisible(false);
            menu.findItem(R.id.nav_study_material_report).setVisible(false);
        //    menu.findItem(R.id.nav_transaction_report).setVisible(false);
            menu.findItem(R.id.nav_reports).setTitle("Reports");
        }


        if(isNotificationExpanded){
            isNotificationExpanded = false;
            menu.findItem(R.id.nav_notification_reminder).setVisible(false);
            menu.findItem(R.id.nav_birthdate).setVisible(false);
            menu.findItem(R.id.nav_notifications).setTitle("Notification");
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // ── INQUIRY expand/collapse ──────────────────────────────
        if (id == R.id.nav_inquiry) {
            isInquiryExpanded = !isInquiryExpanded;

            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_add_inquiry).setVisible(isInquiryExpanded);
            menu.findItem(R.id.nav_get_inquiry).setVisible(isInquiryExpanded);
            menu.findItem(R.id.nav_batch).setVisible(isInquiryExpanded);
            menu.findItem(R.id.nav_summary_report).setVisible(isInquiryExpanded);
            menu.findItem(R.id.nav_detailed_report).setVisible(isInquiryExpanded);

            item.setTitle(isInquiryExpanded ? "Inquiry  ▲" : "Inquiry  ▼");

            styleSubItem(menu.findItem(R.id.nav_add_inquiry), "Add Inquiry");
            styleSubItem(menu.findItem(R.id.nav_get_inquiry), "Get Inquiry");
            styleSubItem(menu.findItem(R.id.nav_batch),       "Get Batch");
            styleSubItem(menu.findItem(R.id.nav_summary_report),       "Summary Report");
            styleSubItem(menu.findItem(R.id.nav_detailed_report),       "Detailed Report");
            return true;
        }

        if (id == R.id.nav_certificates) {
            isCertificateExpanded = !isCertificateExpanded;

            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_certificate_handover).setVisible(isCertificateExpanded);

            item.setTitle(isCertificateExpanded ? "Certificates  ▲" : "Certificates  ▼");
            styleSubItem(menu.findItem(R.id.nav_certificate_handover),       "Certificate Handover");
            return true;
        }

        if (id == R.id.nav_notifications) {
            isNotificationExpanded = !isNotificationExpanded;

            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_notification_reminder).setVisible(isNotificationExpanded);
            menu.findItem(R.id.nav_birthdate).setVisible(isNotificationExpanded);

            item.setTitle(isNotificationExpanded ? "Notifications  ▲" : "Notifications  ▼");
            styleSubItem(menu.findItem(R.id.nav_notification_reminder),       "New Notification");
            styleSubItem(menu.findItem(R.id.nav_birthdate),       "BirthDay Reminder");
            return true;
        }

        if (id == R.id.nav_reports) {
            isReportsExpanded = !isReportsExpanded;

            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_reports_fees).setVisible(isReportsExpanded);
            menu.findItem(R.id.nav_outstanding_report).setVisible(isReportsExpanded);
            menu.findItem(R.id.nav_collection_summary).setVisible(isReportsExpanded);
            menu.findItem(R.id.nav_admission_report).setVisible(isReportsExpanded);
            menu.findItem(R.id.nav_attendance_report).setVisible(isReportsExpanded);
            menu.findItem(R.id.nav_certificate_report).setVisible(isReportsExpanded);
            menu.findItem(R.id.nav_study_material_report).setVisible(isReportsExpanded);
        //    menu.findItem(R.id.nav_transaction_report).setVisible(isReportsExpanded);

            item.setTitle(isReportsExpanded ? "Reports  ▲" : "Reports  ▼");
            styleSubItem(menu.findItem(R.id.nav_reports_fees),       "Summary Report");
            styleSubItem(menu.findItem(R.id.nav_outstanding_report),       "Outstanding Detailed Report");
            styleSubItem(menu.findItem(R.id.nav_collection_summary),       "Fees Collection Summary");
            styleSubItem(menu.findItem(R.id.nav_admission_report),       "Admission Report");
            styleSubItem(menu.findItem(R.id.nav_attendance_report),       "Attendance Report");
            styleSubItem(menu.findItem(R.id.nav_certificate_report),       "Certificate Handover Report");
            styleSubItem(menu.findItem(R.id.nav_study_material_report),       "Study Material Report");
        //    styleSubItem(menu.findItem(R.id.nav_transaction_report),       "Expenses Report");
            return true;
        }

        // ── INQUIRY sub-items ────────────────────────────────────
        if (id == R.id.nav_add_inquiry) {
            startActivity(new Intent(this, InquiryActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_get_inquiry) {
            startActivity(new Intent(this, GetInquiryActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_summary_report) {
            startActivity(new Intent(this, SummaryReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_detailed_report) {
            startActivity(new Intent(this, DetailedReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // ── ADMISSIONS expand/collapse ───────────────────────────
        if (id == R.id.nav_admissions) {
            isAdmissionExpanded = !isAdmissionExpanded;

            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_add_admission).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_get_admission).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_get_timeAllot).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_change_timeAllot).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_distribute_study_material).setVisible(isAdmissionExpanded);
        //    menu.findItem(R.id.nav_certificate_handover).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_birthdate).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_notification_reminder).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_add_summary_report).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_edit_delete).setVisible(isAdmissionExpanded);

            item.setTitle(isAdmissionExpanded ? "Admissions  ▲" : "Admissions  ▼");

            styleSubItem(menu.findItem(R.id.nav_add_admission), "New Admission");
            styleSubItem(menu.findItem(R.id.nav_get_admission), "Manage Admission");
            styleSubItem(menu.findItem(R.id.nav_get_timeAllot), "Allot Batch Timing");
            styleSubItem(menu.findItem(R.id.nav_change_timeAllot), "Change Batch Timing");
            styleSubItem(menu.findItem(R.id.nav_distribute_study_material), "Distribute Study Material");
            styleSubItem(menu.findItem(R.id.nav_add_summary_report), "Summary Report");
            styleSubItem(menu.findItem(R.id.nav_edit_delete), "Edit Delete Transaction");
         //   styleSubItem(menu.findItem(R.id.nav_certificate_handover), "Certificate Handover");
        //    styleSubItem(menu.findItem(R.id.nav_birthdate), "Birthdate Reminder");
        //    styleSubItem(menu.findItem(R.id.nav_notification_reminder), "Notification Reminder");
            return true;
        }

        // ── ADMISSIONS sub-items ─────────────────────────────────
        if (id == R.id.nav_add_admission) {
            startActivity(new Intent(this, NewAdmissionActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_get_admission) {
            startActivity(new Intent(this, GetAdmissionActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_get_timeAllot) {
            startActivity(new Intent(this, AllotBatchActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_change_timeAllot) {
            Intent intent = new Intent(this, AllotBatchActivity.class);
            intent.putExtra(AllotBatchActivity.EXTRA_MODE, AllotBatchActivity.MODE_CHANGE);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }


        if (id == R.id.nav_distribute_study_material) {
            startActivity(new Intent(this, DistributeStudyMaterialActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_add_summary_report) {
            startActivity(new Intent(this, AdmissionSummaryReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

      /*  if (id == R.id.nav_edit_delete) {
            startActivity(new Intent(this, EditDeleteTransactionActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }*/


        if (id == R.id.nav_certificate_handover) {
            startActivity(new Intent(this, CertificateHandOverActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_birthdate) {
            startActivity(new Intent(this, BirthdayReminderActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_notification_reminder) {
            startActivity(new Intent(this, NotificationReminderActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_reports_fees) {
            startActivity(new Intent(this, OuStandingSummaryReport.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_outstanding_report) {
            startActivity(new Intent(this, OutStandingDetailedActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_collection_summary) {
            startActivity(new Intent(this, FeesCollectionSummaryActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_admission_report) {
            startActivity(new Intent(this, AdmissionReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_attendance_report) {
            startActivity(new Intent(this, AttendanceReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_certificate_report) {
            startActivity(new Intent(this, CertificateReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }


        if (id == R.id.nav_study_material_report) {
            startActivity(new Intent(this, StudyMaterialReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }


        /*if (id == R.id.nav_transaction_report) {
            startActivity(new Intent(this, ExpenseReportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }*/
        // ── OTHER items ──────────────────────────────────────────

        if (id == R.id.nav_courses) {
            startActivity(new Intent(this, CourseActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_batch) {
            startActivity(new Intent(this, BatchActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_batch_time) {
            startActivity(new Intent(this, BatchTimeActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_bulk) {
        }else if (id == R.id.nav_dashboard) {
        } else if (id == R.id.nav_templates) {
        } else if (id == R.id.nav_institute) {
        } else if (id == R.id.nav_data) {
        } else if (id == R.id.nav_settings) {
        } else if (id == R.id.nav_logout) {
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ── Sub-item: indented + smaller + grey — clearly different from main items
    private void styleSubItem(MenuItem menuItem, String label) {
        SpannableString s = new SpannableString("        ⤷  " + label);
        s.setSpan(new ForegroundColorSpan(0xFF757575), 0, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(0.88f), 0, s.length(), 0);
        menuItem.setTitle(s);
    }
}