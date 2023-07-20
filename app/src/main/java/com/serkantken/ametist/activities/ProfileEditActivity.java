package com.serkantken.ametist.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.PhotoAdapter;
import com.serkantken.ametist.databinding.ActivityProfileEditNewBinding;
import com.serkantken.ametist.models.PhotoModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.PhotoListener;
import com.serkantken.ametist.utilities.Utilities;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ProfileEditActivity extends BaseActivity implements PhotoListener
{
    ActivityProfileEditNewBinding binding;
    FirebaseFirestore database;
    FirebaseAuth auth;
    UserModel user;
    Utilities utilities;
    ArrayList<String> ageList;
    ArrayAdapter<String> ageAdapter;
    ArrayAdapter<String> relationshipAdapter;
    ArrayAdapter<String> lookingAdapter;
    ArrayAdapter<String> roleAdapter;
    ArrayAdapter<String> sexualityAdapter;
    PhotoAdapter photoAdapter;
    ArrayList<Uri> pictureUris = new ArrayList<>();
    ArrayList<Uri> squareUris = new ArrayList<>();
    ArrayList<PhotoModel> pictureList = new ArrayList<>();
    PhotoModel photoModel;
    ArrayList<String> relationships = new ArrayList<>();
    ArrayList<String> roles = new ArrayList<>();
    ArrayList<String> looking = new ArrayList<>();
    ArrayList<String> sexuality = new ArrayList<>();

    int index = 0;

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

        photoAdapter = new PhotoAdapter(pictureList, ProfileEditActivity.this, this);
        binding.profileImageRV.setAdapter(photoAdapter);

        ageList = new ArrayList<>();
        fillComponents(ageList, getString(R.string.age), 17, 100);
        ageAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, ageList);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAge.setAdapter(ageAdapter);

        lookingAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, looking);
        lookingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLooking.setAdapter(lookingAdapter);

        relationshipAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, relationships);
        relationshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRelationship.setAdapter(relationshipAdapter);

        roleAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRole.setAdapter(roleAdapter);

        sexualityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sexuality);
        sexualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSexuality.setAdapter(sexualityAdapter);

        binding.npHeight.setMinValue(130);
        binding.npHeight.setMaxValue(300);
        binding.npWeight.setMinValue(30);
        binding.npWeight.setMaxValue(300);

        binding.buttonBack.setOnClickListener(view -> finish());

        binding.buttonDone.setOnClickListener(view -> updateUserInfo());

        binding.buttonAddPhoto.setOnClickListener(view -> {
            ImagePicker.with(this).galleryOnly().crop(3,4).start(101);
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

        relationships.add(getString(R.string.not_show));
        relationships.add(getString(R.string.single));
        relationships.add(getString(R.string.married));
        relationships.add(getString(R.string.divorced));
        relationships.add(getString(R.string.other));

        roles.add(getString(R.string.not_show));
        roles.add(getString(R.string.top));
        roles.add(getString(R.string.more_top));
        roles.add(getString(R.string.versatile));
        roles.add(getString(R.string.more_bottom));
        roles.add(getString(R.string.bottom));
        roles.add(getString(R.string.other));

        looking.add(getString(R.string.not_show));
        looking.add(getString(R.string.friendship));
        looking.add(getString(R.string.love));
        looking.add(getString(R.string.conversation));
        looking.add(getString(R.string.other));

        sexuality.add(getString(R.string.not_show));
        sexuality.add(getString(R.string.hetero));
        sexuality.add(getString(R.string.lesbian));
        sexuality.add(getString(R.string.gay));
        sexuality.add(getString(R.string.bisex));
        sexuality.add(getString(R.string.trans));
        sexuality.add(getString(R.string.inter));
        sexuality.add(getString(R.string.other));
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
        userModel.put("about", binding.inputAbout.getText().toString());
        userModel.put("age", binding.spinnerAge.getSelectedItem().toString());
        if (pictureList.get(0) != null)
        {
            /*StorageReference filePath = FirebaseStorage.getInstance().getReference("Users").child(auth.getUid()).child("profilePics").child(UUID.randomUUID().toString() + ".jpg");
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
            })).addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Hata", Toast.LENGTH_SHORT).show());*/
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

    @SuppressLint("NotifyDataSetChanged")
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
                        user.setLooking(documentSnapshot.getString("looking"));
                        user.setRelationship(documentSnapshot.getString("relationship"));
                        user.setRole(documentSnapshot.getString("role"));
                        user.setHeight(Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("height").toString())));
                        user.setWeight(Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("weight").toString())));
                    }
                }

                binding.inputName.setText(user.getName());
                int age = Integer.parseInt(user.getAge()) - 17;
                binding.spinnerAge.setSelection(age, true);
                binding.inputAbout.setText(user.getAbout());
                binding.npWeight.setValue(user.getWeight());
                binding.npHeight.setValue(user.getHeight());

                database.collection(Constants.DATABASE_PATH_USERS).document(Objects.requireNonNull(auth.getUid())).collection("photos").get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful())
                    {
                        for (QueryDocumentSnapshot photoIDs : task1.getResult())
                        {
                            PhotoModel photo = new PhotoModel();
                            photo.setPhotoId(photoIDs.getId());
                            photo.setLink(photoIDs.getString("link"));
                            photo.setDate(photoIDs.getLong("date"));
                            pictureList.add(photo);
                        }
                        pictureList.sort(Comparator.comparing(PhotoModel::getDate));
                        photoAdapter.notifyDataSetChanged();
                    }
                });
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
                photoModel = new PhotoModel();
                photoModel.setLink(resultUri.toString());

                String destUri = UUID.randomUUID().toString() + ".jpg";

                UCrop.Options options = new UCrop.Options();
                options.setLogoColor(getColor(R.color.accent_purple_dark));
                options.setFreeStyleCropEnabled(false);
                options.setToolbarTitle(getString(R.string.crop_square));
                options.withAspectRatio(1, 1);
                UCrop.of(resultUri, Uri.fromFile(new File(getCacheDir(), destUri)))
                        .withOptions(options)
                        .start(ProfileEditActivity.this, 102);
            }
        }
        else if (resultCode == RESULT_OK && requestCode == 102)
        {
            assert data != null;
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null)
            {
                photoModel.setSquareLink(resultUri.toString());
                photoModel.setDate(new Date().getTime());
                pictureList.add(photoModel);
                pictureList.sort(Comparator.comparing(PhotoModel::getDate));
                photoAdapter.notifyDataSetChanged();
            }
        }
        else if (resultCode == RESULT_CANCELED && requestCode == 102)
        {
            photoModel = null;
        }
    }

    @Override
    public void onClick(String link) {

    }

    @Override
    public void onRemove(long date, int position)
    {
        pictureList.removeIf(model -> model.getDate() == date);
        photoAdapter.notifyItemRemoved(position);
    }
}