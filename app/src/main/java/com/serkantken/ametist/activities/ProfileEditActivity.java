package com.serkantken.ametist.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivityProfileEditBinding;
import com.serkantken.ametist.databinding.ActivityProfileEditNewBinding;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Utilities;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ProfileEditActivity extends BaseActivity
{
    ActivityProfileEditNewBinding binding;
    FirebaseFirestore database;
    FirebaseAuth auth;
    UserModel user;
    Utilities utilities;
    ArrayList<String> ageList;
    ArrayAdapter<String> arrayAdapter;
    Uri[] pictureList = new Uri[4];
    ActivityResultLauncher<String> getProfilePic, getSquarePic;
    int requesting = 90;
    int photoAdded = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        utilities = new Utilities(this,this);

        utilities.blur(binding.toolbarBlur, 10f, false);

        binding.toolbarBlur.setPadding(0, utilities.getStatusBarHeight(), 0, 0);

        binding.scrollView.setPadding(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(64), 0, 0);
        binding.scrollView.setClipToPadding(false);

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
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, ageList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAge.setAdapter(arrayAdapter);

        binding.buttonBack.setOnClickListener(view -> {
            startActivity(new Intent(ProfileEditActivity.this, ProfileActivity.class));
            finish();
        });

        binding.buttonDone.setOnClickListener(view -> updateUserInfo());

        binding.profileImage.setOnClickListener(view -> {
            if (isPermissionGranted())
            {
                requesting = 101;
                getProfilePic.launch("image/*");
            }
        });

        /*binding.secondImage.setOnClickListener(view -> {
            if (isPermissionGranted())
            {
                requesting = 102;
                getContent.launch("image/*");
            }
        });

        binding.thirdImage.setOnClickListener(view -> {
            if (isPermissionGranted())
            {
                requesting = 103;
                getContent.launch("image/*");
            }
        });

        binding.fourthImage.setOnClickListener(view -> {
            if (isPermissionGranted())
            {
                requesting = 104;
                getContent.launch("image/*");
            }
        });*/

        getProfilePic = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            String destUri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

            UCrop.Options options = new UCrop.Options();
            options.setLogoColor(getColor(R.color.accent_purple_dark));
            options.setFreeStyleCropEnabled(false);
            options.setToolbarTitle(getString(R.string.crop_portrait));
            options.withAspectRatio(3, 4);
            UCrop.of(result, Uri.fromFile(new File(getCacheDir(), destUri)))
                    .withOptions(options)
                    .start(ProfileEditActivity.this);
        });

        getSquarePic = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            String destUri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

            UCrop.Options options = new UCrop.Options();
            options.setLogoColor(getColor(R.color.accent_purple_dark));
            options.setFreeStyleCropEnabled(false);
            options.setToolbarTitle(getString(R.string.crop_square));
            options.withAspectRatio(1, 1);
            UCrop.of(result, Uri.fromFile(new File(getCacheDir(), destUri)))
                    .withOptions(options)
                    .start(ProfileEditActivity.this);
        });

        getUserInfo();
    }

    private Boolean isPermissionGranted()
    {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q))
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            else
            {
                ActivityCompat.requestPermissions(ProfileEditActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
                return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            else
            {
                ActivityCompat.requestPermissions(ProfileEditActivity.this, new String[]{
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                }, 1);
                return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
        userModel.put("name", binding.inputName.getText().toString());
        final String[] age = {null};
        binding.spinnerAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                age[0] = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                age[0] = "";
            }
        });
        userModel.put("age", age[0]);
        String gender = "0";
        if (binding.radioMale.isChecked())
        {
            gender = "1";
        }
        else if (binding.radioFemale.isChecked())
        {
            gender = "2";
        }
        userModel.put("gender", gender);
        userModel.put("about", binding.inputAbout.getText().toString());
        userModel.put("age", binding.spinnerAge.getSelectedItem().toString());
        if (pictureList[0] != null)
        {
            StorageReference filePath = FirebaseStorage.getInstance().getReference("Users").child(auth.getUid()).child("profilePics").child(new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString());
            UploadTask uploadTask = filePath.putFile(pictureList[0], new StorageMetadata.Builder().build());
            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                dialog.setProgress((int) progress);
            }).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                userModel.put("profilePic", downloadUrl);
                reference.update(userModel).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful())
                    {
                        dialog.dismiss();
                        Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                        intent.putExtra("receiverUser", user);
                        startActivity(intent);
                        finish();
                    }
                });
            })).addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Hata", Toast.LENGTH_SHORT).show());

