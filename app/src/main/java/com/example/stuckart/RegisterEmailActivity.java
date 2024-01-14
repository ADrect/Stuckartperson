package com.example.stuckart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.stuckart.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;

    private static final String TAG = "REGISTER_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        binding.haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String email, password, cPassword;

    private void validateData() {

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        cPassword = binding.cPasswordEt.getText().toString();

        Log.d(TAG, "validateData: email: " + email);
        Log.d(TAG, "validateData: password: " + password);
        Log.d(TAG, "validateData: cPassword: " + cPassword);

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
           //email pattern is invalid,show error
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            //password is not entered, show error
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        } else if (!password.equals(cPassword)) {
             //password and confirm password is not same
            binding.cPasswordEt.setError("Password doesn't match");
            binding.cPasswordEt.requestFocus();

        } else {
            //all data is valid , start sign-up
            registerUser();
        }
    }

    private void registerUser() {

        progressDialog.setMessage("Creating Account");
        progressDialog.show();


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Log.d(TAG, "onSuccess: Register Success");
                        updateUserinfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure:", e);
                        Utils.toast(RegisterEmailActivity.this, "Failed due to " + e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateUserinfo(){

        progressDialog.setTitle("Saving User info");

        long timestamp = Utils.getTimestamp(); //get current timestamp e.g. to show user registration data/time
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail(); //get email of registered user
        String registerUserUid = firebaseAuth.getUid(); //get vid of registered user

        //setup data to save in firebase realtime db. most of the data will be empty and will set in edit profile
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Email"); //possible values Email/Phone/Google
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        //set data to firebase db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Firebase db save success
                        Log.d(TAG, "onSuccess: Info saved...");
                        progressDialog.dismiss();

                        startActivity(new Intent(RegisterEmailActivity.this, MainActivity.class));
                        finishAffinity();
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Firebase db save failed
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(RegisterEmailActivity.this, "Failed to save info due to"+e.getMessage());
                    }
                });
    }
}
