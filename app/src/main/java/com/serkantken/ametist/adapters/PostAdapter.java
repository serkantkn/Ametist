package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.activities.CommentActivity;
import com.serkantken.ametist.activities.FullProfilePhotoActivity;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.activities.ProfileEditActivity;
import com.serkantken.ametist.databinding.LayoutNewPostDialogBinding;
import com.serkantken.ametist.databinding.LayoutPostBinding;
import com.serkantken.ametist.databinding.LayoutProfileBinding;
import com.serkantken.ametist.models.CommentModel;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<PostModel> postModels;
    Context context;
    Activity activity;
    FirebaseFirestore database;
    String currentUserId;
    int likeCount;
    ArrayList<CommentModel> comments = new ArrayList<>();
    boolean result;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.postImage.setVisibility(View.GONE);
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
                holder.binding.username.setText(user.getName());
                Glide.with(context).load(user.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person)).into(holder.binding.profileImage);
            }
        });
        if (Objects.equals(postModel.getPostedBy(), currentUserId))
        {
            holder.binding.buttonMenu.setVisibility(View.VISIBLE);
        }

        isLiked(postModel, holder);

        holder.binding.postText.setText(postModel.getPostText());
        holder.binding.textLikeCount.setText(String.valueOf(postModel.getLikeCount()));
        holder.binding.textCommentCount.setText(String.valueOf(postModel.getCommentCount()));
        holder.binding.date.setText(TimeAgo.using(postModel.getPostedAt()));
        holder.binding.profileImage.setOnClickListener(view -> onUserClicked(user));
        holder.binding.username.setOnClickListener(view -> onUserClicked(user));
        if (!Objects.equals(postModel.getPostPicture(), null))
        {
            holder.binding.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(postModel.getPostPicture()).into(holder.binding.postImage);
            holder.binding.postImage.setOnLongClickListener(view -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", postModel.getPostPicture());
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.binding.postImage, "photograph");
                context.startActivity(intent, optionsCompat.toBundle());
                return true;
            });
        }

        holder.binding.buttonMenu.setOnClickListener(view -> {
            View popupview = View.inflate(context, R.layout.popup_layout, null);
            ConstraintLayout buttonDelete = popupview.findViewById(R.id.post_delete);
            ConstraintLayout buttonEdit = popupview.findViewById(R.id.post_edit);

            int width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            PopupWindow popupWindow = new PopupWindow(popupview, width, height, false);

            popupWindow.showAsDropDown(holder.binding.buttonMenu);
            popupWindow.setElevation(20f);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);

            buttonDelete.setOnClickListener(view1 -> postDelete(postModel.getPostId(), popupWindow));
            buttonEdit.setOnClickListener(view1 -> postEdit(postModel.getPostId(), position, popupWindow));
        });

        holder.binding.buttonLike.setOnClickListener(view -> {
            if (holder.binding.buttonLike.getTag().equals("Liked"))
            {
                unlikePost(postModel, holder);
            }
            else
            {
                likePost(postModel, holder);
            }
        });

        holder.binding.buttonComment.setOnClickListener(view -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", postModel.getPostId());
            intent.putExtra("userId", postModel.getPostedBy());
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.binding.cardLayout, "comment");
            context.startActivity(intent, optionsCompat.toBundle());
        });

        //getComment(postModel.getPostedBy(), postModel.getPostId(), holder);
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

    private void postDelete(String postId, PopupWindow window)
    {
        window.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.delete_post_question);
        dialog.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            database.collection("Users").document(currentUserId).collection("Posts").document(postId).delete();
            notifyDataSetChanged();
            dialogInterface.dismiss();
        });
        dialog.setNegativeButton(R.string.no, (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        dialog.create();
        dialog.show();
    }

    private void postEdit(String postId, int position, PopupWindow window)
    {
        window.dismiss();
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
                if (!Objects.equals(model.get("postPicture").toString(), ""))
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

    public void onUserClicked(UserModel userModel) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme_Chat);
        LayoutProfileBinding bottomSheetView = LayoutProfileBinding.inflate(LayoutInflater.from(context));

        Utilities utilities = new Utilities(context, activity);
        utilities.blur(bottomSheetView.bottomSheetContainer, 10f, false);
        bottomSheetView.username.setText(userModel.getName());
        Glide.with(context).load(userModel.getProfilePic()).placeholder(R.drawable.ic_person).into(bottomSheetView.profileImage);
        bottomSheetView.textAbout.setText(userModel.getAbout());
        bottomSheetView.textAge.setText(userModel.getAge());
        switch (userModel.getGender()) {
            case "0":
                bottomSheetView.textGender.setText("-");
                break;
            case "1":
                bottomSheetView.textGender.setText(activity.getString(R.string.man));
                break;
            case "2":
                bottomSheetView.textGender.setText(activity.getString(R.string.woman));
                break;
        }

        bottomSheetView.buttonMore.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("receiverUser", userModel);
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });
        bottomSheetView.buttonMessage.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverUser", userModel);
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });
        bottomSheetView.buttonEdit.setOnClickListener(view -> context.startActivity(new Intent(activity, ProfileEditActivity.class)));
        bottomSheetView.buttonClose.setOnClickListener(view1 -> bottomSheetDialog.dismiss());
        if (Objects.equals(userModel.getUserId(), FirebaseAuth.getInstance().getUid()))
        {
            bottomSheetView.buttonEdit.setVisibility(View.VISIBLE);
            bottomSheetView.buttonMessage.setVisibility(View.GONE);
        }
        else
        {
            bottomSheetView.buttonEdit.setVisibility(View.GONE);
            bottomSheetView.buttonMessage.setVisibility(View.VISIBLE);
        }

        bottomSheetDialog.setContentView(bottomSheetView.getRoot());
        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {
        return postModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LayoutPostBinding binding;

        public ViewHolder(@NonNull LayoutPostBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
