package com.serkantken.ametist.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.NotificationsAdapter;
import com.serkantken.ametist.adapters.PostAdapter;
import com.serkantken.ametist.databinding.FragmentDashboardBinding;
import com.serkantken.ametist.databinding.LayoutNewPostDialogBinding;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import org.checkerframework.checker.units.qual.C;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import eightbitlab.com.blurview.BlurView;

public class DashboardFragment extends Fragment
{
    FragmentDashboardBinding binding;
    ArrayList<PostModel> postModels;
    ArrayList<String> followingUsers;
    PostAdapter adapter;
    FirebaseFirestore database;
    FirebaseAuth auth;
    UserModel currentUser;
    Utilities utilities;
    ActivityResultLauncher<Intent> getContent;
    LayoutNewPostDialogBinding dialogBinding;
    String postUri;
    Uri resultUri;
    Boolean isPhotoSelected = false, isPressed = false;
    Balloon balloon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        currentUser = new UserModel();
        postModels = new ArrayList<>();
        followingUsers = new ArrayList<>();
        utilities = new Utilities(requireContext(), requireActivity());
        adapter = new PostAdapter(postModels, getContext(), getActivity());
        binding.dashboardRV.setAdapter(adapter);
        binding.dashboardRV.setLayoutManager(new LinearLayoutManager(getContext()));

        getUserInfo();
        getPosts();

        CoordinatorLayout.LayoutParams newPostButtonParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newPostButtonParams.setMargins(0, 0, utilities.convertDpToPixel(16), utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(66));
        newPostButtonParams.gravity = Gravity.BOTTOM|Gravity.END;
        binding.newPostButton.setLayoutParams(newPostButtonParams);

        binding.dashboardRV.setPadding(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(56), 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(66));

