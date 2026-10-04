package com.example.bookhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BookDetailsActivity extends AppCompatActivity {

    private TextView detailTitle, detailAuthor, detailPrice, detailSemester, detailCondition, ownerNameText, availabilityText;
    private View chatSellerBtn, backBtn;
    private Button buyBookBtn, lendRequestBtn;
    private ImageView interestBtn;
    private String sellerPhone = "";
    private String bookId = "";
    private boolean isInterested = false;
    private DatabaseReference interestedRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        currentUserId = FirebaseAuth.getInstance().getUid();

        detailTitle = findViewById(R.id.detailTitle);
        detailAuthor = findViewById(R.id.detailAuthor);
        detailPrice = findViewById(R.id.detailPrice);
        detailSemester = findViewById(R.id.detailSemester);
        detailCondition = findViewById(R.id.detailCondition);
        ownerNameText = findViewById(R.id.ownerNameText);
        availabilityText = findViewById(R.id.availabilityText);
        chatSellerBtn = findViewById(R.id.chatSellerBtn);
        buyBookBtn = findViewById(R.id.buyBookBtn);
        lendRequestBtn = findViewById(R.id.lendRequestBtn);
        backBtn = findViewById(R.id.backBtn);
        interestBtn = findViewById(R.id.interestBtn);

        Intent intent = getIntent();
        if (intent != null) {
            bookId = intent.getStringExtra("bookId");
            detailTitle.setText(intent.getStringExtra("title"));
            detailAuthor.setText("By " + intent.getStringExtra("author"));
            detailPrice.setText("৳" + intent.getStringExtra("price"));
            detailSemester.setText("🎓 Semester: " + intent.getStringExtra("semester"));
            detailCondition.setText("⭐ Condition: " + intent.getStringExtra("condition"));
            sellerPhone = intent.getStringExtra("phone");

            boolean isSold = intent.getBooleanExtra("isSold", false);
            if (isSold) {
                availabilityText.setText("SOLD");
                availabilityText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                availabilityText.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                buyBookBtn.setEnabled(false);
                buyBookBtn.setText("ALREADY SOLD");
            } else {
                availabilityText.setText("AVAILABLE");
                availabilityText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
            
            // ছবি লোডিং অপশন বন্ধ রাখা হয়েছে

            String sellerName = intent.getStringExtra("sellerName");
            if (sellerName != null && !sellerName.isEmpty()) {
                ownerNameText.setText(sellerName);
            } else {
                ownerNameText.setText("A Student");
            }

            if (currentUserId != null && bookId != null) {
                interestedRef = FirebaseDatabase.getInstance().getReference("InterestedBooks").child(currentUserId).child(bookId);
                checkInterestStatus();
            }
        }

        backBtn.setOnClickListener(v -> finish());

        interestBtn.setOnClickListener(v -> toggleInterest());

        chatSellerBtn.setOnClickListener(v -> {
            if (sellerPhone != null && !sellerPhone.isEmpty()) {
                String cleanNumber = sellerPhone.replaceAll("[^\\d]", "");
                Intent chatIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + cleanNumber));
                startActivity(chatIntent);
            } else {
                Toast.makeText(this, "Seller phone not available", Toast.LENGTH_SHORT).show();
            }
        });

        buyBookBtn.setOnClickListener(v -> {
            if (sellerPhone != null && !sellerPhone.isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + sellerPhone));
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "Seller phone not available", Toast.LENGTH_SHORT).show();
            }
        });

        lendRequestBtn.setOnClickListener(v -> {
            if (sellerPhone != null && !sellerPhone.isEmpty()) {
                String bookTitle = detailTitle.getText().toString();
                String message = "Hello, I am interested in borrowing your book: " + bookTitle + ". Is it available for lending?";
                String cleanNumber = sellerPhone.replaceAll("[^\\d]", "");
                Intent borrowIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + cleanNumber + "?text=" + Uri.encode(message)));
                startActivity(borrowIntent);
            } else {
                Toast.makeText(this, "Seller phone not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkInterestStatus() {
        interestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInterested = snapshot.exists();
                if (isInterested) {
                    interestBtn.setImageResource(android.R.drawable.btn_star_big_on);
                    interestBtn.setColorFilter(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    interestBtn.setImageResource(android.R.drawable.btn_star_big_off);
                    interestBtn.setColorFilter(getResources().getColor(android.R.color.darker_gray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void toggleInterest() {
        if (isInterested) {
            interestedRef.removeValue();
            Toast.makeText(this, "Removed from Interested", Toast.LENGTH_SHORT).show();
        } else {
            interestedRef.setValue(true);
            Toast.makeText(this, "Added to Interested", Toast.LENGTH_SHORT).show();
        }
    }
}