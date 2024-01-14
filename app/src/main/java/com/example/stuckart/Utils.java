package com.example.stuckart;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/*A class that will contain static functions, constants, variables that we will be used in sole applications*/
public class Utils {
    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_SOLD = "SOLD";

    //Categories array of the Ads
    public static final String[] categories = {
            "Mobiles",
            "Computer/Laptop",
            "Electronics & Home Appliances",
            "Vehicles",
            "Furniture & Home Decor",
            "Fashion & Beauty",
            "Books",
            "Sports",
            "Animals",
            "Businesses",
            "Agriculture"
    };

    //Categories icon array of Ads
    public static final int[] categoryIcons = {
            R.drawable.ic_category_mobiles,
            R.drawable.ic_category_computer,
            R.drawable.ic_category_electronics,
            R.drawable.ic_category_vehicles,
            R.drawable.ic_category_furniture,
            R.drawable.ic_category_fashion,
            R.drawable.ic_category_books,
            R.drawable.ic_category_sports,
            R.drawable.ic_category_animals,
            R.drawable.ic_category_business,
            R.drawable.ic_category_agriculture
    };

    //Conditions array of the Add
    public static final String[] conditions = {"New", "Used", "Refurbished"};


    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestampDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        ;

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    /*

     */

    public static void addToFavorite(Context context, String adId) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {

            Utils.toast(context, "You're not logged in!");
        } else {

            long timestamp = Utils.getTimestamp();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("adId", adId);
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)

                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Added to favorite...!");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Failed to add to favorite due to " + e.getMessage());
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String adId) {
        //we con add only if user is logged in
        // 1) Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            //net logged in, can't remove from favorite /
            Utils.toast(context, "You're not logged in!");
        } else {
            //Logged in, can remove from favorite //Remove data from do. Users uid Favorites > adid

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Removed from favorite");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Failed
                            Utils.toast(context, "Failed to remove fron favorite due to "+e.getMessage());
                        }

                    });
        }
    }
}

