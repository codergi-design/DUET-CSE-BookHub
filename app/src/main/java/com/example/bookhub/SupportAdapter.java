package com.example.bookhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SupportAdapter extends RecyclerView.Adapter<SupportAdapter.SupportViewHolder> {

    private final Context context;
    private final List<SupportRequest> requestList;

    public SupportAdapter(Context context, List<SupportRequest> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public SupportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_support_request, parent, false);
        return new SupportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupportViewHolder holder, int position) {
        SupportRequest request = requestList.get(position);

        holder.userName.setText(request.getUserName());
        holder.queryType.setText(request.getQueryType());
        holder.phone.setText(request.getPhone());
        holder.description.setText(request.getDescription());

        if (request.isResolved()) {
            holder.btnResolve.setVisibility(View.GONE);
            holder.queryType.setText(request.getQueryType() + " (RESOLVED)");
            holder.queryType.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"));
        } else {
            holder.btnResolve.setVisibility(View.VISIBLE);
        }

        holder.btnResolve.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("SupportRequests")
                    .child(request.getId()).child("resolved").setValue(true)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Request marked as resolved", Toast.LENGTH_SHORT).show());
        });

        holder.btnDelete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("SupportRequests")
                    .child(request.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Request deleted", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class SupportViewHolder extends RecyclerView.ViewHolder {
        TextView userName, queryType, phone, description, btnResolve, btnDelete;

        public SupportViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.reqUser);
            queryType = itemView.findViewById(R.id.reqType);
            phone = itemView.findViewById(R.id.reqPhone);
            description = itemView.findViewById(R.id.reqDesc);
            btnResolve = itemView.findViewById(R.id.btnResolve);
            btnDelete = itemView.findViewById(R.id.btnDeleteReq);
        }
    }
}