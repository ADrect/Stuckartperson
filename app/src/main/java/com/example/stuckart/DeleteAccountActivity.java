package com.example.stuckart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.stuckart.databinding.ActivityDeleteAccountBinding;

public class DeleteAccountActivity extends AppCompatActivity {

    //View Binding
    private ActivityDeleteAccountBinding binding;
    //TAG for logs in logcat
    private static final String TAG = "DELETE_ACCOUNT_TAG";
    //ProgressDialog to show while sending password recovery instructions
    private ProgressDialog progressDialog;
    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;
    //FirebaseUser to get current user and delete
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init view binding... activity_delete_account.xml = ActivityDeleteAccountBinding
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init/setup ProgressDialog to show while changing password
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //get instance of FireboseAuth for Auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();
        //get instance of FirebaseUser to get current user and delete
        firebaseUser = firebaseAuth.getCurrentUser();

        //handle toolbarBackBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle submitBtn click, stort account deletion
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {
        Log.d(TAG, "deleteAccount: ");

        String myUid = firebaseAuth.getUid();

        //shew progress
        progressDialog.setMessage("Deleting User Account");
        progressDialog.show();

        //Step 1: Delete User Account
        firebaseUser.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //User account deleted
                        Log.d(TAG, "onSuccess: Account deleted");

                        progressDialog.setMessage("Deleting User Ads");

                        //Step 2: Remove User Ads, Currently We have not worked on Ads, Ads will be saved in 08 > Ads > Adid. each Ad will contain uid of the owner
                        DatabaseReference refUserAds = FirebaseDatabase.getInstance().getReference("Ads");
                        refUserAds.orderByChild("uid").equalTo(myUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //there maybe multiple ads by user need to loop
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            //delete Ad
                                            ds.getRef().removeValue();
                                        }

                                        progressDialog.setMessage("Deleting User Data");
                                        //Step 2: Remove User Date, DB Users UserId
                                        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
                                        refUsers.child(myUid)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        //User data deleted
                                                        Log.d(TAG, "onSuccess: User data deleted...");
                                                        startMainActivity();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //failed deleting user data
                                                        Log.e(TAG, "onFailure: failed deleting user data: " + e.getMessage());
                                                        progressDialog.dismiss();
                                                        Utils.toast(DeleteAccountActivity.this, "Failed to delete user data due to " + e.getMessage());
                                                        startMainActivity();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        //failed deleting user ads
                                    }
                                });
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to delete user account, maybe user need to re-login for authentication purpose before deleting
                            Log.e(TAG, "onFailure: ", e);
                            progressDialog.dismiss();
                            Utils.toast(DeleteAccountActivity.this, "Failed to delete account due to " + e.getMessage());
                        }
                    });

    }

    private void startMainActivity() {
        Log.d(TAG, "startMainActivity: ");

        startActivity(new Intent(this, MainActivity.class));

        finishAffinity(); //clear back-stack of activities
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed(); //go back -- Additional Added
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity(); //clear back-stack of activities
    }
}
