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

public class InterestedBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> interestedBookList;
    private TextView noDataText;
    private DatabaseReference interestedRef, booksRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_books);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        noDataText = findViewById(R.id.noDataText);
        recyclerView = findViewById(R.id.recyclerViewInterested);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        interestedBookList = new ArrayList<>();
        adapter = new BookAdapter(this, interestedBookList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            interestedRef = FirebaseDatabase.getInstance().getReference("InterestedBooks").child(currentUserId);
            booksRef = FirebaseDatabase.getInstance().getReference("Books");
            fetchInterestedBooks();
        }
    }

    private void fetchInterestedBooks() {
        interestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                interestedBookList.clear();
                if (!snapshot.exists()) {
                    noDataText.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                }
                
                noDataText.setVisibility(View.GONE);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String bookId = dataSnapshot.getKey();
                    if (bookId != null) {
                        fetchBookDetails(bookId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchBookDetails(String bookId) {
        booksRef.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book book = snapshot.getValue(Book.class);
                if (book != null) {
                    interestedBookList.add(book);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}