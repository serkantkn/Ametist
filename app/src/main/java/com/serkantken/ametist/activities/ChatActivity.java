package com.serkantken.ametist.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.ChatAdapter;
import com.serkantken.ametist.databinding.ActivityChatBinding;
import com.serkantken.ametist.databinding.LayoutChatPhotoSelectorBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.network.ApiClient;
import com.serkantken.ametist.network.ApiService;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private Utilities utilities;
    private UserModel receiverUser;
    private ArrayList<MessageModel> messageModels;
    private ChatAdapter chatAdapter;
    private String conversationId;
    private Boolean isReceiverAvailable = false;
    private ActivityResultLauncher<String> getContent;
    private String photoUri;
    private BottomSheetDialog choosePhotoDialog;
    private LayoutChatPhotoSelectorBinding choosePhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(view -> onBackPressed());

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        utilities = new Utilities(getApplicationContext(), this);
        receiverUser = (UserModel) getIntent().getSerializableExtra("receiverUser");

        messageModels = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageModels, this);
        binding.messageRV.setAdapter(chatAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        binding.messageRV.setLayoutManager(manager);

        //Get receiver user's name
        database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.getId().equals(receiverUser.getUserId())) {
                        receiverUser.setName(documentSnapshot.getString("name"));
                    }
                }
                binding.username.setText(receiverUser.getName());
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
                Toast.makeText(this, "Mesaj kutusu boş", Toast.LENGTH_SHORT).show();
            }
        });

        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            String destUri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

            UCrop.Options options = new UCrop.Options();
            options.setLogoColor(getColor(R.color.accent_purple_dark));
            options.setFreeStyleCropEnabled(false);
            options.setToolbarTitle(getString(R.string.crop));
            options.withAspectRatio(1, 1);
            UCrop.of(result, Uri.fromFile(new File(getCacheDir(), destUri)))
                    .withOptions(options)
                    .start(ChatActivity.this);
        });
    }

    private void selectPhotoFromGallery() {
        choosePhotoDialog = new BottomSheetDialog(ChatActivity.this, R.style.BottomSheetDialogTheme_Chat);
        choosePhotoView = LayoutChatPhotoSelectorBinding.inflate(getLayoutInflater());

        utilities.blur(choosePhotoView.blur, 10f, false);

        choosePhotoView.buttonGallery.setOnClickListener(view -> {
            if (isPermissionGranted()) {
                getContent.launch("image/*");
            }
        });

        choosePhotoView.buttonDelete.setOnClickListener(view -> {
            photoUri = "";
            choosePhotoDialog.dismiss();
        });

        choosePhotoView.buttonSend.setOnClickListener(view -> {
            sendPhoto();
        });

        choosePhotoDialog.setContentView(choosePhotoView.getRoot());
        choosePhotoDialog.show();
    }

    private void sendPhotoFromGallery() {
        if (!photoUri.isEmpty()) {
            Glide.with(this).load(photoUri).into(choosePhotoView.postImage);
            choosePhotoView.titleMessageBox.setText(getString(R.string.photo_preview));
            choosePhotoView.buttonCamera.setVisibility(View.GONE);
            choosePhotoView.buttonGallery.setVisibility(View.GONE);
            choosePhotoView.buttonDelete.setVisibility(View.VISIBLE);
            choosePhotoView.buttonSend.setVisibility(View.VISIBLE);
            choosePhotoView.postImage.setVisibility(View.VISIBLE);
            choosePhotoView.messageLayout.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isPermissionGranted() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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

        database.collection("chats").add(model).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                model.put("messageId", task.getResult().getId());
                database.collection("chats").document(task.getResult().getId()).update(model);

                if (conversationId != null) {
                    updateConversation(binding.inputMessage.getText().toString());
                } else {
                    MessageModel messageModel = new MessageModel();
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
                        data.put("username", utilities.getPreferences("username"));
                        data.put("token", utilities.getPreferences("token"));
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


    }

    private void sendPhoto() {
        ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.setMessage(getString(R.string.photo_uploading));
        progressDialog.create();
        progressDialog.show();

        String message = choosePhotoView.inputMessage.getText().toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put("timestamp", new Date().getTime());
        model.put("message", message);
        model.put("senderId", auth.getUid());
        model.put("receiverId", receiverUser.getUserId());

        StorageReference filePath = FirebaseStorage.getInstance()
                .getReference("Chats")
                .child(auth.getUid())
                .child(receiverUser.getUserId())
                .child(new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString());

        StorageTask uploadTask = filePath.putFile(Uri.parse(photoUri));
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful())
            {
                throw task.getException();
            }
            return filePath.getDownloadUrl();
        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
            Uri downloadUri = task.getResult();
            String downloadUrl = downloadUri.toString();
            model.put("photo", downloadUrl);

            database.collection("chats").add(model).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    model.put("messageId", task1.getResult().getId());

                    database.collection("chats").document(task1.getResult().getId()).update(model);

                    if (conversationId != null) {
                        updateConversation(choosePhotoView.inputMessage.getText().toString());
                    } else {
                        MessageModel messageModel = new MessageModel();
                        messageModel.setSenderId(auth.getUid());
                        messageModel.setReceiverId(receiverUser.getUserId());
                        if (Objects.equals(choosePhotoView.inputMessage.getText().toString(), ""))
                        {
                            messageModel.setMessage("Photo");
                        }
                        else
                        {
                            messageModel.setMessage(choosePhotoView.inputMessage.getText().toString());
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
                            data.put("username", utilities.getPreferences("username"));
                            data.put("token", utilities.getPreferences("token"));
                            data.put("messageType", "3");
                            data.put("message", choosePhotoView.inputMessage.getText().toString());

                            JSONObject body = new JSONObject();
                            body.put(Constants.REMOTE_MSG_DATA, data);
                            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                            sendNotification(body.toString());
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressDialog.dismiss();
                    binding.inputMessage.setText("");
                    choosePhotoDialog.dismiss();
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
        database.collection("Users").document(receiverUser.getUserId()).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getBoolean("online") != null) {
                    Boolean availability = Objects.requireNonNull(value.getBoolean("online")).booleanValue();
                    isReceiverAvailable = availability == true;
                }
                receiverUser.setToken(value.getString("token"));
            }
            if (isReceiverAvailable) {
                binding.availability.setText(getResources().getString(R.string.online));
            } else {
                if (value.getLong("lastSeen") != null) {
                    binding.availability.setText(String.format("%s%s", getString(R.string.last_seen), TimeAgo.using(Objects.requireNonNull(value.getLong("lastSeen")))));
                } else {
                    binding.availability.setText(getResources().getString(R.string.offline));
                }
            }
        }));
    }

    private void listenMessages() {
        database.collection("chats")
                .whereEqualTo("senderId", auth.getUid())
                .whereEqualTo("receiverId", receiverUser.getUserId())
                .addSnapshotListener(eventListener);
        database.collection("chats")
                .whereEqualTo("senderId", receiverUser.getUserId())
                .whereEqualTo("receiverId", auth.getUid())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    MessageModel model = new MessageModel();
                    model.setSenderId(documentChange.getDocument().getString("senderId"));
                    model.setReceiverId(documentChange.getDocument().getString("receiverId"));
                    model.setMessage(documentChange.getDocument().getString("message"));
                    model.setPhoto(documentChange.getDocument().getString("photo"));
                    model.setTimestamp(documentChange.getDocument().getLong("timestamp"));
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
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                photoUri = resultUri.toString();
                sendPhotoFromGallery();
            }
        }
    }
}