package com.example.manga_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Modelos.VolumenResponse;
import com.example.manga_project.R;
import com.example.manga_project.SectionVolumen;
import java.util.List;

public class SectionVolumenAdapter extends RecyclerView.Adapter<SectionVolumenAdapter.SectionViewHolder> {
    private List<SectionVolumen> sectionList;
    private Context context;

    public SectionVolumenAdapter(List<SectionVolumen> sectionList, Context context) {
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
        SectionVolumen section = sectionList.get(position);
        holder.sectionTitle.setText(section.getTitle());
        // Botón "más" puede tener funcionalidad extra si lo deseas
        VolumenAdapter volumenAdapter = new VolumenAdapter(section.getVolumenes(), context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(volumenAdapter);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        RecyclerView recyclerView;
        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.mCategoryTitle);
            recyclerView = itemView.findViewById(R.id.mChildRvBooks);
        }
    }
}

