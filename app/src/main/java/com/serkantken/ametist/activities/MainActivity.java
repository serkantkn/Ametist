package com.serkantken.ametist.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.MainAdapter;
import com.serkantken.ametist.databinding.ActivityMainBinding;
import com.serkantken.ametist.databinding.LayoutProfileBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import java.util.Objects;

public class MainActivity extends BaseActivity
{
    private ActivityMainBinding binding;
    private MainAdapter mainAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private Utilities utilities;
    private UserModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        utilities = new Utilities(getApplicationContext(), this);

        binding.toolbarBlur.setPadding(0, utilities.getStatusBarHeight(), 0, 0);
        binding.tabBarBlur.setPadding(0, utilities.convertDpToPixel(5), 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(5));

        auth = FirebaseAuth.getInstance();
        Hawk.init(this).build();
        database = FirebaseFirestore.getInstance();
        user = new UserModel();
        getUserInfo();
        setTabs();

        utilities.blur(binding.tabBarBlur, 10f, false);
        utilities.blur(binding.toolbarBlur, 10f, false);

        if (Hawk.contains(Constants.IS_BALLOONS_SHOWED))
        {
            if (!(Boolean)Hawk.get(Constants.IS_BALLOONS_SHOWED))
            {
                new Handler().postDelayed(() -> {
                    showBalloon(getString(R.string.your_profile_here), binding.profileImage, 3);
                    Hawk.put(Constants.IS_BALLOONS_SHOWED, true);
                }, 2000);
            }
        }
        else
        {
            Hawk.put(Constants.IS_BALLOONS_SHOWED, false);
        }
        binding.buttonSettings.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        binding.profileImage.setOnClickListener(view -> {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme_Chat);
            LayoutProfileBinding bottomSheetView = LayoutProfileBinding.inflate(getLayoutInflater());

            utilities.blur(bottomSheetView.bottomSheetContainer, 10f, false);
            bottomSheetView.username.setText(user.getName());
            Glide.with(this).load(user.getProfilePic()).placeholder(R.drawable.ic_person).into(bottomSheetView.profileImage);
            bottomSheetView.textAbout.setText(user.getAbout());
            bottomSheetView.textAge.setText(user.getAge());
            if (user.getGender().equals("0") || user.getGender().isEmpty())
            {
                bottomSheetView.textGender.setText("-");
            }
            else if (user.getGender().equals("1"))
            {
                bottomSheetView.textGender.setText(getString(R.string.man));
            }
            else if (user.getGender().equals("2"))
            {
                bottomSheetView.textGender.setText(getString(R.string.woman));
            }

            bottomSheetView.buttonMore.setOnClickListener(view1 -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("receiverUser", user);
                startActivity(intent);
                bottomSheetDialog.dismiss();
            });
            bottomSheetView.buttonClose.setOnClickListener(view1 -> bottomSheetDialog.dismiss());
            bottomSheetView.buttonEdit.setVisibility(View.GONE);
            bottomSheetView.buttonMessage.setVisibility(View.GONE);

            bottomSheetDialog.setContentView(bottomSheetView.getRoot());
            bottomSheetDialog.show();
        });
    }

    private void setTabs()
    {
        mainAdapter = new MainAdapter(this, this);
        binding.viewPager.setAdapter(mainAdapter);

        binding.tabLayout.addTab(binding.tabLayout.newTab());
        binding.tabLayout.addTab(binding.tabLayout.newTab());
        binding.tabLayout.addTab(binding.tabLayout.newTab());
        binding.tabLayout.addTab(binding.tabLayout.newTab());
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setIcon(mainAdapter.getTabIcon(position));
        }).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));

                Animation fadeout = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out);
                fadeout.setDuration(500);
                fadeout.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        binding.username.setText(mainAdapter.getPageTitle(position));

                        Animation fadein = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in);
                        fadein.setDuration(500);
                        binding.username.startAnimation(fadein);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                binding.username.startAnimation(fadeout);
            }
        });

        int[] tabicons = {R.drawable.ic_home, R.drawable.ic_group, R.drawable.ic_message, R.drawable.ic_notifications};
        binding.tabLayout.getTabAt(1).setIcon(tabicons[1]);
        binding.tabLayout.getTabAt(2).setIcon(tabicons[2]);
        binding.tabLayout.getTabAt(3).setIcon(tabicons[3]);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_blue_dark), PorterDuff.Mode.SRC_IN);
                    tab.view.performClick();
                }
                else if (tab.getPosition() == 1)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_blue_dark), PorterDuff.Mode.SRC_IN);
                    tab.view.performClick();
                }
                else if (tab.getPosition() == 2)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_blue_dark), PorterDuff.Mode.SRC_IN);
                    tab.view.performClick();
                }
                else if (tab.getPosition() == 3)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_blue_dark), PorterDuff.Mode.SRC_IN);
                    tab.view.performClick();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_purple_light), PorterDuff.Mode.SRC_IN);
                }
                else if (tab.getPosition() == 1)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_purple_light), PorterDuff.Mode.SRC_IN);
                }
                else if (tab.getPosition() == 2)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_purple_light), PorterDuff.Mode.SRC_IN);
                }
                else if (tab.getPosition() == 3)
                {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_purple_light), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getUserInfo()
    {
        /*database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    if (documentSnapshot.getId().equals(auth.getUid()))
                    {
                        user.setName(documentSnapshot.getString("name"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setGender(documentSnapshot.getString("gender"));
                        user.setAge(documentSnapshot.getString("age"));
                        user.setUserId(documentSnapshot.getId());
                        utilities.setPreferences("username", documentSnapshot.getString("name"));
                    }
                }

            }
        });*/
        user = (UserModel) getIntent().getSerializableExtra("currentUserInfo");
        if (user != null)
        {
            Glide.with(getApplicationContext()).load(user.getProfilePic()).placeholder(R.mipmap.ametist_logo).into(binding.profileImage);
        }
    }

    private void showBalloon(String message, View view, int position)
    {
        Balloon balloon = new Balloon.Builder(getApplicationContext())
                .setArrowSize(10)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setText(message)
                .setTextSize(15f)
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white))
                .setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.accent_blue_dark))
                .setArrowPosition(0.5f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setPadding(12)
                .setCornerRadius(8f)
                .setBalloonAnimation(BalloonAnimation.ELASTIC)
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
}