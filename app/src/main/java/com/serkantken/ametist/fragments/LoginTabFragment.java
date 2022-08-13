package com.serkantken.ametist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.MainActivity;
import com.serkantken.ametist.databinding.FragmentLoginTabBinding;

public class LoginTabFragment extends Fragment
{
    private FragmentLoginTabBinding binding;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentLoginTabBinding.inflate(getLayoutInflater());

        auth = FirebaseAuth.getInstance();

        binding.buttonLogin.setOnClickListener(view -> {
            if (isInformationCorrect())
            {
                loading(true);
                String email = binding.inputMail.getText().toString().toLowerCase();
                String password = binding.inputPassword.getText().toString();
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                    else
                    {
                        loading(false);
                        showToast(getString(R.string.error_at_login));
                    }
                }).addOnFailureListener(e -> {
                    loading(false);
                    showToast(getString(R.string.error_at_login)+e.toString());
                });
            }
        });

        return binding.getRoot();
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
            binding.inputMail.setEnabled(false);
            binding.inputPassword.setEnabled(false);
            binding.buttonLogin.setVisibility(View.GONE);
            binding.progressbar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.inputMail.setEnabled(true);
            binding.inputPassword.setEnabled(true);
            binding.buttonLogin.setVisibility(View.VISIBLE);
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