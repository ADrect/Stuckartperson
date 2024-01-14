package com.example.stuckart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.stuckart.databinding.ActivityLoginEmailBinding;
import com.example.stuckart.databinding.ActivityLoginOptionsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginEmailActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;

    private static final String TAG = "LOGIN_TAG";
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog( this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, RegisterEmailActivity.class));
            }
        });

        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, ForgotPasswordActivity.class));
            }
        });


        binding.loginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String email, password;
    private void validateData(){

        //input data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();

        Log.d(TAG, "validateData: email: "+email);
        Log.d(TAG, "validateData: password: "+password);

        //validate data

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //email pattern is invalid, show error
            binding.emailEt.setError("Invalid Email");
            binding.emailEt.requestFocus();

        } else if (password.isEmpty()) {
            //password is not entered, show error
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();

        } else {
            //email pattern is valid and password is enter. start login
            loginUser();
        }
    }

    private void loginUser(){
        //show progress
        progressDialog.setMessage("Logging in");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Logged In");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginEmailActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast( LoginEmailActivity.this, "Failed due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }
}
