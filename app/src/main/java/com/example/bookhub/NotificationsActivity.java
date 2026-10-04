package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private TextView noNotificationsText;
    private DatabaseReference notificationsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        noNotificationsText = findViewById(R.id.noNotificationsText);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications").child(currentUserId);
            fetchNotifications();
        }
    }

    private void fetchNotifications() {
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                if (!snapshot.exists()) {
                    noNotificationsText.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                }

                noNotificationsText.setVisibility(View.GONE);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(0, notification); // Latest first

                        // Mark as read if it's currently unread
                        if (!notification.isRead()) {
                            dataSnapshot.getRef().child("read").setValue(true);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}