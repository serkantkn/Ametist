package com.serkantken.ametist.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.PhotoAdapter;
import com.serkantken.ametist.adapters.PostAdapter;
import com.serkantken.ametist.databinding.ActivityProfileBinding;
import com.serkantken.ametist.databinding.LayoutQrCodeBinding;
import com.serkantken.ametist.models.PhotoModel;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.network.ApiClient;
import com.serkantken.ametist.network.ApiService;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.PhotoListener;
import com.serkantken.ametist.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity implements PhotoListener
{
    private ActivityProfileBinding binding;
    private ArrayList<PostModel> postModels;
    private PostAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private UserModel user;
    private Utilities utilities;
    private ArrayList<PhotoModel> pictureList;
    private PhotoAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        utilities = new Utilities(this, this);
        utilities.blur(binding.toolbarBlur, 10f, false);

        binding.navbarBlur.setMinimumHeight(utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT));
        utilities.blur(binding.navbarBlur, 10f, false);

        binding.toolbarBlur.setPadding(0, utilities.getStatusBarHeight(), 0, 0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;

        //utilities.getStatusBarHeight()+(screenHeight-(screenHeight/4))
        binding.scrollView.setPadding(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(64), 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT));
        binding.scrollView.setClipToPadding(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.profileRoot);
        constraintSet.connect(R.id.buttonMessage, ConstraintSet.END, R.id.profileRoot, ConstraintSet.END, utilities.convertDpToPixel(16));
        constraintSet.connect(R.id.buttonMessage, ConstraintSet.BOTTOM, R.id.profileRoot, ConstraintSet.BOTTOM, utilities.convertDpToPixel(16)+utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT));
        constraintSet.applyTo(binding.profileRoot);

        user = (UserModel) getIntent().getSerializableExtra("receiverUser");
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        postModels = new ArrayList<>();
        pictureList = new ArrayList<>();

        adapter = new PostAdapter(postModels, ProfileActivity.this, ProfileActivity.this);
        binding.posts.setAdapter(adapter);
        binding.posts.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));

        photoAdapter = new PhotoAdapter(pictureList, ProfileActivity.this, this);
        binding.profileImageRV.setAdapter(photoAdapter);

        getUserInfo();
        getPosts();

        binding.buttonBack.setOnClickListener(view -> onBackPressed());

        if (Objects.equals(user.getUserId(), auth.getUid()))
        {
            binding.buttonBlock.setVisibility(View.GONE);
        }

        binding.scrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (view, x, y, oldX, oldY) -> {
            if (y > oldY && binding.buttonMessage.isExtended())
            {
                binding.buttonMessage.shrink();
                if (!user.getUserId().equals(auth.getUid()))
                    binding.buttonFollow.hide();
            }
            if (y < oldY && !binding.buttonMessage.isExtended())
            {
                binding.buttonMessage.extend();
                if (!user.getUserId().equals(auth.getUid()))
                    binding.buttonFollow.show();
            }
            if (y > screenHeight/1.5)
            {
                binding.username.setVisibility(View.VISIBLE);
                binding.username2.setVisibility(View.INVISIBLE);
            }
            else
            {
                binding.username.setVisibility(View.INVISIBLE);
                binding.username2.setVisibility(View.VISIBLE);
            }
        });

        binding.buttonMessage.setOnClickListener(view -> {
            if (user.getUserId().equals(auth.getUid()))
            {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
            else
            {
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.putExtra("receiverUser", user);
                startActivity(intent);
            }
        });

        binding.buttonFollow.setOnClickListener(view -> follow());

        binding.buttonQr.setOnClickListener(view -> {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme_Chat);
            LayoutQrCodeBinding bottomSheetView = LayoutQrCodeBinding.inflate(getLayoutInflater());

            utilities.blur(bottomSheetView.bottomSheetContainer, 10f, false);
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            float oldBrightness = layoutParams.screenBrightness;
            layoutParams.screenBrightness = 1.0f;
            getWindow().setAttributes(layoutParams);

            // QR kodunun boyut
            int qrCodeSize = 512;

            // QR kodunun oluşturulması
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = null;
                try {
                    bitMatrix = multiFormatWriter.encode(Objects.requireNonNull(auth.getUid()), BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);
                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }
                int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Logo Bitmap'inin yüklenmesi
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ametist_logo, options);

            // QR kodu ve logo Bitmap'lerinin üst üste bindirilmesi
            Bitmap mergedBitmap = Bitmap.createBitmap(qrCodeSize, qrCodeSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mergedBitmap);
            canvas.drawBitmap(qrBitmap, 0, 0, null);
            int logoSize = qrCodeSize / 4;
            logoBitmap = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, false);
            int centerX = (qrCodeSize - logoSize) / 2;
            int centerY = (qrCodeSize - logoSize) / 2;
            canvas.drawBitmap(logoBitmap, centerX, centerY, null);

            // İşlem sonucunda oluşan Bitmap'in ImageView'a set edilmesi
            bottomSheetView.qrView.setImageBitmap(mergedBitmap);

            bottomSheetView.buttonQrScanner.setOnClickListener(v -> {
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setPrompt(getString(R.string.scan_qr_code_message)); // Kullanıcıya gösterilecek mesaj
                integrator.setOrientationLocked(false); // Ekran yönü kilitli değil
                integrator.setBeepEnabled(false);
                integrator.initiateScan(); // Tarama işlemini başlat

                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.setOnDismissListener(dialog -> {
                layoutParams.screenBrightness = oldBrightness;
                getWindow().setAttributes(layoutParams);
            });

            bottomSheetDialog.setContentView(bottomSheetView.getRoot());
            bottomSheetDialog.show();
        });
    }

    private void follow() {
        if (binding.buttonFollow.getContentDescription().equals(getString(R.string.followed)))
        {
            database.collection(Constants.DATABASE_PATH_USERS)
                    .document(user.getUserId())
                    .collection("followers")
                    .document(Objects.requireNonNull(auth.getUid()))
                    .delete().addOnCompleteListener(task -> {
                        database.collection(Constants.DATABASE_PATH_USERS)
                                .document(auth.getUid())
                                .collection("followings")
                                .document(user.getUserId())
                                .delete().addOnCompleteListener(task12 -> {
                                    binding.buttonFollow.setContentDescription(getString(R.string.follow));
                                    binding.buttonFollow.setBackgroundColor(getColor(R.color.accent_purple_dark));
                                    binding.buttonFollow.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_add_borderless));
                                });
                    });
        }
        else
        {
            //Karşı tarafın listesine ekle
            HashMap<String, Object> followMap = new HashMap<>();
            followMap.put("followedBy", FirebaseAuth.getInstance().getUid());
            followMap.put("followedAt", new Date().getTime());
            followMap.put("isRead", false);

            database.collection(Constants.DATABASE_PATH_USERS)
                    .document(user.getUserId())
                    .collection("followers")
                    .document(Objects.requireNonNull(auth.getUid()))
                    .set(followMap).addOnCompleteListener(task -> {
                        //Bildirim gönder
                        database.collection(Constants.DATABASE_PATH_USERS)
                                .document(user.getUserId())
                                .collection("notifications")
                                .add(followMap).addOnCompleteListener(task13 -> {
                                    try {
                                        JSONArray tokens = new JSONArray();
                                        tokens.put(user.getToken());

                                        JSONObject data = new JSONObject();
                                        data.put(Constants.USER_ID, FirebaseAuth.getInstance().getUid());
                                        data.put(Constants.USERNAME, getString(R.string.new_follower));
                                        data.put(Constants.TOKEN, Hawk.get(Constants.TOKEN));
                                        data.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_FOLLOW);
                                        data.put(Constants.MESSAGE, Hawk.get(Constants.USERNAME));

                                        JSONObject body = new JSONObject();
                                        body.put(Constants.REMOTE_MSG_DATA, data);
                                        body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                                        sendNotification(body.toString());
                                    } catch (Exception e) {
                                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    //Kendi listene ekle
                                    HashMap<String, Object> followMap2 = new HashMap<>();
                                    followMap2.put("followedTo", user.getUserId());
                                    followMap2.put("followedAt", new Date().getTime());

                                    database.collection(Constants.DATABASE_PATH_USERS)
                                            .document(auth.getUid())
                                            .collection("followings")
                                            .document(user.getUserId())
                                            .set(followMap2).addOnCompleteListener(task1 -> {
                                                //Butonu düzenle
                                                binding.buttonFollow.setContentDescription(getString(R.string.followed));
                                                binding.buttonFollow.setBackgroundColor(getColor(R.color.accent_blue_dark));
                                                binding.buttonFollow.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_check));
                                            });
                                });
                    });
        }
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
                                Toast.makeText(ProfileActivity.this, error.getString("error"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ProfileActivity.this, "Bildirim gönderildi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Hata: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getUserInfo()
    {
        database.collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    if (documentSnapshot.getId().equals(user.getUserId()))
                    {
                        user.setAge(documentSnapshot.getString("age"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                        user.setGender(documentSnapshot.getString("gender"));
                        user.setName(documentSnapshot.getString("name"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setRole(documentSnapshot.getString("role"));
                        user.setLooking(documentSnapshot.getString("looking"));
                        user.setRelationship(documentSnapshot.getString("relationship"));
                        user.setSexuality(documentSnapshot.getString("sexuality"));
                        user.setFollowerCount(Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("followerCount")).toString()));
                        user.setFollowingCount(Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("followingCount")).toString()));
                        Object height = documentSnapshot.get("height");
                        if (height != null)
                        {
                            user.setHeight(Integer.parseInt(height.toString()));
                        }
                        else
                        {
                            database.collection(Constants.DATABASE_PATH_USERS).document(user.getUserId()).update("height", 130);
                            user.setHeight(130);
                        }
                        Object weight = documentSnapshot.get("weight");
                        if (weight != null)
                        {
                            user.setWeight(Integer.parseInt(weight.toString()));
                        }
                        else
                        {
                            database.collection(Constants.DATABASE_PATH_USERS).document(user.getUserId()).update("weight", 30);
                            user.setWeight(30);
                        }
                        Object signupDate = documentSnapshot.get("signupDate");
                        if (signupDate != null)
                        {
                            user.setSignupDate(Long.parseLong(signupDate.toString()));
                        }
                        else
                        {
                            database.collection(Constants.DATABASE_PATH_USERS).document(user.getUserId()).update("signupDate", Long.parseLong("1685965594357"));
                            user.setSignupDate(Long.parseLong("1685965594357"));
                        }

                        if (documentSnapshot.getId().equals(auth.getUid()))
                        {
                            binding.buttonFollow.setVisibility(View.GONE);
                            binding.buttonMessage.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_edit));
                            binding.buttonMessage.setText(getString(R.string.edit));
                        }

                        database.collection(Constants.DATABASE_PATH_USERS).document(Objects.requireNonNull(user.getUserId())).collection("photos").get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful())
                            {
                                pictureList.clear();
                                for (QueryDocumentSnapshot photoIDs : task1.getResult())
                                {
                                    if (photoIDs.exists())
                                    {
                                        PhotoModel photo = new PhotoModel();
                                        photo.setPhotoId(photoIDs.getId());
                                        photo.setLink(photoIDs.getString("link"));
                                        photo.setDate(photoIDs.getLong("date"));
                                        pictureList.add(photo);
                                    }
                                }
                                pictureList.sort(Comparator.comparing(PhotoModel::getDate));
                                photoAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

                binding.username.setText(user.getName());
                binding.username2.setText(user.getName());
                binding.textAbout.setText(user.getAbout());
                binding.textAge.setText(user.getAge());
                switch (user.getGender()) {
                    case "1":
                        binding.textGender.setText(getString(R.string.man));
                        break;
                    case "2":
                        binding.textGender.setText(getString(R.string.woman));
                        break;
                }
                binding.textRole.setText(user.getRole());
                binding.textRelationship.setText(user.getRelationship());
                binding.textSeek.setText(user.getLooking());
                binding.textHeight.setText(user.getHeight()+" cm");
                binding.textWeight.setText(user.getWeight()+" kg");
                binding.textSexuality.setText(user.getSexuality());
                binding.textSignupDate.setText(TimeAgo.using(user.getSignupDate()));
            }
        });
    }

    private void getPosts()
    {
        database.collection(Constants.DATABASE_PATH_USERS).document(user.getUserId()).collection("Posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                postModels.clear();
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    PostModel postModel = new PostModel();
                    postModel.setPostId(documentSnapshot.getId());
                    postModel.setPostText(documentSnapshot.getString("postText"));
                    postModel.setPostPicture(documentSnapshot.getString("postPicture"));
                    postModel.setPostedBy(documentSnapshot.getString("postedBy"));
                    postModel.setPostedAt(documentSnapshot.getLong("postedAt"));
                    postModel.setCommentCount(Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("commentCount")).toString()));
                    postModel.setLikeCount(Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("likeCount")).toString()));
                    postModels.add(postModel);
                }
                postModels.sort(Comparator.comparing(PostModel::getPostedAt).reversed());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Tarama iptal edildi", Toast.LENGTH_LONG).show();
            } else {
                String qrText = result.getContents();
                // QR kodu başarıyla tarandı, yapılacak işlemler burada gerçekleştirilebilir
                Toast.makeText(this, qrText, Toast.LENGTH_SHORT).show();

                database.collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        UserModel scannedUser = new UserModel();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                        {
                            if (documentSnapshot.getId().equals(qrText))
                            {
                                scannedUser.setName(documentSnapshot.getString("name"));
                                scannedUser.setProfilePic(documentSnapshot.getString("profilePic"));
                                scannedUser.setAbout(documentSnapshot.getString("about"));
                                scannedUser.setGender(documentSnapshot.getString("gender"));
                                scannedUser.setAge(documentSnapshot.getString("age"));
                                scannedUser.setUserId(documentSnapshot.getId());
                                scannedUser.setFollowerCount(Integer.parseInt(String.valueOf(documentSnapshot.get("followerCount"))));
                                scannedUser.setFollowingCount(Integer.parseInt(String.valueOf(documentSnapshot.get("followingCount"))));
                            }
                        }
                        Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                        intent.putExtra("receiverUser", scannedUser);
                        startActivity(intent);
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(String link) {
        Intent intent = new Intent(ProfileActivity.this, FullProfilePhotoActivity.class);
        intent.putExtra("pictureUrl", link);
        startActivity(intent);
    }

    @Override
    public void onRemove(long date, int position) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
        getPosts();
    }
}