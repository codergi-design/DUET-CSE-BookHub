package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    private TextView profileNameText, profileEmailText;
    private Button editProfileBtn;
    private View navHome, navMyBooks, navAddBook, navProfile;
    private View menuAdminPanel;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Dynamically handle top bar padding for camera
        View profileTopBar = findViewById(R.id.profileTopBar);
        ViewCompat.setOnApplyWindowInsetsListener(profileTopBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + 20, v.getPaddingRight(), v.getPaddingBottom());
            v.getLayoutParams().height = systemBars.top + (int)(60 * getResources().getDisplayMetrics().density);
            v.requestLayout();
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        profileNameText = findViewById(R.id.profileNameText);
        profileEmailText = findViewById(R.id.profileEmailText);
        editProfileBtn = findViewById(R.id.editProfileBtn);

        navHome = findViewById(R.id.navHome);
        navMyBooks = findViewById(R.id.navMyBooks);
        navAddBook = findViewById(R.id.navAddBook);
        navProfile = findViewById(R.id.navProfile);
        menuAdminPanel = findViewById(R.id.menuAdminPanel);

        loadUserProfile();
        setupNavigation();

        editProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, EditProfileActivity.class));
        });

        findViewById(R.id.profileBackBtn).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.menuChangePassword).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, ChangePasswordActivity.class));
        });

        findViewById(R.id.menuSoldBooks).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, SoldBooksActivity.class));
        });

        findViewById(R.id.menuDeletedBooks).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, DeletedBooksActivity.class));
        });

        menuAdminPanel.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, AdminDashboardActivity.class));
        });
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, HomeActivity.class));
            finish();
        });

        navMyBooks.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, MyBooksActivity.class));
            finish();
        });

        navAddBook.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, AddBookActivity.class));
            finish();
        });

        // navProfile is current activity
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            if (email.equalsIgnoreCase("eshaofficial800@gmail.com")) {
                menuAdminPanel.setVisibility(View.VISIBLE);
            } else {
                menuAdminPanel.setVisibility(View.GONE);
            }
        }

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        profileNameText.setText(user.getName());
                        profileEmailText.setText(user.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}