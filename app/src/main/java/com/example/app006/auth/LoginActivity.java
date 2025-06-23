
package com.example.app006.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app006.R;
import com.example.app006.admin.AdminActivity;
import com.example.app006.employee.EmployeeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText loginUsername, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText;
    private SharedPreferences prefs;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        // If already logged in, go straight to dashboard
        if (prefs.getBoolean("isLoggedIn", false)) {
            navigateToDashboard();
            return;
        }

        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                checkUser();
            }
        });

        signupRedirectText.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );
    }

    private boolean validateInputs() {
        boolean ok = true;
        if (loginUsername.getText().toString().trim().isEmpty()) {
            loginUsername.setError("Please enter your username");
            ok = false;
        }
        if (loginPassword.getText().toString().trim().isEmpty()) {
            loginPassword.setError("Please enter your password");
            ok = false;
        }
        return ok;
    }

    private void checkUser() {
        String userKey = loginUsername.getText().toString().trim();
        String passInput = loginPassword.getText().toString().trim();

        db.collection("users").document(userKey).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (!doc.exists()) {
                            loginUsername.setError("User not found");
                            return;
                        }

                        // Get the fields
                        String pwd        = doc.getString("password");
                        String type       = doc.getString("userType");
                        String email      = doc.getString("email");
                        String company    = doc.getString("companyName");
                        String name       = doc.getString("name");

                        if (pwd == null || type == null) {
                            Toast.makeText(LoginActivity.this,
                                    "Invalid user data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!passInput.equals(pwd)) {
                            loginPassword.setError("Incorrect password");
                            return;
                        }

                        // Store all needed details in SharedPreferences
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("username", userKey);
                        editor.putString("role", type);
                        if (email     != null) editor.putString("email", email);
                        if (company   != null) editor.putString("companyName", company);
                        if (name      != null) editor.putString("name", name);
                        editor.apply();

                        // Navigate based on role
                        if (type.equalsIgnoreCase("employee")) {
                            Toast.makeText(LoginActivity.this,
                                    "Employee login successful", Toast.LENGTH_SHORT).show();

                            //delete this after

                            //new QuestionImporter(LoginActivity.this).parseAndImportFromRaw(R.raw.question_dataset);
                            startActivity(new Intent(LoginActivity.this, EmployeeActivity.class));

                        } else if (type.equalsIgnoreCase("admin")) {
                            Toast.makeText(LoginActivity.this,
                                    "Admin login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, AdminActivity.class));

                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Unknown user type", Toast.LENGTH_SHORT).show();
                        }

                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToDashboard() {
        String role = prefs.getString("role", "");
        if (role.equalsIgnoreCase("employee")) {
            startActivity(new Intent(this, EmployeeActivity.class));
        } else if (role.equalsIgnoreCase("admin")) {
            startActivity(new Intent(this, AdminActivity.class));
        }
        finish();
    }
}








//package com.example.app006.auth;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.app006.R;
//import com.example.app006.admin.AdminActivity;
//import com.example.app006.employee.EmployeeActivity;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private EditText loginUsername, loginPassword;
//    private Button loginButton;
//    private TextView signupRedirectText;
//    private SharedPreferences prefs;
//    private FirebaseFirestore db;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        loginUsername = findViewById(R.id.login_username);
//        loginPassword = findViewById(R.id.login_password);
//        loginButton = findViewById(R.id.login_button);
//        signupRedirectText = findViewById(R.id.signupRedirectText);
//
//        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
//        db = FirebaseFirestore.getInstance();
//
//        if (prefs.getBoolean("isLoggedIn", false)) {
//            navigateToDashboard();
//            return;
//        }
//
//        loginButton.setOnClickListener(v -> {
//            if (validateInputs()) {
//                checkUser();
//            }
//        });
//
//        signupRedirectText.setOnClickListener(v ->
//                startActivity(new Intent(this, SignupActivity.class))
//        );
//    }
//
//    private boolean validateInputs() {
//        boolean ok = true;
//        if (loginUsername.getText().toString().trim().isEmpty()) {
//            loginUsername.setError("Please enter your username");
//            ok = false;
//        }
//        if (loginPassword.getText().toString().trim().isEmpty()) {
//            loginPassword.setError("Please enter your password");
//            ok = false;
//        }
//        return ok;
//    }
//
//    private void checkUser() {
//        String user = loginUsername.getText().toString().trim();
//        String pass = loginPassword.getText().toString().trim();
//
//        db.collection("users").document(user).get()
//                .addOnSuccessListener(doc -> {
//                    if (!doc.exists()) {
//                        loginUsername.setError("User not found");
//                        return;
//                    }
//                    String pwd = doc.getString("password");
//                    String type = doc.getString("userType");
//
//                    if (pwd == null || type == null) {
//                        Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if (!pass.equals(pwd)) {
//                        loginPassword.setError("Incorrect password");
//                        return;
//                    }
//
//                    // Store login status
//                    SharedPreferences.Editor editor = prefs.edit();
//                    editor.putBoolean("isLoggedIn", true);
//                    editor.putString("usename", user);
//                    editor.putString("role", type);
//                    editor.apply();
//
//                    if (type.equalsIgnoreCase("employee")) {
//                        Toast.makeText(this, "Employee login successful", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(this, EmployeeActivity.class));
//                    } else if (type.equalsIgnoreCase("admin")) {
//                        Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(this, AdminActivity.class));
//                    } else {
//                        Toast.makeText(this, "Unknown user type", Toast.LENGTH_SHORT).show();
//                    }
//
//                    finish();
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
//                );
//    }
//
//    private void navigateToDashboard() {
//        String role = prefs.getString("role", "");
//        if (role.equalsIgnoreCase("employee")) {
//            startActivity(new Intent(this, EmployeeActivity.class));
//        } else if (role.equalsIgnoreCase("admin")) {
//            startActivity(new Intent(this, AdminActivity.class));
//        }
//        finish();
//    }
//}
