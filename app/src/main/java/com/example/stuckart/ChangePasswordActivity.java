package com.example.stuckart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.stuckart.databinding.ActivityChangePasswordBinding;

import android.os.Bundle;

public class ChangePasswordActivity extends AppCompatActivity {

    //View Binding
    private ActivityChangePasswordBinding binding;

    //TAG for logs in logcat I
    private static final String TAG = "CHANGE_PASS_TAG";

   //Firebase Auth for auth related tosks
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init view binding... activity_change_password.xml ActivityChangePasswordBinding
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get instance of firebase auth for Auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //init/setup ProgressDialog to show while changing password
        progressDialog = new ProgressDialog( this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside (false);

       //handle toolbarBackBen click, go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
                }
            });
                //handle submitBtn click, validate data to start password change
                binding.submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validateData();
                    }
                });
            }

            private String currentPassword = "";
            private String newPassword = "";
            private String confirmNewPassword = "";

            private void validateData() {
                Log.d(TAG, "validateData: ");

                //input dato
                currentPassword = binding.currentPasswordEt.getText().toString();
                newPassword = binding.newPasswordEt.getText().toString();
                confirmNewPassword = binding.confirmNewPasswordEt.getText().toString();

                Log.d(TAG, "validateData: currentPassword: " + currentPassword);
                Log.d(TAG, "validateData: newPassword: " + newPassword);
                Log.d(TAG, "validateData: confirmNewPassword: " + confirmNewPassword);

                //volidate data
                if (currentPassword.isEmpty()) {
                    //Current Password Field (currentPasswordEt) is empty, show error in currentPasswordEt
                    binding.currentPasswordEt.setError("Enter current password!");
                    binding.currentPasswordEt.requestFocus();
                } else if (newPassword.isEmpty()) {
                    //New Password Field (newPosseordet) is empty, show error in newPosswordEt
                    binding.newPasswordEt.setError("Enter new password!");
                    binding.newPasswordEt.requestFocus();
                } else if (confirmNewPassword.isEmpty()) {
                    //Confirm New Password Field (confireNewPasswordEt) is empty, show error in confiraNewPasswordEt
                    binding.confirmNewPasswordEt.setError("Enter Confirm Password!");
                    binding.confirmNewPasswordEt.requestFocus();
                } else if (!newPassword.equals(confirmNewPassword)) {
                    //passeard in neallessmordet & confiraneaPasseordft doesn't match, show error in confiraNeePasswordEt
                    binding.confirmNewPasswordEt.setError("Password doesn't match!");
                    binding.confirmNewPasswordEt.requestFocus();
                } else {
                    //all date is validated, verify current password is correct first before updating password
                    authenticateUserForUpdatePassword();
                }
            }

            private void authenticateUserForUpdatePassword() {
                Log.d(TAG, "authenticateUserForUpdatePassword: ");

                //show progress
                progressDialog.setMessage("Authenticating User");
                progressDialog.show();

                //before changing password re-outhenticate the user to check if the user hos entered correct current possword
                AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
                firebaseUser.reauthenticate(authCredential)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //successfully authenticated, begin update
                                updatePassword();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: ", e);
                                progressDialog.dismiss();
                                Utils.toast(ChangePasswordActivity.this, "Failed to authenticate due to " + e.getMessage());


                            }
                        });
            }

            private void updatePassword() {
                Log.e(TAG, "updatePassword: ");

                //show progress
                progressDialog.setMessage("Updating Password");
                progressDialog.show();

                //begin update password, pass the new password as parameter
                firebaseUser.updatePassword(newPassword)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //password update success, you may de Logout and move to login activity if you wanτ
                                progressDialog.dismiss();
                                Utils.toast(ChangePasswordActivity.this, "Password Updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //password update failure, show error messege
                                Log.e(TAG, "onFailure:");
                                progressDialog.dismiss();
                                Utils.toast(ChangePasswordActivity.this, "Failed to update password due to " + e.getMessage());
                            }
                        });
            }
        }
