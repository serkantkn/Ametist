package com.serkantken.ametist.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.adapters.UsersAdapter;
import com.serkantken.ametist.databinding.FragmentDiscoverBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import java.util.ArrayList;
import java.util.Objects;

public class DiscoverFragment extends Fragment implements UserListener
{
    private FragmentDiscoverBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private Utilities utilities;
    private ArrayList<UserModel> userList;
    private UsersAdapter adapter;
    private Balloon balloon;
    private UserModel userModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        utilities = new Utilities(requireContext(), requireActivity());

        adapter = new UsersAdapter(userList, getContext(), getActivity(), this);
        binding.discoverRV.setAdapter(adapter);
        binding.discoverRV.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        binding.discoverRefresher.setOnRefreshListener(this::getUsers);

        CoordinatorLayout.LayoutParams searchBlurParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchBlurParams.setMargins(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(64), 0, 0);
        searchBlurParams.gravity = Gravity.TOP;
        binding.searchBlur.setLayoutParams(searchBlurParams);
        utilities.blur(binding.searchBlur, 10f, false);

        binding.discoverRV.setPadding(utilities.convertDpToPixel(10), utilities.getStatusBarHeight()+utilities.convertDpToPixel(112), utilities.convertDpToPixel(10), utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(66));
        binding.discoverRV.setClipToPadding(false);

        getUsers();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return binding.getRoot();
    }

    private void getUsers() {
        binding.discoverRefresher.setRefreshing(true);
        database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    if (Objects.equals(auth.getUid(), queryDocumentSnapshot.getId())) {
                        continue;
                    }
                    userModel = new UserModel();
                    userModel.setUserId(queryDocumentSnapshot.getId());
                    userModel.setToken(queryDocumentSnapshot.getString("token"));
                    userModel.setName(queryDocumentSnapshot.getString("name"));
                    userModel.setEmail(queryDocumentSnapshot.getString("email"));
                    userModel.setProfilePic(queryDocumentSnapshot.getString("profilePic"));
                    userModel.setGender(queryDocumentSnapshot.getString("gender"));
                    userModel.setAge(queryDocumentSnapshot.getString("age"));
                    userModel.setAbout(queryDocumentSnapshot.getString("about"));
                    database.collection("Users").document(queryDocumentSnapshot.getId()).collection("followers").get().addOnCompleteListener(task1 -> {
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
                    database.collection("Users").document(queryDocumentSnapshot.getId()).collection("followings").get().addOnCompleteListener(task12 -> {
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
                    userList.add(userModel);
                }
                adapter.notifyDataSetChanged();
                binding.discoverRefresher.setRefreshing(false);
            }
        });
    }

    private void searchUser(String s)
    {
        database.collection("Users").orderBy("name").startAt(s).endAt(s + "\uf8ff").get().addOnCompleteListener(task -> {
            userList.clear();
            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
            {
                UserModel userModel = new UserModel();
                userModel.setUserId(queryDocumentSnapshot.getId());
                userModel.setName(queryDocumentSnapshot.getString("name"));
                userModel.setEmail(queryDocumentSnapshot.getString("email"));
                userModel.setProfilePic(queryDocumentSnapshot.getString("profilePic"));
                userModel.setGender(queryDocumentSnapshot.getString("gender"));
                userModel.setAge(queryDocumentSnapshot.getString("age"));
                userModel.setAbout(queryDocumentSnapshot.getString("about"));
                userList.add(userModel);
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onUserClicked(UserModel userModel)
    {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra("receiverUser", userModel);
        requireContext().startActivity(intent);

        /*final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme_Chat);
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
        bottomSheetDialog.show();*/
    }
}