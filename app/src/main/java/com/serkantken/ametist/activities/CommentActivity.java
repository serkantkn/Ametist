package com.serkantken.ametist.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.serkantken.ametist.databinding.ActivityCommentBinding;
import com.serkantken.ametist.models.CommentModel;
import com.serkantken.ametist.utilities.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class CommentActivity extends AppCompatActivity {
    private ActivityCommentBinding binding;
    private FirebaseFirestore database;
    private ArrayList<CommentModel> comments;
    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(view -> onBackPressed());

        database = FirebaseFirestore.getInstance();

        String userId = getIntent().getStringExtra("userId");
        String postId = getIntent().getStringExtra("postId");

        comments = new ArrayList<>();
        adapter = new CommentAdapter(comments, this, CommentActivity.this);
        binding.commentRV.setAdapter(adapter);
        binding.commentRV.setLayoutManager(new LinearLayoutManager(this));

        getComments(userId, postId);

        binding.buttonSend.setOnClickListener(view -> {
            sendComment(userId, postId);
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

            database.collection(Constants.DATABASE_PATH_USERS)
                    .document(userId)
                    .collection("Posts")
                    .document(postId)
                    .collection("Comments")
                    .add(comment).addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                        {
                            comment.put("commentId", task.getResult().getId());

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}