package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView totalUsersCount, totalBooksCount, pendingBooksCount, pendingUsersCount;
    private DatabaseReference userRef, bookRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Final Security Layer: Check email inside Admin Activity
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null || !user.getEmail().equalsIgnoreCase("eshaofficial800@gmail.com")) {
            Toast.makeText(this, "Access Denied!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        totalUsersCount = findViewById(R.id.totalUsersCount);
        totalBooksCount = findViewById(R.id.totalBooksCount);
        pendingBooksCount = findViewById(R.id.pendingBooksCount);
        pendingUsersCount = findViewById(R.id.pendingUsersCount);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        bookRef = FirebaseDatabase.getInstance().getReference("Books");

        findViewById(R.id.adminBackBtn).setOnClickListener(v -> finish());

        findViewById(R.id.btnManageBooks).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageBooksActivity.class));
        });

        findViewById(R.id.btnQuickManage).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageBooksActivity.class));
        });

        findViewById(R.id.btnManageUsers).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class));
        });

        findViewById(R.id.btnQuickUsers).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class));
        });

        findViewById(R.id.btnSupportRequests).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageSupportActivity.class));
        });

        fetchStats();
    }

    private void fetchStats() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalCount = snapshot.getChildrenCount();
                long unverifiedCount = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    User u = data.getValue(User.class);
                    if (u != null && !u.isAdmin() && !u.isVerified()) {
                        unverifiedCount++;
                    }
                }
                totalUsersCount.setText(String.valueOf(totalCount));
                pendingUsersCount.setText(String.valueOf(unverifiedCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        bookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long approvedCount = 0;
                long pendingCount = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Book book = data.getValue(Book.class);
                    if (book != null) {
                        if ("approved".equals(book.getStatus())) {
                            approvedCount++;
                        } else if ("pending".equals(book.getStatus()) || book.getStatus() == null) {
                            pendingCount++;
                        }
                    }
                }
                totalBooksCount.setText(String.valueOf(approvedCount));
                pendingBooksCount.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}