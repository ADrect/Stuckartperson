package com.example.stuckart;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stuckart.databinding.RowImagesPickedBinding;

import java.util.ArrayList;
public class AdapterImagesPicked extends RecyclerView.Adapter<AdapterImagesPicked.HolderImagesPicked> {

    //View Binding
    private RowImagesPickedBinding binding;
    //Tag to show logs in logcat
    private static final String TAG = "IMAGES_TAG";

    //Context of activity/fragment from where instance of AdapterImagesPicked class is created
    private Context context;

    //imagePickedArrayList The list of the images picked/captured from Gallery/Camera or from Internet
    private ArrayList<ModelImagePicked> imagePickedArrayList;

    public AdapterImagesPicked(Context context, ArrayList<ModelImagePicked> imagePickedArrayList) {
        this.context = context;
        this.imagePickedArrayList = imagePickedArrayList;
    }

    @NonNull
    @Override
    public HolderImagesPicked onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
    //inflate/bind the row images picked.xml
    binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false);

    return new HolderImagesPicked(binding.getRoot());
    }

    @Override
    public void onBindViewHolder (@NonNull HolderImagesPicked holder, int position) {
    //get data from particular position of list and set to the UI Views of row images picked.aal and Handle clicks
    ModelImagePicked model = imagePickedArrayList.get(position);
    //get uri of the image to set in imageIv
    Uri imageUri = model.getImageUri();

    Log.d(TAG, "onBindViewHolder: imageUri: "+imageUri);

    //set the image in imageIv
    try{
        Glide.with(context)
               .load(imageUri)
               .placeholder(R.drawable.ic_image_gray)
               .into(holder.imageIv);
        } catch (Exception e) {
            Log.e(TAG,  "onBindViewHolder: ", e);
        }

        // handle closeBtn click, remove image from imagePickedArrayList
        holder.closeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            imagePickedArrayList.remove(model);
            notifyItemRemoved(position);
        }
    });
}

    @Override
    public int getItemCount() {
        return imagePickedArrayList.size(); //return the size of list
    }

    /** View holder class to hold/init UI Views of the row_images_picked.xml*/
    class HolderImagesPicked extends RecyclerView.ViewHolder{

         //UI Views of the row_images_picked.xml
         ImageView imageIv;
         ImageButton closeBtn;
         public HolderImagesPicked(@NonNull View itemView) {
             super(itemView);

             //init UI Views of the row_images_picked.xml
             imageIv = binding.imageIv;
             closeBtn = binding.closeBtn;
         }
    }
}

