package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private DatabaseReference adminNotiRef;
    private TextView noNotiText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        noNotiText = findViewById(R.id.noNotiText);
        recyclerView = findViewById(R.id.recyclerViewAdminNoti);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        adminNotiRef = FirebaseDatabase.getInstance().getReference("AdminNotifications");

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.btnClearAll).setOnClickListener(v -> clearAllNotifications());

        fetchNotifications();
    }

    private void fetchNotifications() {
        adminNotiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Notification n = data.getValue(Notification.class);
                    if (n != null) {
                        notificationList.add(0, n);
                        // Mark as read
                        if (!n.isRead()) {
                            data.getRef().child("read").setValue(true);
                        }
                    }
                }
                
                if (notificationList.isEmpty()) {
                    noNotiText.setVisibility(View.VISIBLE);
                } else {
                    noNotiText.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void clearAllNotifications() {
        adminNotiRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Notifications cleared", Toast.LENGTH_SHORT).show();
        });
    }
}