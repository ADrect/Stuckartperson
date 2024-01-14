package com.example.stuckart;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.stuckart.databinding.RowAdBinding;

import java.util.ArrayList;

public class AdapterAd extends RecyclerView.Adapter<AdapterAd.HolderAd> implements Filterable {
    //View Binding
    private RowAdBinding binding;
    private static final String TAG = "ADAPTER_AD_TAG";
    //Firebase Auth for auth related tosks
    private FirebaseAuth firebaseAuth;
    //Context of activity/fragment from where instance of AdapterAd class is created
    private Context context;
    //odArrayList The list of the Ads
    public ArrayList<ModelAd> adArrayList;
    private ArrayList<ModelAd> filterList;
    private FilterAd filter;

    /**
     * Constructor
     *
     * @param context     The context of activity/fragment from where instance of AdapterAd class is created
     * @param adArrayList The list of Ads
     */

    public AdapterAd(Context context, ArrayList<ModelAd> adArrayList) {
        this.context = context;
        this.adArrayList = adArrayList;
        this.filterList = adArrayList;

        //get instance of firebase auth for Auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAd onCreateViewHolder(@NonNull ViewGroup parent, int viewTуре) {
        //inflate/bind the row od.xal
        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderAd(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAd holder, int position) {
        //get data from particular position of list and set to the UI Views of row od.xml ond Handle clicks
        ModelAd modelAd = adArrayList.get(position);

        String title = modelAd.getTitle();
        String description = modelAd.getDescription();
        String address = modelAd.getAddress();
        String condition = modelAd.getCondition();
        String price = modelAd.getPrice();
        Long timestamp = modelAd.getTimestamp();
        String formattedDate = Utils.formatTimestampDate(timestamp);
        //function colt: load first image from available images of Ad e.g. if there are 5 images of Ad, load first one
        loadAdFirstImage(modelAd, holder);

        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite(modelAd, holder);

        }

        //set data to UI Views of r ow_ad, xnl
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.addressTv.setText(address);
        holder.conditionTv.setText(condition);
        holder.priceTv.setText(price);
        holder.dateTv.setText(formattedDate);

        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean favorite = modelAd.isFavourite();
                if (favorite){
                    Utils.removeFromFavorite(context, modelAd.getId());
                } else {
                    Utils.addToFavorite(context, modelAd.getId());
                }
            }
        });
    }

    private void checkIsFavorite(ModelAd modelAd, HolderAd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean favorite = snapshot.exists();
                        modelAd.setFavourite(favorite);

                        if(favorite){

                            holder.favBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            holder.favBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        }

    private void loadAdFirstImage(ModelAd modelAd, HolderAd holder) {
        Log.d(TAG, "loadAdFirstImage: ");
        //Load first image from available images of Ad e.g. if there are 5 images of Ad, load first one
        // Ad id to get image of it
        String adId = modelAd.getId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
        reference.child(adId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //this will return only 1 image as we have used query LimitToFirst(1)
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            //get url of the image
                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: imageUrl: " + imageUrl);
                            //set image to Image Vew 1.e. imageIv
                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_image_gray)
                                        .into(holder.imageIv);
                            } catch (Exception e) {
                                Log.e(TAG,"onDataChange:", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        //return the size of list
        return adArrayList.size();
    }

    @Override
    public Filter getFilter() {
        //init the filter obf only if it is null
        if (filter == null) {
            filter = new FilterAd(this, filterList);
        }
        return filter;
    }
    class HolderAd extends RecyclerView.ViewHolder {
        //UI Views of the row_ad.xml
        ShapeableImageView imageIv;
        TextView titleTv, descriptionTv, addressTv, conditionTv, priceTv, dateTv;
        ImageButton favBtn;
        public HolderAd(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_ad.xml
            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            favBtn = binding.favBtn;
            addressTv = binding.addressTv;
            conditionTv = binding.conditionTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;
        }
    }
}

