package com.example.stuckart;

import androidx.appcompat.app.AppCompatActivity;

import android.nfc.Tag;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.app.ProgressDialog;
import android.content.Intent;
import android.telephony.ims.ImsStateCallback;
import android.util.Log;
import android.view.View;

import com.example.stuckart.databinding.ActivityLoginPhoneBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private ActivityLoginPhoneBinding binding;

    //ProgressDialog to show while phone login
    private ProgressDialog progressDialog;

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;

    private static final String TAG = "LOGIN_PHONE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //For the start show phone input UI and hide OTP UI
        binding.phoneInputRL.setVisibility(View.VISIBLE);
        binding.optInputRl.setVisibility(View.GONE);

        //init/setup ProgressDialog to show while login
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        //Listen for phone login callbacks. Hint: you may put here instead of creating a func
        phoneLoginCallback();

        //handle toolbarBackBtn click, go.back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        binding.send0tpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();

            }
        });

        binding.resend0tpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(forceResendingToken);
            }
        });

        binding.verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Input Otp  String otp binding.otpET.getText().toString().trim();
                String otp = binding.otpEt.getText().toString().trim();

                Log.d(TAG, "onClick: OTP: " + otp);

                //validate if otp is entered and length is 6 characters
                if (otp.isEmpty()) {
                    //OTP is empty, show error
                    binding.otpEt.setError("Enter OTP");
                    binding.otpEt.requestFocus();
                } else if (otp.length() < 6) {
                    //OTP length is less then 6, show error
                    binding.otpEt.setError("OTP length must be 6 Characters");
                    binding.otpEt.requestFocus();
                } else {
                    verifyPhoneNumberWithCode(mVerificationId, otp);
                }
            }
        });
    }

    private String phoneCode = "", phoneNumber = "", phoneNumberWithCode = "";

    private void validateData(){
        //input data
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode+phoneNumber;

        Log.d(TAG, "validateData: phoneCode: " + phoneCode);
        Log.d(TAG, "validateData: phoneNumber: " + phoneNumber);
        Log.d(TAG, "validateData: phone NumberWithCode: " + phoneNumberWithCode);

        //validate data
        if (phoneNumber.isEmpty()) {
            //phoneNumber is not entered, show error
            Utils.toast(this, "Please enter phone number");
        } else {
            startPhoneNumberVerification();
        }
    }

    private void startPhoneNumberVerification() {
        Log.d(TAG, "startPhoneNumberVerification: ");
        progressDialog.setMessage("Sending OTP to "+phoneNumberWithCode);
        progressDialog.show();


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode) //Phone Number with country code e.g. +92******************
                        .setTimeout(60L, TimeUnit.SECONDS) //Timeout and unit
                        .setActivity(this)                 //Activity (for callback binding)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void phoneLoginCallback() {
        Log.d(TAG, "phoneLoginCallBack: ");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: ");
                // This callback will be invoked in two situations:
                // 1 Instant verification. In some cases the phone number can be instantly
                // verified without needing to send or enter a verification code.
                // 2- Auto-retrieval. On some devices Google Play services con automatically
                // detect the incoming verification SMS and perform verification without

                signinWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: ", e);

                // This coll back is invoked in an invalid request for verification is mode,
                // for instance if the the phone number format is not valid.
                progressDialog.dismiss();
                Utils.toast(LoginPhoneActivity.this, "" + e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct credential
                // by combining the code with a verification ID.

                // Save verification 10 and resending token so we con use then later
                mVerificationId = verificationId;
                forceResendingToken = token;
                //OTP is sent so hide progress for now
                progressDialog.dismiss();
                // OTP is sent so hide phone us and shoe otp ui
                binding.phoneInputRL.setVisibility(View.INVISIBLE);
                binding.optInputRl.setVisibility(View.VISIBLE);

                //Snow toast for success sending OTP
                Utils.toast(LoginPhoneActivity.this, "OTP Sent to " + phoneNumberWithCode);
                //show user a message that Please type the verification code sent to the phone number user has input
                binding.loginLabelTv.setText("Please type the verification code sent to " + phoneNumberWithCode);
            }
        };
    }

    private void verifyPhoneNumberWithCode(String verificationId, String otp) {
        Log.d(TAG, "verifyPhoneNumberWithCode: verificationId: " + verificationId);
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: " + otp);

        //show progress
        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();
        //PhoneAuthCredential with verification id and OTP to signIn user with signInWithPhoneAuthCredential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        signinWithPhoneAuthCredential(credential);

    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token) {
        Log.d(TAG, "resendVerificationCode: ForceResendingToken: " + token);
        //show progress dialog
        progressDialog.setMessage("Resending OTP to" +phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)      //FirebaseAuth Instance
                        .setPhoneNumber(phoneNumberWithCode)   //Phone Number with country code e.g. +92
                        .setTimeout(60L , TimeUnit.SECONDS)   //Timeout and unit
                        .setActivity(this)                     //Activity (for callback Binding)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signinWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signinWithPhoneAuthCredential: ");
        progressDialog.setMessage("Logging In");

        //SignIn in to firebase auth using Phone Credentials
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: ");
                        //SignIn Success, let's check if the user is now (New Account Register) or existing (Existing Login)
                        if (authResult.getAdditionalUserInfo().isNewUser()) {
                            //New User, Account created, Let's save user info to firebase realtime database
                            Log.d(TAG, "onSuccess: New User, Account created...");

                            updateUserInfoDb();
                        }
                        else {
                            Log.d(TAG, "onSuccess: Existing User, Logged In");
                            //New User, Account created. No need to save veer info to firebase realtime database, Start MoinActivity
                            startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //SignIn failed, show exception message
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(LoginPhoneActivity.this, "Failed to login due to " + e.getMessage());
                    }
                });
    }

    private void updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ");
        progressDialog.setMessage("Saving user info");
        progressDialog.show();

        //Let's save user info to Firebase Realtime database key names should be same as we done in Register User via email and Google
        //get current timestamp e.g. to show user registration date/time
        long timestamp = Utils.getTimestamp();
        String registerUserUid = firebaseAuth.getUid();

        //setup data to save in firebase realtime db. most of the date will be empty and will set in edit profile
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode",""+phoneCode);
        hashMap.put("phoneNumber", "" + phoneNumber);
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Phone"); //possible valves Email/Phone/Google
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", "");
        hashMap.put("uid", registerUserUid);
        //set date to firebase db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //User inf save success
                        Log.d(TAG, "onSuccess: User info saved");
                        progressDialog.dismiss();

                        //Start MainActivity
                        startActivity(new Intent( LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //User inf save failed
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(LoginPhoneActivity.this, "Failed to save user info due to "+e.getMessage());

                    }
                });
    }
}



