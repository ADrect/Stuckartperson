package com.example.stuckart;

import android.content.Context;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.example.stuckart.databinding.FragmentMyAdsAdsBinding;

import java.util.ArrayList;

public class MyAdsAdsFragment extends Fragment {
    //View Binding
    private FragmentMyAdsAdsBinding binding;
    //TAS to show logs in logcat
    private static final String TAG = "MY ADS_TAG";
    //Context for this fragment class
    private Context mContext;
    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;
    //adArrayList to hold ads list by currently logged-in user to show in RecyclerView
    private ArrayList<ModelAd> adArrayList;
    //AdopterAd class instance to set to Recyclerview to show Ads list
    private AdapterAd adapterAd;

    public MyAdsAdsFragment() {
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
        // Inflate/bind the Layout (fragment_my_ads_ods.xat) for this fragment
        binding = FragmentMyAdsAdsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Firebase Auth for auth related tasks

        firebaseAuth = FirebaseAuth.getInstance();
        //function call to load ads by currently logged-in users
        loadAds();

        //add text change listener to searchEt to search ads using filter applied in AdapterAd class
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
        Log.d(TAG, "loadAds: ");
        //init adArrayList before starting adding date into it
        adArrayList = new ArrayList<>();

        //Firebase 08 listener to load ads by currently logged in user. i.e Show only those ads whose key uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear odArrayList each time starting adding date into it
                        adArrayList.clear();
                        //load ads list
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            try {
                                //Prepare ModelAd with all data from Firebase DB
                                ModelAd modelAd = ds.getValue(ModelAd.class);
                                adArrayList.add(modelAd);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                        //init/setup AdapterAd class and set to recyclerview "
                        adapterAd = new AdapterAd(mContext, adArrayList);
                        binding.adsRv.setAdapter(adapterAd);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}

