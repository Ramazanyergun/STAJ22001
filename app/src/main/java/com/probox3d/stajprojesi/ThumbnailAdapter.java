package com.probox3d.stajprojesi;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private final List<Uri> thumbnailUris;
    private final Context context;

    public ThumbnailAdapter(Context context, List<Uri> thumbnailUris) {
        this.context = context;
        this.thumbnailUris = thumbnailUris;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        Uri uri = thumbnailUris.get(position);
        holder.imageView.setImageURI(uri);

        holder.itemView.setOnClickListener(v -> {
            // Resme tıklandığında yapılacak işlemler (örneğin, tam ekran gösterme)
        });
    }

    @Override
    public int getItemCount() {
        return thumbnailUris.size();
    }

    public void addThumbnail(Uri uri) {
        thumbnailUris.add(uri);
        notifyItemInserted(thumbnailUris.size() - 1);
    }

    static class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewThumbnail);
        }
    }
}
