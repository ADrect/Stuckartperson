package com.example.stuckart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.stuckart.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){

            startLoginOptions();
        }

        showHomeFragment();

        binding.bottomNv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {

                    showHomeFragment();

                    return true;
                } else if (itemId == R.id.menu_chats) {

                    if (firebaseAuth.getCurrentUser() == null){
                        Utils.toast(MainActivity.this,"Login Required...");
                        startLoginOptions();
                        return false;
                    } else {
                        showChatsFragment();

                        return true;
                    }

                } else if (itemId == R.id.menu_my_ads) {

                    if (firebaseAuth.getCurrentUser() == null){
                        Utils.toast(MainActivity.this,"Login Required...");
                        startLoginOptions();
                        return false;
                    } else {
                        showMyAdsFragment();

                        return true;
                    }

                } else if (itemId == R.id.menu_account) {

                    if (firebaseAuth.getCurrentUser() == null){
                        Utils.toast(MainActivity.this,"Login Required...");
                        startLoginOptions();
                        return false;
                    } else {
                        showAccountFragment();

                        return true;
                    }
                } else {

                    return false;
                }
            }
        });

        // handle sellFab click, start AdCreateActivity
        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AdCreateActivity.class));
            }
        });
    }

    private void showHomeFragment() {

        binding.toolbarTitleTv.setText("Home");

        //Show HomeFragment
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment,"HomeFragment");
        fragmentTransaction.commit();

    }
    private void showChatsFragment(){

        binding.toolbarTitleTv.setText("Chats");

        //Show ChatsFragment
        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment,"ChatsFragment");
        fragmentTransaction.commit();

    }
    private void showMyAdsFragment(){

        binding.toolbarTitleTv.setText("My Ads");

        //Show MyAdsFragment
        MyAdsFragment fragment = new MyAdsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment,"MyAdsFragment");
        fragmentTransaction.commit();
    }
    private void showAccountFragment(){

        binding.toolbarTitleTv.setText("Account");

        //Show AccountFragment
        AccountFragment fragment = new AccountFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment,"AccountFragment");
        fragmentTransaction.commit();
    }

    private void startLoginOptions(){
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }

}


