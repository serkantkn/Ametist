package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.CommentItemBinding;
import com.serkantken.ametist.models.CommentModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;

import java.util.ArrayList;
import java.util.Objects;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>
{
    private ArrayList<CommentModel> commentModels;
    private Context context;
    private Activity activity;

    public CommentAdapter(ArrayList<CommentModel> commentModels, Context context, Activity activity)
    {
        this.commentModels = commentModels;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ViewHolder(CommentItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        CommentModel comment = commentModels.get(position);

        holder.binding.commentText.setText(comment.getCommentText());
        holder.binding.date.setText(TimeAgo.using(comment.getCommentedAt()));

        FirebaseFirestore.getInstance().collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                {
                    if (Objects.equals(queryDocumentSnapshot.getId(), comment.getUserId()))
                    {
                        UserModel userModel = new UserModel();
                        userModel.setUserId(queryDocumentSnapshot.getId());
                        userModel.setToken(queryDocumentSnapshot.getString("token"));
                        userModel.setName(queryDocumentSnapshot.getString("name"));
                        userModel.setEmail(queryDocumentSnapshot.getString("email"));
                        userModel.setProfilePic(queryDocumentSnapshot.getString("profilePic"));
                        userModel.setGender(queryDocumentSnapshot.getString("gender"));
                        userModel.setAge(queryDocumentSnapshot.getString("age"));
                        userModel.setAbout(queryDocumentSnapshot.getString("about"));
                        FirebaseFirestore.getInstance().collection("Users").document(queryDocumentSnapshot.getId()).collection("followers").get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful() && !task1.getResult().isEmpty())
                            {
                                ArrayList<String> followers = new ArrayList<>();
                                for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult())
                                {
                                    followers.add(queryDocumentSnapshot1.getId());
                                }
                                userModel.setFollowerCount(followers.size());
                            }
                        });
                        FirebaseFirestore.getInstance().collection("Users").document(queryDocumentSnapshot.getId()).collection("followings").get().addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful() && !task12.getResult().isEmpty())
                            {
                                ArrayList<String> followings = new ArrayList<>();
                                for (QueryDocumentSnapshot queryDocumentSnapshot2 : task12.getResult())
                                {
                                    followings.add(queryDocumentSnapshot2.getId());
                                }
                                userModel.setFollowingCount(followings.size());
                            }
                        });

                        Glide.with(context).load(userModel.getProfilePic()).placeholder(R.drawable.ic_person).into(holder.binding.profileImage);
                        holder.binding.commentUsername.setText(userModel.getName());

                        holder.binding.profileImage.setOnClickListener(v -> {
                        });
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return commentModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        CommentItemBinding binding;

        public ViewHolder(@NonNull CommentItemBinding itemView)
        {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
