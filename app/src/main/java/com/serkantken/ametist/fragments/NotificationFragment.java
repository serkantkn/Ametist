package com.serkantken.ametist.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.adapters.NotificationsAdapter;
import com.serkantken.ametist.databinding.FragmentNotificationBinding;
import com.serkantken.ametist.models.NotificationModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class NotificationFragment extends Fragment implements UserListener
{
    ArrayList<NotificationModel> notifList;
    FragmentNotificationBinding binding;
    Utilities utilities;
    FirebaseAuth auth;
    FirebaseFirestore database;
    NotificationsAdapter adapter;
    NotificationModel notificationModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        notifList = new ArrayList<>();
        utilities = new Utilities(requireContext(), requireActivity());
        adapter = new NotificationsAdapter(requireContext(), notifList, this);
        binding.notifRV.setAdapter(adapter);
        binding.notifRV.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.notifRefresher.setOnRefreshListener(this::getNotifications);
        binding.notifRV.setPadding(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(56), 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(66));

        getNotifications();

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getNotifications()
    {
        binding.notifRefresher.setRefreshing(true);
        database.collection("Users")
                .document(Objects.requireNonNull(auth.getUid()))
                .collection("notifications")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        notifList.clear();
                        for (QueryDocumentSnapshot snapshot : task.getResult())
                        {
                            notificationModel = new NotificationModel();
                            notificationModel.setNotifId(snapshot.getId());
                            notificationModel.setUserId(snapshot.getString("followedBy"));
                            notificationModel.setNotifDate(snapshot.getLong("followedAt"));
                            notificationModel.setRead(snapshot.getBoolean("isRead"));
                            notifList.add(notificationModel);
                        }

                        notifList.sort(Comparator.comparing(NotificationModel::getNotifDate).reversed());
                        adapter.notifyDataSetChanged();
                        binding.notifRefresher.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onUserClicked(UserModel userModel) {
        Intent intent = new Intent(requireContext(), ProfileActivity.class);
        intent.putExtra("receiverUser", userModel);
        requireContext().startActivity(intent);
    }
}