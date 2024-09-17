package com.example.OOglasi.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.OOglasi.R;
import com.example.OOglasi.Utils;
import com.example.OOglasi.adapters.AdapterImageSlider;
import com.example.OOglasi.databinding.ActivityAdDetailsBinding;
import com.example.OOglasi.models.ModelAd;
import com.example.OOglasi.models.ModelImageSlider;

import java.util.ArrayList;
import java.util.HashMap;

public class AdDetailsActivity extends AppCompatActivity {

    
    private ActivityAdDetailsBinding binding;

    
    private static final String TAG = "AD_DETAILS_TAG";

    
    private FirebaseAuth firebaseAuth;

    
    private String adId = "";
    
    private double adLatitude = 0;
    private double adLongitude = 0;
    
    private String sellerUid = null;
    private String sellerPhone = "";

    
    private boolean favorite = false;

    
    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        
        binding.toolbarEditBtn.setVisibility(View.GONE);
        binding.toolbarDeleteBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);


        
        adId = getIntent().getStringExtra("adId");

        Log.d(TAG, "onCreate: adId: "+adId);

        
        firebaseAuth = FirebaseAuth.getInstance();

        
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }


        loadAdDetails();
        loadAdImages();

        
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        
        binding.toolbarDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(AdDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Obrisi oglas")
                        .setMessage("Da li ste sigurni da želite da izbrišete ovaj oglas?")
                        .setPositiveButton("OBRISI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                                deleteAd();
                            }
                        })
                        .setNegativeButton("ODUSTANI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        
        binding.toolbarEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOptions();
            }
        });

        
        binding.toolbarFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favorite) {
                    
                    Utils.removeFromFavorite(AdDetailsActivity.this, adId);
                } else {
                    
                    Utils.addToFavorite(AdDetailsActivity.this, adId);
                }
            }
        });

        
        binding.sellerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdDetailsActivity.this, AdSellerProfileActivity.class);
                intent.putExtra("sellerUid", sellerUid);
                startActivity(intent);
            }
        });


        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdDetailsActivity.this, ChatActivity.class);
                intent.putExtra("receiptUid", sellerUid);
                startActivity(intent);
            }
        });

        
        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.callIntent(AdDetailsActivity.this, sellerPhone);
            }
        });

    }

    private void editOptions(){
        Log.d(TAG, "editOptions: ");
        
        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarEditBtn);
        
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Izmeni");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Oznacite kao prodato");
        
        popupMenu.show();
        
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) { 
                int itemId = item.getItemId();

                if (itemId == 0){
                    
                    Intent intent = new Intent(AdDetailsActivity.this, AdCreateActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("adId", adId);
                    startActivity(intent);
                } else if (itemId == 1){
                    
                    showMarkAsSoldDialog();
                }

                return true;
            }
        });
    }

    private void showMarkAsSoldDialog(){
        
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Oznacite kao prodato")
                .setMessage("Da li ste sigurni da želite da označite ovaj oglas kao prodat?")
                .setPositiveButton("SOLD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Sold Clicked...");

                        
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", ""+ Utils.AD_STATUS_SOLD);

                        
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                        ref.child(adId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        
                                        Log.d(TAG, "onSuccess: Prodato");
                                        Utils.toast(AdDetailsActivity.this, "Prodato");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        
                                        Log.e(TAG, "onFailure: ", e);
                                        Utils.toast(AdDetailsActivity.this, "Neuspesno "+e.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel Clicked...");
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ");
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            
                            ModelAd modelAd = snapshot.getValue(ModelAd.class);

                            
                            sellerUid = modelAd.getUid();
                            String title = modelAd.getTitle();
                            String description = modelAd.getDescription();
                            String address = modelAd.getAddress();
                            String condition = modelAd.getCondition();
                            String category = modelAd.getCategory();
                            String price = modelAd.getPrice();
                            adLatitude = modelAd.getLatitude();
                            adLongitude = modelAd.getLongitude();
                            long timestamp = modelAd.getTimestamp();

                            
                            String formattedDate = Utils.formatTimestampDate(timestamp);

                            
                            if (sellerUid.equals(firebaseAuth.getUid())){
                                
                                
                                binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                                binding.toolbarDeleteBtn.setVisibility(View.VISIBLE);
                                
                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);

                                binding.sellerProfileLabelTv.setVisibility(View.GONE);
                                binding.sellerProfileCv.setVisibility(View.GONE);
                            } else {
                                
                                
                                binding.toolbarEditBtn.setVisibility(View.GONE);
                                binding.toolbarDeleteBtn.setVisibility(View.GONE);
                                
                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);

                                binding.sellerProfileLabelTv.setVisibility(View.VISIBLE);
                                binding.sellerProfileCv.setVisibility(View.VISIBLE);
                            }

                            
                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.conditionTv.setText(condition);
                            binding.categoryTv.setText(category);
                            binding.priceTv.setText(price);
                            binding.dateTv.setText(formattedDate);

                            
                            loadSellerDetails();


                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadSellerDetails(){
        Log.d(TAG, "loadSellerDetails: ");
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        
                        String phoneCode = ""+ snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+ snapshot.child("phoneNumber").getValue();
                        String name = ""+ snapshot.child("name").getValue();
                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();
                        long timestamp = (Long) snapshot.child("timestamp").getValue();
                        
                        String formattedDate = Utils.formatTimestampDate(timestamp);
                        
                        sellerPhone = phoneCode +""+phoneNumber;

                        
                        binding.sellerNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);
                        try {
                            Glide.with(AdDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.sellerProfileIv);
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: ");
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        
                        favorite = snapshot.exists();

                        Log.d(TAG, "onDataChange: favorite: "+favorite);
                        
                        if (favorite){
                            
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadAdImages(){
        Log.d(TAG, "loadAdImages: ");

        
        imageSliderArrayList = new ArrayList<>();

        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        
                        imageSliderArrayList.clear();
                        
                        for (DataSnapshot ds: snapshot.getChildren()){
                            
                            ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);
                            
                            imageSliderArrayList.add(modelImageSlider);
                        }
                        
                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(AdDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void deleteAd(){
        Log.d(TAG, "deleteAd: ");

        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        
                        Log.d(TAG, "onSuccess: Deleted");
                        Utils.toast(AdDetailsActivity.this, "Obrisano");
                        
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(AdDetailsActivity.this, "Neuspesno "+e.getMessage());
                    }
                });
    }
}