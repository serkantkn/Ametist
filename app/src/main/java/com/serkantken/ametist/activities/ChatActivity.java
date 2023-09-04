package com.serkantken.ametist.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.ChatAdapter;
import com.serkantken.ametist.databinding.ActivityChatBinding;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
    private String conversationId, photoUri, audioFilePath;
    private Boolean isReceiverAvailable = false;
    private Boolean isReply = false;
    private Boolean isReplyWithPhoto = false;
    private Boolean isPhotoUploading = false;
    private Boolean isNotification = false;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private Balloon balloon;
    private MessageModel repliedMessage;
    private ListenerRegistration senderRegistration;
    private ListenerRegistration receiverRegistration;
    private long currentTimestamp = new Date().getTime();

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
        isNotification = getIntent().getBooleanExtra("messageNotification", false);

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
        database.collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task -> {
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

        mediaPlayer = MediaPlayer.create(this, R.raw.new_message);

        binding.buttonCancel.setOnClickListener(v -> {
            binding.replyMessageContainer.setVisibility(View.GONE);
            repliedMessage = null;
            isReply = false;
        });

        binding.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(""))
                {
                    binding.buttonSend.setBackground(AppCompatResources.getDrawable(ChatActivity.this, R.drawable.background_mic_button));
                    binding.buttonSend.setImageDrawable(AppCompatResources.getDrawable(ChatActivity.this, R.drawable.ic_mic));
                    binding.buttonSend.setTag("Voice");
                    if (isRecordingPermissionGranted())
                    {
                        binding.buttonSend.setListenForRecord(true);
                    }
                }
                else
                {
                    binding.buttonSend.setBackground(AppCompatResources.getDrawable(ChatActivity.this, R.drawable.background_send_button));
                    binding.buttonSend.setImageDrawable(AppCompatResources.getDrawable(ChatActivity.this, R.drawable.ic_send));
                    binding.buttonSend.setTag("Message");
                    binding.buttonSend.setListenForRecord(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.buttonSend.setRecordView(binding.recordView);

        binding.buttonSend.setListenForRecord(isRecordingPermissionGranted());

        binding.buttonSend.setOnRecordClickListener(view -> {
            if (binding.buttonSend.getTag().equals("Voice"))
            {
                if (isRecordingPermissionGranted())
                {
                    binding.buttonSend.setListenForRecord(true);
                }
                else
                {
                    requestRecordingPermission();
                }
            }
            else
            {
                Animation anim = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.scale);
                binding.buttonSend.startAnimation(anim);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation anim = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.scale_reverse);
                        binding.buttonSend.startAnimation(anim);
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (!Objects.equals(binding.inputMessage.getText().toString(), ""))
                                {
                                    sendMessage();
                                }
                                else
                                {
                                    Toast.makeText(ChatActivity.this, R.string.messagebox_empty, Toast.LENGTH_SHORT).show();
                                }
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
            }
        });

        binding.recordView.setSlideToCancelTextColor(getColor(R.color.accent_purple_dark));
        binding.recordView.setTimeLimit(30000);
        binding.recordView.setTrashIconColor(getColor(com.denzcoskun.imageslider.R.color.grey_font));
        binding.recordView.setCounterTimeColor(getColor(R.color.primary_text));
        binding.recordView.setCancelBounds(-75);

        binding.recordView.setOnRecordListener(new OnRecordListener() {

            @Override
            public void onStart() {
                setupForRecording();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Prepare failed", Toast.LENGTH_SHORT).show();
                }
                binding.inputMessage.setVisibility(View.GONE);
                binding.addPhoto.setVisibility(View.GONE);
                binding.recordView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                binding.recordView.setOnBasketAnimationEndListener(() -> {
                    showMessagebox();
                });
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                showMessagebox();
                sendAudio();
            }

            @Override
            public void onLessThanSecond() {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                File file = new File(audioFilePath);
                if (file.exists())
                {
                    file.delete();
                }
                showMessagebox();
            }

            @Override
            public void onLock() {

            }
        });

        binding.buttonDelete.setOnClickListener(view -> {
            if (!isPhotoUploading)
            {
                photoUri = "";
                binding.layoutPhotoPreview.setVisibility(View.GONE);
                binding.inputMessage.setVisibility(View.VISIBLE);
                binding.buttonSend.setVisibility(View.VISIBLE);
                binding.addPhoto.setVisibility(View.VISIBLE);
            }
        });

        binding.buttonSendPhoto.setOnClickListener(view -> {
            if (!isPhotoUploading)
            {
                sendPhoto();
            }
        });
    }

    private void showMessagebox()
    {
        Animation animation = AnimationUtils.loadAnimation(ChatActivity.this, android.R.anim.fade_in);
        animation.setDuration(500);
        binding.inputMessage.startAnimation(animation);
        binding.addPhoto.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.recordView.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                binding.inputMessage.setVisibility(View.VISIBLE);
                binding.addPhoto.setVisibility(View.VISIBLE);
                binding.buttonSend.setListenForRecord(true);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private boolean isRecordingPermissionGranted()
    {
        return ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordingPermission()
    {
        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
    }

    private void setupForRecording()
    {
        File mediaDir = new File(getFilesDir(), "Media/Audio");
        if (!mediaDir.exists()) {
            mediaDir.mkdirs(); // Gerekirse dizinleri oluştur
        }
        File audioFile = new File(mediaDir, new Date().getTime() + ".3gp");
        audioFilePath = audioFile.getAbsolutePath();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    private void selectPhotoFromGallery() {
        showBalloon(binding.addPhoto, 1);
        BlurView blurView = balloon.getContentView().findViewById(R.id.blur);
        utilities.blur(blurView, 10f, false);

        CardView cameraButton = balloon.getContentView().findViewById(R.id.buttonCamera);
        cameraButton.setOnClickListener(v -> {
            ImagePicker.with(this).cameraOnly().crop().start(102);
            balloon.dismiss();
        });

        CardView galleryButton = balloon.getContentView().findViewById(R.id.buttonGallery);
        galleryButton.setOnClickListener(v -> {
            ImagePicker.with(this).galleryOnly().crop().start(102);
            balloon.dismiss();
        });
    }

    private void sendPhotoFromGallery() {
        if (!photoUri.isEmpty()) {
            binding.inputMessage.setVisibility(View.GONE);
            binding.buttonSend.setVisibility(View.GONE);
            binding.addPhoto.setVisibility(View.GONE);
            binding.layoutPhotoPreview.setVisibility(View.VISIBLE);

            Glide.with(this).load(photoUri).into(binding.postImage);
            binding.titleMessageBox.setText(getString(R.string.photo_preview));
        }
    }

    private void sendMessage() {
        String message = binding.inputMessage.getText().toString();
        MessageModel model = new MessageModel();
        model.setTimestamp(new Date().getTime());
        model.setMessage(message);
        model.setPhoto("null");
        model.setSenderId(auth.getUid());
        model.setReceiverId(receiverUser.getUserId());
        if (isReply)
        {
            model.setHasReply(true);
            model.setRepliedUserId(repliedMessage.getSenderId());
            model.setRepliedMessage(repliedMessage.getMessage());
            if (isReplyWithPhoto)
            {
                model.setReplyHasPhoto(true);
                model.setRepliedPhoto(repliedMessage.getPhoto());
            }
            else
            {
                model.setReplyHasPhoto(false);
            }
        }
        else
        {
            model.setHasReply(false);
        }
        model.setSeen(false);

        database.collection(Constants.DATABASE_PATH_CHATS).add(model).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                model.setMessageId(task.getResult().getId());
                database.collection(Constants.DATABASE_PATH_CHATS).document(task.getResult().getId()).update("messageId", task.getResult().getId());

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
                        data.put(Constants.USER_ID, auth.getUid());
                        data.put(Constants.USERNAME, Hawk.get(Constants.USERNAME));
                        data.put(Constants.TOKEN, Hawk.get(Constants.TOKEN));
                        data.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TEXT);
                        data.put(Constants.MESSAGE, binding.inputMessage.getText().toString());

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
        binding.progressBlur.setVisibility(View.VISIBLE);
        binding.sendColor.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
        binding.deleteColor.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
        binding.inputPhotoMessage.setEnabled(false);
        isPhotoUploading = true;

        String message = binding.inputPhotoMessage.getText().toString();
        MessageModel model = new MessageModel();
        model.setTimestamp(new Date().getTime());
        model.setMessage(message);
        model.setSenderId(auth.getUid());
        model.setReceiverId(receiverUser.getUserId());
        if (isReply)
        {
            model.setHasReply(true);
            model.setRepliedUserId(repliedMessage.getSenderId());
            model.setRepliedMessage(repliedMessage.getMessage());
            if (isReplyWithPhoto)
            {
                model.setReplyHasPhoto(true);
                model.setRepliedPhoto(repliedMessage.getPhoto());
            }
            else
            {
                model.setReplyHasPhoto(false);
            }
        }
        else
        {
            model.setHasReply(false);
        }
        model.setSeen(false);

        StorageReference filePath = FirebaseStorage.getInstance()
                .getReference("Conversations")
                .child(conversationId)
                .child("Photos")
                .child(Objects.requireNonNull(auth.getUid()))
                .child(receiverUser.getUserId())
                .child(UUID.randomUUID().toString() + ".jpg");

        UploadTask uploadTask = filePath.putFile(Uri.parse(photoUri), new StorageMetadata.Builder().build());
        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            binding.progressbar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                model.setPhoto(downloadUrl);

                database.collection(Constants.DATABASE_PATH_CHATS).add(model).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        model.setMessageId(task1.getResult().getId());
                        database.collection(Constants.DATABASE_PATH_CHATS).document(task1.getResult().getId()).update("messageId", task1.getResult().getId());

                        if (conversationId != null) {
                            updateConversation(binding.inputPhotoMessage.getText().toString());
                        } else {
                            MessageModel messageModel = new MessageModel();
                            messageModel.setSenderId(auth.getUid());
                            messageModel.setReceiverId(receiverUser.getUserId());
                            if (Objects.equals(binding.inputPhotoMessage.getText().toString(), ""))
                            {
                                messageModel.setMessage("Photo");
                            }
                            else
                            {
                                messageModel.setMessage(binding.inputPhotoMessage.getText().toString());
                            }
                            messageModel.setTimestamp(new Date().getTime());
                            addConversation(messageModel);
                        }
                        if (!isReceiverAvailable) {
                            try {
                                JSONArray tokens = new JSONArray();
                                tokens.put(receiverUser.getToken());

                                JSONObject data = new JSONObject();
                                data.put(Constants.USER_ID, auth.getUid());
                                data.put(Constants.USERNAME, Hawk.get(Constants.USERNAME));
                                data.put(Constants.TOKEN, Hawk.get(Constants.TOKEN));
                                data.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_PHOTO);
                                data.put(Constants.MESSAGE, binding.inputPhotoMessage.getText().toString());

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
                        binding.layoutPhotoPreview.setVisibility(View.GONE);
                        binding.inputMessage.setVisibility(View.VISIBLE);
                        binding.buttonSend.setVisibility(View.VISIBLE);
                        binding.addPhoto.setVisibility(View.VISIBLE);
                        messageModels.clear();
                        listenMessages();
                    }
                });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(ChatActivity.this, "Hata oluştu", Toast.LENGTH_SHORT).show();

            binding.progressBlur.setVisibility(View.INVISIBLE);
            binding.sendColor.setBackground(getResources().getDrawable(R.drawable.purple_gradient,null));
            binding.deleteColor.setBackground(getResources().getDrawable(R.drawable.red_gradient, null));
            binding.inputPhotoMessage.setEnabled(true);
            isPhotoUploading = false;
        });

        binding.replyMessageContainer.setVisibility(View.GONE);
        repliedMessage = null;
        isReply = false;
        isReplyWithPhoto = false;
    }

    private void sendAudio()
    {
        MessageModel model = new MessageModel();
        model.setTimestamp(new Date().getTime());
        model.setSenderId(auth.getUid());
        model.setReceiverId(receiverUser.getUserId());
        model.setMessage("null");
        model.setPhoto("null");
        if (isReply)
        {
            model.setHasReply(true);
            model.setRepliedUserId(repliedMessage.getSenderId());
            model.setRepliedMessage(repliedMessage.getMessage());
            if (isReplyWithPhoto)
            {
                model.setReplyHasPhoto(true);
                model.setRepliedPhoto(repliedMessage.getPhoto());
            }
            else
            {
                model.setReplyHasPhoto(false);
            }
        }
        else
        {
            model.setHasReply(false);
        }
        model.setSeen(false);
        StorageReference filePath = FirebaseStorage.getInstance()
                .getReference("Conversations")
                .child(conversationId)
                .child("Records")
                .child(Objects.requireNonNull(auth.getUid()))
                .child(receiverUser.getUserId());
        Uri audioFile = Uri.fromFile(new File(audioFilePath));
        filePath.putFile(audioFile).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> audioUrl = taskSnapshot.getStorage().getDownloadUrl();
            audioUrl.addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    String url = task.getResult().toString();
                    model.setVoice(url);

                    database.collection(Constants.DATABASE_PATH_CHATS).add(model).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            model.setMessageId(task1.getResult().getId());
                            database.collection(Constants.DATABASE_PATH_CHATS).document(task1.getResult().getId()).update("messageId", task1.getResult().getId());

                            if (conversationId != null) {
                                updateConversation("Voice message");
                            } else {
                                MessageModel messageModel = new MessageModel();
                                messageModel.setSenderId(auth.getUid());
                                messageModel.setReceiverId(receiverUser.getUserId());
                                messageModel.setMessage("Voice");
                                messageModel.setTimestamp(new Date().getTime());
                                addConversation(messageModel);
                            }
                            if (!isReceiverAvailable) {
                                try {
                                    JSONArray tokens = new JSONArray();
                                    tokens.put(receiverUser.getToken());

                                    JSONObject data = new JSONObject();
                                    data.put(Constants.USER_ID, auth.getUid());
                                    data.put(Constants.USERNAME, Hawk.get(Constants.USERNAME));
                                    data.put(Constants.TOKEN, Hawk.get(Constants.TOKEN));
                                    data.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TEXT);
                                    data.put(Constants.MESSAGE, "Voice");

                                    JSONObject body = new JSONObject();
                                    body.put(Constants.REMOTE_MSG_DATA, data);
                                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                                    sendNotification(body.toString());
                                } catch (Exception e) {
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            binding.inputMessage.setText("");
                            messageModels.clear();
                            listenMessages();
                        }
                    });
                }
            });
        });
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
        database.collection(Constants.DATABASE_PATH_USERS).document(receiverUser.getUserId()).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getBoolean("online") != null)
                    isReceiverAvailable = Objects.requireNonNull(value.getBoolean("online"));

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
        senderRegistration = database.collection(Constants.DATABASE_PATH_CHATS)
                .whereEqualTo("senderId", auth.getUid())
                .whereEqualTo("receiverId", receiverUser.getUserId())
                .addSnapshotListener(eventListenerMessages);
        receiverRegistration = database.collection(Constants.DATABASE_PATH_CHATS)
                .whereEqualTo("senderId", receiverUser.getUserId())
                .whereEqualTo("receiverId", auth.getUid())
                .addSnapshotListener(eventListenerMessages);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListenerMessages = (value, error) -> {
        if (error != null)
        {
            return;
        }
        if (value != null)
        {
            for (DocumentChange documentChange : value.getDocumentChanges())
            {

                if (documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    MessageModel model = new MessageModel();
                    model.setMessageId(documentChange.getDocument().getId());
                    model.setSenderId(documentChange.getDocument().getString("senderId"));
                    model.setReceiverId(documentChange.getDocument().getString("receiverId"));
                    model.setMessage(documentChange.getDocument().getString("message"));
                    model.setPhoto(documentChange.getDocument().getString("photo"));
                    model.setVoice(documentChange.getDocument().getString("voice"));
                    model.setTimestamp(documentChange.getDocument().getLong("timestamp"));
                    model.setHasReply(Boolean.TRUE.equals(documentChange.getDocument().getBoolean("hasReply")));
                    model.setRepliedMessage(documentChange.getDocument().getString("repliedMessage"));
                    model.setReplyHasPhoto(Boolean.TRUE.equals(documentChange.getDocument().getBoolean("isReplyHasPhoto")));
                    model.setRepliedPhoto(documentChange.getDocument().getString("repliedPhoto"));
                    if (Objects.equals(documentChange.getDocument().getString("receiverId"), auth.getUid()))
                    {
                        if (!Boolean.TRUE.equals(documentChange.getDocument().getBoolean("isSeen")))
                        {
                            long seenDate = new Date().getTime();
                            database.collection(Constants.DATABASE_PATH_CHATS).document(Objects.requireNonNull(documentChange.getDocument().getId())).update("isSeen", true, "seenTimestamp", seenDate);
                            model.setSeen(true);
                            model.setSeenTimestamp(seenDate);
                        }
                        else
                        {
                            model.setSeen(true);
                            model.setSeenTimestamp(documentChange.getDocument().getLong("seenTimestamp"));
                        }
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
                    messageModels.add(model);
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
        database.collection(Constants.DATABASE_PATH_CONVERSATIONS)
                .add(conversation).addOnCompleteListener(task -> conversationId = task.getResult().getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference = database.collection(Constants.DATABASE_PATH_CONVERSATIONS).document(conversationId);
        documentReference.update(
                "conversationId", conversationId,
                "message", message,
                "timestamp", new Date().getTime()
                , "senderId", auth.getUid(),
                "receiverId", receiverUser.getUserId()
        );
    }

    private void checkForConversation() {
        if (messageModels.size() != 0) {
            checkForConversationRemotely(auth.getUid(), receiverUser.getUserId());
            checkForConversationRemotely(receiverUser.getUserId(), auth.getUid());
        }
    }

    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection(Constants.DATABASE_PATH_CONVERSATIONS)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        conversationId = documentSnapshot.getId();
                    }
                });
    }

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

    private void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 102)
            {
                assert data != null;
                Uri resultUri = data.getData();
                if (resultUri != null)
                {
                    photoUri = resultUri.toString();
                    sendPhotoFromGallery();
                }
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
        if (senderRegistration != null && receiverRegistration != null) {
            senderRegistration.remove();
            receiverRegistration.remove();
        }
        if (isNotification)
        {
            Intent intent = new Intent(ChatActivity.this, IntroActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null)
        {
            mediaPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}