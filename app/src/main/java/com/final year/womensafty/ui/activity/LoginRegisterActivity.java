package com.pemchip.womensafty.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.pemchip.womensafty.R;
import com.pemchip.womensafty.databinding.ActivityLoginRegisterBinding;

@SuppressWarnings("FieldCanBeLocal")
public class LoginRegisterActivity extends AppCompatActivity {

    private ActivityLoginRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}