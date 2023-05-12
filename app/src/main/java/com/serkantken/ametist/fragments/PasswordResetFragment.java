package com.serkantken.ametist.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.FragmentPasswordResetBinding;

import java.util.List;

public class PasswordResetFragment extends Fragment {
    private FragmentPasswordResetBinding binding;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentPasswordResetBinding.inflate(getLayoutInflater());

        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        auth = FirebaseAuth.getInstance();

        assert getArguments() != null;
        PasswordResetFragmentArgs bundle = PasswordResetFragmentArgs.fromBundle(getArguments());
        binding.inputMail.setText(bundle.getEmail());

        String language = getResources().getConfiguration().getLocales().get(0).getLanguage();
        auth.setLanguageCode(language);

        binding.buttonResetPassword.setOnClickListener(v -> {
            if (isInformationCorrect())
            {
                validateEmail(binding.inputMail.getText().toString());
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
        else
            return true;
    }

    private void validateEmail(String email)
    {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                SignInMethodQueryResult result = task.getResult();
                List<String> signInMethods = result.getSignInMethods();
                assert signInMethods != null;
                if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                {
                    resetPassword(email);
                }
                else
                {
                    showError(binding.inputMail, "Girilen Email adresine kayıtlı bir hesap bulunamadı");
                }
            }
            else
            {
                Toast.makeText(requireContext(), "Bir hata oluştu. İnternet bağlantınızı kontrol edin ve daha sonra tekrar deneyin.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String email)
    {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Parolayı sıfırla");
                builder.setMessage("Parolanızı sıfırlamak için yazdığınız email adresinize bir link gönderdik. Gelen kutunuzu kontrol ediniz.");
                builder.setNeutralButton(requireContext().getString(R.string.ok), (dialog, which) -> {
                    requireActivity().onBackPressed();
                    dialog.dismiss();
                });
                builder.create();
                builder.show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Hata");
                builder.setMessage("Parola sıfırlama linki gönderilirken sorun oluştu. Daha sonra tekrar deneyin.");
                builder.setNeutralButton(requireContext().getString(R.string.ok), (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.create();
                builder.show();
            }
        });
    }

    private void showError(EditText input, String message)
    {
        input.setError(message);
        input.requestFocus();
    }
}