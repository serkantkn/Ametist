package com.serkantken.ametist.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.ChatListAdapter;
import com.serkantken.ametist.adapters.MainAdapter;
import com.serkantken.ametist.databinding.ActivityMainBinding;
import com.serkantken.ametist.databinding.LayoutMessageListBinding;
import com.serkantken.ametist.databinding.LayoutProfileBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class MainActivity extends BaseActivity implements UserListener
{
    private ActivityMainBinding binding;
    private MainAdapter mainAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private Balloon balloon;
    private Utilities utilities;
    private UserModel user;
    private ArrayList<MessageModel> messageList = new ArrayList<>();
    private ChatListAdapter adapter;
    private BottomSheetDialog bottomSheetDialog;
    private LayoutMessageListBinding bottomSheetView;

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
        database = FirebaseFirestore.getInstance();
        user = new UserModel();
        getUserInfo();
        setTabs();

        utilities.blur(binding.tabBarBlur, 10f, false);
        utilities.blur(binding.toolbarBlur, 10f, false);

        mainAdapter = new MainAdapter(getSupportFragmentManager(), this, binding.tabLayout.getTabCount());
        binding.viewPager.setAdapter(mainAdapter);
        binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));

        String balloons_showed = utilities.getPreferences(Constants.IS_BALLOONS_SHOWED);
        if (TextUtils.equals(balloons_showed, Constants.PREF_NO))
        {
            new Handler().postDelayed(() -> {
                showBalloon(getString(R.string.your_profile_here), binding.profileImage, 3);
                utilities.setPreferences(Constants.IS_BALLOONS_SHOWED, Constants.PREF_YES);
            }, 2000);
        }

        binding.buttonSettings.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        binding.profileImage.setOnClickListener(view -> {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme_Chat);
            LayoutProfileBinding bottomSheetView = LayoutProfileBinding.inflate(getLayoutInflater());

            utilities.blur((BlurView) bottomSheetView.bottomSheetContainer, 10f, false);
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

        binding.buttonMessages.setOnClickListener(view -> {
            showMessageListDialog();
        });
    }

    private void setTabs()
    {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getResources().getString(R.string.homepage)));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(""));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(""));
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.viewPager));
        int[] tabicons = {R.drawable.ic_home, R.drawable.ic_group, R.drawable.ic_notifications};
        binding.tabLayout.getTabAt(1).setIcon(tabicons[1]);
        binding.tabLayout.getTabAt(2).setIcon(tabicons[2]);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                {
                    tab.setText(getResources().getString(R.string.homepage));
                    tab.setIcon(null);
                    tab.view.performClick();
                }
                else if (tab.getPosition() == 1)
                {
                    tab.setText(getResources().getString(R.string.discover));
                    tab.setIcon(null);
                    tab.view.performClick();
                }
                else if (tab.getPosition() == 2)
                {
                    //tab.setText(getResources().getString(R.string.notifications));
                    //tab.setIcon(null);
                    tab.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_notifications));

                    tab.view.performClick();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                {
                    tab.setText("");
                    tab.setIcon(tabicons[0]);
                }
                else if (tab.getPosition() == 1)
                {
                    tab.setText("");
                    tab.setIcon(tabicons[1]);
                }
                else if (tab.getPosition() == 2)
                {
                    tab.setText("");
                    tab.setIcon(tabicons[2]);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void showMessageListDialog()
    {
        bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme_Chat);
        bottomSheetView = LayoutMessageListBinding.inflate(getLayoutInflater());

        utilities.blur(bottomSheetView.blur, 10f, false);
        adapter = new ChatListAdapter(messageList, MainActivity.this, MainActivity.this, this);
        bottomSheetView.rvMessageList.setAdapter(adapter);
        bottomSheetView.rvMessageList.setLayoutManager(new LinearLayoutManager(this));
        listenConversations();
        bottomSheetView.imgRefresh.setOnClickListener(view -> listenConversations());

        bottomSheetDialog.setContentView(bottomSheetView.getRoot());
        bottomSheetDialog.show();
    }

    private void listenConversations()
    {
        bottomSheetView.progressbar.setVisibility(View.VISIBLE);
        messageList.clear();
        database.collection("conversations")
                .whereEqualTo("senderId", auth.getUid())
                .addSnapshotListener(eventListener);
        database.collection("conversations")
                .whereEqualTo("receiverId", auth.getUid())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null)
        {
            return;
        }
        if (value != null)
        {
            for (DocumentChange documentChange : value.getDocumentChanges())
            {
                if (documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    MessageModel model = new MessageModel();
                    model.setSenderId(documentChange.getDocument().getString("senderId"));
                    model.setReceiverId(documentChange.getDocument().getString("receiverId"));

                    if (Objects.equals(auth.getUid(), documentChange.getDocument().getString("senderId")))
                    {
                        model.setConversationId(documentChange.getDocument().getString("receiverId"));
                    }
                    else
                    {
                        model.setConversationId(documentChange.getDocument().getString("senderId"));
                    }

                    model.setMessage(documentChange.getDocument().getString("message"));
                    model.setTimestamp(documentChange.getDocument().getLong("timestamp"));
                    messageList.add(model);
                }
                else if (documentChange.getType() == DocumentChange.Type.MODIFIED)
                {
                    for (int i = 0; i < messageList.size(); i++)
                    {
                        String senderId = documentChange.getDocument().getString("senderId");
                        String receiverId = documentChange.getDocument().getString("receiverId");
                        if (messageList.get(i).getSenderId().equals(senderId) && messageList.get(i).getReceiverId().equals(receiverId))
                        {
                            messageList.get(i).setMessage(documentChange.getDocument().getString("message"));
                            messageList.get(i).setTimestamp(documentChange.getDocument().getLong("timestamp"));
                            break;
                        }
                    }
                }
            }
            messageList.sort((obj1, obj2) -> obj2.getTimestamp().compareTo(obj1.getTimestamp()));
            adapter.notifyDataSetChanged();
            bottomSheetView.rvMessageList.smoothScrollToPosition(0);
            bottomSheetView.progressbar.setVisibility(View.GONE);
        }
    });

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
        balloon = new Balloon.Builder(getApplicationContext())
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

    @Override
    public void onUserClicked(UserModel userModel) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("receiverUser", userModel);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        bottomSheetDialog.dismiss();
    }
}