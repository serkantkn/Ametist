package com.serkantken.ametist.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.WindowCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivitySignupBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;

public class SignupActivity extends AppCompatActivity {
    ActivitySignupBinding binding;
    ArrayList<String> ageList;
    ArrayAdapter<String> arrayAdapter;
    String name, age, email, password, gender;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    Utilities utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(decor.findViewById(R.id.container), true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        makeVisible();

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        utilities = new Utilities(this, this);
        Hawk.init(this).build();

        utilities.blur(new BlurView[]{binding.blurMail, binding.blurPassword, binding.blurName, binding.loadingBlur, binding.alsoLoginCard}, 10f, true);

        binding.backButton.setOnClickListener(v -> onBackPressed());

        binding.inputMail.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
                    getWindow().setStatusBarColor(getColor(android.R.color.black));
                    getWindow().setNavigationBarColor(getColor(android.R.color.black));
                    return true;
                case MotionEvent.ACTION_UP:
                    showKeyboard(binding.inputMail);
                    return true;
                default:
                    return false;
            }
        });

        ageList = new ArrayList<>();
        for (int i = 17; i <= 100; i++)
        {
            if (i == 17)
            {
                ageList.add(getString(R.string.age));
            }
            else
            {
                ageList.add(i + "");
            }
        }
        arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, ageList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAge.setAdapter(arrayAdapter);

        binding.spinnerAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                age = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                age = "";
            }
        });

        binding.buttonSignup.setOnClickListener(view -> {
            if (isInformationCorrect())
            {
                loading(true);
                name = binding.inputName.getText().toString().trim();
                if (binding.radioMale.isChecked())
                {
                    gender = "1";
                }
                else if (binding.radioFemale.isChecked())
                {
                    gender = "2";
                }
                email = binding.inputMail.getText().toString().trim().toLowerCase();
                password = binding.inputPassword.getText().toString();

                signUpWithEmailAndPassword();
            }
        });
    }

    private void signUpWithEmailAndPassword()
    {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                UserModel userModel = new UserModel();
                userModel.setUserId(auth.getUid());
                userModel.setName(name);
                userModel.setAge(age);
                userModel.setGender(gender);
                userModel.setEmail(email);
                userModel.setFollowingCount(0);
                userModel.setFollowerCount(0);
                userModel.setAbout("");
                userModel.setSignupDate(new Date().getTime());
                userModel.setOnline(true);

                database.collection(Constants.DATABASE_PATH_USERS).document(Objects.requireNonNull(auth.getUid())).set(userModel).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful())
                    {
                        loading(false);
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("currentUserInfo", userModel);
                        Hawk.put(Constants.IS_BALLOONS_SHOWED, false);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        loading(false);
                        showToast(getString(R.string.error_at_saving));
                    }
                });
            }
            else
            {
                loading(false);
                showToast(getString(R.string.error_at_creating_user));
            }
        });
    }

    private boolean isInformationCorrect()
    {
        if (binding.inputMail.getText().toString().isEmpty() || !binding.inputMail.getText().toString().contains("@"))
        {
            showError(binding.inputMail, getString(R.string.valid_email));
            return false;
        }
        else if (binding.inputPassword.getText().toString().isEmpty())
        {
            showError(binding.inputPassword, getString(R.string.no_empty_password));
            return false;
        }
        else if (binding.inputPassword.getText().toString().length() < 6)
        {
            showError(binding.inputPassword, getString(R.string.six_char_password));
            return false;
        }
        else if (!binding.radioMale.isChecked() && !binding.radioFemale.isChecked())
        {
            showBalloon(binding.radioGender, "Cinsiyet belirtmek zorunludur", 3);
            return false;
        }
        else
        {
            return true;
        }
    }

    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            binding.inputName.setEnabled(false);
            binding.spinnerAge.setEnabled(false);
            binding.radioMale.setEnabled(false);
            binding.radioFemale.setEnabled(false);
            binding.inputMail.setEnabled(false);
            binding.inputPassword.setEnabled(false);
            binding.buttonSignup.setVisibility(View.GONE);
            binding.progressbar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.inputName.setEnabled(true);
            binding.spinnerAge.setEnabled(true);
            binding.radioMale.setEnabled(true);
            binding.radioFemale.setEnabled(true);
            binding.inputMail.setEnabled(true);
            binding.inputPassword.setEnabled(true);
            binding.buttonSignup.setVisibility(View.VISIBLE);
            binding.progressbar.setVisibility(View.GONE);
        }
    }

    private void showError(EditText input, String message)
    {
        input.setError(message);
        input.requestFocus();
    }

    private void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showBalloon(View view, String message, int position)
    {
        Balloon balloon = new Balloon.Builder(this)
                .setArrowSize(10)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setText(message)
                .setTextSize(15f)
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.accent_blue_dark))
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
                balloon.showAlignEnd(view);
                break;
            case 3:
                balloon.showAlignBottom(view);
                break;
            case 4:
                balloon.showAlignStart(view);
                break;
        }
    }

    private void makeInvisible()
    {
        binding.imgLogo.setVisibility(View.GONE);
        binding.appName.setVisibility(View.GONE);
        binding.scrollView.setVisibility(View.GONE);
    }

    private void makeVisible()
    {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Animation fadein = AnimationUtils.loadAnimation(SignupActivity.this, android.R.anim.fade_in);
            fadein.setDuration(250);
            fadein.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    binding.imgLogo.setVisibility(View.VISIBLE);
                    binding.appName.setVisibility(View.VISIBLE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            binding.imgLogo.startAnimation(fadein);
            binding.appName.startAnimation(fadein);
            binding.scrollView.startAnimation(fadein);
        }, 500);
    }

    private void animateEditText(boolean isPressed, View view) {
        if (isPressed)
        {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale);
            anim.setFillAfter(true);
            view.startAnimation(anim);
        }
        else
        {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale_reverse);
            view.startAnimation(anim);
        }
    }

    private void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeVisible();
    }

    @Override
    public void onBackPressed() {
        makeInvisible();
        super.onBackPressed();
    }
}