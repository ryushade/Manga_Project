package com.example.manga_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.R;
import com.example.manga_project.SectionVolumen;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ItemViewHolder> {
    private final List<SectionVolumen> items;
    private final Context context;

    public HomeAdapter(Context context, List<SectionVolumen> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        SectionVolumen item = items.get(position);
        holder.bind(item, context);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSectionTitle;
        private final RecyclerView rvBooks;
        private VolumenAdapter volumenAdapter;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.mCategoryTitle);
            rvBooks = itemView.findViewById(R.id.mChildRvBooks);
            rvBooks.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvBooks.setHasFixedSize(true);
            volumenAdapter = new VolumenAdapter(null);
            rvBooks.setAdapter(volumenAdapter);
        }
        public void bind(SectionVolumen item, Context context) {
            tvSectionTitle.setText(item.getTitle());
            volumenAdapter.setVolumenes(item.getVolumenes());
        }
    }
}
