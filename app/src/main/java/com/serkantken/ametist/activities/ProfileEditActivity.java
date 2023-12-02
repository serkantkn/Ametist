package com.serkantken.ametist.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivityProfileEditBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import eightbitlab.com.blurview.BlurView;

public class ProfileEditActivity extends BaseActivity
{
    ActivityProfileEditBinding binding;
    FirebaseFirestore database;
    FirebaseAuth auth;
    UserModel user;
    Utilities utilities;
    ArrayList<String> ageList;
    ArrayAdapter<String> ageAdapter;
    String photoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        utilities = new Utilities(this,this);

        utilities.blur(new BlurView[]{binding.toolbarBlur, binding.blurAbout, binding.usernameBlur}, 10f, false);

        binding.toolbarBlur.setPadding(0, utilities.getStatusBarHeight(), 0, 0);

        binding.scrollView.setPadding(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(64), 0, 0);
        binding.scrollView.setClipToPadding(false);

        ageList = new ArrayList<>();
        fillComponents(ageList, getString(R.string.age), 17, 100);
        ageAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, ageList);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAge.setAdapter(ageAdapter);

        binding.buttonBack.setOnClickListener(view -> finish());

        binding.buttonDone.setOnClickListener(view -> updateUserInfo());

        binding.profileImage.setOnClickListener(view -> {
            ImagePicker.with(this).galleryOnly().crop(3, 4).start(101);
        });

        getUserInfo();
    }

    private void fillComponents(ArrayList<String> list, String name, int start, int end)
    {
        for (int i = start; i <= end; i++)
        {
            if (i == start)
            {
                list.add(name);
            }
            else
            {
                list.add(i + "");
            }
        }
    }

    private void updateUserInfo()
    {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.saving));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(false);
        dialog.create();
        dialog.show();
        DocumentReference reference = database.collection("Users").document(Objects.requireNonNull(auth.getUid()));
        user.setUserId(auth.getUid());
        HashMap<String, Object> userModel = new HashMap<>();
        userModel.put("name", binding.etUsername.getText().toString());
        userModel.put("about", binding.inputAbout.getText().toString());
        userModel.put("age", binding.spinnerAge.getSelectedItem().toString());
        if (binding.radioMale.isChecked()) {
            userModel.put("gender", "1");
        } else if (binding.radioFemale.isChecked()) {
            userModel.put("gender", "2");
        } else {
            userModel.put("gender", "0");
        }
        if (!Objects.isNull(photoUri))
        {
            StorageReference filePath = FirebaseStorage.getInstance().getReference("Users").child(auth.getUid()).child("profilePics").child(UUID.randomUUID().toString() + ".jpg");
            UploadTask uploadTask = filePath.putFile(Uri.parse(photoUri), new StorageMetadata.Builder().build());
            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                dialog.setProgress((int) progress);
            }).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                userModel.put("profilePic", downloadUrl);
                updateInfo(reference, userModel, dialog);
            })).addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Hata", Toast.LENGTH_SHORT).show());
        }
        else
        {
            updateInfo(reference, userModel, dialog);
        }
    }

    private void updateInfo(DocumentReference reference, HashMap<String, Object> userModel, ProgressDialog dialog)
    {
        reference.update(userModel).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                dialog.dismiss();
                Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                intent.putExtra("receiverUser", user);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getUserInfo()
    {
        database.collection(Constants.DATABASE_PATH_USERS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                user = new UserModel();
                for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                {
                    if (documentSnapshot.getId().equals(auth.getUid()))
                    {
                        user.setAge(documentSnapshot.getString("age"));
                        user.setGender(documentSnapshot.getString("gender"));
                        user.setAbout(documentSnapshot.getString("about"));
                        user.setName(documentSnapshot.getString("name"));
                        user.setProfilePic(documentSnapshot.getString("profilePic"));
                    }
                }

                binding.etUsername.setText(user.getName());
                binding.inputAbout.setText(user.getAbout());
                try {
                    int age = Integer.parseInt(user.getAge()) - 17;
                    binding.spinnerAge.setSelection(age, true);
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 101)
        {
            assert data != null;
            Uri resultUri = data.getData();
            if (resultUri != null)
            {
                photoUri = resultUri.toString();
                Glide.with(this).load(photoUri).into(binding.profileImage);
            }
        }
    }
}