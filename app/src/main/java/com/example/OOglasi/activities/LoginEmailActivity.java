package com.example.OOglasi.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.example.OOglasi.Utils;
import com.example.OOglasi.databinding.ActivityLoginEmailBinding;

public class LoginEmailActivity extends AppCompatActivity {


    private ActivityLoginEmailBinding binding;


    private static final String TAG = "LOGIN_TAG";
    
    private ProgressDialog progressDialog;

    
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        
        firebaseAuth = FirebaseAuth.getInstance();

        
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, RegisterEmailActivity.class));
            }
        });

        


        
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email, password;

    private void validateData() {

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();

        Log.d(TAG, "validateData: email: " + email);
        Log.d(TAG, "validateData: password: " + password);


        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.emailEt.setError("Invalid Email");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {

            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        } else {

            loginUser();
        }

    }


    private void loginUser(){
        
        progressDialog.setMessage("Logging In");
        progressDialog.show();

        
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        
                        Log.d(TAG, "onSuccess: Logged In...");
                        progressDialog.dismiss();

                        
                        startActivity(new Intent(LoginEmailActivity.this, MainActivity.class));
                        finishAffinity(); 
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(LoginEmailActivity.this, "Failed due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }
}