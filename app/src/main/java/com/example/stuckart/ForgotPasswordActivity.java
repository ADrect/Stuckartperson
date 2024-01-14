package com.example.stuckart;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth. FirebaseAuth;
import com.example.stuckart.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    //View Binding
    private ActivityForgotPasswordBinding binding;

    //TAG for logs in logcat
    private static final String TAG = "FORGOT PASS TAG";

   //Firebase Auth for auth related tasks usages
    private FirebaseAuth firebaseAuth;

//ProgressDialog to show while sending password recovery instructions
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding... activity forgot password.xml = ActivityForgotPasswordBinding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init/setup ProgressDialog to show sending password recovery Instructions
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //get instance of firebase auth for Auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        //handle toolbarBackBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        //handle submitBtn click, validate data to stort possword recovery
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            validateData();
        }

        });
    }

    private String email = "";

    private void validateData() {
        Log.d(TAG, "validateData: ");
        //input data
        email = binding.emailEt.getText().toString().trim();

        Log.d(TAG, "validateData: email: " + email);
        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email pattern, show error in emailEt
            binding.emailEt.setError("Invalid Email Pattern!");
            binding.emailEt.requestFocus();
        } else {
            //email pattern is valid, send password recovery instructions
            sendPasswordRecoveryInstructions();
        }
    }

    private void sendPasswordRecoveryInstructions(){
        Log.d(TAG, "sendPasswordRecoveryInstructions: "); //show progress
        progressDialog.setMessage("Sending password recovery instructions to "+email);
        progressDialog.show();

        //send password recovery instructions to given email
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // instructions sent, check email, sometimes it goes in spam folder so if not in inbox check your spam folder
                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this, "Instructions to reset password is sent to " + email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to send instructions
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this, "Failed to send due to " + e.getMessage());
                    }
                });
    }
}