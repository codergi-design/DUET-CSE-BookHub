package com.example.bookhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditBookActivity extends AppCompatActivity {

    private EditText editTitle, editAuthor, editPrice, editSemester, editCondition, editPhone;
    private Button updateBtn;
    private String bookId;
    private DatabaseReference bookRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        editPrice = findViewById(R.id.editPrice);
        editSemester = findViewById(R.id.editSemester);
        editCondition = findViewById(R.id.editCondition);
        editPhone = findViewById(R.id.editPhone);
        updateBtn = findViewById(R.id.updateBookBtn);

        bookId = getIntent().getStringExtra("bookId");
        bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);

        // Pre-fill data
        editTitle.setText(getIntent().getStringExtra("title"));
        editAuthor.setText(getIntent().getStringExtra("author"));
        editPrice.setText(getIntent().getStringExtra("price"));
        editSemester.setText(getIntent().getStringExtra("semester"));
        editCondition.setText(getIntent().getStringExtra("condition"));
        editPhone.setText(getIntent().getStringExtra("phone"));

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        updateBtn.setOnClickListener(v -> updateBook());
    }

    private void updateBook() {
        String title = editTitle.getText().toString().trim();
        String author = editAuthor.getText().toString().trim();
        String price = editPrice.getText().toString().trim();
        String semester = editSemester.getText().toString().trim();
        String condition = editCondition.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (title.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Title and Phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("author", author);
        updates.put("price", price);
        updates.put("semester", semester);
        updates.put("condition", condition);
        updates.put("phone", phone);
        updates.put("status", "pending"); // Reset status to pending after edit

        bookRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditBookActivity.this, "Book Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditBookActivity.this, "Failed to update book", Toast.LENGTH_SHORT).show();
            }
        });
    }
}