package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.FullProfilePhotoActivity;
import com.serkantken.ametist.databinding.LayoutEmptyChatBinding;
import com.serkantken.ametist.databinding.LayoutReceivedMessageBinding;
import com.serkantken.ametist.databinding.LayoutReceivedPhotoBinding;
import com.serkantken.ametist.databinding.LayoutSendMessageBinding;
import com.serkantken.ametist.databinding.LayoutSendPhotoBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;

import java.util.ArrayList;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter
{
    ArrayList<MessageModel> messageModels;
    Context context;
    Activity activity;
    String receiverId;
    int EMPTY_VIEW_TYPE = 5;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    int PHOTO_SENDER_VIEW_TYPE = 3;
    int PHOTO_RECEIVER_VIEW_TYPE = 4;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, Activity activity)
    {
        this.messageModels = messageModels;
        this.context = context;
        this.activity = activity;
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
        else if (viewType == PHOTO_SENDER_VIEW_TYPE)
        {
            return new PhotoSenderViewHolder(LayoutSendPhotoBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        }
        else if (viewType == RECEIVER_VIEW_TYPE)
        {
            return new ReceiverViewHolder(LayoutReceivedMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        }
        else if (viewType == PHOTO_RECEIVER_VIEW_TYPE)
        {
            return new PhotoReceiverViewHolder(LayoutReceivedPhotoBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        }
        else
        {
            return new EmptyChatViewHolder(LayoutEmptyChatBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if (getItemCount() == 0)
        {
            return EMPTY_VIEW_TYPE;
        }
        else
        {
            if (Objects.equals(messageModels.get(position).getSenderId(), FirebaseAuth.getInstance().getUid()))
            {
                if (Objects.equals(messageModels.get(position).getPhoto(), "null"))
                {
                    return SENDER_VIEW_TYPE;
                }
                else
                {
                    return PHOTO_SENDER_VIEW_TYPE;
                }
            }
            else
            {
                if (Objects.equals(messageModels.get(position).getPhoto(), "null"))
                {
                    return RECEIVER_VIEW_TYPE;
                }
                else
                {
                    return PHOTO_RECEIVER_VIEW_TYPE;
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        MessageModel messageModel = messageModels.get(position);

        if (holder.getClass() == SenderViewHolder.class)
        {
            if (position == 0)
            {
                ((SenderViewHolder) holder).binding.container.setPadding(0, sizeInPixel(70), 0, 0);
            }
            ((SenderViewHolder)holder).binding.sentMessage.setText(messageModel.getMessage());
            ((SenderViewHolder)holder).binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
        }
        else if (holder.getClass() == ReceiverViewHolder.class)
        {
            if (position == 0)
            {
                ((ReceiverViewHolder) holder).binding.container.setPadding(0, sizeInPixel(70), 0, 0);
            }
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
        else if (holder.getClass() == PhotoSenderViewHolder.class)
        {
            if (position == 0)
            {
                ((PhotoSenderViewHolder) holder).binding.container.setPadding(0, sizeInPixel(70), 0, 0);
            }
            if (Objects.equals(messageModel.getMessage(), ""))
            {
                ((PhotoSenderViewHolder)holder).binding.sentMessage.setVisibility(View.GONE);
            }
            else
            {
                ((PhotoSenderViewHolder)holder).binding.sentMessage.setText(messageModel.getMessage());
            }
            ((PhotoSenderViewHolder)holder).binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            Glide.with(context).load(messageModel.getPhoto()).into(((PhotoSenderViewHolder)holder).binding.sentPhoto);
            ((PhotoSenderViewHolder)holder).binding.sentPhoto.setOnClickListener(view -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", messageModel.getPhoto());
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, ((PhotoSenderViewHolder) holder).binding.sentPhoto, "photograph");
                context.startActivity(intent, optionsCompat.toBundle());
            });
        }
        else
        {
            if (position == 0)
            {
                ((PhotoReceiverViewHolder)holder).binding.container.setPadding(0, sizeInPixel(70), 0, 0);
            }
            if (Objects.equals(messageModel.getMessage(), ""))
            {
                ((PhotoReceiverViewHolder)holder).binding.receivedMessage.setVisibility(View.GONE);
            }
            else
            {
                ((PhotoReceiverViewHolder)holder).binding.receivedMessage.setText(messageModel.getMessage());
            }
            ((PhotoReceiverViewHolder)holder).binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
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
                            .into(((PhotoReceiverViewHolder)holder).binding.profileImage);
                    Glide.with(context).load(messageModel.getPhoto()).into(((PhotoReceiverViewHolder)holder).binding.receivedPhoto);
                    ((PhotoReceiverViewHolder)holder).binding.username.setText(userModel.getName());
                    ((PhotoReceiverViewHolder)holder).binding.receivedPhoto.setOnClickListener(view -> {
                        Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                        intent.putExtra("pictureUrl", messageModel.getPhoto());
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, ((PhotoReceiverViewHolder) holder).binding.receivedPhoto, "photograph");
                        context.startActivity(intent, optionsCompat.toBundle());
                    });
                }
            });
        }
    }

    private int sizeInPixel(int sizeInDp)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(sizeInDp * density);
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

    public static class PhotoReceiverViewHolder extends RecyclerView.ViewHolder
    {
        LayoutReceivedPhotoBinding binding;

        public PhotoReceiverViewHolder(@NonNull LayoutReceivedPhotoBinding itemView)
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

    public static class PhotoSenderViewHolder extends RecyclerView.ViewHolder
    {
        LayoutSendPhotoBinding binding;

        public PhotoSenderViewHolder(@NonNull LayoutSendPhotoBinding itemView)
        {
            super(itemView.getRoot());
            binding = itemView;
        }
    }

    public static class EmptyChatViewHolder extends RecyclerView.ViewHolder
    {
        LayoutEmptyChatBinding binding;

        public  EmptyChatViewHolder(@NonNull LayoutEmptyChatBinding itemView)
        {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
