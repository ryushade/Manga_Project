package com.example.ebook_proyect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook_proyect.Book;
import com.example.ebook_proyect.R;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private List<Book> filteredBookList;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
        this.filteredBookList = new ArrayList<>(bookList); // Lista filtrada inicializada con todos los libros
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_description, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = filteredBookList.get(position);
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.descriptionTextView.setText(book.getDescription());
        holder.imageView.setImageResource(book.getImageResId());
    }

    @Override
    public int getItemCount() {
        return filteredBookList.size();
    }

    public void filter(String query) {
        filteredBookList.clear();
        if (query.isEmpty()) {
            filteredBookList.addAll(bookList);
        } else {
            query = query.toLowerCase();
            for (Book book : bookList) {
                if (book.getTitle().toLowerCase().contains(query) ||
                        book.getAuthor().toLowerCase().contains(query)) {
                    filteredBookList.add(book);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView authorTextView;
        TextView descriptionTextView;
        ImageView imageView;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
