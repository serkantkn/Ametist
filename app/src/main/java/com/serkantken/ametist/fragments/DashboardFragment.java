package com.serkantken.ametist.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.PostAdapter;
import com.serkantken.ametist.databinding.FragmentDashboardBinding;
import com.serkantken.ametist.databinding.LayoutNewPostDialogBinding;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Utilities;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
    ActivityResultLauncher<String> getContent;
    LayoutNewPostDialogBinding dialogBinding;
    String postUri;
    Uri resultUri;
    Boolean isPhotoSelected = false;

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

        binding.newPostButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
            dialogBinding = LayoutNewPostDialogBinding.inflate(getLayoutInflater());
            builder.setView(dialogBinding.getRoot());
            AlertDialog dialog = builder.create();
            dialogBinding.buttonSubmitBackground.setBackgroundColor(requireContext().getColor(R.color.secondary_text));
            dialogBinding.buttonSubmit.setEnabled(false);
            AtomicReference<Boolean> isPrivacyLockChecked = new AtomicReference<>(false);

            Glide.with(requireContext()).load(currentUser.getProfilePic()).placeholder(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_person_profile)).into(dialogBinding.profileImage);
            dialogBinding.username.setText(currentUser.getName());
            dialogBinding.buttonAddPhoto.setOnClickListener(view1 -> {
                if (isPermissionGranted())
                {
                    getContent.launch("image/*");
                }
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
                ProgressDialog progressDialog = new ProgressDialog(requireContext());
                progressDialog.setMessage(getString(R.string.sending));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.create();
                progressDialog.show();
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
                    StorageReference filePath = FirebaseStorage.getInstance().getReference("Users").child(Objects.requireNonNull(auth.getUid())).child("postPics").child(UUID.randomUUID().toString() + ".jpg");
                    StorageTask uploadTask = filePath.putFile(resultUri);
                    uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                        Uri downloadUri = task.getResult();
                        String downloadUrl = downloadUri.toString();
                        postModel.setPostPicture(downloadUrl);
                        database.collection("Users").document(auth.getUid()).collection("Posts").document(postId).set(postModel).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful())
                            {
                                isPhotoSelected = false;
                                progressDialog.dismiss();
                                dialog.dismiss();
                                getPosts();
                            }
                        });
                    });
                }
                else
                {
                    database.collection("Users").document(Objects.requireNonNull(auth.getUid())).collection("Posts").document(postId).set(postModel).addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                        {
                            progressDialog.dismiss();
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
        });

        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            String destUri = UUID.randomUUID().toString() + ".jpg";

            UCrop.Options options = new UCrop.Options();
            options.setLogoColor(getResources().getColor(R.color.accent_purple_dark, null));
            options.setFreeStyleCropEnabled(false);
            options.setToolbarTitle(getString(R.string.crop));
            options.withAspectRatio(1, 1);
            UCrop.of(result, Uri.fromFile(new File(requireContext().getCacheDir(), destUri)))
                    .withOptions(options)
                    .start(requireActivity(), this);
        });

        return binding.getRoot();
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

    private boolean isPermissionGranted()
    {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void getUserInfo()
    {
        database.collection("Users").get().addOnCompleteListener(task -> {
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

    private void getPosts()
    {
        binding.dashboardRefresher.setRefreshing(true);
        followingUsers.clear();
        followingUsers.add(auth.getUid());
        database.collection("Users").document(Objects.requireNonNull(auth.getUid())).collection("followings").get().addOnCompleteListener(task -> {
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
                    database.collection("Users").document(followingUsers.get(index)).collection("Posts").get().addOnCompleteListener(task1 -> {
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                postUri = resultUri.toString();
                isPhotoSelected = true;
                dialogBinding.postImage.setVisibility(View.VISIBLE);
                Glide.with(requireContext()).load(postUri).into(dialogBinding.postImage);
                dialogBinding.buttonSubmitBackground.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.background_post_footer_buttons));
                dialogBinding.buttonSubmit.setEnabled(true);
            }
        }
    }
}