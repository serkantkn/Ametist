package com.serkantken.ametist.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.serkantken.ametist.R;
import com.serkantken.ametist.adapters.LoginAdapter;
import com.serkantken.ametist.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.serkantken.ametist.databinding.ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getResources().getString(R.string.login)));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getResources().getString(R.string.sing_up)));
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.viewPager));

        LoginAdapter loginAdapter = new LoginAdapter(getSupportFragmentManager(), this, binding.tabLayout.getTabCount());
        binding.viewPager.setAdapter(loginAdapter);
        binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
    }
}