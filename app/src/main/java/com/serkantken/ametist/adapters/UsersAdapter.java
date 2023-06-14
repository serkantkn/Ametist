package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.UserListLayoutBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    ArrayList<UserModel> list;
    Context context;
    Activity activity;
    UserListener listener;
    FirebaseFirestore database;

    public UsersAdapter(ArrayList<UserModel> list, Context context, Activity activity, UserListener listener) {
        this.list = list;
        this.context = context;
        this.activity = activity;
        this.listener = listener;
        database = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(UserListLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(list.get(position).getProfilePic()).placeholder(R.drawable.ic_person).into(holder.binding.profileImage);
        holder.binding.username.setText(list.get(position).getName());
        holder.binding.getRoot().setOnClickListener(view -> listener.onUserClicked(list.get(position)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        UserListLayoutBinding binding;

        public ViewHolder(UserListLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
