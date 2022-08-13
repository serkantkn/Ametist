package com.serkantken.ametist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.LayoutReceivedMessageBinding;
import com.serkantken.ametist.databinding.LayoutSendMessageBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter
{
    ArrayList<MessageModel> messageModels;
    Context context;
    String receiverId;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context)
    {
        this.messageModels = messageModels;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == SENDER_VIEW_TYPE)
        {
            return new SenderViewHolder(LayoutSendMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        }
        else
        {
            return new ReceiverViewHolder(LayoutReceivedMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if (messageModels.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid()))
        {
            return SENDER_VIEW_TYPE;
        }
        else
        {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        MessageModel messageModel = messageModels.get(position);

        if (holder.getClass() == SenderViewHolder.class)
        {
            ((SenderViewHolder)holder).binding.sentMessage.setText(messageModel.getMessage());
            ((SenderViewHolder)holder).binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
        }
        else
        {
            ((ReceiverViewHolder)holder).binding.receivedMessage.setText(messageModel.getMessage());
            ((ReceiverViewHolder)holder).binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                    {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId()))
                        {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(((ReceiverViewHolder)holder).binding.profileImage);
                    ((ReceiverViewHolder)holder).binding.username.setText(userModel.getName());
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return messageModels.size();
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder
    {
        LayoutReceivedMessageBinding binding;

        public ReceiverViewHolder(@NonNull LayoutReceivedMessageBinding itemView)
        {
            super(itemView.getRoot());
            binding = itemView;
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder
    {
        LayoutSendMessageBinding binding;

        public SenderViewHolder(@NonNull LayoutSendMessageBinding itemView)
        {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
