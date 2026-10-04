package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddBookActivity extends AppCompatActivity {

    private EditText titleEditText, authorEditText, priceEditText, semesterEditText, conditionEditText, phoneEditText;
    private Button postBookBtn;
    private DatabaseReference databaseBooks, userDatabase;
    private FirebaseAuth mAuth;
    private String currentUserId, currentUserName = "A Student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_book);

        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + 20, v.getPaddingRight(), v.getPaddingBottom());
            v.getLayoutParams().height = systemBars.top + (int)(60 * getResources().getDisplayMetrics().density);
            v.requestLayout();
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        priceEditText = findViewById(R.id.priceEditText);
        semesterEditText = findViewById(R.id.semesterEditText);
        conditionEditText = findViewById(R.id.conditionEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        postBookBtn = findViewById(R.id.postBookBtn);

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getName() != null) currentUserName = user.getName();
                            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                                phoneEditText.setText(user.getPhone());
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        databaseBooks = FirebaseDatabase.getInstance().getReference("Books");

        setupNavigation();

        postBookBtn.setOnClickListener(v -> postBook());
    }

    private void setupNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navMyBooks).setOnClickListener(v -> {
            startActivity(new Intent(this, MyBooksActivity.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, Profile.class));
            finish();
        });
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void postBook() {
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String semester = semesterEditText.getText().toString().trim();
        String condition = conditionEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (title.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Title and Phone are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check upload limit (max 20 books)
        databaseBooks.orderByChild("sellerId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() >= 20) {
                    Toast.makeText(AddBookActivity.this, "Upload limit reached! (Max 20 books)", Toast.LENGTH_LONG).show();
                } else {
                    proceedToPost(title, author, price, semester, condition, phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddBookActivity.this, "Error checking limits", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToPost(String title, String author, String price, String semester, String condition, String phone) {
        String id = databaseBooks.push().getKey();
        // Removed imageUrl field from Book constructor
        Book book = new Book(id, title, author, price, semester, condition, phone, currentUserId, currentUserName);

        if (id != null) {
            databaseBooks.child(id).setValue(book).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendNotification("Book Posted", "Your book '" + title + "' has been posted successfully.");
                    sendAdminNotification("New Book Uploaded", "A new book '" + title + "' was uploaded by " + currentUserName, id);
                    Toast.makeText(AddBookActivity.this, "Book Posted Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddBookActivity.this, "Failed to post book", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendNotification(String title, String message) {
        if (currentUserId == null) return;
        DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference("Notifications").child(currentUserId);
        String notiId = notiRef.push().getKey();
        Notification notification = new Notification(notiId, title, message, System.currentTimeMillis(), "INFO", "");
        if (notiId != null) {
            notiRef.child(notiId).setValue(notification);
        }
    }

    private void sendAdminNotification(String title, String message, String bookId) {
        DatabaseReference adminNotiRef = FirebaseDatabase.getInstance().getReference("AdminNotifications");
        String id = adminNotiRef.push().getKey();
        Notification notification = new Notification(id, title, message, System.currentTimeMillis(), "BOOK_APPROVAL", bookId);
        if (id != null) {
            adminNotiRef.child(id).setValue(notification);
        }
    }
}