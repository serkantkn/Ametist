package com.serkantken.ametist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.serkantken.ametist.databinding.PhotoItemBinding;
import com.serkantken.ametist.models.PhotoModel;
import com.serkantken.ametist.utilities.PhotoListener;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private final ArrayList<PhotoModel> photoList;
    private final Context context;
    private final PhotoListener listener;

    public PhotoAdapter(ArrayList<PhotoModel> photoList, Context context, PhotoListener listener) {
        this.photoList = photoList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(PhotoItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(photoList.get(position).getLink()).into(holder.binding.profileImage);

        holder.binding.profileImage.setOnClickListener(v -> listener.onClick(photoList.get(holder.getAdapterPosition()).getLink()));

        holder.binding.profileImage.setOnLongClickListener(v -> {
            listener.onRemove(photoList.get(holder.getAdapterPosition()).getDate(), holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PhotoItemBinding binding;
        public ViewHolder(@NonNull PhotoItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
