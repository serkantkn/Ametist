package com.serkantken.ametist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.MainActivity;
import com.serkantken.ametist.databinding.FragmentSignupTabBinding;
import com.serkantken.ametist.models.SettingsModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Objects;

public class SignupTabFragment extends Fragment
{
    private FragmentSignupTabBinding binding;
    ArrayList<String> ageList;
    ArrayAdapter<String> arrayAdapter;
    String name, age, email, password, gender;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    Utilities utilities;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentSignupTabBinding.inflate(getLayoutInflater());

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        utilities = new Utilities(requireContext(), requireActivity());

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
        arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, ageList);
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
                else
                {
                    gender = "0";
                }
                email = binding.inputMail.getText().toString().trim().toLowerCase();
                password = binding.inputPassword.getText().toString();

                signUpWithEmailAndPassword();
            }
        });

        return binding.getRoot();
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
                userModel.setPassword(password);
                userModel.setOnline(true);

                database.collection("Users").document(Objects.requireNonNull(auth.getUid())).set(userModel).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful())
                    {
                        loading(false);
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        getActivity().finish();
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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}