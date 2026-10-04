package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBooks;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private DatabaseReference databaseBooks;
    private EditText searchEditText;
    private Button btn1st, btn2nd, btn3rd, btn4th;
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout navHome, navMyBooks, navAddBook, navProfile;
    private TextView btnSeeAll;
    private boolean isShowingAllBooks = false; // বর্তমানে সব বই দেখা যাচ্ছে কি না তার ট্র্যাকিং

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // সিস্টেম বার (Status Bar) এর জন্য প্যাডিং অ্যাডজাস্ট করা
        View topBar = findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(
                topBar,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(v.getPaddingLeft(), systemBars.top + 20, v.getPaddingRight(), v.getPaddingBottom());
                    return insets;
                }
        );


        // UI কম্পোনেন্টগুলোর রেফারেন্স নেওয়া
        recyclerViewBooks = findViewById(R.id.recyclerViewBooks);
        searchEditText = findViewById(R.id.searchEditText);
        btn1st = findViewById(R.id.btn1st);
        btn2nd = findViewById(R.id.btn2nd);
        btn3rd = findViewById(R.id.btn3rd);
        btn4th = findViewById(R.id.btn4th);
        btnSeeAll = findViewById(R.id.btnSeeAll);
        
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        navHome = findViewById(R.id.navHome);
        navMyBooks = findViewById(R.id.navMyBooks);
        navAddBook = findViewById(R.id.navAddBook);
        navProfile = findViewById(R.id.navProfile);

        databaseBooks = FirebaseDatabase.getInstance().getReference("Books");

        // RecyclerView এবং Adapter সেটআপ
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(this, bookList);
        recyclerViewBooks.setAdapter(bookAdapter);

        // ইনিশিয়াল ডাটা এবং ইউজার ইন্টারফেস আপডেট করা
        fetchBooksFromFirebase();
        updateNavHeader();
        setupNotificationBadge(findViewById(R.id.notificationCount));
        hideContactForAdmin();

        // মেনু আইকন ক্লিক করলে ড্রয়ার ওপেন হবে
        findViewById(R.id.menuIcon).setOnClickListener(v -> drawerLayout.openDrawer(androidx.core.view.GravityCompat.START));

        // নেভিগেশন ড্রয়ারের অপশন ক্লিক হ্যান্ডেল করা
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) startActivity(new Intent(HomeActivity.this, Profile.class));
            else if (id == R.id.nav_upload) startActivity(new Intent(HomeActivity.this, AddBookActivity.class));
            else if (id == R.id.nav_logout) logoutUser();
            drawerLayout.closeDrawers();
            return true;
        });

        // লাইভ সার্চ টেক্সট ওয়াচার
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ফিল্টার বাটন ক্লিক লজিক
        btn1st.setOnClickListener(v -> filterBySemester("1st"));
        btn2nd.setOnClickListener(v -> filterBySemester("2nd"));
        btn3rd.setOnClickListener(v -> filterBySemester("3rd"));
        btn4th.setOnClickListener(v -> filterBySemester("4th"));

        // "See All / See Less" বাটন ক্লিকের মাধ্যমে লিস্ট ছোট-বড় করা
        btnSeeAll.setOnClickListener(v -> {
            if (!searchEditText.getText().toString().isEmpty()) {
                searchEditText.setText(""); 
                isShowingAllBooks = true;
                bookAdapter.updateList(bookList);
                btnSeeAll.setText("See Less");
            } else {
                isShowingAllBooks = !isShowingAllBooks;
                if (isShowingAllBooks) {
                    bookAdapter.updateList(bookList);
                    btnSeeAll.setText("See Less");
                } else {
                    showLimitedBooks();
                    btnSeeAll.setText("See All");
                }
            }
        });

        // পপুলার কোর্স ক্লিক লজিক
        findViewById(R.id.courseDataStructure).setOnClickListener(v -> filter("Data Structure"));
        findViewById(R.id.courseDatabase).setOnClickListener(v -> filter("Database"));
        findViewById(R.id.courseNetworks).setOnClickListener(v -> filter("Computer Networks"));

        // বটম নেভিগেশন অ্যাকশন
        navMyBooks.setOnClickListener(v -> startActivity(new Intent(this, MyBooksActivity.class)));
        navAddBook.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
    }

    // ড্রয়ারের হেডার সেকশনে ইউজারের নাম দেখানো
    private void updateNavHeader() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null && user.getName() != null && navigationView.getHeaderCount() > 0) {
                                    TextView nameText = navigationView.getHeaderView(0).findViewById(R.id.navHeaderName);
                                    if (nameText != null) nameText.setText("Welcome " + user.getName());
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    // নোটিফিকেশন ব্যাজ আপডেট করা (আনরিড মেসেজ সংখ্যা)
    private void setupNotificationBadge(TextView badge) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && badge != null) {
            FirebaseDatabase.getInstance().getReference("Notifications").child(user.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int count = 0;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Notification n = ds.getValue(Notification.class);
                                if (n != null && !n.isRead()) count++;
                            }
                            if (count > 0) {
                                badge.setText(String.valueOf(count));
                                badge.setVisibility(View.VISIBLE);
                            } else badge.setVisibility(View.GONE);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    // অ্যাডমিন হলে কন্টাক্ট অপশন হাইড করা
    private void hideContactForAdmin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && "eshaofficial800@gmail.com".equalsIgnoreCase(user.getEmail())) {
            navigationView.getMenu().findItem(R.id.nav_contact).setVisible(false);
        }
    }

    // ফায়ারবেস থেকে বই লোড করা এবং রিয়েল-টাইম আপডেট হ্যান্ডেল করা
    private void fetchBooksFromFirebase() {
        databaseBooks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookList.clear();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = (user != null) ? user.getUid() : "";

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Book book = ds.getValue(Book.class);
                    if (book != null && !book.isSold()) {
                        String status = book.getStatus();
                        boolean isApproved = status == null || status.equalsIgnoreCase("approved");
                        boolean isMine = !uid.isEmpty() && uid.equals(book.getSellerId());
                        
                        if (isApproved || isMine) {
                            bookList.add(0, book);
                        }
                    }
                }

                if (isShowingAllBooks) {
                    bookAdapter.updateList(bookList);
                    btnSeeAll.setText("See Less");
                    btnSeeAll.setVisibility(bookList.size() > 4 ? View.VISIBLE : View.GONE);
                } else {
                    showLimitedBooks();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // হোম পেজে বইয়ের সংখ্যা লিমিট করা (প্রাথমিকভাবে ৪টি)
    private void showLimitedBooks() {
        if (bookList.size() > 4) {
            btnSeeAll.setVisibility(View.VISIBLE);
            btnSeeAll.setText("See All");
            bookAdapter.updateList(new ArrayList<>(bookList.subList(0, 4)));
        } else {
            btnSeeAll.setVisibility(View.GONE);
            bookAdapter.updateList(bookList);
        }
    }

    // সেমিস্টার ফিল্টার লজিক
    private void filterBySemester(String semester) {
        List<Book> filteredList = new ArrayList<>();
        for (Book item : bookList) {
            if (item.getSemester() != null && item.getSemester().equalsIgnoreCase(semester)) {
                filteredList.add(item);
            }
        }
        bookAdapter.filterList(filteredList);
        btnSeeAll.setVisibility(View.VISIBLE);
        btnSeeAll.setText("See All");
        isShowingAllBooks = false;
    }

    // সার্চ ফিল্টার লজিক
    private void filter(String text) {
        if (text.isEmpty()) {
            if (isShowingAllBooks) {
                bookAdapter.updateList(bookList);
                btnSeeAll.setText("See Less");
                btnSeeAll.setVisibility(bookList.size() > 4 ? View.VISIBLE : View.GONE);
            } else showLimitedBooks();
            return;
        }
        
        List<Book> filteredList = new ArrayList<>();
        for (Book item : bookList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                    item.getAuthor().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        bookAdapter.filterList(filteredList);
        btnSeeAll.setVisibility(View.VISIBLE);
        btnSeeAll.setText("See All");
        isShowingAllBooks = false;
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        finish();
    }
}