package com.example.app006;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app006.auth.LoginActivity;
import com.example.app006.admin.AdminActivity;
import com.example.app006.employee.EmployeeActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            Intent intent;
            if (isLoggedIn) {
                String role = sharedPreferences.getString("role", "");
                if ("Admin".equals(role)) {
                    intent = new Intent(SplashActivity.this, AdminActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, EmployeeActivity.class);
                }
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
