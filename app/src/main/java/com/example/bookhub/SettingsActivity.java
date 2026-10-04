package com.example.bookhub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private SwitchCompat switchCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Dynamically handle top bar padding for camera
        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Adjust height dynamically to include status bar
            v.getLayoutParams().height = systemBars.top + (int)(60 * getResources().getDisplayMetrics().density);
            v.requestLayout();
            return insets;
        });

        findViewById(R.id.backBtn).setOnClickListener(v -> {
            // Return to previous activity smoothly
            finish();
        });

        SwitchCompat switchNotifications = findViewById(R.id.switchNotifications);
        switchCamera = findViewById(R.id.switchCamera);

        // Check current camera permission status
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            switchCamera.setChecked(true);
        }

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Notifications Enabled" : "Notifications Disabled";
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        });

        switchCamera.setOnClickListener(v -> {
            if (switchCamera.isChecked()) {
                requestCameraPermission();
            } else {
                Toast.makeText(this, "Permission can be revoked from System Settings", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is required to take photos of books.")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE))
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        switchCamera.setChecked(false);
                        dialog.dismiss();
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                switchCamera.setChecked(true);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                switchCamera.setChecked(false);
            }
        }
    }
}