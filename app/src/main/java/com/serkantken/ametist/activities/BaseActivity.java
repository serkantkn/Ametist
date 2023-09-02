package com.serkantken.ametist.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.utilities.Utilities;

import java.util.Date;
import java.util.Objects;

public class BaseActivity extends AppCompatActivity
{
    private DocumentReference documentReference;
    private FirebaseFirestore database;
    private FirebaseAuth auth;
    int online, lastSeen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities utilities = new Utilities(getApplicationContext(), this);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        Hawk.init(this).build();
        if (Hawk.contains("online"))
            online = Hawk.get("online", 1);
        if (Hawk.contains("lastSeen"))
            lastSeen = Hawk.get("lastSeen", 1);

        documentReference = database.collection("Users").document(Objects.requireNonNull(auth.getUid()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (online == 1)
            documentReference.update("online", false);
        if (lastSeen == 1)
            documentReference.update("lastSeen", new Date().getTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (online == 1)
            documentReference.update("online", true);
        if (lastSeen == 1)
            documentReference.update("lastSeen", FieldValue.delete());
    }
}
