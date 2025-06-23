package com.example.app006.employee.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app006.R;
import com.example.app006.auth.LoginActivity;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView greetingText, employeeName, employeeEmail, employeeRole, employeeUsername;
    private Button logoutButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ✅ Inflate layout
        View view = inflater.inflate(R.layout.admin_fragment_home, container, false);

        // ✅ Initialize UI components
        greetingText = view.findViewById(R.id.greeting_text);
        employeeName = view.findViewById(R.id.employee_name);
        employeeEmail = view.findViewById(R.id.employee_email);
        employeeRole = view.findViewById(R.id.employee_role);
        employeeUsername = view.findViewById(R.id.employee_username);
        logoutButton = view.findViewById(R.id.logout_button);

        // ✅ Load user details from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "John Doe");
        String email = sharedPreferences.getString("email", "johndoe@example.com");
        String role = sharedPreferences.getString("role", "Software Engineer");
        String username = sharedPreferences.getString("username", "johndoe");

        // ✅ Set dynamic greeting and user details
        setGreetingMessage(name);
        employeeName.setText(name);
        employeeEmail.setText(email);
        employeeRole.setText(role);
        employeeUsername.setText(username);

        // ✅ Logout button functionality
        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void setGreetingMessage(String name) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning, " + name + "!";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon, " + name + "!";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening, " + name + "!";
        } else {
            greeting = "Good Night, " + name + "!";
        }
        greetingText.setText(greeting);
    }

    private void logoutUser() {
        // ✅ Clear user session
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // ✅ Redirect to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        requireActivity().finish();
    }
}
