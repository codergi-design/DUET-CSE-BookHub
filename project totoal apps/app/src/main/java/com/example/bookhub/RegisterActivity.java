package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // সিস্টেম প্যাডিং হ্যান্ডেল করা (Status Bar-এর জন্য)
        View rootLayout = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ফায়ারবেস অথেন্টিকেশন এবং ডাটাবেস শুরু করা
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // UI কম্পোনেন্টগুলো আইডি দিয়ে খুঁজে বের করা
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button registerBtn = findViewById(R.id.registerBtn);

        // রেজিস্টার বাটনে ক্লিক করলে মেথড কল হবে
        registerBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // সব ফিল্ড পূরণ করা হয়েছে কি না চেক করা
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // ইমেইল ফরম্যাট চেক করা
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // ফায়ারবেস অথেন্টিকেশনে নতুন ইউজার তৈরি করা
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // সাকসেসফুল হলে ডাটাবেসে সেভ করার মেথড কল করা
                            saveUserToDatabase(firebaseUser, name, email, phone);
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Authentication Failed";
                        Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ইউজারের প্রোফাইল তথ্য ডাটাবেসে সেভ করা
    private void saveUserToDatabase(FirebaseUser firebaseUser, String name, String email, String phone) {
        String userId = firebaseUser.getUid();
        
        // Removed image URL fields from User constructor
        User newUser = new User(userId, name, email, phone, false, false);

        userDatabase.child(userId).setValue(newUser).addOnCompleteListener(dbTask -> {
            if (dbTask.isSuccessful()) {
                // অ্যাডমিনের জন্য নোটিফিকেশন পাঠানো
                sendAdminNotification("New User Waiting", "User " + name + " is waiting for approval.", userId);
                
                Toast.makeText(RegisterActivity.this, "Registration Done! Please wait for Admin Approval.", Toast.LENGTH_LONG).show();
                
                // অ্যাডমিন অ্যাপ্রুভালের জন্য সাইন আউট করে মেইন পেজে পাঠিয়ে দিবে
                mAuth.signOut();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // অ্যাডমিন ড্যাশবোর্ডের জন্য নোটিফিকেশন পাঠানোর লজিক
    private void sendAdminNotification(String title, String message, String userId) {
        DatabaseReference adminNotiRef = FirebaseDatabase.getInstance().getReference("AdminNotifications");
        String id = adminNotiRef.push().getKey(); 
        Notification notification = new Notification(id, title, message, System.currentTimeMillis(), "USER_VERIFICATION", userId);
        if (id != null) {
            adminNotiRef.child(id).setValue(notification);
        }
    }
}