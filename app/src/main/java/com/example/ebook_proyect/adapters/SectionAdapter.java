package com.example.ebook_proyect.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ebook_proyect.Api_cliente.Libro;

import com.example.ebook_proyect.activities.DescriptionBookActivity;
import com.example.ebook_proyect.R;
import com.example.ebook_proyect.Section;

import java.util.ArrayList;
import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {
    private List<Section> sectionList;
    private Context context;

    public SectionAdapter(List<Section> sectionList, Context context) {
        this.sectionList = sectionList;
        this.context = context;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);

        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sectionList.get(position);
        holder.categoryTitle.setText(section.getTitle());

        // RecyclerView de libros
        holder.booksRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        // Pasar lista vacía como purchasedBooks y añadir el nuevo parámetro boolean
        LibroAdapter libroAdapter = new LibroAdapter(section.getLibros(), new ArrayList<>(), context, (view, bookId) -> {
            Intent intent = new Intent(context, DescriptionBookActivity.class);
            intent.putExtra("BOOK_ID", bookId);
            context.startActivity(intent);
        }, false); // `false` indica que no es la sección de guardados

        holder.booksRecyclerView.setAdapter(libroAdapter);
    }



    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        RecyclerView booksRecyclerView;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.mCategoryTitle);
            booksRecyclerView = itemView.findViewById(R.id.mChildRvBooks);
        }
    }
}