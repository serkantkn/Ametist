package com.serkantken.ametist.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.serkantken.ametist.utilities.Utilities;

import java.util.Date;

public class BaseActivity extends AppCompatActivity
{
    private DocumentReference documentReference;
    private FirebaseFirestore database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities utilities = new Utilities(getApplicationContext(), this);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        documentReference = database.collection("Users").document(auth.getUid());
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update("online", false);
        documentReference.update("lastSeen", new Date().getTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update("online", true);
        documentReference.update("lastSeen", FieldValue.delete());
    }
}
