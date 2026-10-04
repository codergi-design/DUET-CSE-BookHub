package com.example.bookhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        // ছবি লোডিং সরিয়ে দেওয়া হয়েছে

        // Admin cannot verify/delete themselves
        if (user.isAdmin() || "eshaofficial800@gmail.com".equalsIgnoreCase(user.getEmail())) {
            holder.btnVerify.setVisibility(View.GONE);
            holder.btnViewId.setVisibility(View.GONE);
            holder.name.setText(user.getName() + " (Admin)");
        } else {
            // Logic for regular Students
            holder.btnViewId.setVisibility(View.GONE); // আইডি কার্ড অপশন বন্ধ
            
            if (user.isVerified()) {
                holder.btnVerify.setVisibility(View.GONE);
                holder.name.setText(user.getName() + " ✓");
            } else {
                holder.btnVerify.setVisibility(View.VISIBLE);
                holder.name.setText(user.getName() + " (Pending)");
            }
        }

        holder.btnVerify.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.getUserId()).child("verified").setValue(true)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "User Approved!", Toast.LENGTH_SHORT).show());
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete user " + user.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUserId()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User removed", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, btnDelete, btnVerify, btnViewId;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            email = itemView.findViewById(R.id.userEmail);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
            btnVerify = itemView.findViewById(R.id.btnVerifyUser);
            btnViewId = itemView.findViewById(R.id.btnViewIdCard);
        }
    }
}