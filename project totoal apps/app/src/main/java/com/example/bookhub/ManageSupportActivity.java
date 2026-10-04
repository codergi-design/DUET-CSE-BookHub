package com.example.bookhub;

import android.os.Bundle;
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

public class ManageSupportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SupportAdapter adapter;
    private List<SupportRequest> requestList;
    private DatabaseReference supportRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_support);

        recyclerView = findViewById(R.id.recyclerViewSupport);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new SupportAdapter(this, requestList);
        recyclerView.setAdapter(adapter);

        supportRef = FirebaseDatabase.getInstance().getReference("SupportRequests");

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        fetchRequests();
    }

    private void fetchRequests() {
        supportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    SupportRequest request = data.getValue(SupportRequest.class);
                    if (request != null) {
                        requestList.add(0, request);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageSupportActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}