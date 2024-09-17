package com.example.OOglasi.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.OOglasi.R;
import com.example.OOglasi.Utils;
import com.example.OOglasi.activities.DeleteAccountActivity;
import com.example.OOglasi.activities.MainActivity;
import com.example.OOglasi.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    private static final String TAG = "ACCOUNT_TAG";

    
    private FirebaseAuth firebaseAuth;

    
    private Context mContext;


    private ProgressDialog progressDialog;

    @Override
    public void onAttach(@NonNull Context context) {
        
        mContext = context;
        super.onAttach(context);
    }

    public AccountFragment() {
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentAccountBinding.inflate(LayoutInflater.from(mContext), container, false);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Molimo sacekajte");
        progressDialog.setCanceledOnTouchOutside(false);

        
        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        
        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                firebaseAuth.signOut();
                
                startActivity(new Intent(mContext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });



        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(mContext, DeleteAccountActivity.class));
                getActivity().finishAffinity();
            }
        });

    }

    private void loadMyInfo(){
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        
                        String dob = ""+ snapshot.child("dob").getValue();
                        String email = ""+ snapshot.child("email").getValue();
                        String name = ""+ snapshot.child("name").getValue();
                        String phoneCode = ""+ snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+ snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();
                        String timestamp = ""+ snapshot.child("timestamp").getValue();
                        String userType = ""+ snapshot.child("userType").getValue();

                        
                        String phone = phoneCode + phoneNumber;

                        
                        if (timestamp.equals("null")){
                            timestamp = "0";
                        }

                        
                        String formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));

                        
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.dobTv.setText(dob);
                        binding.phoneTv.setText(phone);
                        binding.memberSinceTv.setText(formattedDate);

                        
                        if (userType.equals("Email")){
                            
                            boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                            if (isVerified){
                                binding.verificationTv.setText("Verifikovan");
                            }
                            else{

                                binding.verificationTv.setText("Nije verifikovan");
                            }
                        }
                        else {
                            

                            binding.verificationTv.setText("Verifikovan");
                        }

                        try {
                            
                            Glide.with(mContext)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void verifyAccount(){
        Log.d(TAG, "verifyAccount: ");
        
        progressDialog.setMessage("Slanje uputstava za verifikaciju naloga na vašu e-poštu");
        progressDialog.show();

        
        firebaseAuth.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        
                        Log.d(TAG, "onSuccess: Sent");
                        progressDialog.dismiss();
                        Utils.toast(mContext, "Slanje uputstava za verifikaciju naloga na vašu e-poštu");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(mContext, "Failed due to "+e.getMessage());
                    }
                });
    }
}