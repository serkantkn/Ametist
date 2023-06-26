package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.databinding.ChatListLayoutBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;

import java.util.ArrayList;
import java.util.Objects;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder>
{
    ArrayList<MessageModel> mlist;
    Context context;
    Activity activity;
    UserListener listener;

    public ChatListAdapter(ArrayList<MessageModel> mlist, Context context, Activity activity, UserListener listener) {
        this.mlist = mlist;
        this.context = context;
        this.activity = activity;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ChatListLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (Objects.equals(mlist.get(position).getSenderId(), FirebaseAuth.getInstance().getUid())) {
            holder.binding.lastMessage.setText(String.format("%s%s", activity.getString(R.string.you), mlist.get(position).getMessage()));
        } else {
            holder.binding.lastMessage.setText(mlist.get(position).getMessage());
        }
        holder.binding.lastMessage.setVisibility(View.VISIBLE);

        holder.binding.date.setText(TimeAgo.using(mlist.get(position).getTimestamp()));

        UserModel receiverUser = new UserModel();
        FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    if (documentSnapshot.getId().equals(mlist.get(position).getConversationId()))
                    {
                        receiverUser.setUserId(documentSnapshot.getId());
                        holder.binding.username.setText(documentSnapshot.getString("name"));
                        Glide.with(context).load(documentSnapshot.getString("profilePic")).placeholder(R.drawable.ic_person_blue).into(holder.binding.profileImage);
                    }
                }
            }
        });

        holder.binding.userCard.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale);
            holder.binding.userCard.startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_reverse);
                    holder.binding.userCard.startAnimation(anim);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            listener.onUserClicked(receiverUser);
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
        return mlist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        ChatListLayoutBinding binding;
        public ViewHolder(@NonNull ChatListLayoutBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
