package com.serkantken.ametist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.PostAdapter;
import com.serkantken.ametist.databinding.ActivityProfileBinding;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ProfileActivity extends BaseActivity
{
    private ActivityProfileBinding binding;
    private ArrayList<PostModel> postModels;
    private PostAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private UserModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationIcon(R.drawable.ic_back_profile);
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        user = (UserModel) getIntent().getSerializableExtra("receiverUser");
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        postModels = new ArrayList<>();

        getUserInfo();
        getPosts();

        adapter = new PostAdapter(postModels, ProfileActivity.this, ProfileActivity.this, database);
        binding.posts.setAdapter(adapter);
        binding.posts.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));

        if (!(binding.profileImage.getDrawable() == AppCompatResources.getDrawable(this, R.drawable.ic_person_profile)))
        {
            binding.profileImage.setOnClickListener(view -> {
                Intent intent = new Intent(ProfileActivity.this, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", user.getProfilePic());
                startActivity(intent);
            });
        }

        binding.buttonMessage.setOnClickListener(view -> {
            if (user.getUserId().equals(auth.getUid()))
            {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
                finish();
            }
            else
            {
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.putExtra("receiverUser", user);
                startActivity(intent);
            }
        });

        binding.buttonFollow.setOnClickListener(view -> {
            //follow();
        });
    }

    private void follow() {
        HashMap<String, Object> follow = new HashMap<>();
        follow.put("followId", user.getUserId());
        follow.put("followedBy", auth.getUid());
        follow.put("followedAt", new Date().getTime());
        database.collection("Users").document(auth.getUid()).collection("followings").add(follow).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful())
                {
                    HashMap<String, Object> follower = new HashMap<>();
                    follower.put("followId", auth.getUid());
                    follower.put("followedAt", new Date().getTime());
                    database.collection("Users").document(user.getUserId()).collection("followers").add(follower).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful())
                            {
                                binding.textFollow.setText(getString(R.string.unfollow));
                            }
                        }
                    });
                }
            }
        });
    }

    private void getUserInfo()
    {
        database.collection("Users").get().addOnCompleteListener(task -> {
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

                        if (documentSnapshot.getId().equals(auth.getUid()))
                        {
                            binding.buttonFollow.setVisibility(View.GONE);
                            binding.iconMessage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_edit));
                            binding.textMessage.setText(getString(R.string.edit));
                        }
                    }
                }

                binding.collapsingToolbar.setTitle(user.getName());
                binding.collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
                binding.collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.primary_text));
                binding.textAbout.setText(user.getAbout());
                binding.textAge.setText(user.getAge());
                if (user.getGender().equals("0"))
                {
                    binding.textGender.setText("-");
                }
                else if (user.getGender().equals("1"))
                {
                    binding.textGender.setText(getString(R.string.man));
                }
                else if (user.getGender().equals("2"))
                {
                    binding.textGender.setText(getString(R.string.woman));
                }
                Glide.with(ProfileActivity.this).load(user.getProfilePic()).placeholder(R.drawable.ic_person_profile).into(binding.profileImage);
            }
        });
    }

    private void getPosts()
    {
        database.collection("Users").document(user.getUserId()).collection("Posts").get().addOnCompleteListener(task -> {
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
                    postModel.setCommentCount(Integer.parseInt(documentSnapshot.get("commentCount").toString()));
                    postModel.setLikeCount(Integer.parseInt(documentSnapshot.get("likeCount").toString()));
                    postModels.add(postModel);
                }
                postModels.sort(Comparator.comparing(PostModel::getPostedAt).reversed());
                adapter.notifyDataSetChanged();
            }
        });
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
    }
}