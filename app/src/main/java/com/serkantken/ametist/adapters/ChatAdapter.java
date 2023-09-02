package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.serkantken.ametist.databinding.LayoutReceivedVoiceBinding;
import com.serkantken.ametist.databinding.LayoutRepliedReceivedMessageBinding;
import com.serkantken.ametist.databinding.LayoutRepliedReceivedPhotoBinding;
import com.serkantken.ametist.databinding.LayoutRepliedReceivedVoiceBinding;
import com.serkantken.ametist.databinding.LayoutRepliedSendMessageBinding;
import com.serkantken.ametist.databinding.LayoutRepliedSendPhotoBinding;
import com.serkantken.ametist.databinding.LayoutRepliedSendVoiceBinding;
import com.serkantken.ametist.databinding.LayoutSendMessageBinding;
import com.serkantken.ametist.databinding.LayoutSendPhotoBinding;
import com.serkantken.ametist.databinding.LayoutSendVoiceBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.MessageListener;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import eightbitlab.com.blurview.BlurView;

public class ChatAdapter extends RecyclerView.Adapter {
    ArrayList<MessageModel> messageModels;
    Context context;
    static Activity activity;
    FirebaseFirestore database;
    private static Balloon balloon;
    private MessageListener messageListener;
    int EMPTY_VIEW_TYPE = 0;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    int PHOTO_SENDER_VIEW_TYPE = 3;
    int PHOTO_RECEIVER_VIEW_TYPE = 4;
    int REPLIED_SENDER_VIEW_TYPE = 5;
    int REPLIED_RECEIVER_VIEW_TYPE = 6;
    int REPLIED_PHOTO_SENDER_VIEW_TYPE = 7;
    int REPLIED_PHOTO_RECEIVER_VIEW_TYPE = 8;
    int VOICE_SENDER_VIEW_TYPE = 9;
    int VOICE_RECEIVER_VIEW_TYPE = 10;
    int REPLIED_VOICE_SENDER_VIEW_TYPE = 11;
    int REPLIED_VOICE_RECEIVER_VIEW_TYPE = 12;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, Activity activity, MessageListener messageListener, FirebaseFirestore database) {
        this.messageModels = messageModels;
        this.context = context;
        this.activity = activity;
        this.messageListener = messageListener;
        this.database = database;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            return new SenderViewHolder(LayoutSendMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == PHOTO_SENDER_VIEW_TYPE) {
            return new PhotoSenderViewHolder(LayoutSendPhotoBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == RECEIVER_VIEW_TYPE) {
            return new ReceiverViewHolder(LayoutReceivedMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == PHOTO_RECEIVER_VIEW_TYPE) {
            return new PhotoReceiverViewHolder(LayoutReceivedPhotoBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == REPLIED_SENDER_VIEW_TYPE) {
            return new RepliedSenderViewHolder(LayoutRepliedSendMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == REPLIED_RECEIVER_VIEW_TYPE) {
            return new RepliedReceiverViewHolder(LayoutRepliedReceivedMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == REPLIED_PHOTO_SENDER_VIEW_TYPE) {
            return new RepliedPhotoSenderViewHolder(LayoutRepliedSendPhotoBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == REPLIED_PHOTO_RECEIVER_VIEW_TYPE) {
            return new RepliedPhotoReceiverViewHolder(LayoutRepliedReceivedPhotoBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == VOICE_SENDER_VIEW_TYPE) {
            return new VoiceSenderViewHolder(LayoutSendVoiceBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == REPLIED_VOICE_SENDER_VIEW_TYPE) {
            return new RepliedVoiceSenderViewHolder(LayoutRepliedSendVoiceBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == VOICE_RECEIVER_VIEW_TYPE) {
            return new VoiceReceiverViewHolder(LayoutReceivedVoiceBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else if (viewType == REPLIED_VOICE_RECEIVER_VIEW_TYPE) {
            return new RepliedVoiceReceiverViewHolder(LayoutRepliedReceivedVoiceBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        } else {
            return new EmptyChatViewHolder(LayoutEmptyChatBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel model = messageModels.get(position);

        if (messageModels.size() == 0) {
            return EMPTY_VIEW_TYPE;
        } else {
            if (Objects.equals(model.getSenderId(), FirebaseAuth.getInstance().getUid())) {
                if (Objects.equals(model.getPhoto(), "null")) {
                    if (Objects.equals(model.getMessage(), "null")) {
                        if (model.hasReply()) {
                            return REPLIED_VOICE_SENDER_VIEW_TYPE;
                        } else {
                            return VOICE_SENDER_VIEW_TYPE;
                        }
                    } else    //Has Text Message
                    {
                        if (model.hasReply()) {
                            return REPLIED_SENDER_VIEW_TYPE;
                        } else {
                            return SENDER_VIEW_TYPE;
                        }
                    }
                } else {
                    if (model.hasReply()) {
                        return REPLIED_PHOTO_SENDER_VIEW_TYPE;
                    } else {
                        return PHOTO_SENDER_VIEW_TYPE;
                    }
                }
            } else {
                if (Objects.equals(model.getPhoto(), "null")) {
                    if (Objects.equals(model.getMessage(), "null")) {
                        if (model.hasReply()) {
                            return REPLIED_VOICE_RECEIVER_VIEW_TYPE;
                        } else {
                            return VOICE_RECEIVER_VIEW_TYPE;
                        }
                    } else {
                        if (model.hasReply()) {
                            return REPLIED_RECEIVER_VIEW_TYPE;
                        } else {
                            return RECEIVER_VIEW_TYPE;
                        }
                    }
                } else {
                    if (model.hasReply()) {
                        return REPLIED_PHOTO_RECEIVER_VIEW_TYPE;
                    } else {
                        return PHOTO_RECEIVER_VIEW_TYPE;
                    }
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messageModels.get(position);


        if (holder.getItemViewType() == EMPTY_VIEW_TYPE) {

        } else if (holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        } else if (holder.getItemViewType() == RECEIVER_VIEW_TYPE) {
            ((ReceiverViewHolder) holder).Bind(context, messageModel, position, messageListener);
        } else if (holder.getItemViewType() == PHOTO_SENDER_VIEW_TYPE) {
            ((PhotoSenderViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        } else if (holder.getItemViewType() == PHOTO_RECEIVER_VIEW_TYPE) {
            ((PhotoReceiverViewHolder) holder).Bind(context, messageModel, position, messageListener);
        } else if (holder.getItemViewType() == REPLIED_SENDER_VIEW_TYPE) {
            ((RepliedSenderViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        } else if (holder.getItemViewType() == REPLIED_RECEIVER_VIEW_TYPE) {
            ((RepliedReceiverViewHolder) holder).Bind(context, messageModel, position, messageListener);
        } else if (holder.getItemViewType() == REPLIED_PHOTO_SENDER_VIEW_TYPE) {
            ((RepliedPhotoSenderViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        } else if (holder.getItemViewType() == REPLIED_PHOTO_RECEIVER_VIEW_TYPE) {
            ((RepliedPhotoReceiverViewHolder) holder).Bind(context, messageModel, position, messageListener);
        } else if (holder.getItemViewType() == VOICE_SENDER_VIEW_TYPE) {
            ((VoiceSenderViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        } else if (holder.getItemViewType() == VOICE_RECEIVER_VIEW_TYPE) {
            ((VoiceReceiverViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        } else if (holder.getItemViewType() == REPLIED_VOICE_SENDER_VIEW_TYPE) {
            ((RepliedVoiceSenderViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        } else if (holder.getItemViewType() == REPLIED_VOICE_RECEIVER_VIEW_TYPE) {
            ((RepliedVoiceReceiverViewHolder) holder).Bind(context, messageModel, position, messageListener, database);
        }
    }

    private void showOptions(Context context, View view, View background, boolean isSender, MessageModel messageModel, String profilePic, boolean isPhoto, MessageListener messageListener) {
        showBalloon(context, view, 1);
        BlurView blurView = balloon.getContentView().findViewById(R.id.blur);
        //utilities.blur(blurView, 10f, false);

        ConstraintLayout buttonAnswer = balloon.getContentView().findViewById(R.id.button_answer);
        ConstraintLayout buttonDelete = balloon.getContentView().findViewById(R.id.button_delete);
        background.setBackground(context.getResources().getDrawable(R.drawable.yellow_gradient, null));

        if (isSender) {
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
            if (isSender) {
                background.setBackground(context.getResources().getDrawable(R.drawable.purple_gradient, null));
            } else {
                background.setBackground(context.getResources().getDrawable(R.drawable.blue_gradient, null));
            }
        });
    }

    private void showBalloon(Context context, View view, int position) {
        balloon = new Balloon.Builder(context)
                .setLayout(R.layout.layout_message_options)
                .setArrowSize(0)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                .build();
        switch (position) {
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
    public int getItemCount() {
        return messageModels.size();
    }

    private void ifFirstMessage(Context context, int position, View view) {
        if (position == 0) {
            float density = context.getResources().getDisplayMetrics().density;
            view.setPadding(0, (int) (70 * density), 0, 0);
        }
    }

    private void ifMessageContainsText(TextView textView, String message) {
        if (Objects.equals(message, ""))
            textView.setVisibility(View.GONE);
        else
            textView.setText(message);
    }

    public class EmptyChatViewHolder extends RecyclerView.ViewHolder {
        LayoutEmptyChatBinding binding;

        public EmptyChatViewHolder(@NonNull LayoutEmptyChatBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        LayoutReceivedMessageBinding binding;

        public ReceiverViewHolder(@NonNull LayoutReceivedMessageBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener) {
            ifFirstMessage(context, position, binding.container);

            binding.receivedMessage.setText(messageModel.getMessage());
            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            AtomicReference<String> profilePic = new AtomicReference<>();
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId())) {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(binding.profileImage);
                    binding.username.setText(userModel.getName());
                }
            });
            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, false, messageModel, profilePic.get(), false, messageListener);
                return true;
            });
        }
    }

    public class PhotoReceiverViewHolder extends RecyclerView.ViewHolder {
        LayoutReceivedPhotoBinding binding;

        public PhotoReceiverViewHolder(@NonNull LayoutReceivedPhotoBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener) {
            ifFirstMessage(context, position, binding.container);
            ifMessageContainsText(binding.receivedMessage, messageModel.getMessage());

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            AtomicReference<String> profilePic = new AtomicReference<>();
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId())) {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(binding.profileImage);
                    Glide.with(context).load(messageModel.getPhoto()).into(binding.receivedPhoto);
                    binding.username.setText(userModel.getName());
                }
            });
            binding.receivedPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", messageModel.getPhoto());
                context.startActivity(intent);
            });
            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, false, messageModel, profilePic.get(), true, messageListener);
                return true;
            });
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        LayoutSendMessageBinding binding;

        public SenderViewHolder(@NonNull LayoutSendMessageBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);

            binding.sentMessage.setText(messageModel.getMessage());
            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));

            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, true, messageModel, null, false, messageListener);
                return true;
            });

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen"))) {
                            binding.seenCheck.setVisibility(View.VISIBLE);
                            binding.seenCheck.playAnimation();
                        } else {
                            binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
    }

    public class PhotoSenderViewHolder extends RecyclerView.ViewHolder {
        LayoutSendPhotoBinding binding;

        public PhotoSenderViewHolder(@NonNull LayoutSendPhotoBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);
            ifMessageContainsText(binding.sentMessage, messageModel.getMessage());

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            Glide.with(context).load(messageModel.getPhoto()).into(binding.sentPhoto);
            binding.sentPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", messageModel.getPhoto());
                context.startActivity(intent);
            });

            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, true, messageModel, null, true, messageListener);
                return true;
            });

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen"))) {
                            binding.seenCheck.setVisibility(View.VISIBLE);
                            binding.seenCheck.playAnimation();
                        } else {
                            binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
    }

    public class RepliedSenderViewHolder extends RecyclerView.ViewHolder {
        LayoutRepliedSendMessageBinding binding;

        public RepliedSenderViewHolder(@NonNull LayoutRepliedSendMessageBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);

            binding.repliedMessage.setText(messageModel.getRepliedMessage());
            if (messageModel.isReplyHasPhoto()) {
                Glide.with(context).load(messageModel.getRepliedPhoto()).into(binding.repliedPhoto);
                binding.repliedPhoto.setVisibility(View.VISIBLE);
            }

            binding.sentMessage.setText(messageModel.getMessage());
            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));

            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, true, messageModel, null, false, messageListener);
                return true;
            });

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen"))) {
                            binding.seenCheck.setVisibility(View.VISIBLE);
                            binding.seenCheck.playAnimation();
                        } else {
                            binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
    }

    public class RepliedReceiverViewHolder extends RecyclerView.ViewHolder {
        LayoutRepliedReceivedMessageBinding binding;

        public RepliedReceiverViewHolder(@NonNull LayoutRepliedReceivedMessageBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener) {
            ifFirstMessage(context, position, binding.container);

            binding.repliedMessage.setText(messageModel.getRepliedMessage());
            if (messageModel.isReplyHasPhoto()) {
                Glide.with(context).load(messageModel.getRepliedPhoto()).into(binding.repliedPhoto);
                binding.repliedPhoto.setVisibility(View.VISIBLE);
            }

            binding.receivedMessage.setText(messageModel.getMessage());
            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            AtomicReference<String> profilePic = new AtomicReference<>();
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId())) {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(binding.profileImage);
                    binding.username.setText(userModel.getName());
                }
            });
            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, false, messageModel, profilePic.get(), false, messageListener);
                return true;
            });
        }
    }

    public class RepliedPhotoSenderViewHolder extends RecyclerView.ViewHolder {
        LayoutRepliedSendPhotoBinding binding;

        public RepliedPhotoSenderViewHolder(@NonNull LayoutRepliedSendPhotoBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);

            binding.repliedMessage.setText(messageModel.getRepliedMessage());
            if (messageModel.isReplyHasPhoto()) {
                Glide.with(context).load(messageModel.getRepliedPhoto()).into(binding.repliedPhoto);
                binding.repliedPhoto.setVisibility(View.VISIBLE);
            }

            ifMessageContainsText(binding.sentMessage, messageModel.getMessage());

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            Glide.with(context).load(messageModel.getPhoto()).into(binding.sentPhoto);
            binding.sentPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", messageModel.getPhoto());
                context.startActivity(intent);
            });

            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, true, messageModel, null, true, messageListener);
                return true;
            });

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen"))) {
                            binding.seenCheck.setVisibility(View.VISIBLE);
                            binding.seenCheck.playAnimation();
                        } else {
                            binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
    }

    public class RepliedPhotoReceiverViewHolder extends RecyclerView.ViewHolder {
        LayoutRepliedReceivedPhotoBinding binding;

        public RepliedPhotoReceiverViewHolder(@NonNull LayoutRepliedReceivedPhotoBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener) {
            ifFirstMessage(context, position, binding.container);

            binding.repliedMessage.setText(messageModel.getRepliedMessage());
            if (messageModel.isReplyHasPhoto()) {
                Glide.with(context).load(messageModel.getRepliedPhoto()).into(binding.repliedPhoto);
                binding.repliedPhoto.setVisibility(View.VISIBLE);
            }

            ifMessageContainsText(binding.receivedMessage, messageModel.getMessage());

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            AtomicReference<String> profilePic = new AtomicReference<>();
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId())) {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(binding.profileImage);
                    Glide.with(context).load(messageModel.getPhoto()).into(binding.receivedPhoto);
                    binding.username.setText(userModel.getName());
                }
            });
            binding.receivedPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", messageModel.getPhoto());
                context.startActivity(intent);
            });
            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, false, messageModel, profilePic.get(), true, messageListener);
                return true;
            });
        }
    }

    public class VoiceReceiverViewHolder extends RecyclerView.ViewHolder {
        LayoutReceivedVoiceBinding binding;

        public VoiceReceiverViewHolder(@NonNull LayoutReceivedVoiceBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            AtomicReference<String> profilePic = new AtomicReference<>();
            database.collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId())) {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(binding.profileImage);
                    binding.username.setText(userModel.getName());
                }
            });
            if (messageModel.isPlayed())
            {
                binding.voicePlayerView.setPlayPaueseBackgroundColor(context.getColor(R.color.accent_purple_dark));
                binding.voicePlayerView.setSeekBarStyle(context.getColor(R.color.accent_purple_dark), context.getColor(R.color.accent_purple_dark));
            }
            binding.voicePlayerView.setAudio(messageModel.getVoice());
            binding.voicePlayerView.setSeekBarListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress > 0) {
                        if (!messageModel.isPlayed())
                        {
                            database.collection("chats").document(messageModel.getMessageId()).update("isPlayed", true).addOnCompleteListener(task -> {
                                binding.voicePlayerView.setPlayPaueseBackgroundColor(context.getColor(R.color.accent_purple_dark));
                                binding.voicePlayerView.setSeekBarStyle(context.getColor(R.color.accent_purple_dark), context.getColor(R.color.accent_purple_dark));
                            });
                        }
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, false, messageModel, profilePic.get(), false, messageListener);
                return true;
            });
        }
    }

    public class VoiceSenderViewHolder extends RecyclerView.ViewHolder {
        LayoutSendVoiceBinding binding;

        public VoiceSenderViewHolder(@NonNull LayoutSendVoiceBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            binding.voicePlayerView.setAudio(messageModel.getVoice());

            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, true, messageModel, null, false, messageListener);
                return true;
            });

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen"))) {
                            binding.seenCheck.setVisibility(View.VISIBLE);
                            binding.seenCheck.playAnimation();
                        } else {
                            binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                    if (value.getBoolean("isPlayed") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isPlayed"))) {
                            binding.voicePlayerView.setPlayPaueseBackgroundColor(context.getColor(R.color.accent_blue_dark));
                            binding.voicePlayerView.setSeekBarStyle(context.getColor(R.color.accent_blue_dark), context.getColor(R.color.accent_blue_dark));
                        }
                    }
                }
            });
        }
    }

    public class RepliedVoiceReceiverViewHolder extends RecyclerView.ViewHolder {
        LayoutRepliedReceivedVoiceBinding binding;

        public RepliedVoiceReceiverViewHolder(@NonNull LayoutRepliedReceivedVoiceBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);

            binding.repliedMessage.setText(messageModel.getRepliedMessage());
            if (messageModel.isReplyHasPhoto()) {
                Glide.with(context).load(messageModel.getRepliedPhoto()).into(binding.repliedPhoto);
                binding.repliedPhoto.setVisibility(View.VISIBLE);
            }

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            AtomicReference<String> profilePic = new AtomicReference<>();
            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserModel userModel = new UserModel();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getId().equals(messageModel.getSenderId())) {
                            userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                            profilePic.set(documentSnapshot.getString("profilePic"));
                            userModel.setName(documentSnapshot.getString("name"));
                        }
                    }
                    Glide.with(context).load(userModel.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person))
                            .into(binding.profileImage);
                    binding.username.setText(userModel.getName());
                }
            });
            if (messageModel.isPlayed())
            {
                binding.voicePlayerView.setPlayPaueseBackgroundColor(context.getColor(R.color.accent_purple_dark));
                binding.voicePlayerView.setSeekBarStyle(context.getColor(R.color.accent_purple_dark), context.getColor(R.color.accent_purple_dark));
            }
            binding.voicePlayerView.setAudio(messageModel.getVoice());
            binding.voicePlayerView.setSeekBarListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress > 0) {
                        if (!messageModel.isPlayed())
                        {
                            database.collection("chats").document(messageModel.getMessageId()).update("isPlayed", true).addOnCompleteListener(task -> {
                                binding.voicePlayerView.setPlayPaueseBackgroundColor(context.getColor(R.color.accent_purple_dark));
                                binding.voicePlayerView.setSeekBarStyle(context.getColor(R.color.accent_purple_dark), context.getColor(R.color.accent_purple_dark));
                            });
                        }
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, false, messageModel, profilePic.get(), false, messageListener);
                return true;
            });
        }
    }

    public class RepliedVoiceSenderViewHolder extends RecyclerView.ViewHolder {
        LayoutRepliedSendVoiceBinding binding;

        public RepliedVoiceSenderViewHolder(@NonNull LayoutRepliedSendVoiceBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void Bind(Context context, MessageModel messageModel, int position, MessageListener messageListener, FirebaseFirestore database) {
            ifFirstMessage(context, position, binding.container);

            binding.repliedMessage.setText(messageModel.getRepliedMessage());
            if (messageModel.isReplyHasPhoto()) {
                Glide.with(context).load(messageModel.getRepliedPhoto()).into(binding.repliedPhoto);
                binding.repliedPhoto.setVisibility(View.VISIBLE);
            }

            binding.date.setText(TimeAgo.using(messageModel.getTimestamp()));
            binding.voicePlayerView.setAudio(messageModel.getVoice());

            //Görüldü kontrolü
            database.collection("chats").document(messageModel.getMessageId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getBoolean("isSeen") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isSeen"))) {
                            binding.seenCheck.setVisibility(View.VISIBLE);
                            binding.seenCheck.playAnimation();
                        } else {
                            binding.seenCheck.setVisibility(View.INVISIBLE);
                        }
                    }
                    if (value.getBoolean("isPlayed") != null) {
                        if (Boolean.TRUE.equals(value.getBoolean("isPlayed"))) {
                            binding.voicePlayerView.setPlayPaueseBackgroundColor(context.getColor(R.color.accent_blue_dark));
                            binding.voicePlayerView.setSeekBarStyle(context.getColor(R.color.accent_blue_dark), context.getColor(R.color.accent_blue_dark));
                        }
                    }
                }
            });
            binding.container.setOnLongClickListener(v -> {
                showOptions(context, binding.card, binding.cardBackground, false, messageModel, null, false, messageListener);
                return true;
            });
        }
    }
}
