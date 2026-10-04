package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class DeletedBooksActivity extends AppCompatActivity {

    private BookAdapter adapter;
    private List<Book> deletedBookList;
    private View emptyState;
    private DatabaseReference deletedBooksRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deleted_books);

        // Dynamically handle top bar padding for camera
        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + 20, v.getPaddingRight(), v.getPaddingBottom());
            v.getLayoutParams().height = systemBars.top + (int)(60 * getResources().getDisplayMetrics().density);
            v.requestLayout();
            return insets;
        });

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        emptyState = findViewById(R.id.emptyState);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDeletedBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deletedBookList = new ArrayList<>();
        adapter = new BookAdapter(this, deletedBookList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            deletedBooksRef = FirebaseDatabase.getInstance().getReference("DeletedBooks").child(currentUserId);
            fetchDeletedBooks();
        }
    }

    private void fetchDeletedBooks() {
        deletedBooksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deletedBookList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Book book = dataSnapshot.getValue(Book.class);
                    if (book != null) {
                        deletedBookList.add(0, book);
                    }
                }

                if (deletedBookList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    emptyState.setVisibility(View.GONE);
                }

                adapter.updateList(deletedBookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}