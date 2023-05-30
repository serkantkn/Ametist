package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
import com.serkantken.ametist.utilities.MessageListener;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.skydoves.balloon.OnBalloonDismissListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import eightbitlab.com.blurview.BlurView;

public class ChatAdapter extends RecyclerView.Adapter
{
    ArrayList<MessageModel> messageModels;
    Context context;
    Activity activity;
    String receiverId;

    FirebaseFirestore database;
    private Balloon balloon;
    private Utilities utilities;
    private MessageListener messageListener;
    int EMPTY_VIEW_TYPE = 5;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    int PHOTO_SENDER_VIEW_TYPE = 3;
    int PHOTO_RECEIVER_VIEW_TYPE = 4;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, Activity activity, MessageListener messageListener, FirebaseFirestore database)
    {
        this.messageModels = messageModels;
        this.context = context;
        this.activity = activity;
        this.messageListener = messageListener;
        this.database = database;
        utilities = new Utilities(context, activity);
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

            if (messageModel.hasReply())
            {
                ((SenderViewHolder)holder).binding.replyMessageContainer.setVisibility(View.VISIBLE);
                ((SenderViewHolder)holder).binding.repliedMessage.setText(messageModel.getRepliedMessage());
                if (messageModel.isReplyHasPhoto())
                {
                    Glide.with(context).load(messageModel.getRepliedPhoto()).into(((SenderViewHolder)holder).binding.repliedPhoto);
                    ((SenderViewHolder)holder).binding.repliedPhoto.setVisibility(View.VISIBLE);
                }
            }
            ((SenderViewHolder)holder).binding.sentMessage.setText(messageModel.getMessage());
            ((SenderViewHolder)holder).binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));

            ((SenderViewHolder)holder).binding.container.setOnLongClickListener(v -> {
                showOptions(((SenderViewHolder)holder).binding.card, ((SenderViewHolder)holder).binding.cardBackground, true, messageModel, null, false);
                return true;
            });

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen")))
                        {
                            ((SenderViewHolder)holder).binding.seenCheck.setVisibility(View.VISIBLE);
                            ((SenderViewHolder)holder).binding.seenCheck.playAnimation();
                        }
                        else
                        {
                            ((SenderViewHolder)holder).binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
        else if (holder.getClass() == ReceiverViewHolder.class)
        {
            if (position == 0)
            {
                ((ReceiverViewHolder) holder).binding.container.setPadding(0, sizeInPixel(70), 0, 0);
            }

            if (messageModel.hasReply())
            {
                ((ReceiverViewHolder)holder).binding.replyMessageContainer.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder)holder).binding.repliedMessage.setText(messageModel.getRepliedMessage());
                if (messageModel.isReplyHasPhoto())
                {
                    Glide.with(context).load(messageModel.getRepliedPhoto()).into(((ReceiverViewHolder)holder).binding.repliedPhoto);
                    ((ReceiverViewHolder)holder).binding.repliedPhoto.setVisibility(View.VISIBLE);
                }
            }
            ((ReceiverViewHolder)holder).binding.receivedMessage.setText(messageModel.getMessage());
            ((ReceiverViewHolder)holder).binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            AtomicReference<String> profilePic = new AtomicReference<>();
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                    {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId()))
                        {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(((ReceiverViewHolder)holder).binding.profileImage);
                    ((ReceiverViewHolder)holder).binding.username.setText(userModel.getName());
                }
            });
            ((ReceiverViewHolder)holder).binding.container.setOnLongClickListener(v -> {
                showOptions(((ReceiverViewHolder)holder).binding.card, ((ReceiverViewHolder)holder).binding.cardBackground, false, messageModel, profilePic.get(), false);
                return true;
            });
        }
        else if (holder.getClass() == PhotoSenderViewHolder.class)
        {
            if (position == 0)
            {
                ((PhotoSenderViewHolder) holder).binding.container.setPadding(0, sizeInPixel(70), 0, 0);
            }

            if (messageModel.hasReply())
            {
                ((PhotoSenderViewHolder)holder).binding.replyMessageContainer.setVisibility(View.VISIBLE);
                ((PhotoSenderViewHolder)holder).binding.repliedMessage.setText(messageModel.getRepliedMessage());
                if (messageModel.isReplyHasPhoto())
                {
                    Glide.with(context).load(messageModel.getRepliedPhoto()).into(((PhotoSenderViewHolder)holder).binding.repliedPhoto);
                    ((PhotoSenderViewHolder)holder).binding.repliedPhoto.setVisibility(View.VISIBLE);
                }
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
            ((PhotoSenderViewHolder)holder).binding.sentPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", messageModel.getPhoto());
                context.startActivity(intent);
            });

            ((PhotoSenderViewHolder)holder).binding.container.setOnLongClickListener(v -> {
                showOptions(((PhotoSenderViewHolder)holder).binding.card, ((PhotoSenderViewHolder)holder).binding.cardBackground, true, messageModel, null, true);
                return true;
            });

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen")))
                        {
                            ((PhotoSenderViewHolder)holder).binding.seenCheck.setVisibility(View.VISIBLE);
                            ((PhotoSenderViewHolder)holder).binding.seenCheck.playAnimation();
                        }
                        else
                        {
                            ((PhotoSenderViewHolder)holder).binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
        else
        {
            if (position == 0)
            {
                ((PhotoReceiverViewHolder)holder).binding.container.setPadding(0, sizeInPixel(70), 0, 0);
            }

            if (messageModel.hasReply())
            {
                ((PhotoReceiverViewHolder)holder).binding.replyMessageContainer.setVisibility(View.VISIBLE);
                ((PhotoReceiverViewHolder)holder).binding.repliedMessage.setText(messageModel.getRepliedMessage());
                if (messageModel.isReplyHasPhoto())
                {
                    Glide.with(context).load(messageModel.getRepliedPhoto()).into(((PhotoReceiverViewHolder)holder).binding.repliedPhoto);
                    ((PhotoReceiverViewHolder)holder).binding.repliedPhoto.setVisibility(View.VISIBLE);
                }
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
            AtomicReference<String> profilePic = new AtomicReference<>();
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                    {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId()))
                        {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(((PhotoReceiverViewHolder)holder).binding.profileImage);
                    Glide.with(context).load(messageModel.getPhoto()).into(((PhotoReceiverViewHolder)holder).binding.receivedPhoto);
                    ((PhotoReceiverViewHolder)holder).binding.username.setText(userModel.getName());
                }
            });
            ((PhotoReceiverViewHolder)holder).binding.receivedPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", messageModel.getPhoto());
                context.startActivity(intent);
            });
            ((PhotoReceiverViewHolder)holder).binding.container.setOnLongClickListener(v -> {
                showOptions(((PhotoReceiverViewHolder)holder).binding.card, ((PhotoReceiverViewHolder)holder).binding.cardBackground, false, messageModel, profilePic.get(), true);
                return true;
            });
        }
    }

    private int sizeInPixel(int sizeInDp)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(sizeInDp * density);
    }

    private void showOptions(View view, View background, boolean isSender, MessageModel messageModel, String profilePic, boolean isPhoto)
    {
        showBalloon(view, 1);
        BlurView blurView = balloon.getContentView().findViewById(R.id.blur);
        //utilities.blur(blurView, 10f, false);

        ConstraintLayout buttonAnswer = balloon.getContentView().findViewById(R.id.button_answer);
        ConstraintLayout buttonDelete = balloon.getContentView().findViewById(R.id.button_delete);
        background.setBackground(context.getResources().getDrawable(R.drawable.yellow_gradient, null));

        if (isSender)
        {
            buttonDelete.setVisibility(View.VISIBLE);

            buttonDelete.setOnClickListener(v -> {
                Toast.makeText(context, "Çok yakında", Toast.LENGTH_SHORT).show();
                balloon.dismiss();
            });
        }

        buttonAnswer.setOnClickListener(v -> {
            messageListener.onMessageReplied(messageModel, profilePic, isPhoto);
            balloon.dismiss();
        });

        balloon.setOnBalloonDismissListener(() -> {
            if (isSender)
            {
                background.setBackground(context.getResources().getDrawable(R.drawable.purple_gradient, null));
            }
            else
            {
                background.setBackground(context.getResources().getDrawable(R.drawable.blue_gradient, null));
            }
        });
    }

    private void showBalloon(View view, int position)
    {
        balloon = new Balloon.Builder(context)
                .setLayout(R.layout.layout_message_options)
                .setArrowSize(0)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                .build();
        switch (position)
        {
            case 1:
                balloon.showAlignTop(view);
                break;
            case 2:
                balloon.showAlignRight(view);
                break;
            case 3:
                balloon.showAlignBottom(view);
                break;
            case 4:
                balloon.showAlignLeft(view);
                break;
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
