package com.serkantken.ametist.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.NotifListLayoutBinding;
import com.serkantken.ametist.models.NotificationModel;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder>
{
    Context context;
    ArrayList<NotificationModel> notifList;
    //ArrayList<UserModel> userList;
    UserListener userListener;

    public NotificationsAdapter(Context context, ArrayList<NotificationModel> notifList, UserListener listener)
    {
        this.context = context;
        this.notifList = notifList;
        //this.userList = userList;
        this.userListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationsAdapter.ViewHolder(NotifListLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        NotificationModel notification = notifList.get(position);
        UserModel userModel = new UserModel();

        FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    if (Objects.equals(documentSnapshot.getId(), notification.getUserId()))
                    {
                        userModel.setUserId(documentSnapshot.getId());
                        userModel.setName(documentSnapshot.getString("name"));
                        userModel.setProfilePic(documentSnapshot.getString("profilePic"));
                        userModel.setGender(documentSnapshot.getString("gender"));
                        userModel.setAge(documentSnapshot.getString("age"));
                        userModel.setAbout(documentSnapshot.getString("about"));
                        userModel.setFollowingCount(Integer.parseInt(documentSnapshot.get("followingCount").toString()));
                        userModel.setFollowerCount(Integer.parseInt(documentSnapshot.get("followerCount").toString()));
                    }
                }
                Glide.with(context).load(userModel.getProfilePic()).placeholder(R.drawable.ic_person).into(holder.binding.profileImage);
                holder.binding.message.setText(userModel.getName() + context.getResources().getString(R.string.is_now_following_you));
                holder.binding.date.setText(TimeAgo.using(notification.getNotifDate()));
                holder.binding.getRoot().setOnClickListener(view -> {
                    userListener.onUserClicked(userModel);
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return notifList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        NotifListLayoutBinding binding;

        public ViewHolder(@NonNull NotifListLayoutBinding itemView)
        {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
}