/*
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful())
                {
                    throw Objects.requireNonNull(task.getException());
                }
                return filePath.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                Uri downloadUri = task.getResult();
                String downloadUrl = downloadUri.toString();
                userModel.put("profilePic", downloadUrl);
                reference.update(userModel).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful())
                    {
                        dialog.dismiss();
                        Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                        intent.putExtra("receiverUser", user);
                        startActivity(intent);
                        finish();
                    }
                });
            });*/
        }
        else
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
    }

    private void getUserInfo()
    {
        database.collection("Users").get().addOnCompleteListener(task -> {
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
                        user.setPicSecond(documentSnapshot.getString("picSecond"));
                        user.setPicThird(documentSnapshot.getString("picThird"));
                        user.setPicFourth(documentSnapshot.getString("picFourth"));
                    }
                }

                if (user.getProfilePic() != null)
                {
                    Glide.with(this).load(user.getProfilePic()).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_profile)).into(binding.profileImage);
                    photoAdded++;
                }
                else
                {
                    Glide.with(this).load(AppCompatResources.getDrawable(this, R.drawable.ic_person_profile)).into(binding.profileImage);
                }
                /*
                if (user.getPicSecond() != null)
                {
                    Glide.with(this).load(user.getPicSecond()).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_add)).into(binding.secondImage);
                    photoAdded++;
                }
                else
                {
                    Glide.with(this).load(AppCompatResources.getDrawable(this, R.drawable.ic_person_add)).into(binding.secondImage);
                }
                if (user.getPicThird() != null)
                {
                    Glide.with(this).load(user.getPicThird()).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_add)).into(binding.thirdImage);
                    photoAdded++;
                }
                else
                {
                    Glide.with(this).load(AppCompatResources.getDrawable(this, R.drawable.ic_person_add)).into(binding.thirdImage);
                }
                if (user.getPicFourth() != null)
                {
                    Glide.with(this).load(user.getPicFourth()).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_add)).into(binding.fourthImage);
                    photoAdded++;
                }
                else
                {
                    Glide.with(this).load(AppCompatResources.getDrawable(this, R.drawable.ic_person_add)).into(binding.fourthImage);
                }
                */

                binding.inputName.setText(user.getName());
                int age = Integer.parseInt(user.getAge()) - 17;
                binding.spinnerAge.setSelection(age, true);
                String gender = user.getGender();
                if (gender.equals("1"))
                {
                    binding.radioMale.setChecked(true);
                }
                else if (gender.equals("2"))
                {
                    binding.radioFemale.setChecked(true);
                }
                binding.inputAbout.setText(user.getAbout());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && requesting == 101)
        {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null)
            {
                pictureList[0] = resultUri;
            }

            Glide.with(this).load(resultUri).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_profile)).into(binding.profileImage);
        }
        /*
        else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && requesting == 102)
        {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null)
            {
                pictureList[1] = resultUri;
                ConstraintSet set = new ConstraintSet();
                set.clone(binding.imageLayout);
                set.setHorizontalBias(binding.secondImage.getId(), 0);
            }

            Glide.with(this).load(resultUri).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_profile)).into(binding.secondImage);
        }
        else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && requesting == 103)
        {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null)
            {
                pictureList[2] = resultUri;
            }

            Glide.with(this).load(resultUri).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_profile)).into(binding.thirdImage);
        }
        else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && requesting == 104)
        {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null)
            {
                pictureList[3] = resultUri;
            }

            Glide.with(this).load(resultUri).placeholder(AppCompatResources.getDrawable(this, R.drawable.ic_person_profile)).into(binding.fourthImage);
        }
        */
    }
}