        try {
            Field f = binding.dashboardRefresher.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView imageView = (ImageView) f.get(binding.dashboardRefresher);
            assert imageView != null;
            imageView.setAlpha(0.0f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        binding.dashboardRefresher.setOnRefreshListener(this::getPosts);

        binding.dashboardRV.setOnScrollChangeListener((view, x, y, oldX, oldY) -> {
            if (y > oldY && binding.newPostButton.isExtended())
            {
                binding.newPostButton.shrink();
            }
            if (y < oldY && !binding.newPostButton.isExtended())
            {
                binding.newPostButton.extend();
            }
        });

        binding.newPostButton.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isPressed = true;
                    animateCard(true, binding.newPostButton, 1);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isPressed)
                    {
                        animateCard(false, binding.newPostButton, 1);
                    }
                    isPressed = false;
                    return true;
            }
            return false;
        });

        getContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                assert result.getData() != null;
                resultUri = result.getData().getData();
                if (resultUri != null) {
                    postUri = resultUri.toString();
                    isPhotoSelected = true;
                    dialogBinding.postImage.setVisibility(View.VISIBLE);
                    Glide.with(requireContext()).load(postUri).into(dialogBinding.postImage);
                    dialogBinding.buttonSubmitBackground.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.background_post_footer_buttons));
                    dialogBinding.buttonSubmit.setEnabled(true);
                }
            }
        });

        return binding.getRoot();
    }

    private void animateCard(boolean isPressed, View view, int mode)
    {
        if (isPressed)
        {
            Animation anim = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
            anim.setFillAfter(true);
            view.startAnimation(anim);
        }
        else
        {
            Animation anim = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_reverse);
            view.startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    switch (mode)
                    {
                        case 1:
                            showNewPostDialog();
                            break;
                        case 2:
                            showBalloon(dialogBinding.buttonAddPhoto, 3);
                            BlurView blurView = balloon.getContentView().findViewById(R.id.blur);
                            utilities.blur(blurView, 10f, false);

                            CardView cameraButton = balloon.getContentView().findViewById(R.id.buttonCamera);
                            cameraButton.setOnClickListener(v -> {
                                ImagePicker.with(requireActivity()).cameraOnly().cropSquare().createIntent(intent -> {
                                    getContent.launch(intent);
                                    return null;
                                });
                                balloon.dismiss();
                            });

                            CardView galleryButton = balloon.getContentView().findViewById(R.id.buttonGallery);
                            galleryButton.setOnClickListener(v -> {
                                ImagePicker.with(requireActivity()).galleryOnly().cropSquare().createIntent(intent -> {
                                    getContent.launch(intent);
                                    return null;
                                });
                                balloon.dismiss();
                            });
                            break;
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    private void showNewPostDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
        dialogBinding = LayoutNewPostDialogBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialogBinding.buttonSubmitBackground.setBackgroundColor(requireContext().getColor(R.color.secondary_text));
        dialogBinding.buttonSubmit.setEnabled(false);
        AtomicReference<Boolean> isPrivacyLockChecked = new AtomicReference<>(false);

        Glide.with(requireContext()).load(currentUser.getProfilePic()).placeholder(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_person_profile)).into(dialogBinding.profileImage);
        dialogBinding.username.setText(currentUser.getName());
        dialogBinding.buttonAddPhoto.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isPressed = true;
                    animateCard(true, dialogBinding.buttonAddPhoto, 2);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isPressed)
                    {
                        animateCard(false, dialogBinding.buttonAddPhoto, 2);
                    }
                    isPressed = false;
                    return true;
            }
            return false;
        });
        dialogBinding.postText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0)
                {
                    dialogBinding.buttonSubmitBackground.setBackgroundColor(requireContext().getColor(R.color.secondary_text));
                    dialogBinding.buttonSubmit.setEnabled(false);
                }
                else
                {
                    dialogBinding.buttonSubmitBackground.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.background_post_footer_buttons));
                    dialogBinding.buttonSubmit.setEnabled(true);
                }
            }
        });

        dialogBinding.privacyLock.setOnClickListener(view1 -> isPrivacyLockChecked.set(selectPrivacyLock(isPrivacyLockChecked.get())));
        dialogBinding.privacyLockText.setOnClickListener(view1 -> isPrivacyLockChecked.set(selectPrivacyLock(isPrivacyLockChecked.get())));

        dialogBinding.buttonSubmit.setOnClickListener(view1 -> {
            dialogBinding.buttonAddPhoto.setVisibility(View.GONE);
            dialogBinding.buttonSubmit.setVisibility(View.GONE);
            dialogBinding.profileImage.setVisibility(View.GONE);
            dialogBinding.username.setVisibility(View.GONE);
            dialogBinding.postText.setVisibility(View.GONE);
            dialogBinding.postImage.setVisibility(View.GONE);
            dialogBinding.privacyLockText.setVisibility(View.GONE);
            dialogBinding.privacyLock.setVisibility(View.GONE);
            dialogBinding.progressBar.setVisibility(View.VISIBLE);
            dialogBinding.loadingText.setVisibility(View.VISIBLE);

            PostModel postModel = new PostModel();
            String postId = UUID.randomUUID().toString();
            postModel.setPostId(postId);
            postModel.setPostedAt(new Date().getTime());
            postModel.setPostedBy(auth.getUid());
            postModel.setPostText(dialogBinding.postText.getText().toString());
            postModel.setLikeCount(0);
            postModel.setCommentCount(0);
            if (isPhotoSelected)
            {
                StorageReference filePath = FirebaseStorage.getInstance().getReference(Constants.DATABASE_PATH_USERS).child(Objects.requireNonNull(auth.getUid())).child(Constants.DATABASE_PATH_POST_PICS).child(UUID.randomUUID().toString() + ".jpg");
                UploadTask uploadTask = filePath.putFile(resultUri, new StorageMetadata.Builder().build());
                uploadTask.addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    dialogBinding.progressBar.setProgress((int) progress, true);
                }).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    postModel.setPostPicture(downloadUrl);
                    database.collection(Constants.DATABASE_PATH_USERS).document(auth.getUid()).collection(Constants.DATABASE_PATH_POSTS).document(postId).set(postModel).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            isPhotoSelected = false;
                            dialog.dismiss();
                            getPosts();
                        }
                    });
                }));
            }
            else
            {
                database.collection(Constants.DATABASE_PATH_USERS).document(Objects.requireNonNull(auth.getUid())).collection(Constants.DATABASE_PATH_POSTS).document(postId).set(postModel).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        dialog.dismiss();
                        getPosts();
                    }
                });
            }
        });
        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        dialog.show();
    }

    private void showBalloon(View view, int position)
    {
        balloon = new Balloon.Builder(requireContext())
                .setLayout(R.layout.layout_chat_content_source)
                .setArrowSize(0)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
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

    private boolean selectPrivacyLock(boolean isPrivacyLockSelected)
    {
        if (!isPrivacyLockSelected)
        {
            dialogBinding.privacyLock.setSpeed(3);
            dialogBinding.privacyLock.playAnimation();
            return true;
        }
        else
        {
            dialogBinding.privacyLock.setSpeed(-3);
            dialogBinding.privacyLock.playAnimation();
            return false;
        }
    }

    private void getUserInfo()
    {
        database.collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    if (documentSnapshot.getId().equals(auth.getUid()))
                    {
                        currentUser.setUserId(documentSnapshot.getId());
                        currentUser.setProfilePic(documentSnapshot.getString("profilePic"));
                        currentUser.setName(documentSnapshot.getString("name"));
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getPosts()
    {
        binding.dashboardRefresher.setRefreshing(false);
        binding.dashboardRV.showShimmerAdapter();
        followingUsers.clear();
        followingUsers.add(auth.getUid());
        database.collection(Constants.DATABASE_PATH_USERS).document(Objects.requireNonNull(auth.getUid())).collection(Constants.DATABASE_PATH_FOLLOWINGS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null)
            {
                for (QueryDocumentSnapshot snapshot : task.getResult())
                {
                    followingUsers.add(snapshot.getId());
                }

                postModels.clear();
                for (int i = 0; i < followingUsers.size(); i++)
                {
                    Log.i("sira", i+"");
                    int index = i;
                    database.collection(Constants.DATABASE_PATH_USERS).document(followingUsers.get(index)).collection(Constants.DATABASE_PATH_POSTS).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful())
                        {
                            for (QueryDocumentSnapshot documentSnapshot : task1.getResult())
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
                            if (postModels.size() != 0)
                                postModels.sort(Comparator.comparing(PostModel::getPostedAt).reversed());
                            if (index == followingUsers.size()-1)
                                adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(requireContext(), getString(R.string.check_your_connection), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        binding.dashboardRefresher.setRefreshing(false);
        binding.dashboardRV.hideShimmerAdapter();
    }
}