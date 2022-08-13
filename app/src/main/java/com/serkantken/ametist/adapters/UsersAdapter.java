package com.serkantken.ametist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.databinding.UserListLayoutBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.network.ApiClient;
import com.serkantken.ametist.network.ApiService;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    ArrayList<UserModel> list;
    Context context;
    Activity activity;
    UserListener listener;
    FirebaseFirestore database;

    public UsersAdapter(ArrayList<UserModel> list, Context context, Activity activity, UserListener listener) {
        this.list = list;
        this.context = context;
        this.activity = activity;
        this.listener = listener;
        database = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(UserListLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        database.collection("Users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("followings")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot snapshot : task.getResult())
                        {
                            if (snapshot.getId().equals(list.get(holder.getAdapterPosition()).getUserId()))
                            {
                                holder.binding.textFollow.setText(context.getString(R.string.followed));
                                holder.binding.buttonFollowBackground.setBackground(context.getDrawable(R.drawable.purple_gradient));
                            }
                        }
                    }
                });
        Glide.with(context).load(list.get(position).getProfilePic()).placeholder(R.drawable.ic_person).into(holder.binding.profileImage);
        holder.binding.username.setText(list.get(position).getName());
        holder.binding.getRoot().setOnClickListener(view -> listener.onUserClicked(list.get(position)));
        holder.binding.buttonFollow.setOnClickListener(view -> {
            if (holder.binding.textFollow.getText().equals(context.getString(R.string.followed)))
            {
                database.collection("Users")
                        .document(list.get(position).getUserId())
                        .collection("followers")
                        .document(FirebaseAuth.getInstance().getUid())
                        .delete().addOnCompleteListener(task -> {
                            database.collection("Users")
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .collection("followings")
                                    .document(list.get(position).getUserId())
                                    .delete().addOnCompleteListener(task12 -> {
                                        holder.binding.textFollow.setText(context.getString(R.string.follow));
                                        holder.binding.buttonFollowBackground.setBackground(context.getDrawable(R.drawable.blue_gradient));
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

                database.collection("Users")
                        .document(list.get(position).getUserId())
                        .collection("followers")
                        .document(FirebaseAuth.getInstance().getUid())
                        .set(followMap).addOnCompleteListener(task -> {
                            //Bildirim gönder
                            database.collection("Users")
                                    .document(list.get(position).getUserId())
                                    .collection("notifications")
                                    .add(followMap).addOnCompleteListener(task13 -> {
                                        Utilities utilities = new Utilities(context, activity);
                                        try {
                                            JSONArray tokens = new JSONArray();
                                            tokens.put(list.get(position).getToken());

                                            JSONObject data = new JSONObject();
                                            data.put("userId", FirebaseAuth.getInstance().getUid());
                                            data.put("username", activity.getString(R.string.new_follower));
                                            data.put("token", utilities.getPreferences("token"));
                                            data.put("messageType", "2");
                                            data.put("message", utilities.getPreferences("username"));

                                            JSONObject body = new JSONObject();
                                            body.put(Constants.REMOTE_MSG_DATA, data);
                                            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                                            sendNotification(body.toString());
                                        } catch (Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        //Kendi listene ekle
                                        HashMap<String, Object> followMap2 = new HashMap<>();
                                        followMap2.put("followedTo", list.get(position).getUserId());
                                        followMap2.put("followedAt", new Date().getTime());

                                        database.collection("Users")
                                                .document(FirebaseAuth.getInstance().getUid())
                                                .collection("followings")
                                                .document(list.get(position).getUserId())
                                                .set(followMap2).addOnCompleteListener(task1 -> {
                                                    //Butonu düzenle
                                                    holder.binding.textFollow.setText(context.getString(R.string.followed));
                                                    holder.binding.buttonFollowBackground.setBackground(context.getDrawable(R.drawable.purple_gradient));
                                                });
                                    });
                        });
            }
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
                                Toast.makeText(context, error.getString("error"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "Bildirim gönderildi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Hata: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        UserListLayoutBinding binding;

        public ViewHolder(UserListLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
