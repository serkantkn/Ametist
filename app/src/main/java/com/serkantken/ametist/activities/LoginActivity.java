package com.serkantken.ametist.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.WindowCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivityLoginBinding;
import com.serkantken.ametist.databinding.LayoutPasswordResetDialogBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private Utilities utilities;
    private LayoutPasswordResetDialogBinding dialogBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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
        String language = getResources().getConfiguration().getLocales().get(0).getLanguage();
        auth.setLanguageCode(language);
        utilities = new Utilities(this, this);

        utilities.blur(new BlurView[]{binding.alsoLoginCard, binding.blurInputMail, binding.blurInputPassword, binding.loadingBlur}, 10f, true);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, gso);

        binding.buttonSigninWithGoogle.setOnClickListener(v -> {
            loading(true);
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, 401);
        });

        binding.buttonSignup.setOnClickListener(v -> makeInvisible());
        binding.forgotPassword.setOnClickListener(v -> showPasswordResetDialog());

        binding.buttonLogin.setOnClickListener(view -> {
            if (isInformationCorrect())
            {
                loginWithEmailAndPassword();
            }
        });
    }

    private void showPasswordResetDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme);
        dialogBinding = LayoutPasswordResetDialogBinding.inflate(getLayoutInflater());
        utilities.blur(new BlurView[]{dialogBinding.dialogBlur}, 10f, true);
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialogBinding.buttonSubmitBackground.setBackgroundColor(getColor(R.color.secondary_text));
        dialogBinding.buttonSubmit.setEnabled(false);

        dialogBinding.email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0 || !editable.toString().contains("@"))
                {
                    dialogBinding.buttonSubmitBackground.setBackgroundColor(getColor(R.color.secondary_text));
                    dialogBinding.buttonSubmit.setEnabled(false);
                }
                else
                {
                    dialogBinding.buttonSubmitBackground.setBackground(AppCompatResources.getDrawable(LoginActivity.this, R.drawable.background_post_footer_buttons));
                    dialogBinding.buttonSubmit.setEnabled(true);
                }
            }
        });

        dialogBinding.buttonSubmit.setOnClickListener(v -> validateEmail(dialogBinding.email.getText().toString().trim().toLowerCase()));

        dialogBinding.buttonDone.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void validateEmail(String email)
    {
        dialogBinding.textPasswordReset.setVisibility(View.GONE);
        dialogBinding.email.setVisibility(View.GONE);
        dialogBinding.buttonSubmit.setVisibility(View.GONE);
        dialogBinding.progressBar.setVisibility(View.VISIBLE);
        dialogBinding.loadingText.setVisibility(View.VISIBLE);

        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                SignInMethodQueryResult result = task.getResult();
                List<String> signInMethods = result.getSignInMethods();
                assert signInMethods != null;
                if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                {
                    resetPassword(email);
                }
                else if (signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD))
                {
                    dialogBinding.messageText.setText("Girilen email adresi Google ile giriş yapma yöntemine kayıtlıdır. Google hesabınızı kullanarak giriş yapabilirsiniz.");
                    dialogBinding.loadingText.setVisibility(View.GONE);
                    dialogBinding.progressBar.setVisibility(View.GONE);
                    dialogBinding.textPasswordReset.setVisibility(View.VISIBLE);
                    dialogBinding.messageText.setVisibility(View.VISIBLE);
                    dialogBinding.buttonDone.setVisibility(View.VISIBLE);
                }
                else
                {
                    dialogBinding.textPasswordReset.setVisibility(View.VISIBLE);
                    dialogBinding.email.setVisibility(View.VISIBLE);
                    dialogBinding.buttonSubmit.setVisibility(View.VISIBLE);
                    dialogBinding.progressBar.setVisibility(View.GONE);
                    dialogBinding.loadingText.setVisibility(View.GONE);
                    showError(dialogBinding.email, "Girilen Email adresine kayıtlı bir hesap bulunamadı");
                }
            }
            else
            {
                dialogBinding.textPasswordReset.setVisibility(View.VISIBLE);
                dialogBinding.email.setVisibility(View.VISIBLE);
                dialogBinding.buttonSubmit.setVisibility(View.VISIBLE);
                dialogBinding.progressBar.setVisibility(View.GONE);
                dialogBinding.loadingText.setVisibility(View.GONE);
                Toast.makeText(this, "Bir hata oluştu. İnternet bağlantınızı kontrol edin ve daha sonra tekrar deneyin.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String email)
    {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                dialogBinding.messageText.setText("Parolanızı sıfırlamak için yazdığınız email adresinize bir link gönderdik. Gelen kutunuzu kontrol ediniz.");
            }
            else
            {
                dialogBinding.messageText.setText("Parola sıfırlama linki gönderilirken sorun oluştu. Daha sonra tekrar deneyin.");
            }
            dialogBinding.loadingText.setVisibility(View.GONE);
            dialogBinding.progressBar.setVisibility(View.GONE);
            dialogBinding.textPasswordReset.setVisibility(View.VISIBLE);
            dialogBinding.messageText.setVisibility(View.VISIBLE);
            dialogBinding.buttonDone.setVisibility(View.VISIBLE);
        });
    }

    private void loginWithEmailAndPassword()
    {
        loading(true);
        String email = binding.inputMail.getText().toString().toLowerCase();
        String password = binding.inputPassword.getText().toString();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                loginSuccessful();
            }
            else
            {
                loading(false);
                showToast(getString(R.string.error_at_login));
            }
        }).addOnFailureListener(e -> {
            loading(false);
            showToast(getString(R.string.error_at_login)+ e);
        });
    }

    private void loginSuccessful()
    {
        FirebaseFirestore.getInstance().collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful())
            {
                UserModel user = new UserModel();
                for (QueryDocumentSnapshot documentSnapshot : task1.getResult())
                {
                    if (documentSnapshot.getId().equals(auth.getUid()))
                    {
                        user.setName(documentSnapshot.getString("name"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setGender(documentSnapshot.getString("gender"));
                        user.setAge(documentSnapshot.getString("age"));
                        user.setUserId(documentSnapshot.getId());
                        Hawk.put("username", documentSnapshot.getString("name"));
                    }
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("currentUserInfo", user);
                startActivity(intent);
                finish();
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
        else
        {
            return true;
        }
    }

    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            binding.inputMail.setVisibility(View.GONE);
            binding.inputPassword.setVisibility(View.GONE);
            binding.buttonLogin.setVisibility(View.GONE);
            binding.forgotPassword.setVisibility(View.GONE);
            binding.buttonSignup.setVisibility(View.GONE);
            binding.alsoLoginCard.setVisibility(View.GONE);
            binding.loadingBlur.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.inputMail.setVisibility(View.VISIBLE);
            binding.inputPassword.setVisibility(View.VISIBLE);
            binding.buttonLogin.setVisibility(View.VISIBLE);
            binding.forgotPassword.setVisibility(View.VISIBLE);
            binding.buttonSignup.setVisibility(View.VISIBLE);
            binding.alsoLoginCard.setVisibility(View.VISIBLE);
            binding.loadingBlur.setVisibility(View.GONE);
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

    private void makeInvisible()
    {
        Animation fadeout = AnimationUtils.loadAnimation(LoginActivity.this, android.R.anim.fade_out);
        fadeout.setDuration(250);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                binding.imgLogo.setVisibility(View.GONE);
                binding.appName.setVisibility(View.GONE);
                binding.scrollView.setVisibility(View.GONE);
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                Pair[] pairedViews = new Pair[5];
                pairedViews[0] = new Pair(binding.circle1, "circle1");
                pairedViews[1] = new Pair(binding.circle2, "circle2");
                pairedViews[2] = new Pair(binding.circle3, "circle3");
                pairedViews[3] = new Pair(binding.circle4, "circle4");
                pairedViews[4] = new Pair(binding.circle5, "circle5");
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(LoginActivity.this, pairedViews);
                startActivity(intent, optionsCompat.toBundle());
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        binding.imgLogo.startAnimation(fadeout);
        binding.appName.startAnimation(fadeout);
        binding.scrollView.startAnimation(fadeout);
    }

    private void makeVisible()
    {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Animation fadein = AnimationUtils.loadAnimation(LoginActivity.this, android.R.anim.fade_in);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 401)
        {
            if (resultCode == RESULT_OK)
            {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    auth.signInWithCredential(credential).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful())
                        {
                            FirebaseFirestore.getInstance().collection(Constants.DATABASE_PATH_USERS).document(Objects.requireNonNull(auth.getUid())).get().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful())
                                {
                                    DocumentSnapshot documentSnapshot = task2.getResult();
                                    if (documentSnapshot.exists())
                                    {
                                        UserModel user = documentSnapshot.toObject(UserModel.class);
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("currentUserInfo", user);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else
                                    {
                                        UserModel user = new UserModel();
                                        user.setUserId(auth.getUid());
                                        user.setName(account.getDisplayName());
                                        user.setEmail(account.getEmail());
                                        user.setProfilePic(Objects.requireNonNull(account.getPhotoUrl()).toString());
                                        user.setSignupDate(new Date().getTime());
                                        user.setAge("-");
                                        user.setGender("");
                                        user.setFollowingCount(0);
                                        user.setFollowerCount(0);
                                        user.setAbout("");
                                        user.setOnline(true);

                                        FirebaseFirestore.getInstance().collection(Constants.DATABASE_PATH_USERS).document(Objects.requireNonNull(auth.getUid())).set(user).addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful())
                                            {
                                                loading(false);
                                                Intent intent = new Intent(this, MainActivity.class);
                                                intent.putExtra("currentUserInfo", user);
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
                                }
                            });
                        }
                    });
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                loading(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeVisible();
    }
}