package com.serkantken.ametist.activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivityIntroBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Utilities;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity
{
    private ActivityIntroBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    Timer timer;
    private UserModel user;
    boolean connected = false;
    AlertDialog.Builder alertDialog;
    Utilities utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        auth = FirebaseAuth.getInstance();
        utilities = new Utilities(this, this);

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        if (auth.getCurrentUser() == null)
        {
            if (connected)
            {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startActivity(new Intent(IntroActivity.this, OnboardingActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                }, 2000);
            }
            else
            {
                showError();
            }
        }
        else
        {
            if (connected)
            {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getUserInfoAndPosts();
                    }
                }, 1000);
            }
            else
            {
                showError();
            }
        }
    }

    private void showError()
    {
        alertDialog = new AlertDialog.Builder(IntroActivity.this);
        alertDialog.setMessage(getString(R.string.error_at_login));
        alertDialog.setNeutralButton(getString(R.string.ok), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            finish();
        });
        alertDialog.create();
        alertDialog.show();
    }

    private void getUserInfoAndPosts()
    {
        binding.progressbar.setVisibility(View.VISIBLE);
        database = FirebaseFirestore.getInstance();
        database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    if (documentSnapshot.getId().equals(auth.getUid()))
                    {
                        user = new UserModel();
                        user.setName(documentSnapshot.getString("name"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setGender(documentSnapshot.getString("gender"));
                        user.setAge(documentSnapshot.getString("age"));
                        user.setUserId(documentSnapshot.getId());
                        user.setFollowerCount(Integer.parseInt(String.valueOf(documentSnapshot.get("followerCount"))));
                        user.setFollowingCount(Integer.parseInt(String.valueOf(documentSnapshot.get("followingCount"))));
                        utilities.setPreferences("username", documentSnapshot.getString("name"));
                    }
                }
                getToken();

                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                intent.putExtra("currentUserInfo", user);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token)
    {
        utilities.setPreferences("token", token);
        DocumentReference documentReference = database.collection("Users").document(Objects.requireNonNull(auth.getUid()));
        documentReference.update("token", token).addOnFailureListener(e -> Toast.makeText(this, "Token güncellenemedi", Toast.LENGTH_SHORT).show());
    }
}