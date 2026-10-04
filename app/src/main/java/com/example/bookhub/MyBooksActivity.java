package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

public class MyBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> myBookList, filteredList;
    private TextView noBooksText, tabActive, tabRejected;
    private DatabaseReference databaseBooks;
    private String currentUserId;
    private String currentFilter = "uploaded"; // uploaded or rejected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_books);

        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + 20, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        noBooksText = findViewById(R.id.noBooksText);
        tabActive = findViewById(R.id.tabActive);
        tabRejected = findViewById(R.id.tabRejected);
        
        recyclerView = findViewById(R.id.recyclerViewMyBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        myBookList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new BookAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        databaseBooks = FirebaseDatabase.getInstance().getReference("Books");

        setupNavigation();
        setupTabs();

        if (currentUserId != null) {
            fetchMyBooks();
        }
    }

    private void setupTabs() {
        tabActive.setOnClickListener(v -> {
            currentFilter = "uploaded";
            updateTabsUI();
            filterBooks();
        });

        tabRejected.setOnClickListener(v -> {
            currentFilter = "rejected";
            updateTabsUI();
            filterBooks();
        });
    }

    private void updateTabsUI() {
        if (currentFilter.equals("uploaded")) {
            tabActive.setBackgroundColor(android.graphics.Color.parseColor("#0284C7"));
            tabActive.setTextColor(android.graphics.Color.WHITE);
            tabRejected.setBackgroundColor(android.graphics.Color.parseColor("#E2E8F0"));
            tabRejected.setTextColor(android.graphics.Color.parseColor("#64748B"));
        } else {
            tabRejected.setBackgroundColor(android.graphics.Color.parseColor("#0284C7"));
            tabRejected.setTextColor(android.graphics.Color.WHITE);
            tabActive.setBackgroundColor(android.graphics.Color.parseColor("#E2E8F0"));
            tabActive.setTextColor(android.graphics.Color.parseColor("#64748B"));
        }
    }

    private void setupNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        findViewById(R.id.navMyBooks).setOnClickListener(v -> {
            // Current activity
        });

        findViewById(R.id.navAddBook).setOnClickListener(v -> {
            startActivity(new Intent(this, AddBookActivity.class));
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, Profile.class));
            finish();
        });
    }

    private void fetchMyBooks() {
        databaseBooks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myBookList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Book book = postSnapshot.getValue(Book.class);
                    if (book != null && currentUserId.equals(book.getSellerId())) {
                        myBookList.add(0, book);
                    }
                }
                filterBooks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyBooksActivity.this, "Failed to load books: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterBooks() {
        filteredList.clear();
        for (Book book : myBookList) {
            if (currentFilter.equals("uploaded")) {
                // Show Pending or Approved in "Uploaded" tab
                if (book.getStatus() == null || !"rejected".equals(book.getStatus())) {
                    filteredList.add(book);
                }
            } else {
                // Show Rejected in "Rejected" tab
                if ("rejected".equals(book.getStatus())) {
                    filteredList.add(book);
                }
            }
        }

        if (filteredList.isEmpty()) {
            noBooksText.setVisibility(View.VISIBLE);
        } else {
            noBooksText.setVisibility(View.GONE);
        }

        adapter.updateList(filteredList);
    }
}