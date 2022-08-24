package com.serkantken.ametist.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.activities.FullProfilePhotoActivity;
import com.serkantken.ametist.activities.ProfileActivity;
import com.serkantken.ametist.databinding.LayoutNewPostDialogBinding;
import com.serkantken.ametist.databinding.LayoutPostBinding;
import com.serkantken.ametist.databinding.LayoutProfileBinding;
import com.serkantken.ametist.models.PostModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<PostModel> postModels;
    Context context;
    Activity activity;
    FirebaseFirestore database;

    public PostAdapter(ArrayList<PostModel> postModels, Context context, Activity activity, FirebaseFirestore database) {
        this.postModels = postModels;
        this.context = context;
        this.activity = activity;
        this.database = database;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutPostBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.postImage.setVisibility(View.GONE);
        UserModel user = new UserModel();
        PostModel postModel = postModels.get(position);
        database.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.getId().equals(postModel.getPostedBy())) {
                        user.setUserId(documentSnapshot.getId());
                        user.setName(documentSnapshot.getString("name"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setAge(documentSnapshot.getString("age"));
                        user.setGender(documentSnapshot.getString("gender"));
                    }
                }
                holder.binding.username.setText(user.getName());
                Glide.with(context).load(user.getProfilePic()).placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_person)).into(holder.binding.profileImage);
                if (Objects.equals(user.getUserId(), FirebaseAuth.getInstance().getUid()))
                {
                    holder.binding.buttonMenu.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.binding.postText.setText(postModel.getPostText());
        holder.binding.date.setText(TimeAgo.using(postModel.getPostedAt()));
        holder.binding.profileImage.setOnClickListener(view -> onUserClicked(user));
        holder.binding.username.setOnClickListener(view -> onUserClicked(user));
        if (!Objects.equals(postModel.getPostPicture(), null))
        {
            holder.binding.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(postModel.getPostPicture()).into(holder.binding.postImage);
            holder.binding.postImage.setOnLongClickListener(view -> {
                Intent intent = new Intent(context, FullProfilePhotoActivity.class);
                intent.putExtra("pictureUrl", postModel.getPostPicture());
                context.startActivity(intent);
                return true;
            });
        }

        holder.binding.buttonMenu.setOnClickListener(view -> {
            View popupview = View.inflate(context, R.layout.popup_layout, null);
            ConstraintLayout buttonDelete = popupview.findViewById(R.id.post_delete);
            ConstraintLayout buttonEdit = popupview.findViewById(R.id.post_edit);

            int width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            PopupWindow popupWindow = new PopupWindow(popupview, width, height, false);

            popupWindow.showAsDropDown(holder.binding.buttonMenu);
            popupWindow.setElevation(20f);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);

            buttonDelete.setOnClickListener(view1 -> postDelete(postModel.getPostId(), popupWindow));
            buttonEdit.setOnClickListener(view1 -> postEdit(postModel.getPostId(), position, popupWindow));
        });
    }

    private void postDelete(String postId, PopupWindow window)
    {
        window.dismiss();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.delete_post_question);
        dialog.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            database.collection("Users").document(FirebaseAuth.getInstance().getUid()).collection("Posts").document(postId).delete();
            notifyDataSetChanged();
            dialogInterface.dismiss();
        });
        dialog.setNegativeButton(R.string.no, (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        dialog.create();
        dialog.show();
    }

    private void postEdit(String postId, int position, PopupWindow window)
    {
        window.dismiss();
        database.collection("Users").document(FirebaseAuth.getInstance().getUid()).collection("Posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                HashMap<String, Object> model = new HashMap<>();
                for (QueryDocumentSnapshot snapshot : task.getResult())
                {
                    if (Objects.equals(snapshot.getId(), postId))
                    {
                        model.put("postId", snapshot.getId());
                        model.put("postText", snapshot.getString("postText"));
                        model.put("postPicture", snapshot.getString("postPicture"));
                        model.put("postedBy", snapshot.getString("postedBy"));
                        model.put("postedAt", snapshot.getLong("postedAt"));
                        model.put("commentCount", Integer.parseInt(snapshot.get("commentCount").toString()));
                        model.put("likeCount", Integer.parseInt(snapshot.get("likeCount").toString()));
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                LayoutNewPostDialogBinding dialogBinding = LayoutNewPostDialogBinding.inflate(activity.getLayoutInflater());
                builder.setView(dialogBinding.getRoot());
                AlertDialog dialog = builder.create();

                dialogBinding.profileImage.setVisibility(View.GONE);
                dialogBinding.username.setVisibility(View.GONE);
                dialogBinding.buttonAddPhoto.setVisibility(View.GONE);
                dialogBinding.postText.setText(model.get("postText").toString());
                if (!Objects.equals(model.get("postPicture").toString(), ""))
                {
                    Glide.with(context).load(model.get("postPicture").toString()).into(dialogBinding.postImage);
                    dialogBinding.postImage.setVisibility(View.VISIBLE);
                }

                dialogBinding.postText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.length() == 0)
                        {
                            dialogBinding.buttonSubmitBackground.setBackgroundColor(context.getColor(R.color.secondary_text));
                            dialogBinding.buttonSubmit.setEnabled(false);
                        }
                        else
                        {
                            dialogBinding.buttonSubmitBackground.setBackground(AppCompatResources.getDrawable(context, R.drawable.background_post_footer_buttons));
                            dialogBinding.buttonSubmit.setEnabled(true);
                        }
                    }
                });

                dialogBinding.buttonSubmit.setOnClickListener(view -> {
                    model.put("postText", dialogBinding.postText.getText().toString());
                    database.collection("Users").document(FirebaseAuth.getInstance().getUid()).collection("Posts").document(postId).update(model);
                    notifyItemChanged(position);
                    dialog.dismiss();
                });

                if (dialog.getWindow() != null)
                {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                dialog.show();
            }
        });
    }

    public void onUserClicked(UserModel userModel) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme_Chat);
        LayoutProfileBinding bottomSheetView = LayoutProfileBinding.inflate(LayoutInflater.from(context));

        Utilities utilities = new Utilities(context, activity);
        utilities.blur(bottomSheetView.bottomSheetContainer, 10f, false);
        bottomSheetView.username.setText(userModel.getName());
        Glide.with(context).load(userModel.getProfilePic()).placeholder(R.drawable.ic_person).into(bottomSheetView.profileImage);
        bottomSheetView.textAbout.setText(userModel.getAbout());
        bottomSheetView.textAge.setText(userModel.getAge());
        switch (userModel.getGender()) {
            case "0":
                bottomSheetView.textGender.setText("-");
                break;
            case "1":
                bottomSheetView.textGender.setText(activity.getString(R.string.man));
                break;
            case "2":
                bottomSheetView.textGender.setText(activity.getString(R.string.woman));
                break;
        }

        bottomSheetView.buttonMore.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("receiverUser", userModel);
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });
        bottomSheetView.buttonMessage.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverUser", userModel);
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });
        bottomSheetView.buttonClose.setOnClickListener(view1 -> bottomSheetDialog.dismiss());
        bottomSheetView.buttonEdit.setVisibility(View.GONE);

        bottomSheetDialog.setContentView(bottomSheetView.getRoot());
        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {
        return postModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LayoutPostBinding binding;

        public ViewHolder(@NonNull LayoutPostBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
