package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.CommentActivity;
import com.serkantken.ametist.activities.FullProfilePhotoActivity;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.databinding.LayoutNewPostDialogBinding;
import com.serkantken.ametist.databinding.LayoutPostBinding;
import com.serkantken.ametist.models.CommentModel;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<PostModel> postModels;
    Context context;
    Activity activity;
    FirebaseFirestore database;
    String currentUserId;
    Balloon balloon;
    int likeCount;
    ArrayList<CommentModel> comments = new ArrayList<>();
    boolean result, isPressed = false;

    public PostAdapter(ArrayList<PostModel> postModels, Context context, Activity activity) {
        this.postModels = postModels;
        this.context = context;
        this.activity = activity;
        database = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutPostBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
            ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.binding.postImage.setVisibility(View.GONE);
        UserModel user = new UserModel();
        PostModel postModel = postModels.get(position);
        database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.getId().equals(postModel.getPostedBy())) {
                        user.setUserId(documentSnapshot.getId());
                        user.setName(documentSnapshot.getString("name"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setAge(documentSnapshot.getString("age"));
                        user.setGender(documentSnapshot.getString("gender"));
                    }
                }
                viewHolder.binding.username.setText(user.getName());
                Glide.with(context).load(user.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person)).into(viewHolder.binding.profileImage);
            }
        });
        if (Objects.equals(postModel.getPostedBy(), currentUserId))
        {
            viewHolder.binding.buttonMenu.setVisibility(View.VISIBLE);
        }

        isLiked(postModel, viewHolder);

        viewHolder.binding.postText.setText(postModel.getPostText());
        viewHolder.binding.textLikeCount.setText(String.valueOf(postModel.getLikeCount()));
        viewHolder.binding.textCommentCount.setText(String.valueOf(postModel.getCommentCount()));
        viewHolder.binding.date.setText(TimeAgo.using(postModel.getPostedAt()));
        viewHolder.binding.profileImage.setOnClickListener(view -> onUserClicked(user));
        viewHolder.binding.username.setOnClickListener(view -> onUserClicked(user));
        if (!Objects.equals(postModel.getPostPicture(), null))
        {
            viewHolder.binding.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(postModel.getPostPicture()).into(viewHolder.binding.postImage);
            viewHolder.binding.postImage.setOnLongClickListener(view -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", postModel.getPostPicture());
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, viewHolder.binding.postImage, "photograph");
                context.startActivity(intent, optionsCompat.toBundle());
                return true;
            });
        }

        viewHolder.binding.buttonLike.setOnClickListener(view -> {
            if (viewHolder.binding.buttonLike.getTag().equals("Liked"))
            {
                unlikePost(postModel, viewHolder);
            }
            else
            {
                likePost(postModel, viewHolder);
            }
        });

        viewHolder.binding.buttonComment.setOnClickListener(view -> {
            if (!Objects.equals(postModel.getPostPicture(), null))
            {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", postModel.getPostId());
                intent.putExtra("userId", postModel.getPostedBy());
                intent.putExtra("postImage", postModel.getPostPicture());
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, new Pair<>(viewHolder.binding.cardLayout,"comment"), new Pair<>(viewHolder.binding.postImage, "photograph"));
                context.startActivity(intent, optionsCompat.toBundle());
            }
            else
            {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", postModel.getPostId());
                intent.putExtra("userId", postModel.getPostedBy());
                intent.putExtra("postImage", "null");
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, viewHolder.binding.cardLayout, "comment");
                context.startActivity(intent, optionsCompat.toBundle());
            }
        });

        viewHolder.binding.buttonMenu.setOnClickListener(view -> {
            showBalloon(viewHolder.binding.buttonMenu, 4);
            BlurView blurView = balloon.getContentView().findViewById(R.id.blur);
            Utilities utilities = new Utilities(context, activity);
            utilities.blur(new BlurView[]{blurView}, 10f, false);

            CardView deleteButton = balloon.getContentView().findViewById(R.id.buttonDelete);
            deleteButton.setOnClickListener(v -> {
                postDelete(postModel.getPostId(), position);
                balloon.dismiss();
            });

            CardView editButton = balloon.getContentView().findViewById(R.id.buttonEdit);
            editButton.setOnClickListener(v -> {
                postEdit(postModel.getPostId(), position);
                balloon.dismiss();
            });
        });
    }

    private void animateCard(PostAdapter.ViewHolder holder, boolean isPressed, UserModel userModel)
    {
        if (isPressed)
        {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale);
            anim.setFillAfter(true);
            holder.binding.getRoot().startAnimation(anim);
        }
        else
        {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_reverse);
            holder.binding.getRoot().startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void likePost(PostModel postModel, ViewHolder holder)
    {
        HashMap<String, Object> liker = new HashMap<>();
        liker.put("userId", FirebaseAuth.getInstance().getUid());
        liker.put("date", new Date().getTime());

        database.collection("LikesComments")
                .document(postModel.getPostId())
                .collection("Likes")
                .document(FirebaseAuth.getInstance().getUid())
                .set(liker)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        database.collection(Constants.DATABASE_PATH_USERS)
                                .document(postModel.getPostedBy())
                                .collection("Posts")
                                .document(postModel.getPostId())
                                .get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful())
                                    {
                                        DocumentSnapshot snapshot = task1.getResult();
                                        likeCount = Integer.parseInt(Objects.requireNonNull(snapshot.get("likeCount")).toString());
                                        likeCount++;
                                        database.collection(Constants.DATABASE_PATH_USERS)
                                                .document(postModel.getPostedBy())
                                                .collection("Posts")
                                                .document(postModel.getPostId())
                                                .update("likeCount", likeCount).addOnCompleteListener(task2 -> {
                                                    holder.binding.textLikeCount.setText(String.valueOf(likeCount));
                                                    holder.binding.buttonLike.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_like_fill));
                                                    holder.binding.buttonLike.setTag("Liked");
                                                });
                                    }
                                });
                    }
                });
    }

    private void unlikePost(PostModel postModel, ViewHolder holder)
    {
        database.collection("LikesComments")
                .document(postModel.getPostId())
                .collection("Likes")
                .document(FirebaseAuth.getInstance().getUid())
                .delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        database.collection(Constants.DATABASE_PATH_USERS)
                                .document(postModel.getPostedBy())
                                .collection("Posts")
                                .document(postModel.getPostId())
                                .get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful())
                                    {
                                        DocumentSnapshot snapshot = task1.getResult();
                                        likeCount = Integer.parseInt(Objects.requireNonNull(snapshot.get("likeCount")).toString());
                                        likeCount--;
                                        database.collection(Constants.DATABASE_PATH_USERS)
                                                .document(postModel.getPostedBy())
                                                .collection("Posts")
                                                .document(postModel.getPostId())
                                                .update("likeCount", likeCount).addOnCompleteListener(task2 -> {
                                                    holder.binding.textLikeCount.setText(String.valueOf(likeCount));
                                                    holder.binding.buttonLike.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_like_empty));
                                                    holder.binding.buttonLike.setTag("NoLike");
                                                });
                                    }
                                });
                    }
                });
    }

    private void isLiked(PostModel postModel, ViewHolder holder)
    {
        holder.binding.buttonLike.setTag("NoLike");
        database.collection("LikesComments")
                .document(postModel.getPostId())
                .collection("Likes")
                .get().addOnCompleteListener((OnCompleteListener<QuerySnapshot>) task -> {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot snapshot : task.getResult())
                        {
                            if (snapshot.getId().equals(FirebaseAuth.getInstance().getUid()))
                            {
                                holder.binding.buttonLike.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_like_fill));
                                holder.binding.buttonLike.setTag("Liked");
                            }
                            else
                            {
                                holder.binding.buttonLike.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_like_empty));
                                holder.binding.buttonLike.setTag("NoLike");
                            }
                        }
                    }
                });
    }

    private void getComment(String userId, String postId, ViewHolder holder) {
        comments.clear();
        database.collection(Constants.DATABASE_PATH_USERS)
                .document(userId)
                .collection("Posts")
                .document(postId)
                .collection("Comments")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            CommentModel model = new CommentModel();
                            model.setCommentId(snapshot.getId());
                            model.setCommentText(snapshot.getString("commentText"));
                            model.setUserId(snapshot.getString("userId"));
                            model.setPostId(snapshot.getString("postId"));
                            model.setCommentedAt(snapshot.getLong("date"));
                            comments.add(model);
                        }
                        comments.sort(Comparator.comparing(CommentModel::getCommentedAt));

                        if (comments.size() > 0)
                        {
                            database.collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful())
                                {
                                    for (QueryDocumentSnapshot snapshot : task1.getResult())
                                    {
                                        if (Objects.equals(snapshot.getId(), comments.get(comments.size()-1).getUserId()))
                                        {
                                            holder.binding.commentUsername.setText(snapshot.getString("name"));
                                        }
                                    }
                                    holder.binding.commentText.setText(comments.get(comments.size()-1).getCommentText());
                                    holder.binding.buttonMoreComment.setOnClickListener(view -> {
                                        Intent intent = new Intent(context, CommentActivity.class);
                                        intent.putExtra("postId", postId);
                                        intent.putExtra("userId", userId);
                                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.binding.cardLayout, "comment");
                                        context.startActivity(intent, optionsCompat.toBundle());
                                    });
                                    holder.binding.commentUsername.setVisibility(View.VISIBLE);
                                    holder.binding.commentText.setVisibility(View.VISIBLE);
                                    holder.binding.buttonMoreComment.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });
    }

    private void postDelete(String postId, int position)
    {
        balloon.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.delete_post_question);
        dialog.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            database.collection("Users").document(currentUserId).collection("Posts").document(postId).delete().addOnCompleteListener(task -> {
                notifyItemRemoved(position);
                dialogInterface.dismiss();
            });
        });
        dialog.setNegativeButton(R.string.no, (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        dialog.create();
        dialog.show();
    }

    private void postEdit(String postId, int position)
    {
        balloon.dismiss();
        database.collection("Users").document(currentUserId).collection("Posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                HashMap<String, Object> model = new HashMap<>();
                for (QueryDocumentSnapshot snapshot : task.getResult())
                {
                    if (Objects.equals(snapshot.getId(), postId))
                    {
                        model.put("postId", snapshot.getId());
                        model.put("postText", snapshot.getString("postText"));
                        model.put("postPicture", snapshot.getString("postPicture"));
                        model.put("postedBy", snapshot.getString("postedBy"));
                        model.put("postedAt", snapshot.getLong("postedAt"));
                        model.put("commentCount", Integer.parseInt(snapshot.get("commentCount").toString()));
                        model.put("likeCount", Integer.parseInt(snapshot.get("likeCount").toString()));
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                LayoutNewPostDialogBinding dialogBinding = LayoutNewPostDialogBinding.inflate(activity.getLayoutInflater());
                builder.setView(dialogBinding.getRoot());
                AlertDialog dialog = builder.create();

                dialogBinding.profileImage.setVisibility(View.GONE);
                dialogBinding.username.setVisibility(View.GONE);
                dialogBinding.buttonAddPhoto.setVisibility(View.GONE);
                dialogBinding.postText.setText(model.get("postText").toString());
                if (!Objects.equals(model.get("postPicture"), null))
                {
                    Glide.with(context).load(model.get("postPicture").toString()).into(dialogBinding.postImage);
                    dialogBinding.postImage.setVisibility(View.VISIBLE);
                }

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
                            dialogBinding.buttonSubmitBackground.setBackgroundColor(context.getColor(R.color.secondary_text));
                            dialogBinding.buttonSubmit.setEnabled(false);
                        }
                        else
                        {
                            dialogBinding.buttonSubmitBackground.setBackground(AppCompatResources.getDrawable(context, R.drawable.background_post_footer_buttons));
                            dialogBinding.buttonSubmit.setEnabled(true);
                        }
                    }
                });

                dialogBinding.buttonSubmit.setOnClickListener(view -> {
                    model.put("postText", dialogBinding.postText.getText().toString());
                    database.collection("Users").document(currentUserId).collection("Posts").document(postId).update(model);
                    notifyItemChanged(position);
                    dialog.dismiss();
                });

                if (dialog.getWindow() != null)
                {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                dialog.show();
            }
        });
    }

    private void showBalloon(View view, int position)
    {
        balloon = new Balloon.Builder(context)
                .setLayout(R.layout.popup_layout)
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

    public void onUserClicked(UserModel userModel) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("receiverUser", userModel);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        if (postModels == null){
            return 0;
        } else {
            return postModels.size();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LayoutPostBinding binding;

        public ViewHolder(@NonNull LayoutPostBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
