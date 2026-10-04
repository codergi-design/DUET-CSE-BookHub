package com.example.bookhub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private List<Book> bookList; // ডাটাবেস থেকে আসা বইয়ের লিস্ট

    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    // ফিল্টার করা লিস্ট আপডেট করার জন্য (যেমন: সার্চ বা সেমিস্টার ফিল্টার)
    public void filterList(List<Book> filteredList) {
        this.bookList = filteredList;
        notifyDataSetChanged(); // RecyclerView কে জানানো যে ডাটা বদলেছে
    }

    // সম্পূর্ণ নতুন লিস্ট সেট করার জন্য
    public void updateList(List<Book> newList) {
        this.bookList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_book.xml লেআউটটি ইন ফ্লেট করা
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // বইয়ের তথ্যগুলো টেক্সটভিউতে সেট করা
        if (holder.title != null) holder.title.setText(book.getTitle());
        if (holder.author != null) holder.author.setText(book.getAuthor());
        if (holder.price != null) holder.price.setText("৳" + book.getPrice());

        // বইয়ের স্ট্যাটাস (Approved/Rejected/Pending) হ্যান্ডেল করা
        if (book.getStatus() != null) {
            holder.statusText.setVisibility(View.VISIBLE);
            String status = book.getStatus();
            holder.statusText.setText("Status: " + status.toUpperCase());
            
            // স্ট্যাটাস অনুযায়ী টেক্সট কালার পরিবর্তন করা
            if (status.equals("approved")) holder.statusText.setTextColor(android.graphics.Color.parseColor("#10B981"));
            else if (status.equals("rejected")) holder.statusText.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            else holder.statusText.setTextColor(android.graphics.Color.parseColor("#F97316"));
        } else {
            holder.statusText.setVisibility(View.GONE);
        }

        // ছবি লোডিং সরিয়ে দেওয়া হয়েছে

        // --- কন্টেক্সট অনুযায়ী বাটন কন্ট্রোল (Admin vs Student) ---
        
        if (context instanceof MyBooksActivity) {
            // ইউজারের নিজের বইয়ের পেজ
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.editBookBtn.setVisibility(View.VISIBLE);
            holder.approveBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
            holder.markSoldBtn.setVisibility((!book.isSold() && !"rejected".equals(book.getStatus())) ? View.VISIBLE : View.GONE);
        } else if (context instanceof ManageBooksActivity) {
            // অ্যাডমিনের বই ম্যানেজমেন্ট পেজ
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.statusText.setVisibility(View.VISIBLE);
            
            // স্ট্যাটাস অনুযায়ী Approve/Reject বাটন শো করা
            String status = book.getStatus();
            if (status == null || "pending".equals(status)) {
                holder.approveBtn.setVisibility(View.VISIBLE);
                holder.rejectBtn.setVisibility(View.VISIBLE);
            } else {
                holder.approveBtn.setVisibility("rejected".equals(status) ? View.VISIBLE : View.GONE);
                holder.rejectBtn.setVisibility("approved".equals(status) ? View.VISIBLE : View.GONE);
            }
        } else {
            // সাধারণ হোম পেজ (অন্যদের বই দেখার জন্য)
            holder.markSoldBtn.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
            holder.approveBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
            holder.statusText.setVisibility(View.GONE);
        }

        // --- বাটন ক্লিক লিসেনার্স ---

        // অ্যাডমিন অ্যাপ্রুভ বাটনে ক্লিক করলে ডাটাবেসে স্ট্যাটাস আপডেট হবে
        holder.approveBtn.setOnClickListener(v -> {
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Books")
                    .child(book.getId()).child("status").setValue("approved")
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Book Approved", Toast.LENGTH_SHORT).show());
        });

        // পুরো আইটেমে ক্লিক করলে বইয়ের ডিটেইলস পেজে নিয়ে যাবে
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);
            intent.putExtra("bookId", book.getId());
            intent.putExtra("title", book.getTitle());
            intent.putExtra("author", book.getAuthor());
            intent.putExtra("price", book.getPrice());
            intent.putExtra("semester", book.getSemester());
            intent.putExtra("condition", book.getCondition());
            intent.putExtra("phone", book.getPhone());
            intent.putExtra("sellerName", book.getSellerName());
            intent.putExtra("isSold", book.isSold());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    // ViewHolder ক্লাস যা UI এলিমেন্টগুলো হোল্ড করে রাখে
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView title, author, price, markSoldBtn, relistBtn, deleteBtn, statusText, approveBtn, rejectBtn, editBookBtn, restoreBtn;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            price = itemView.findViewById(R.id.bookPrice);
            markSoldBtn = itemView.findViewById(R.id.markSoldBtn);
            relistBtn = itemView.findViewById(R.id.relistBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            statusText = itemView.findViewById(R.id.bookStatus);
            approveBtn = itemView.findViewById(R.id.approveBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
            editBookBtn = itemView.findViewById(R.id.editBookBtn);
            restoreBtn = itemView.findViewById(R.id.restoreBtn);
        }
    }
}
