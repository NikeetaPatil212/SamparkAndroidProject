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
            menu.findItem(R.id.nav_inquiry).setTitle("Inquiry");
        }

        if (isAdmissionExpanded) {
            isAdmissionExpanded = false;
            menu.findItem(R.id.nav_add_admission).setVisible(false);
            menu.findItem(R.id.nav_get_admission).setVisible(false);
            menu.findItem(R.id.nav_get_timeAllot).setVisible(false);
            menu.findItem(R.id.nav_admissions).setTitle("Admissions");
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

            item.setTitle(isInquiryExpanded ? "Inquiry  ▲" : "Inquiry  ▼");

            styleSubItem(menu.findItem(R.id.nav_add_inquiry), "Add Inquiry");
            styleSubItem(menu.findItem(R.id.nav_get_inquiry), "Get Inquiry");
            styleSubItem(menu.findItem(R.id.nav_batch),       "Get Batch");
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

        // ── ADMISSIONS expand/collapse ───────────────────────────
        if (id == R.id.nav_admissions) {
            isAdmissionExpanded = !isAdmissionExpanded;

            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_add_admission).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_get_admission).setVisible(isAdmissionExpanded);
            menu.findItem(R.id.nav_get_timeAllot).setVisible(isAdmissionExpanded);

            item.setTitle(isAdmissionExpanded ? "Admissions  ▲" : "Admissions  ▼");

            styleSubItem(menu.findItem(R.id.nav_add_admission), "New Admission");
            styleSubItem(menu.findItem(R.id.nav_get_admission), "Fee Receipt");
            styleSubItem(menu.findItem(R.id.nav_get_timeAllot), "Allot Batch Timing");
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

        // ── OTHER items ──────────────────────────────────────────
        if (id == R.id.nav_certificates) {
        } else if (id == R.id.nav_notifications) {
        } else if (id == R.id.nav_bulk) {
        } else if (id == R.id.nav_reports) {
        } else if (id == R.id.nav_dashboard) {
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