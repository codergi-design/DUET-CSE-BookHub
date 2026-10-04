package com.example.bookhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());

        if (notification.isRead()) {
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Context context = v.getContext();
            markAsRead(notification);

            String type = notification.getType();
            if (type == null) return;

            android.content.Intent intent = null;
            if ("BOOK_APPROVAL".equals(type)) {
                intent = new android.content.Intent(context, ManageBooksActivity.class);
            } else if ("USER_VERIFICATION".equals(type)) {
                intent = new android.content.Intent(context, ManageUsersActivity.class);
            } else if ("SUPPORT_REQUEST".equals(type)) {
                intent = new android.content.Intent(context, ManageSupportActivity.class);
            }

            if (intent != null) {
                context.startActivity(intent);
            }
        });
    }

    private void markAsRead(Notification notification) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        // We attempt both, Firebase will just ignore if the path doesn't exist
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Notifications")
                .child(uid).child(notification.getId()).child("read").setValue(true);
        
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("AdminNotifications")
                .child(notification.getId()).child("read").setValue(true);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notiTitle);
            message = itemView.findViewById(R.id.notiMessage);
        }
    }
}