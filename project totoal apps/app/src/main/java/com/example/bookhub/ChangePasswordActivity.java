package com.example.bookhub;

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

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText newPasswordEt, confirmPasswordEt;
    private Button submitPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        // Dynamically handle top bar padding for camera
        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + 20, v.getPaddingRight(), v.getPaddingBottom());
            v.getLayoutParams().height = systemBars.top + (int)(60 * getResources().getDisplayMetrics().density);
            v.requestLayout();
            return insets;
        });

        newPasswordEt = findViewById(R.id.newPasswordEt);
        confirmPasswordEt = findViewById(R.id.confirmPasswordEt);
        submitPasswordBtn = findViewById(R.id.submitPasswordBtn);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        submitPasswordBtn.setOnClickListener(v -> {
            String newPass = newPasswordEt.getText().toString().trim();
            String confirmPass = confirmPasswordEt.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updatePassword(newPass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = "Update Failed";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }
                        Toast.makeText(ChangePasswordActivity.this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}