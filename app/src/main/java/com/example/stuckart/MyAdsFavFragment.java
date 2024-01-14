package com.example.stuckart;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Handler;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.stuckart.databinding.FragmentMyAdsFavBinding;

import java.util.ArrayList;

public class MyAdsFavFragment extends Fragment {

    //View Binding
    private FragmentMyAdsFavBinding binding;

    //TAG to show logs in logcat
    private static final String TAG = "FAV TAG";

    //Context for this fragment class
    private Context mContext;

    //Firebose Auth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //odArrayList to hold ads list added to favorite by currently logged-in user to st
    private ArrayList<ModelAd> adArrayList;

    //AdapterAd class instance to set to Recyclerview to show Ads list
    private AdapterAd adapterAd;

    public MyAdsFavFragment() {
        // Required empty public constructor

    }

    @Override
    public void onAttach(@NonNull Context context) {
        //get and init the context for this fragment class

        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate/bind the layout fragment_my_ads_fav.xml) for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();
        //function call to load ads by currently logged-in users
        loadAds();

        //add text change Listener to searchEt to search ods using filter opplied in AdapterAd class
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //this function is called whenever user type a letter, search based on what user typed
                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadAds() {
        Log.d(TAG, "LoadAds: ");
        //init adArraylist before starting adding date into it
        adArrayList = new ArrayList<>();
        //Firebase DB Listener to get the ids of the Ads added to favorite by currently logged-in user. e.
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users");
        favRef.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear odArraylist each time starting adding date into it
                        adArrayList.clear();

                        //Loud favorite Ad ida
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get the id of the Ad. e.g. Users uid > Favorites > adId

                            String adId = "" + ds.child("adId").getValue();
                            Log.d(TAG, "onDataChange: adId: " + adId);

                            //Firebase DB Listener to load Ad details based on id of the Ad we just got
                            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference("Ads");
                            adRef.child(adId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            try {
                                                //Prepare ModelAd with all doto from Firebase DB
                                                ModelAd modelAd = snapshot.getValue(ModelAd.class);
                                                //add prepared model to adArrayList
                                                adArrayList.add(modelAd);
                                            } catch (Exception e) {
                                                Log.e(TAG, "onDataChange:", e);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }

                        //sometimes fav ads were not loading due to nested db listeners because we
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //init/setup Adapteråd class and set to recyclerview
                                adapterAd = new AdapterAd(mContext, adArrayList);
                                binding.adsRv.setAdapter(adapterAd);

                            }
                        }, 500);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}





