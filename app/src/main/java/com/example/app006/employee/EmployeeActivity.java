package com.example.app006.employee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.app006.R;
import com.example.app006.auth.LoginActivity;
import com.example.app006.employee.Fragments.EmployeePayrollFragment;
import com.example.app006.employee.Fragments.HomeFragment;
import com.example.app006.employee.Fragments.EmployeeManagementFragment;
import com.example.app006.employee.Fragments.DashboardFragment;
import com.example.app006.employee.Fragments.ReportsFragment;
import com.example.app006.employee.Fragments.LeaveManagementFragment;
import com.example.app006.employee.Fragments.AppraisalsFragment;
import com.example.app006.employee.Fragments.LiveTrackingFragment;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class EmployeeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "EmployeeActivity";
    private DrawerLayout drawerLayout;
    private TextView greetingTextView, greetingNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up the navigation drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set greeting message and name in navigation header
        View headerView = navigationView.getHeaderView(0);
        greetingTextView = headerView.findViewById(R.id.greeting_message);
        greetingNameTextView = headerView.findViewById(R.id.greeting_name);
        setGreetingMessage();
        setGreetingName();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.admin_nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.admin_nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.admin_nav_employee_management) {
            selectedFragment = new EmployeeManagementFragment();
        } else if (itemId == R.id.admin_nav_dashboard) {
            selectedFragment = new DashboardFragment();
        } else if (itemId == R.id.admin_nav_reports) {
            selectedFragment = new ReportsFragment();
        } else if (itemId == R.id.admin_nav_leave) {
            selectedFragment = new LeaveManagementFragment();
        }  else if (itemId == R.id.admin_nav_live_tracking) {
            selectedFragment = new LiveTrackingFragment();
        }
        else if (itemId == R.id.payroll) {
            selectedFragment = new EmployeePayrollFragment();
        }
 else if (itemId == R.id.admin_nav_logout) {
            logoutUser();
            return true;
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void setGreetingMessage() {
        String greeting = getGreetingMessage();
        greetingTextView.setText(greeting);
    }

    private void setGreetingName() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "User");
        Log.d(TAG, "Retrieved name from SharedPreferences: " + name);
        greetingNameTextView.setText("Hello, " + name + "!");
    }

    private String getGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good Morning!";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon!";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening!";
        } else {
            return "Good Night!";
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
