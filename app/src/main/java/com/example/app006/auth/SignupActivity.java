package com.example.app006.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.example.app006.R;
import com.example.app006.models.HelperClass;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;

public class SignupActivity extends AppCompatActivity {
    EditText signupName, signupUsername, signupEmail, signupPassword, signupConfirmPassword, signupCompanyName;
    Spinner signupUserType;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupConfirmPassword = findViewById(R.id.signup_confirm_password);
        signupCompanyName = findViewById(R.id.signup_companyName);
        signupUserType = findViewById(R.id.signup_user_type);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);

        db = FirebaseFirestore.getInstance();

        // Populate Spinner
        List<String> userTypes = Arrays.asList("Select User Type", "Admin", "Employee");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userTypes);
        signupUserType.setAdapter(adapter);




        signupButton.setOnClickListener(view -> {
            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String username = signupUsername.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            String confirmPassword = signupConfirmPassword.getText().toString().trim();
            String userType = signupUserType.getSelectedItem().toString();
            String companyName = signupCompanyName.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || userType.equals("Select User Type")) {
                Toast.makeText(SignupActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }



            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            HelperClass user = new HelperClass(name, email, username, userType, password, companyName);
            db.collection("users").document(username).set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SignupActivity.this, "Signup Successful! 🎉", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    })
                    .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, "Signup Failed!", Toast.LENGTH_SHORT).show());
        });

        loginRedirectText.setOnClickListener(view -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }
}
