package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.activities.FullProfilePhotoActivity;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.databinding.LayoutPostBinding;
import com.serkantken.ametist.databinding.LayoutProfileBinding;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<PostModel> postModels;
    Context context;
    Activity activity;
    FirebaseFirestore database;

    public PostAdapter(ArrayList<PostModel> postModels, Context context, Activity activity, FirebaseFirestore database) {
        this.postModels = postModels;
        this.context = context;
        this.activity = activity;
        this.database = database;
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
        database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.getId().equals(postModels.get(holder.getAdapterPosition()).getPostedBy())) {
                        user.setUserId(documentSnapshot.getId());
                        user.setName(documentSnapshot.getString("name"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setAge(documentSnapshot.getString("age"));
                        user.setGender(documentSnapshot.getString("gender"));
                    }
                }
                holder.binding.username.setText(user.getName());
                Glide.with(context).load(user.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person)).into((ImageView) holder.binding.profileImage);
            }
        });
        holder.binding.postText.setText(postModels.get(position).getPostText());
        holder.binding.date.setText(TimeAgo.using(postModels.get(position).getPostedAt()));
        holder.binding.profileImage.setOnClickListener(view -> onUserClicked(user));
        holder.binding.username.setOnClickListener(view -> onUserClicked(user));
        if (!Objects.equals(postModels.get(position).getPostPicture(), null))
        {
            holder.binding.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(postModels.get(position).getPostPicture()).into(holder.binding.postImage);
            holder.binding.postImage.setOnClickListener(view -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", postModels.get(position).getPostPicture());
                context.startActivity(intent);
            });
        }
    }

    public void onUserClicked(UserModel userModel) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme_Chat);
        LayoutProfileBinding bottomSheetView = LayoutProfileBinding.inflate(LayoutInflater.from(context));

        Utilities utilities = new Utilities(context, activity);
        utilities.blur((BlurView) bottomSheetView.bottomSheetContainer, 10f, false);
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
        bottomSheetView.buttonClose.setOnClickListener(view1 -> bottomSheetDialog.dismiss());
        bottomSheetView.buttonEdit.setVisibility(View.GONE);

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
