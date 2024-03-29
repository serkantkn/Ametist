package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.UserListLayoutBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    ArrayList<UserModel> list;
    Context context;
    UserListener listener;
    FirebaseFirestore database;
    Utilities utilities;

    public UsersAdapter(ArrayList<UserModel> list, Context context, Utilities utilities, UserListener listener) {
        this.list = list;
        this.context = context;
        this.utilities = utilities;
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
        switch (list.get(position).getGender()) {
            case "0":
                holder.binding.profileImage.setBackground(AppCompatResources.getDrawable(context, R.drawable.purple_gradient));
                break;
            case "1":
                holder.binding.profileImage.setBackground(AppCompatResources.getDrawable(context, R.drawable.blue_gradient));
                break;
            case "2":
                holder.binding.profileImage.setBackground(AppCompatResources.getDrawable(context, R.drawable.red_gradient));
                break;
        }
        utilities.blur(new BlurView[]{holder.binding.blur}, 3f, false);
        holder.binding.username.setText(list.get(position).getName());
        holder.binding.username.setSelected(true);
        holder.binding.getRoot().setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale);
            holder.binding.getRoot().startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_reverse);
                    holder.binding.getRoot().startAnimation(anim);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            listener.onUserClicked(list.get(holder.getAdapterPosition()));
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        });
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
