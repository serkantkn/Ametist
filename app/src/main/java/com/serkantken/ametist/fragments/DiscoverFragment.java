package com.serkantken.ametist.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.adapters.UsersAdapter;
import com.serkantken.ametist.databinding.FragmentDiscoverBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class DiscoverFragment extends Fragment implements UserListener
{
    private FragmentDiscoverBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private Utilities utilities;
    private ArrayList<UserModel> userList;
    private UsersAdapter adapter;
    private UserModel userModel;
    private Boolean isPressed = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        utilities = new Utilities(requireContext(), requireActivity());
        Hawk.init(requireContext()).build();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        int spanCount = 3;
        if (deviceWidth >= utilities.convertDpToPixel(500) && deviceWidth < utilities.convertDpToPixel(550)) {
            spanCount = 4;
        }
        if (deviceWidth >= utilities.convertDpToPixel(550) && deviceWidth < utilities.convertDpToPixel(600)) {
            spanCount = 5;
        }
        if (deviceWidth >= utilities.convertDpToPixel(600) && deviceWidth < utilities.convertDpToPixel(650)) {
            spanCount = 6;
        }
        if (deviceWidth >= utilities.convertDpToPixel(650) && deviceWidth < utilities.convertDpToPixel(700)) {
            spanCount = 7;
        }
        if (deviceWidth >= utilities.convertDpToPixel(700)) {
            spanCount = 8;
        }

        adapter = new UsersAdapter(userList, getContext(), utilities, this);
        binding.discoverRV.setAdapter(adapter);
        binding.discoverRV.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));

        try {
            Field f = binding.discoverRefresher.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView imageView = (ImageView) f.get(binding.discoverRefresher);
            assert imageView != null;
            imageView.setAlpha(0.0f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        binding.discoverRefresher.setOnRefreshListener(this::getUsers);

        CoordinatorLayout.LayoutParams searchBlurParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchBlurParams.setMargins(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(64), 0, 0);
        searchBlurParams.gravity = Gravity.TOP;
        binding.searchBlur.setLayoutParams(searchBlurParams);
        utilities.blur(new BlurView[]{binding.searchBlur}, 10f, false);

        binding.discoverRV.setPadding(utilities.convertDpToPixel(10), utilities.getStatusBarHeight()+utilities.convertDpToPixel(112), utilities.convertDpToPixel(10), utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(66));
        binding.discoverRV.setClipToPadding(false);

        getUsers();

        binding.etSearch.setOnTouchListener((v, event) -> {
            if (Objects.equals(binding.etSearch.getText().toString(), ""))
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        animateEditText(true, v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isPressed)
                        {
                            animateEditText(false, v);
                        }
                        isPressed = false;
                        showKeyboard(binding.etSearch);
                        return true;
                }
            }
            return false;
        });

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

    @SuppressLint("NotifyDataSetChanged")
    private void getUsers() {
        database.collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    if (Objects.equals(auth.getUid(), queryDocumentSnapshot.getId())) {
                        continue;
                    }
                    userModel = new UserModel();
                    userModel.setUserId(queryDocumentSnapshot.getId());
                    userModel.setName(queryDocumentSnapshot.getString("name"));
                    userModel.setProfilePic(queryDocumentSnapshot.getString("profilePic"));
                    userModel.setGender(queryDocumentSnapshot.getString("gender"));
                    if (!Objects.isNull(Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("latitude")).toString())))
                    {
                        userModel.setLatitude(Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("latitude")).toString()));
                        if (!Objects.isNull(Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("longitude")).toString())))
                        {
                            userModel.setLongitude(Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("longitude")).toString()));
                            if (Hawk.contains("latitude") && Hawk.contains("longitude"))
                            {
                                double usersLatitude = Hawk.get("latitude");
                                double usersLongitude = Hawk.get("longitude");

                                userModel.setDistance(haversine(usersLatitude, usersLongitude,
                                        Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("latitude")).toString()),
                                        Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("longitude")).toString())));
                            }
                        }
                    }
                    else
                    {
                        userModel.setLatitude(Constants.EXAMPLE_LATITUDE);
                        userModel.setLongitude(Constants.EXAMPLE_LONGITUDE);

                        userModel.setDistance(haversine(Constants.EXAMPLE_LATITUDE, Constants.EXAMPLE_LONGITUDE,
                                Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("latitude")).toString()),
                                Double.parseDouble(Objects.requireNonNull(queryDocumentSnapshot.get("longitude")).toString())));
                    }
                    userList.add(userModel);
                }
                userList.sort(Comparator.comparing(UserModel::getDistance));
                adapter.notifyDataSetChanged();
            }
        });
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2)
    {
        double R = 6371; // Dünya yarıçapı (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchUser(String s)
    {
        database.collection(Constants.DATABASE_PATH_USERS).orderBy("name").startAt(s).endAt(s + "\uf8ff").get().addOnCompleteListener(task -> {
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

    private void animateEditText(boolean isPressed, View view) {
        if (isPressed)
        {
            Animation anim = AnimationUtils.loadAnimation(requireContext(), R.anim.scale);
            anim.setFillAfter(true);
            view.startAnimation(anim);
        }
        else
        {
            Animation anim = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_reverse);
            view.startAnimation(anim);
        }
    }

    private void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onUserClicked(UserModel userModel)
    {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra("receiverUser", userModel);
        requireContext().startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getUsers();
    }
}