package com.example.app006.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.app006.R;
import com.example.app006.admin.Fragments.*;
import com.example.app006.admin.viewmodel.AdminViewModel;
import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private AdminViewModel adminViewModel;
    private TextView greetingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        greetingMessage = headerView.findViewById(R.id.greeting_message);

        // Initialize ViewModel
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Observe Greeting Message Changes
        adminViewModel.greetingMessage.observe(this, message -> greetingMessage.setText(message));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.admin_nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.admin_nav_home) {
            selectedFragment = new HomeFragment();
        }
        else if (item.getItemId() == R.id.admin_nav_payroll) {
            selectedFragment = new PayrollFragment();
        }else if (item.getItemId() == R.id.admin_nav_employee_management) {
            selectedFragment = new EmployeeManagementFragment();
        } else if (item.getItemId() == R.id.admin_set_geofence) {
            selectedFragment = new SetGeofence();
        } else if (item.getItemId() == R.id.admin_nav_dashboard) {
            selectedFragment = new DashboardFragment();
        } else if (item.getItemId() == R.id.admin_nav_reports) {
            selectedFragment = new ReportsFragment();
        } else if (item.getItemId() == R.id.admin_nav_leave) {
            selectedFragment = new LeaveManagementFragment();
        }  else if (item.getItemId() == R.id.admin_nav_live_tracking) {
            selectedFragment = new LiveTrackingFragment();
        }

        else if (item.getItemId() == R.id.admin_nav_logout) {
            adminViewModel.logoutUser(this);
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
