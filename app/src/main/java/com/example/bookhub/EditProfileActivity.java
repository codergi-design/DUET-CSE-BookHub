package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText firstNameEt, lastNameEt;
    private Button updateProfileBtn;
    private DatabaseReference userDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        firstNameEt = findViewById(R.id.firstNameEt);
        lastNameEt = findViewById(R.id.lastNameEt);
        updateProfileBtn = findViewById(R.id.updateProfileBtn);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            loadUserData();
        }

        // Dynamically handle top bar padding for camera
        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + 20, v.getPaddingRight(), v.getPaddingBottom());
            v.getLayoutParams().height = systemBars.top + (int)(60 * getResources().getDisplayMetrics().density);
            v.requestLayout();
            return insets;
        });

        updateProfileBtn.setOnClickListener(v -> updateProfile());
    }

    private void loadUserData() {
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        firstNameEt.setText(user.getName());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateProfile() {
        String name = firstNameEt.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        userDatabase.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}