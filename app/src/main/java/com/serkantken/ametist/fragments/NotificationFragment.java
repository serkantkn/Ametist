package com.serkantken.ametist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.adapters.NotificationsAdapter;
import com.serkantken.ametist.databinding.FragmentNotificationBinding;
import com.serkantken.ametist.databinding.LayoutProfileBinding;
import com.serkantken.ametist.models.NotificationModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Comparator;

import eightbitlab.com.blurview.BlurView;

public class NotificationFragment extends Fragment implements UserListener
{
    ArrayList<NotificationModel> notifList;
    FragmentNotificationBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore database;
    NotificationsAdapter adapter;
    NotificationModel notificationModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        notifList = new ArrayList<>();
        adapter = new NotificationsAdapter(requireContext(), notifList, this);
        binding.notifRV.setAdapter(adapter);
        binding.notifRV.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.notifRefresher.setOnRefreshListener(this::getNotifications);

        getNotifications();

        return binding.getRoot();
    }

    private void getNotifications()
    {
        binding.notifRefresher.setRefreshing(true);
        database.collection("Users")
                .document(auth.getUid())
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
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme_Chat);
        LayoutProfileBinding bottomSheetView = LayoutProfileBinding.inflate(getLayoutInflater());

        Utilities utilities = new Utilities(requireContext(), requireActivity());
        utilities.blur((BlurView) bottomSheetView.bottomSheetContainer, 10f, false);
        bottomSheetView.username.setText(userModel.getName());
        Glide.with(this).load(userModel.getProfilePic()).placeholder(R.drawable.ic_person).into(bottomSheetView.profileImage);
        bottomSheetView.textAbout.setText(userModel.getAbout());
        bottomSheetView.textAge.setText(userModel.getAge());
        bottomSheetView.followCount.setText(""+userModel.getFollowingCount());
        bottomSheetView.followerCount.setText(""+userModel.getFollowerCount());
        switch (userModel.getGender()) {
            case "0":
                bottomSheetView.textGender.setText("-");
                break;
            case "1":
                bottomSheetView.textGender.setText(getString(R.string.man));
                break;
            case "2":
                bottomSheetView.textGender.setText(getString(R.string.woman));
                break;
        }

        bottomSheetView.buttonMore.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra("receiverUser", userModel);
            requireContext().startActivity(intent);
            bottomSheetDialog.dismiss();
        });
        bottomSheetView.buttonMessage.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("receiverUser", userModel);
            requireContext().startActivity(intent);
            bottomSheetDialog.dismiss();
        });
        bottomSheetView.buttonClose.setOnClickListener(view1 -> bottomSheetDialog.dismiss());
        bottomSheetView.buttonEdit.setVisibility(View.GONE);

        bottomSheetDialog.setContentView(bottomSheetView.getRoot());
        bottomSheetDialog.show();

    }
}