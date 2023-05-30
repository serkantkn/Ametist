package com.serkantken.ametist.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.ChatAdapter;
import com.serkantken.ametist.databinding.ActivityChatBinding;
import com.serkantken.ametist.databinding.LayoutChatPhotoSelectorBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.network.ApiClient;
import com.serkantken.ametist.network.ApiService;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.MessageListener;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements MessageListener {
    private ActivityChatBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private Utilities utilities;
    private UserModel receiverUser;
    private ArrayList<MessageModel> messageModels;
    private ChatAdapter chatAdapter;
    private String conversationId;
    private Boolean isReceiverAvailable = false;
    private Boolean isReply = false;
    private Boolean isReplyWithPhoto = false;
    private Boolean isPhotoUploading = false;
    private ActivityResultLauncher<String> getContent;
    private String photoUri;
    private BottomSheetDialog photoPreviewDialog;
    private LayoutChatPhotoSelectorBinding photoPreviewView;
    private MediaPlayer mediaPlayer;
    private Balloon balloon;
    private MessageModel repliedMessage;
    private ListenerRegistration senderRegistration;
    private ListenerRegistration receiverRegistration;
    private long currentTimestamp = new Date().getTime();

    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(view -> onBackPressed());

        Hawk.init(this).build();
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        utilities = new Utilities(getApplicationContext(), this);
        receiverUser = (UserModel) getIntent().getSerializableExtra("receiverUser");

        utilities.blur(binding.toolbar, 10f, false);
        utilities.blur(binding.navbarBlur, 10f, false);

        messageModels = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageModels, this, ChatActivity.this, this, database);
        binding.messageRV.setAdapter(chatAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        binding.messageRV.setLayoutManager(manager);
        binding.messageRV.setClipToPadding(false);

        if (utilities.isMIUI())
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            binding.toolbar.setPadding(0, utilities.getStatusBarHeight(), 0, 0);
            binding.navbarBlur.setPadding(0, 0, 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT));
            binding.messageRV.setPadding(0, utilities.convertDpToPixel(74), 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(70));
        }
        else
        {
            binding.messageRV.setPadding(0, utilities.convertDpToPixel(74), 0, utilities.convertDpToPixel(70));
        }

        //Get receiver user's name
        database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.getId().equals(receiverUser.getUserId())) {
                        receiverUser.setName(documentSnapshot.getString("name"));
                        receiverUser.setProfilePic(documentSnapshot.getString("profilePic"));
                    }
                }
                binding.username.setText(receiverUser.getName());
                Glide.with(this).load(receiverUser.getProfilePic()).placeholder(R.drawable.ic_person).into(binding.profileImage);
            }
        });

        listenMessages();

        binding.addPhoto.setOnClickListener(view -> selectPhotoFromGallery());

        binding.buttonSend.setOnClickListener(view -> {
            if (!Objects.equals(binding.inputMessage.getText().toString(), ""))
            {
                sendMessage();
            }
            else
            {
                Toast.makeText(this, R.string.messagebox_empty, Toast.LENGTH_SHORT).show();
            }
        });

        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            String destUri = UUID.randomUUID().toString() + ".jpg";

            UCrop.Options options = new UCrop.Options();
            options.setLogoColor(getColor(R.color.accent_purple_dark));
            options.setFreeStyleCropEnabled(false);
            options.setToolbarTitle(getString(R.string.crop));
            UCrop.of(result, Uri.fromFile(new File(getCacheDir(), destUri)))
                    .withOptions(options)
                    .start(ChatActivity.this);
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.new_message);

        binding.buttonCancel.setOnClickListener(v -> {
            binding.replyMessageContainer.setVisibility(View.GONE);
            repliedMessage = null;
            isReply = false;
        });
    }

    private void selectPhotoFromGallery() {
        showBalloon(binding.addPhoto, 1);
        BlurView blurView = balloon.getContentView().findViewById(R.id.blur);
        utilities.blur(blurView, 10f, false);

        CardView cameraButton = balloon.getContentView().findViewById(R.id.buttonCamera);
        cameraButton.setOnClickListener(v -> {
            if (isCameraPermissionGranted())
            {

            }
        });

        CardView galleryButton = balloon.getContentView().findViewById(R.id.buttonGallery);
        galleryButton.setOnClickListener(v -> {
            if (isStoragePermissionGranted()) {
                getContent.launch("image/*");
                balloon.dismiss();
            }
        });
    }

    private void sendPhotoFromGallery() {
        if (!photoUri.isEmpty()) {
            photoPreviewDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme_Chat);
            photoPreviewView = LayoutChatPhotoSelectorBinding.inflate(getLayoutInflater());

            utilities.blur(photoPreviewView.blur, 10f, false);

            Glide.with(this).load(photoUri).into(photoPreviewView.postImage);
            photoPreviewView.titleMessageBox.setText(getString(R.string.photo_preview));

            photoPreviewView.buttonDelete.setOnClickListener(view -> {
                if (!isPhotoUploading)
                {
                    photoUri = "";
                    photoPreviewDialog.dismiss();
                }
            });

            photoPreviewView.buttonSend.setOnClickListener(view -> {
                if (!isPhotoUploading)
                {
                    sendPhoto();
                }
            });

            photoPreviewDialog.setContentView(photoPreviewView.getRoot());
            photoPreviewDialog.show();
        }
    }

    private Boolean isStoragePermissionGranted() {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q))
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            else
            {
                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            else
            {
                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                }, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED;
            }
        }
    }

    private Boolean isCameraPermissionGranted() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void sendMessage() {
        String message = binding.inputMessage.getText().toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put("timestamp", new Date().getTime());
        model.put("message", message);
        model.put("photo", "null");
        model.put("senderId", auth.getUid());
        model.put("receiverId", receiverUser.getUserId());
        if (isReply)
        {
            model.put("hasReply", true);
            model.put("repliedUserId", repliedMessage.getSenderId());
            model.put("repliedMessage", repliedMessage.getMessage());
            if (isReplyWithPhoto)
            {
                model.put("isReplyHasPhoto", true);
                model.put("repliedPhoto", repliedMessage.getPhoto());
            }
            else
            {
                model.put("isReplyHasPhoto", false);
            }
        }
        else
        {
            model.put("hasReply", false);
        }
        model.put("isSeen", false);

        database.collection("chats").add(model).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                model.put("messageId", task.getResult().getId());
                database.collection("chats").document(task.getResult().getId()).update(model);

                if (conversationId != null) {
                    updateConversation(binding.inputMessage.getText().toString());
                } else {
                    MessageModel messageModel = new MessageModel();
                    messageModel.setMessageId(task.getResult().getId());
                    messageModel.setSenderId(auth.getUid());
                    messageModel.setReceiverId(receiverUser.getUserId());
                    messageModel.setMessage(binding.inputMessage.getText().toString());
                    messageModel.setTimestamp(new Date().getTime());
                    addConversation(messageModel);
                }
                if (!isReceiverAvailable) {
                    try {
                        JSONArray tokens = new JSONArray();
                        tokens.put(receiverUser.getToken());

                        JSONObject data = new JSONObject();
                        data.put("userId", auth.getUid());
                        data.put("username", Hawk.get(Constants.USERNAME));
                        data.put("token", Hawk.get(Constants.TOKEN));
                        data.put("messageType", "1");
                        data.put("message", binding.inputMessage.getText().toString());

                        JSONObject body = new JSONObject();
                        body.put(Constants.REMOTE_MSG_DATA, data);
                        body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                        sendNotification(body.toString());
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                binding.inputMessage.setText("");
            } else {
                Toast.makeText(this, getString(R.string.error_at_sending_text), Toast.LENGTH_SHORT).show();
            }
        });

        binding.replyMessageContainer.setVisibility(View.GONE);
        repliedMessage = null;
        isReply = false;
        isReplyWithPhoto = false;
    }

    private void sendPhoto() {
        photoPreviewView.progressBlur.setVisibility(View.VISIBLE);
        photoPreviewView.sendColor.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
        photoPreviewView.deleteColor.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
        photoPreviewView.inputMessage.setEnabled(false);
        isPhotoUploading = true;

        String message = photoPreviewView.inputMessage.getText().toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put("timestamp", new Date().getTime());
        model.put("message", message);
        model.put("senderId", auth.getUid());
        model.put("receiverId", receiverUser.getUserId());
        if (isReply)
        {
            model.put("hasReply", true);
            model.put("repliedUserId", repliedMessage.getSenderId());
            model.put("repliedMessage", repliedMessage.getMessage());
            if (isReplyWithPhoto)
            {
                model.put("isReplyHasPhoto", true);
                model.put("repliedPhoto", repliedMessage.getPhoto());
            }
            else
            {
                model.put("isReplyHasPhoto", false);
            }
        }
        else
        {
            model.put("hasReply", false);
        }
        model.put("isSeen", false);

        StorageReference filePath = FirebaseStorage.getInstance()
                .getReference("Chats")
                .child(Objects.requireNonNull(auth.getUid()))
                .child(receiverUser.getUserId())
                .child(UUID.randomUUID().toString() + ".jpg");

        UploadTask uploadTask = filePath.putFile(Uri.parse(photoUri), new StorageMetadata.Builder().build());
        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            photoPreviewView.progressbar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                model.put("photo", downloadUrl);

                database.collection("chats").add(model).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        model.put("messageId", task1.getResult().getId());

                        database.collection("chats").document(task1.getResult().getId()).update(model);

                        if (conversationId != null) {
                            updateConversation(photoPreviewView.inputMessage.getText().toString());
                        } else {
                            MessageModel messageModel = new MessageModel();
                            messageModel.setSenderId(auth.getUid());
                            messageModel.setReceiverId(receiverUser.getUserId());
                            if (Objects.equals(photoPreviewView.inputMessage.getText().toString(), ""))
                            {
                                messageModel.setMessage("Photo");
                            }
                            else
                            {
                                messageModel.setMessage(photoPreviewView.inputMessage.getText().toString());
                            }
                            messageModel.setTimestamp(new Date().getTime());
                            addConversation(messageModel);
                        }
                        if (!isReceiverAvailable) {
                            try {
                                JSONArray tokens = new JSONArray();
                                tokens.put(receiverUser.getToken());

                                JSONObject data = new JSONObject();
                                data.put("userId", auth.getUid());
                                data.put("username", Hawk.get(Constants.USERNAME));
                                data.put("token", Hawk.get(Constants.TOKEN));
                                data.put("messageType", "3");
                                data.put("message", photoPreviewView.inputMessage.getText().toString());

                                JSONObject body = new JSONObject();
                                body.put(Constants.REMOTE_MSG_DATA, data);
                                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                                sendNotification(body.toString());
                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        binding.inputMessage.setText("");
                        isPhotoUploading = false;
                        photoPreviewDialog.dismiss();
                    }
                });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(ChatActivity.this, "Hata oluştu", Toast.LENGTH_SHORT).show();

            photoPreviewView.progressBlur.setVisibility(View.GONE);
            photoPreviewView.sendColor.setBackground(getResources().getDrawable(R.drawable.purple_gradient,null));
            photoPreviewView.deleteColor.setBackground(getResources().getDrawable(R.drawable.red_gradient, null));
            photoPreviewView.inputMessage.setEnabled(true);
            isPhotoUploading = false;
        });

        binding.replyMessageContainer.setVisibility(View.GONE);
        repliedMessage = null;
        isReply = false;
        isReplyWithPhoto = false;
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                Toast.makeText(ChatActivity.this, error.getString("error"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ChatActivity.this, "Bildirim gönderildi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Hata: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        database.collection("Users").document(receiverUser.getUserId()).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getBoolean("online") != null) {
                    isReceiverAvailable = Objects.requireNonNull(value.getBoolean("online"));
                }
                receiverUser.setToken(value.getString("token"));
            }
            if (isReceiverAvailable) {
                binding.availability.setText(getResources().getString(R.string.online));
                binding.availability.setTextColor(getColor(R.color.accent_purple_dark));
                Typeface typeface = ResourcesCompat.getFont(this, R.font.jost_black);
                binding.availability.setTypeface(typeface);
            } else {
                assert value != null;
                if (value.getLong("lastSeen") != null) {
                    binding.availability.setText(String.format("%s%s", getString(R.string.last_seen), TimeAgo.using(Objects.requireNonNull(value.getLong("lastSeen")))));
                    binding.availability.setTextColor(getColor(R.color.secondary_text));
                    Typeface typeface = ResourcesCompat.getFont(this, R.font.jost_light_italic);
                    binding.availability.setTypeface(typeface);
                } else {
                    binding.availability.setText(getResources().getString(R.string.offline));
                    binding.availability.setTextColor(getColor(R.color.secondary_text));
                    Typeface typeface = ResourcesCompat.getFont(this, R.font.jost_light_italic);
                    binding.availability.setTypeface(typeface);
                }
            }
        }));
    }

    private void listenMessages() {
        senderRegistration = database.collection("chats")
                .whereEqualTo("senderId", auth.getUid())
                .whereEqualTo("receiverId", receiverUser.getUserId())
                .addSnapshotListener(eventListenerSentMessages);
        receiverRegistration = database.collection("chats")
                .whereEqualTo("senderId", receiverUser.getUserId())
                .whereEqualTo("receiverId", auth.getUid())
                .addSnapshotListener(eventListenerReceivedMessages);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListenerSentMessages = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    MessageModel model = new MessageModel();
                    model.setMessageId(documentChange.getDocument().getId());
                    model.setSenderId(documentChange.getDocument().getString("senderId"));
                    model.setReceiverId(documentChange.getDocument().getString("receiverId"));
                    model.setMessage(documentChange.getDocument().getString("message"));
                    model.setPhoto(documentChange.getDocument().getString("photo"));
                    model.setTimestamp(documentChange.getDocument().getLong("timestamp"));
                    model.setHasReply(Boolean.TRUE.equals(documentChange.getDocument().getBoolean("hasReply")));
                    model.setRepliedMessage(documentChange.getDocument().getString("repliedMessage"));
                    model.setReplyHasPhoto(Boolean.TRUE.equals(documentChange.getDocument().getBoolean("isReplyHasPhoto")));
                    model.setRepliedPhoto(documentChange.getDocument().getString("repliedPhoto"));
                    model.setSeen(Boolean.TRUE.equals(documentChange.getDocument().getBoolean("isSeen")));
                    messageModels.add(model);
                }
            }
            messageModels.sort(Comparator.comparing(MessageModel::getTimestamp));
            if (messageModels.size() == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(messageModels.size(), messageModels.size());
                binding.messageRV.smoothScrollToPosition(messageModels.size() - 1);
            }
        }
        if (conversationId == null) {
            checkForConversation();
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListenerReceivedMessages = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    MessageModel model = new MessageModel();
                    model.setMessageId(documentChange.getDocument().getId());
                    model.setSenderId(documentChange.getDocument().getString("senderId"));
                    model.setReceiverId(documentChange.getDocument().getString("receiverId"));
                    model.setMessage(documentChange.getDocument().getString("message"));
                    model.setPhoto(documentChange.getDocument().getString("photo"));
                    model.setTimestamp(documentChange.getDocument().getLong("timestamp"));
                    model.setHasReply(Boolean.TRUE.equals(documentChange.getDocument().getBoolean("hasReply")));
                    model.setRepliedMessage(documentChange.getDocument().getString("repliedMessage"));
                    model.setReplyHasPhoto(Boolean.TRUE.equals(documentChange.getDocument().getBoolean("isReplyHasPhoto")));
                    model.setRepliedPhoto(documentChange.getDocument().getString("repliedPhoto"));
                    if (!Boolean.TRUE.equals(documentChange.getDocument().getBoolean("isSeen")))
                    {
                        database.collection("chats").document(Objects.requireNonNull(documentChange.getDocument().getId())).update("isSeen", true);
                        model.setSeen(true);
                    }
                    else
                        model.setSeen(true);
                    messageModels.add(model);
                    if (currentTimestamp - model.getTimestamp() < 2000)
                    {
                        if (mediaPlayer != null)
                        {
                            if (mediaPlayer.isPlaying())
                            {
                                mediaPlayer.seekTo(0);
                            }
                            else
                            {
                                mediaPlayer.start();
                            }
                        }
                    }
                }
            }

            messageModels.sort(Comparator.comparing(MessageModel::getTimestamp));
            if (messageModels.size() == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyDataSetChanged();
                binding.messageRV.smoothScrollToPosition(messageModels.size() - 1);
            }
        }
        if (conversationId == null) {
            checkForConversation();
        }
    };

    private void addConversation(MessageModel conversation) {
        database.collection("conversations")
                .add(conversation).addOnCompleteListener(task -> conversationId = task.getResult().getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference = database.collection("conversations").document(conversationId);
        documentReference.update("message", message, "timestamp", new Date().getTime()
                , "senderId", auth.getUid(), "receiverId", receiverUser.getUserId());
    }

    private void checkForConversation() {
        if (messageModels.size() != 0) {
            checkForConversationRemotely(auth.getUid(), receiverUser.getUserId());
            checkForConversationRemotely(receiverUser.getUserId(), auth.getUid());
        }
    }

    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection("conversations")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get().addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    public void onMessageReplied(MessageModel messageModel, String profilePic, boolean isPhoto)
    {
        binding.replyMessageContainer.setVisibility(View.VISIBLE);
        isReply = true;
        repliedMessage = messageModel;
        Glide.with(this).load(profilePic).placeholder(R.drawable.ic_person).into(binding.replyProfileImage);
        if (!Objects.equals(messageModel.getMessage(), ""))
        {
            binding.repliedMessage.setText(messageModel.getMessage());
        }
        else
        {
            binding.repliedMessage.setVisibility(View.GONE);
        }
        if (isPhoto)
        {
            isReplyWithPhoto = true;
            binding.repliedPhoto.setVisibility(View.VISIBLE);
            Glide.with(this).load(messageModel.getPhoto()).into(binding.repliedPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                photoUri = resultUri.toString();
                sendPhotoFromGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE)
        {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "İzin alındı. Şimdi galeriye gidebilirsiniz.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "İzin verilmedi. Galeri açılamıyor.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CAMERA_REQUEST_CODE)
        {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "İzin alındı. Şimdi kamerayı açabilirsiniz.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "İzin verilmedi. kamera açılamıyor.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showBalloon(View view, int position)
    {
        balloon = new Balloon.Builder(getApplicationContext())
                .setLayout(R.layout.layout_chat_content_source)
                .setArrowSize(0)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
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
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.release();
        mediaPlayer = null;
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (senderRegistration != null && receiverRegistration != null) {
            senderRegistration.remove();
            receiverRegistration.remove();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null)
        {
            mediaPlayer.release();
        }
    }
}