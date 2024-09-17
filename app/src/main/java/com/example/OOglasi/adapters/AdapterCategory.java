package com.example.OOglasi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.example.OOglasi.RvListenerCategory;
import com.example.OOglasi.databinding.RowCategoryBinding;
import com.example.OOglasi.models.ModelCategory;

import java.util.ArrayList;
import java.util.Random;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> {

    
    private RowCategoryBinding binding;
    
    private Context context;
    
    private ArrayList<ModelCategory> categoryArrayList;
    
    private RvListenerCategory rvListenerCategory;

    /**Constructor*
     * @param context The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param categoryArrayList The list of categories
     * @param rvListenerCategory instance of the RvListenerCategory interface*/
    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList, RvListenerCategory rvListenerCategory) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.rvListenerCategory = rvListenerCategory;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        
        ModelCategory modelCategory = categoryArrayList.get(position);

        
        String category = modelCategory.getCategory();
        int icon = modelCategory.getIcon();

        
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));

        
        holder.categoryIconIv.setImageResource(icon);
        holder.categoryTitleTv.setText(category);
        holder.categoryIconIv.setBackgroundColor(color);

        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvListenerCategory.onCategoryClick(modelCategory);
            }
        });


    }

    @Override
    public int getItemCount() {
        
        return categoryArrayList.size();
    }


    class HolderCategory extends RecyclerView.ViewHolder{

        
        ShapeableImageView categoryIconIv;
        TextView categoryTitleTv;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            
            categoryIconIv = binding.categoryIconIv;
            categoryTitleTv = binding.categoryTitleTv;
        }
    }
}
