package com.example.stuckart;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.stuckart.databinding.ActivityAdCreateBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdCreateActivity extends AppCompatActivity {

    //View Binding
    private ActivityAdCreateBinding binding;
    //TAG for logs in logcat
    private static final String TAG = "AD_CREATE_TAG";
    //ProgressDialog to show while adding/updating the Ad
    private ProgressDialog progressDialog;
    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;
    //Image Uri to hold uri of the image (picked/captured using Gallery/Camera) to add in Ad Images List
    private Uri imageUri = null;
    //List of images (picked/captured using Gallery/Camera or from internet)
    private ArrayList<ModelImagePicked> imagePickedArrayList;
    //Adapter to show images in RecyclerView
    private AdapterImagesPicked adapterImagesPicked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding... activity.od_create.xml ActivityAdCreateBinding
        binding = ActivityAdCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init/setup ProgressDialog to show while adding/updating the Ad
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        //Setup and set the categories adapter to the Category Input Filed i.e. cotegoryAct
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoryAct.setAdapter(adapterCategories);

        //Setup and set the conditions adopter to the Condition Input Filed 1.e. conditionAct
        ArrayAdapter<String> adapterConditions = new ArrayAdapter<>(this, R.layout.row_condition_act, Utils.conditions);
        binding.conditionAct.setAdapter(adapterConditions);

        //init imagePickedArrayList
        imagePickedArrayList = new ArrayList<>();
        //LoadImages
        loadImages();

        //handle toolbarBockBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle toolbarAddImageBtn click, show image odd options (Gallery/Camera)
        binding.toolbarAddImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });
        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start LocationPickerActivity to pick location //
                Intent intent = new Intent(AdCreateActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });
        binding.postAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.d(TAG, "onActivityResult: ");
               //get result of location picked from LocationPickerActivity
                if (result.getResultCode() == Activity.RESULT_OK) {

                    Intent data = result.getData();

                    if (data != null){
                        latitude = data.getDoubleExtra( "latitude",  0.0);
                        longitude = data.getDoubleExtra(  "longitude",  0.8);
                        address = data.getStringExtra(  "address");

                        Log.d(TAG,  "onActivityResult: latitude: "+latitude);
                        Log.d(TAG,  "onActivityResult: longitude: "+longitude);
                        Log.d(TAG, "onActivityResult: address: "+address);

                        binding.locationAct.setText(address);
                    }
                } else {
                    Log.d(TAG, "onActivityResult: cancelled");
                    Utils.toast( AdCreateActivity.this,  "Cancelled");
                }
            }
        }
    );
    private void loadImages(){
        Log.d(TAG,  "LoadImages: ");
        //init setup adapterImagesPicked to set it RecyclerView 1.e. imagesRv. Param 1 is Context, Poran 2 is adapter Images Picked new
        adapterImagesPicked = new AdapterImagesPicked(this, imagePickedArrayList);
        //set the adopter to the RecyclerView i.e. ImagesRv
        binding.imagesRv.setAdapter(adapterImagesPicked);
    }
    private void showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions: ");
        //init the Popupmenu. Param 1 is context. Param 2 is Anchor view for this popup. The popup will appear Popupmenu
        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarAddImageBtn);

        //add menu items to our popup menu Paramel is GroupID, Param#2 is ItemID, Paran#3 is Order10, Param
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        //Show Popup Menu
        popupMenu.show();
        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the id of the iten clicked in popup menu
                int itemId = item.getItemId();
                //check which itam id is clicked from popup menu. 1-Camaro. 2 Gallery as en defined
                if (itemId == 1) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is TIRAMISU on coove. Be only need Camera permission
                        String[] cameraPermissions = new String[]{Manifest.permission.CAMERA};
                        requestCameraPermissions.launch(cameraPermissions);
                    } else {
                        //Device version is below TIRAMISU. Be need Camera & Storage permissions
                        String[] cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermissions.launch(cameraPermissions);
                    }
                } else if (itemId == 2) {
                    //Gallery is clicked we need to check if we have permission of Storage befers Launching Gallery to Pick image

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is TIRAMISU or above. He can't need Storage permission to Launch Gallery
                        pickImageGallery();
                    } else {
                        //Device version is below TIRAMISU. We teed Storoge permission to launch Gallery
                        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        requestStoragePermission.launch(storagePermission);
                    }
                }
                return true;
            }
        });
    }

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: "+isGranted);
                    //Let's check if permission is granted or not
                    if (isGranted) {
                        //Storage Permission granted, we can now Launch gallery to pick image
                        pickImageGallery();
                    } else{
                        //Storage Permission denied, we can't launch gallery to pick image
                        Utils.toast(AdCreateActivity.this, "Storage Permission denied...");
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: ");
                    Log.d(TAG, "onActivityResult: "+ result.toString());

                    //Let's check if all permissions are granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {

                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        //ALL Permissions Camera, Storage are granted, se con now launch camera to capture image
                        pickImageCamera();
                    } else {
                        //Camera or Storage ar Both permissions are denied, Can't Launch comero to capture Looge
                        Utils.toast(AdCreateActivity.this, "Camera or Storage or both permissions denied...");
                    }
                }
            }
    );
    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");
        //Intent to Launch Image Picker e.g. Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //Be only want to pick images
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }
    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");
        //Setup Content values, Mediastore to capture high quality image using camera intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMPORARY_IMAGE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMPORARY_IMAGE_DESCRIPTION");
        //uri of the image to be captured from camera
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        //Intent to launch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>(){
                @Override
                public void onActivityResult(ActivityResult result){
                Log.d(TAG, "onActivityResult: ");
                //Check if smugs is picked or not

                if (result.getResultCode() == Activity.RESULT_OK) {
                    //gat date from result paras
                    Intent data = result.getData();
                    //get image uri
                    imageUri = data.getData();

                    //get uri of image picked imageüri data.getData();
                    Log.d(TAG, "onActivityResult: imageUri: "+ imageUri);
                    //timestomp will be used as id of the inoge picked
                    String timestamp = ""+ Utils.getTimestamp();

                    //setup model far image. Param 1 is id, Paran 2 is inageüri, Poran 3 is InugeUrl, fromInternet
                    ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false); //
                    imagePickedArrayList.add(modelImagePicked);

                    //reload the images
                    loadImages();
                } else {
                    //Cancelled
                    Utils.toast(AdCreateActivity.this, "Cancelled...!");
                }
            }
        }
    );

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    //Check if Laage is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //no need to get image uri nars wilt have it in pickImageConere() function

                        Log.d(TAG, "onActivityResult: imageUri: "+ imageUri);
                        //timestomp will be used as id of the image picked
                        String timestamp = ""+ Utils.getTimestamp();

                        //setup model for leege. Porom 1 is id, Purse 2 is imageüri, Peras 3 is imageürt, frominternat
                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false); //edd de
                        imagePickedArrayList.add(modelImagePicked);

                        //reload the Images
                        loadImages();
                    } else {
                        //Cancelled
                        Utils.toast( AdCreateActivity.this,  "Cancelled...!");
                   }
               }
           }
    );

        //variables to hold Ad data
        private String brand = "";
        private String category = "";
        private String condition = "";
        private String address = "";
        private String price = "";
        private String title = "";
        private String description = "";
        private double latitude = 0;
        private double longitude = 0;
        private void validateData(){
            Log.d(TAG, "validateData: ");
            //input dato
            brand = binding.brandEt.getText().toString().trim();
            category = binding.categoryAct.getText().toString().trim();
            condition = binding.conditionAct.getText().toString().trim();
            address = binding.locationAct.getText().toString().trim();
            price = binding.priceEt.getText().toString().trim();
            title = binding.titleEt.getText().toString().trim();
            description = binding.descriptionEt.getText().toString().trim();

            if (brand.isEmpty()) {
                //no brand entered in brandEt, show error in brandEt and focus
                binding.brandEt.setError("Enter Brand");
                binding.brandEt.requestFocus();
            } else if (category.isEmpty()) {
                //no categoryAct entered in categoryAct, show error in categoryAct and focus
                binding.categoryAct.setError("Choose Category");
                binding.categoryAct.requestFocus();
            } else if (condition.isEmpty()) {
                //na conditionAct entered in conditionAct, show error in conditionAct and focus
                binding.conditionAct.setError("Choose Condition");
                binding.conditionAct.requestFocus();
            } /*else if (address.isEmpty()) {
                //no locationAct entered in locationAct, show error in locationAct and focus
                binding.locationAct.setError("Choose Location");
                binding.locationAct.requestFocus();
            } */ else if (title.isEmpty()) {
                //no titleEt entered in titleEt, show error in titleEt and focus
                binding.titleEt.setError("Enter Title");
                binding.titleEt.requestFocus();
            } else if (description.isEmpty()) {
                //no descriptionEt entered in descriptionEt, show error in descriptionEt and focus
                binding.descriptionEt.setError("Enter Description");
                binding.descriptionEt.requestFocus();
            } else if (imagePickedArrayList.isEmpty()) {

                Utils.toast(this, "Pick at-least one image");
            } else {
                //All data is validated, we con proceed further new
                postAd();
            }
        }

        private void postAd(){
            Log.d(TAG, "postAd: ");
            //shew progress
            progressDialog.setMessage("Publishing Ad");
            progressDialog.show();

            //get current timestamp
            long timestamp = Utils.getTimestamp();
            //firebase database Ads reference to store nea Ads
            DatabaseReference refAds = FirebaseDatabase.getInstance().getReference("Ads");
            //key id from the reference to use as Ad id
            String keyId = refAds.push().getKey();

            // setup data to add in firebase database
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", ""+ keyId);
            hashMap.put("uid", ""+ firebaseAuth.getUid());
            hashMap.put("brand", ""+ brand);
            hashMap.put("category", ""+ category);
            hashMap.put("condition", ""+ condition);
            hashMap.put("address", ""+ address);
            hashMap.put("price", ""+ price);
            hashMap.put("title", ""+ title);
            hashMap.put("description", ""+ description);
            hashMap.put("status", ""+ Utils.AD_STATUS_AVAILABLE);
            hashMap.put("timestamp", timestamp);
            hashMap.put("latitude", latitude);
            hashMap.put("Longitude", longitude);
            //set date to firebase database. Ads AdIdAdDataJSON
            refAds.child(keyId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: Ad Published");

                            uploadImagesStorage(keyId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                            progressDialog.dismiss();
                            Utils.toast(AdCreateActivity.this, "Failed to publish Ad due to "+e.getMessage());
                        }
                    });
        }

        private void uploadImagesStorage(String adId){
            Log.d(TAG,"uploadImagesStorage: ");
            //there ore multiple images in image PickedArraylist, loop to upload all
            for (int i=0; i<imagePickedArrayList.size(); i++){
                //get model from the current position of the imagePickedArrayList M
                ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);
                //for name of the image in firebase storage
                String imageName = modelImagePicked.getId();
                //path and name of the image in firebase storage
                String filePathAndName = "Ads/"+imageName;

                int imageIndexForProgress = i+1;

                //Storage reference with filePathAndName
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

                storageReference.putFile(modelImagePicked.getImageUri())
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress (@NonNull UploadTask.TaskSnapshot snapshot) {
                                //calculate the current progress of the image being uploaded
                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                //setup progress dialog message on basis of current progress. e.g. Uploading 1 of 10 images... Progress 95% /
                                String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + " images...\nProgress " + (int) progress + "%";
                                Log.d(TAG, "onProgress: message: "+message);
                                //show progress
                                progressDialog.setMessage(message);
                                progressDialog.show();

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG,"onSuccess: ");
                                //image uploaded get url of uploaded image
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());
                                Uri uploadedImageUrl = uriTask.getResult();

                                if (uriTask.isSuccessful()) {

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("id", ""+modelImagePicked.getId());
                                    hashMap.put("imageUrl", ""+uploadedImageUrl);

                                    //add in firebase db. Ads -> AdId Images -> ImageId > ImageData
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                                    ref.child(adId).child("Images")
                                            .child(imageName)
                                            .updateChildren(hashMap);
                                }
                                progressDialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure (@NonNull Exception e) {
                                Log.e(TAG,  "onFailure: ", e);
                                progressDialog.dismiss();
                    }
                });
        }
    }
}