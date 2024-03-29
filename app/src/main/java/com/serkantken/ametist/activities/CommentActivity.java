package com.serkantken.ametist.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.transition.Fade;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.CommentAdapter;
import com.serkantken.ametist.adapters.NotificationsAdapter;
import com.serkantken.ametist.databinding.ActivityCommentBinding;
import com.serkantken.ametist.models.CommentModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class CommentActivity extends BaseActivity {
    private ActivityCommentBinding binding;
    private FirebaseFirestore database;
    private ArrayList<CommentModel> comments;
    private CommentAdapter adapter;
    private Utilities utilities;
    private Boolean isPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        utilities = new Utilities(this, this);

        binding.buttonBack.setOnClickListener(view -> onBackPressed());

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(decor.findViewById(R.id.container), true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        database = FirebaseFirestore.getInstance();

        String userId = getIntent().getStringExtra("userId");
        String postId = getIntent().getStringExtra("postId");
        String postImage = getIntent().getStringExtra("postImage");

        if (postImage != null && !postImage.equals("null")) {
            Glide.with(this).load(postImage).into(binding.postImage);
            binding.postImage.setVisibility(View.VISIBLE);
        }

        comments = new ArrayList<>();
        adapter = new CommentAdapter(comments, this, CommentActivity.this);
        binding.commentRV.setAdapter(adapter);
        binding.commentRV.setLayoutManager(new LinearLayoutManager(this));

        getComments(userId, postId);

        binding.inputMessage.setOnTouchListener((v, event) -> {
            if (Objects.equals(binding.inputMessage.getText().toString(), ""))
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        animateEditText(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isPressed)
                        {
                            animateEditText(false);
                        }
                        isPressed = false;
                        showKeyboard(binding.inputMessage);
                        return true;
                }
            }
            return false;
        });

        binding.buttonSend.setOnTouchListener((v, event) -> {
            if (Objects.equals(binding.inputMessage.getText().toString(), ""))
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        animateCard(true, userId, postId);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isPressed)
                        {
                            animateCard(false, userId, postId);
                        }
                        isPressed = false;
                        return true;
                }
            }
            return false;
        });
    }

    private void sendComment(String userId, String postId)
    {
        HashMap<String, Object> comment = new HashMap<>();
        if (Objects.equals(binding.inputMessage.getText().toString(), ""))
        {
            Toast.makeText(this, getString(R.string.messagebox_empty), Toast.LENGTH_SHORT).show();
        }
        else
        {
            comment.put("commentText", binding.inputMessage.getText().toString());
            comment.put("postId", postId);
            comment.put("userId", FirebaseAuth.getInstance().getUid());
            comment.put("date", new Date().getTime());

            //Yorumu ekle
            database.collection(Constants.DATABASE_PATH_USERS)
                    .document(userId)
                    .collection("Posts")
                    .document(postId)
                    .collection("Comments")
                    .add(comment).addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                        {
                            comment.put("commentId", task.getResult().getId());

                            //Yorum IDsini al
                            database.collection(Constants.DATABASE_PATH_USERS)
                                    .document(userId)
                                    .collection("Posts")
                                    .document(postId)
                                    .collection("Comments")
                                    .document(task.getResult().getId())
                                    .update(comment)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful())
                                        {
                                            //Yorumun bulunduğu postun yorum sayısını al
                                            database.collection(Constants.DATABASE_PATH_USERS)
                                                    .document(userId)
                                                    .collection("Posts")
                                                    .get().addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful())
                                                        {
                                                            int commentCount = 0;
                                                            for (QueryDocumentSnapshot snapshot : task2.getResult())
                                                            {
                                                                if (Objects.equals(snapshot.getId(), postId))
                                                                {
                                                                    commentCount = Integer.parseInt(Objects.requireNonNull(snapshot.get("commentCount")).toString());
                                                                }
                                                            }
                                                            //Yorum sayısını bir arttır ve yaz
                                                            database.collection(Constants.DATABASE_PATH_USERS)
                                                                    .document(userId)
                                                                    .collection("Posts")
                                                                    .document(postId)
                                                                    .update("commentCount", commentCount + 1).addOnCompleteListener(task3 -> {
                                                                        if (task3.isSuccessful())
                                                                        {
                                                                            binding.inputMessage.setText("");
                                                                            getComments(userId, postId);
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
        }
    }

    private void getComments(String userId, String postId)
    {
        comments.clear();
        database.collection(Constants.DATABASE_PATH_USERS)
                .document(userId)
                .collection("Posts")
                .document(postId)
                .collection("Comments")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            CommentModel model = new CommentModel();
                            model.setCommentId(snapshot.getId());
                            model.setCommentText(snapshot.getString("commentText"));
                            model.setUserId(snapshot.getString("userId"));
                            model.setPostId(snapshot.getString("postId"));
                            model.setCommentedAt(snapshot.getLong("date"));
                            comments.add(model);
                        }
                        comments.sort(Comparator.comparing(CommentModel::getCommentedAt));
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void animateCard(boolean isPressed, String userId, String postId)
    {
        if (isPressed)
        {
            Animation anim = AnimationUtils.loadAnimation(CommentActivity.this, R.anim.scale);
            anim.setFillAfter(true);
            binding.sendCard.startAnimation(anim);
        }
        else
        {
            Animation anim = AnimationUtils.loadAnimation(CommentActivity.this, R.anim.scale_reverse);
            binding.sendCard.startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    sendComment(userId, postId);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void animateEditText(boolean isPressed) {
        if (isPressed)
        {
            Animation anim = AnimationUtils.loadAnimation(CommentActivity.this, R.anim.scale);
            anim.setFillAfter(true);
            binding.inputMessage.startAnimation(anim);
        }
        else
        {
            Animation anim = AnimationUtils.loadAnimation(CommentActivity.this, R.anim.scale_reverse);
            binding.inputMessage.startAnimation(anim);
        }
    }

    private void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}