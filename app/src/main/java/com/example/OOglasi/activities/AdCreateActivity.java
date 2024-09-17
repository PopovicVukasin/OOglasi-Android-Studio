package com.example.OOglasi.activities;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.OOglasi.R;
import com.example.OOglasi.Utils;
import com.example.OOglasi.adapters.AdapterImagesPicked;
import com.example.OOglasi.databinding.ActivityAdCreateBinding;
import com.example.OOglasi.models.ModelImagePicked;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdCreateActivity extends AppCompatActivity {

    
    private ActivityAdCreateBinding binding;
    
    private static final String TAG = "AD_CREATE_TAG";
    
    private ProgressDialog progressDialog;
    
    private FirebaseAuth firebaseAuth;

    
    private Uri imageUri = null;

    
    private ArrayList<ModelImagePicked> imagePickedArrayList;

    private AdapterImagesPicked adapterImagesPicked;

    private boolean isEditMode = false;
    private String adIdForEditing = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityAdCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Molimo sacekajte...");
        progressDialog.setCanceledOnTouchOutside(false);

        
        firebaseAuth = FirebaseAuth.getInstance();

        
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoryAct.setAdapter(adapterCategories);

        
        ArrayAdapter<String> adapterConditions = new ArrayAdapter<>(this, R.layout.row_condition_act, Utils.conditions);
        binding.conditionAct.setAdapter(adapterConditions);

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        Log.d(TAG, "onCreate: isEditMode: "+isEditMode);

        if (isEditMode){
            
            adIdForEditing = intent.getStringExtra("adId");

            
            loadAdDetails();

            
            binding.toolbarTitleTv.setText("Izmeni oglas");
            binding.postAdBtn.setText("Izmeni oglas");
        } else {
            
            binding.toolbarTitleTv.setText("Objavi oglas");
            binding.postAdBtn.setText("Objavi oglas");
        }

        
        imagePickedArrayList = new ArrayList<>();
        
        loadImages();

        
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        
        binding.toolbarAddImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });

        
        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    
                    if (result.getResultCode() == Activity.RESULT_OK){

                        Intent data = result.getData();

                        if (data != null){
                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            address = data.getStringExtra("address");

                            Log.d(TAG, "onActivityResult: latitude: "+latitude);
                            Log.d(TAG, "onActivityResult: longitude: "+longitude);
                            Log.d(TAG, "onActivityResult: address: "+address);

                            binding.locationAct.setText(address);
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Utils.toast(AdCreateActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void loadImages(){
        Log.d(TAG, "loadImages: ");
        
        adapterImagesPicked = new AdapterImagesPicked(this, imagePickedArrayList, adIdForEditing);
        
        binding.imagesRv.setAdapter(adapterImagesPicked);
    }

    private void showImagePickOptions(){
        Log.d(TAG, "showImagePickOptions: ");
        
        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarAddImageBtn);

        
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        
        popupMenu.show();
        
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                
                int itemId = item.getItemId();
                
                if (itemId == 1){
                    

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        
                        String[] cameraPermissions = new String[]{Manifest.permission.CAMERA};
                        requestCameraPermissions.launch(cameraPermissions);
                    } else {
                        
                        String[] cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermissions.launch(cameraPermissions);
                    }

                } else if (itemId == 2) {
                    

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        
                        pickImageGallery();
                    } else {
                        
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
                    
                    if (isGranted){
                        
                        pickImageGallery();
                    } else{
                        
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

                    
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()){

                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted){
                        
                        pickImageCamera();
                    } else {
                        
                        Utils.toast(AdCreateActivity.this, "Camera or Storage or both permissions denied...");
                    }
                }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");
        
        Intent intent = new Intent(Intent.ACTION_PICK);
        
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");
        
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMPORARY_IMAGE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMPORARY_IMAGE_DESCRIPTION");
        
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        
                        Intent data = result.getData();
                        
                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: imageUri: "+ imageUri);
                        
                        String timestamp = ""+ Utils.getTimestamp();

                        
                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false); 
                        imagePickedArrayList.add(modelImagePicked);

                        
                        loadImages();
                    } else {
                        
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
                    
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        

                        Log.d(TAG, "onActivityResult: imageUri: "+ imageUri);
                        
                        String timestamp = ""+ Utils.getTimestamp();

                        
                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false); 
                        imagePickedArrayList.add(modelImagePicked);

                        
                        loadImages();
                    } else {
                        
                        Utils.toast(AdCreateActivity.this, "Cancelled...!");
                    }
                }
            }
    );

    
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
        
        brand = binding.brandEt.getText().toString().trim();
        category = binding.categoryAct.getText().toString().trim();
        condition = binding.conditionAct.getText().toString().trim();
        address = binding.locationAct.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if (brand.isEmpty()){
            
            binding.brandEt.setError("Unesite brend");
            binding.brandEt.requestFocus();
        } else if (category.isEmpty()) {
            
            binding.categoryAct.setError("Odaberite kategoriju");
            binding.categoryAct.requestFocus();
        } else if (condition.isEmpty()) {
            
            binding.conditionAct.setError("Odaberite stanje");
            binding.conditionAct.requestFocus();
        } /*else if (address.isEmpty()) {
            
            binding.locationAct.setError("Choose Location");
            binding.locationAct.requestFocus();
        }*/ else if (title.isEmpty()) {
            
            binding.titleEt.setError("Unesite naziv");
            binding.titleEt.requestFocus();
        } else if (description.isEmpty()) {
            
            binding.descriptionEt.setError("Unesite opis");
            binding.descriptionEt.requestFocus();
        } else if (imagePickedArrayList.isEmpty()) {

            Utils.toast(this, "Odaberite bar jednu sliku");
        } else {
            
            if (isEditMode){
                updateAd();
            } else {
                postAd();
            }
        }
    }

    private void postAd(){
        Log.d(TAG, "postAd: ");
        
        progressDialog.setMessage("Objavi oglas");
        progressDialog.show();

        
        long timestamp = Utils.getTimestamp();
        
        DatabaseReference refAds = FirebaseDatabase.getInstance().getReference("Ads");
        
        String keyId = refAds.push().getKey();

        
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
        hashMap.put("longitude", longitude);
        
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


    private void updateAd(){
        Log.d(TAG, "updateAd: ");

        progressDialog.setMessage("Updating Ad...");
        progressDialog.show();

        
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("brand", ""+ brand);
        hashMap.put("category", ""+ category);
        hashMap.put("condition", ""+ condition);
        hashMap.put("address", ""+ address);
        hashMap.put("price", ""+ price);
        hashMap.put("title", ""+ title);
        hashMap.put("description", ""+ description);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adIdForEditing)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        
                        progressDialog.dismiss();
                        
                        uploadImagesStorage(adIdForEditing);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(AdCreateActivity.this, "Failed to update Ad due to "+e.getMessage());
                    }
                });
    }

    private void uploadImagesStorage(String adId){
        Log.d(TAG, "uploadImagesStorage: ");
        
        for (int i=0; i<imagePickedArrayList.size(); i++){
            
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);

            
            if (!modelImagePicked.getFromInternet()){

                
                String imageName = modelImagePicked.getId();
                
                String filePathAndName = "Ads/"+imageName;

                int imageIndexForProgress = i+1;

                
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

                storageReference.putFile(modelImagePicked.getImageUri())
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                
                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                
                                String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + " images...\nProgress " + (int) progress + "%";
                                Log.d(TAG, "onProgress: message: "+message);
                                
                                progressDialog.setMessage(message);
                                progressDialog.show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "onSuccess: ");
                                
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());
                                Uri uploadedImageUrl = uriTask.getResult();

                                if (uriTask.isSuccessful()){

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("id", ""+modelImagePicked.getId());
                                    hashMap.put("imageUrl", ""+uploadedImageUrl);

                                    
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
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: ", e);
                                progressDialog.dismiss();
                            }
                        });
            }

        }
    }

    private void loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ");
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adIdForEditing)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        
                        String brand = ""+ snapshot.child("brand").getValue();
                        String category = ""+ snapshot.child("category").getValue();
                        String condition = ""+ snapshot.child("condition").getValue();
                        latitude = (Double) snapshot.child("latitude").getValue();
                        longitude = (Double) snapshot.child("longitude").getValue();
                        String address = ""+ snapshot.child("address").getValue();
                        String price = ""+ snapshot.child("price").getValue();
                        String title = ""+ snapshot.child("title").getValue();
                        String description = ""+ snapshot.child("description").getValue();

                        
                        binding.brandEt.setText(brand);
                        binding.categoryAct.setText(category);
                        binding.conditionAct.setText(condition);
                        binding.locationAct.setText(address);
                        binding.priceEt.setText(price);
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        
                        DatabaseReference refImages = snapshot.child("Images").getRef();
                        refImages.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String id = ""+ ds.child("id").getValue();
                                    String imageUrl = ""+ ds.child("imageUrl").getValue();


                                    ModelImagePicked modelImagePicked = new ModelImagePicked(id, null, imageUrl, true);
                                    imagePickedArrayList.add(modelImagePicked);
                                }

                                loadImages();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}