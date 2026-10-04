package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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

public class SoldBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> soldBookList;
    private View emptyState;
    private DatabaseReference databaseBooks;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sold_books);

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
        recyclerView = findViewById(R.id.recyclerViewSoldBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        soldBookList = new ArrayList<>();
        adapter = new BookAdapter(this, soldBookList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        databaseBooks = FirebaseDatabase.getInstance().getReference("Books");

        if (currentUserId != null) {
            fetchSoldBooks();
        }
    }

    private void fetchSoldBooks() {
        databaseBooks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                soldBookList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Book book = postSnapshot.getValue(Book.class);
                    if (book != null && book.isSold() && currentUserId.equals(book.getSellerId())) {
                        soldBookList.add(0, book);
                    }
                }

                if (soldBookList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    emptyState.setVisibility(View.GONE);
                }

                adapter.updateList(soldBookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}