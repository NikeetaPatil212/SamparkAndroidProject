package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

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


        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(DashboardActivity.this);
       /* List<NavMenuItem> drawerList = new ArrayList<>();

        drawerList.add(new NavMenuItem("Inquiry", R.drawable.baseline_call_24, true));
        drawerList.add(new NavMenuItem("Admissions", R.drawable.baseline_article_24));
        drawerList.add(new NavMenuItem("Certificates", R.drawable.baseline_account_circle_24));
        drawerList.add(new NavMenuItem("Notifications", R.drawable.baseline_lock_24));

        RecyclerView rvDrawer = findViewById(R.id.rvDrawer);
        rvDrawer.setLayoutManager(new LinearLayoutManager(this));
        rvDrawer.setAdapter(new DrawerAdapter(drawerList));*/


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_inquiry) {
            // Open InquiryActivity
            Intent intent = new Intent(this, InquiryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_admissions) {
            // Handle other items
        }


        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START); // close drawer after click
        return true;
        }
}