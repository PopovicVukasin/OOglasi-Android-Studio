package com.example.OOglasi.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.OOglasi.R;
import com.example.OOglasi.databinding.RowImageSliderBinding;
import com.example.OOglasi.models.ModelImageSlider;

import java.util.ArrayList;

public class AdapterImageSlider extends RecyclerView.Adapter<AdapterImageSlider.HolderImageSlider> {

    
    private RowImageSliderBinding binding;


    private static final String TAG = "IMAGE_SLIDER_TAG";

    
    private Context context;

    
    private ArrayList<ModelImageSlider> imageSliderArrayList;

    /**
     * Constructor*
     *
     * @param context     The context of activity/fragment from where instance of AdapterAd class is created *
     * @param imageSliderArrayList The list of images
     */
    public AdapterImageSlider(Context context, ArrayList<ModelImageSlider> imageSliderArrayList) {
        this.context = context;
        this.imageSliderArrayList = imageSliderArrayList;
    }

    @NonNull
    @Override
    public HolderImageSlider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        
        binding = RowImageSliderBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderImageSlider(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImageSlider holder, int position) {
        
        ModelImageSlider modelImageSlider = imageSliderArrayList.get(position);

        
        String imageUrl = modelImageSlider.getImageUrl();
        
        String imageCount = (position + 1) +"/" +imageSliderArrayList.size();

        
        holder.imageCountTv.setText(imageCount);
        
        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv);
        } catch (Exception e){
            Log.e(TAG, "onBindViewHolder: ", e);
        }

        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        
        return imageSliderArrayList.size();
    }


    class HolderImageSlider extends RecyclerView.ViewHolder{

        
        ShapeableImageView imageIv;
        TextView imageCountTv;

        public HolderImageSlider(@NonNull View itemView) {
            super(itemView);

            
            imageIv = binding.imageIv;
            imageCountTv = binding.imageCountTv;
        }
    }
}
