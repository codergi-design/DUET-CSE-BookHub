package com.example.bookhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ContactUsActivity extends AppCompatActivity {

    private Spinner queryTypeSpinner;
    private EditText contactPhone, contactDescription;
    private Button submitBtn;
    private DatabaseReference supportRef, userRef;
    private String currentUserId, currentUserName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        queryTypeSpinner = findViewById(R.id.queryTypeSpinner);
        contactPhone = findViewById(R.id.contactPhone);
        contactDescription = findViewById(R.id.contactDescription);
        submitBtn = findViewById(R.id.submitContactBtn);

        currentUserId = FirebaseAuth.getInstance().getUid();
        supportRef = FirebaseDatabase.getInstance().getReference("SupportRequests");
        
        if (currentUserId != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) currentUserName = user.getName();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        submitBtn.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String queryType = queryTypeSpinner.getSelectedItem().toString();
        String phone = contactPhone.getText().toString().trim();
        String description = contactDescription.getText().toString().trim();

        if (phone.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = supportRef.push().getKey();
        SupportRequest request = new SupportRequest(id, currentUserId, currentUserName, queryType, phone, description, System.currentTimeMillis());

        if (id != null) {
            supportRef.child(id).setValue(request).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendAdminNotification("Support Request", "New support request from " + currentUserName, id);
                    Toast.makeText(ContactUsActivity.this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ContactUsActivity.this, "Submission failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendAdminNotification(String title, String message, String requestId) {
        DatabaseReference adminNotiRef = FirebaseDatabase.getInstance().getReference("AdminNotifications");
        String id = adminNotiRef.push().getKey();
        Notification notification = new Notification(id, title, message, System.currentTimeMillis(), "SUPPORT_REQUEST", requestId);
        if (id != null) {
            adminNotiRef.child(id).setValue(notification);
        }
    }
}