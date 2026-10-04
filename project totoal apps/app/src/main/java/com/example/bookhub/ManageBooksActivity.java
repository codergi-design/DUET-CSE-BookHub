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

public class ManageBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> bookList;
    private DatabaseReference bookRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_books);

        recyclerView = findViewById(R.id.recyclerViewManageBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        bookList = new ArrayList<>();
        adapter = new BookAdapter(this, bookList);
        recyclerView.setAdapter(adapter);

        bookRef = FirebaseDatabase.getInstance().getReference("Books");

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        fetchBooks();
    }

    private void fetchBooks() {
        bookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Book book = data.getValue(Book.class);
                    if (book != null) {
                        bookList.add(0, book);
                    }
                }
                adapter.updateList(bookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageBooksActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}