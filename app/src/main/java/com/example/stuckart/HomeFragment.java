package com.example.stuckart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.stuckart.databinding.FragmentHomeBinding;

import java.security.Key;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private static final String TAG = "HOME_TAG";
    //View Binding
    private static final int MAX_DISTANCE_TO_LOAD_ADS_KM = 10;
    //Context for this fragment class
    private Context mContext;
    //odArrayList to hold ads list to show in RecyclerView
    private ArrayList<ModelAd> adArrayList;
    //AdapterAd class instance to set to Recyclerview to show Ads List
    private AdapterAd adapterAd;

    //ShoredPreferences to store the selected location from map to load ads nearby
    private SharedPreferences locationSp;
    //Location info required to load ads nearby. We will get this info from the Shares
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";

    @Override
    public void onAttach(@NonNull Context context) {//get and init the context for this fragment class
        mContext = context;
        super.onAttach(context);
    }
    public HomeFragment() {

    }
    // Required empty public constructor
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate/bind the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container,false);

        return binding.getRoot();

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //init the shared preferences param 1 is none of the Shared Preferences file, param 2 is node of the Shar
        locationSp = mContext.getSharedPreferences("LOCATION_SP",Context.MODE_PRIVATE);
        //get saved current latitude, longitude, address from the Shared Preferences. In next steps we will pick
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "");

        //if current location is not @ 1.e. location is picked
        if (currentLatitude !=0.0 && currentLongitude !=0.0){
            binding.locationTv.setText(currentAddress);

        }
        //function call, load categories
        loadCategories();
        //function call, load oll ofs
        loadAds("All");


        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG,"onTextChanged: Query: "+s);

                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG,"onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.locationCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, LocationPickerActivity.class);
                locationPickerActivityResult.launch(intent);
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //check if from map, location is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG,"onActivityResult: RESULT OK");

                        Intent data = result.getData();

                        if (data != null) {
                            Log.d(TAG,"onActivityResult: Location picked");
                            //get location Info from intent
                            currentLatitude = data.getDoubleExtra("latitude", 0.0);
                            currentLongitude = data.getDoubleExtra("longitude", 0.0);
                            currentAddress = data.getStringExtra("address");
                            //save location info to shared preferences so when we launch opp next time we don't need to pick again
                            locationSp.edit()
                                    .putFloat("CURRENT_LATITUDE", Float.parseFloat(""+currentLatitude))
                                    .putFloat("CURRENT_LONGITUDE", Float.parseFloat(""+currentLongitude))
                                    .putString("CURRENT_ADDRESS", currentAddress)
                                    .apply();
                            //set the picked address
                            binding.locationTv.setText(currentAddress);
                            //after picking address reload all ads again based on newly picked location
                            loadAds("All");
                        }
                    } else {
                        Log.d(TAG,"ohActivityResult: Cancelled!");
                        Utils.toast(mContext,"Cancelled!");
                    }
                }
            }
    );

    private void loadCategories(){
        //init category ArrayList
        ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();
        //ModelCategory instance to show all products
        ModelCategory modelCategoryAll = new ModelCategory("All", R.drawable.ic_category_all);
        categoryArrayList.add(modelCategoryAll);

        //get categories fren Utils class and add in category Arraylist
        for (int i=0; i<Utils.categories.length; i++) {
            //ModelCategory instance to get/hold category from current index
            ModelCategory modelCategory = new ModelCategory(Utils.categories[i], Utils.categoryIcons[i]);
            //odd modelCategory to categoryArrayList
            categoryArrayList.add(modelCategory);
        }

        //init/setup AdapterCategory
        AdapterCategory adapterCategory = new AdapterCategory(mContext, categoryArrayList, new RvListenerCategory() {
            @Override
            public void onCategoryClick(ModelCategory modelCategory) {

                loadAds(modelCategory.getCategory());
            }
        });
        //set adapter to the Recyclerview 1.e. categoriesRv
        binding.categoriesRv.setAdapter(adapterCategory);
    }

    private void loadAds(String category){
        Log.d(TAG,"loadAds: Category: "+category);
        //init odArraylist before starting adding data into it
        adArrayList = new ArrayList<>();

        //Firebase DB Listener to lead ads based on Category & Distance
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.addValueEventListener(new ValueEventListener() {

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        //clear odArraylist each time starting adding data into it
        adArrayList.clear();
        //load ods list
        for (DataSnapshot ds: snapshot.getChildren()) {
            //Prepare ModelAd with all data from Firebase Da
            ModelAd modelAd = ds.getValue(ModelAd.class);
            //function call with returned value as distance in kilometer.
            double distance = calculateDistanceKm(modelAd.getLatitude(), modelAd.getLongitude());
            Log.d(TAG,"onDataChange: distance:" +distance);
            //filter
            if (category.equals("All")) {
                //Category All is selected, non check distance if is <= required e.g. 10ka then show
                if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM) {
                    //the distance is required e.g. 10km. Add to list
                    adArrayList.add(modelAd);
                        }
                    } else{
                        //Som category is selected e.g. Furniture
                        if (modelAd.getCategory().equals(category)) {
                            if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM) {
                                //the distance is <= required e.g. 10kn. Add to List
                                adArrayList.add(modelAd);
                            }
                        }
                    }
                }

                adapterAd = new AdapterAd(mContext, adArrayList);
                binding.adsRv.setAdapter(adapterAd);
            }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private double calculateDistanceKm(double adLatitude, double adLongitude) {
        Log.d(TAG,"calculateDistancekm: currentLatitude: "+currentLatitude);
        Log.d(TAG,"calculateDistancekm: currentLongitude:"+currentLongitude);
        Log.d(TAG,"calculateDistancekm: adLatitude: "+adLatitude);
        Log.d(TAG,"calculateDistancekm: adLongitude: "+adLongitude);

        //Source Location i.e. user's current location
        Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
        startPoint.setLatitude (currentLatitude);
        startPoint.setLongitude(currentLongitude);

        //Destination Location i.e. Ad's location
        Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
        endPoint.setLatitude(adLatitude);
        endPoint.setLongitude(adLongitude);

        //calculate distance
        double distanceInMeters = startPoint.distanceTo(endPoint);
        double distanceInkm = distanceInMeters / 1000; //e.g. 1km 1000m so kn = m/1000

        return distanceInkm;

    }
}


