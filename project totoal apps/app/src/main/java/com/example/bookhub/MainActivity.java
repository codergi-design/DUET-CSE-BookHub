package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerText;
    private FirebaseAuth mAuth;
    private final String ADMIN_EMAIL = "eshaofficial800@gmail.com"; // অ্যাপের প্রধান অ্যাডমিন ইমেইল

    @Override
    protected void onStart() {
        super.onStart();
        // অ্যাপ চালু হওয়ার সময় যদি কোনো ইউজার আগে থেকেই লগইন করা থাকে
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // যদি সেই ইউজার অ্যাডমিন হয়, তবে সরাসরি হোম পেজে পাঠাবে
            if (currentUser.getEmail() != null && currentUser.getEmail().equalsIgnoreCase(ADMIN_EMAIL)) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
                return;
            }
            // সাধারণ ইউজার হলে তার ভেরিফিকেশন স্ট্যাটাস চেক করবে
            checkUserVerification(currentUser.getUid());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // ফায়ারবেস অথেন্টিকেশন শুরু করা

        // স্ট্যাটাস বার এবং সিস্টেম নেভিগেশনের জন্য প্যাডিং সেট করা
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        // UI এলিমেন্টগুলো আইডি দিয়ে খুঁজে বের করা
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerText);

        // লগইন বাটনে ক্লিক করলে যা হবে
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // খালি ঘর চেক করা
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // ফায়ারবেস অথেন্টিকেশন দিয়ে লগইন করা
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // যদি লগইন করা ইউজার অ্যাডমিন হয়
                                if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(ADMIN_EMAIL)) {
                                    Toast.makeText(MainActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    // সাধারণ ইউজার হলে ভেরিফিকেশন ফাংশন কল করা
                                    checkUserVerification(user.getUid());
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // রেজিস্ট্রেশন টেক্সটে ক্লিক করলে RegisterActivity-তে নিয়ে যাবে
        registerText.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
    }

    // ডাটাবেস থেকে ইউজারের ভেরিফিকেশন স্ট্যাটাস চেক করার মেথড
    private void checkUserVerification(String uid) {
        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                // অ্যাডমিন যদি অ্যাপ্রুভ করে থাকে (isVerified == true)
                                if (user.isVerified()) {
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    // অ্যাপ্রুভ না করা পর্যন্ত অ্যাপ থেকে বের করে দিবে
                                    Toast.makeText(MainActivity.this, "Access Denied: Pending Admin Approval", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            } else {
                                mAuth.signOut();
                                Toast.makeText(MainActivity.this, "User record error", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mAuth.signOut();
                            Toast.makeText(MainActivity.this, "Account not found in database", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mAuth.signOut();
                        Toast.makeText(MainActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}