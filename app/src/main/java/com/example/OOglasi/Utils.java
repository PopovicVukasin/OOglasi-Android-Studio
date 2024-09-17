package com.example.OOglasi;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/*A class that will contain static functions, constants, variables that we will be used in whole application*/
public class Utils {

    public static final String MESSAGE_TYPE_TEXT = "TEXT";
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";

    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_SOLD = "SOLD";

    public static final String NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE";

    public static final String FCM_SERVER_KEY = "AAAAw_XBpjM:APA91bFLIeLnS7X7UqWDj845UjOraTGUk0ryG_FfDulKN_SgTjhweepdjQzj6ve6jJmpROG5Dx_73AoRKLbtvG4DqJCb6niu7tPGlaLzJDnpViB2nowlgk-TI_vWPniRx0CNuqRQ-6Qn";


    public static final String[] categories = {
            "Telefoni",
            "Racunari",
            "Elektronika i kucni aparati",
            "Vozila",
            "Namestaj i dekoracija",
            "Odeca",
            "Knjige",
            "Sport",
            "Zivotinje",
            "Biznis",
            "Poljoprivreda"
    };


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


    public static final String[] conditions = {"Novo", "Korisceno", "Kao novo", "Neispravno"};

    /** A Function to show Toast
    @param context the context of activity/fragment from where this function will be called
    @param message the message to be shown in the Toast */
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /** A Function to get current timestamp
    @return Return the current timestamp as long datatype
    */
    public static long getTimestamp(){
        return System.currentTimeMillis();
    }

    /** A Function to show Toast
     @param timestamp the timestamp of type Long that we need to format to dd/MM/yyyy
     @return timestamp formatted to date dd/MM/yyyy*/
    public static String formatTimestampDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }


    /** A Function to show Toast
     @param timestamp the timestamp of type Long that we need to format to dd/MM/yyyy hh:mm:a
     @return timestamp formatted to date dd/MM/yyyy hh:mm:a*/
    public static String formatTimestampDateTime(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString();

        return date;
    }



    /**
     * Add the add to favorite
     *
     * @param context the context of activity/fragment from where this function will be called
     * @param adId    the Id of the add to be added to favorite of current user
     */
    public static void addToFavorite(Context context, String adId){


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){

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

                            Utils.toast(context, "Dodato u omiljeno...!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Utils.toast(context, "Failed to add to favorite due to "+e.getMessage());
                        }
                    });
        }
    }

    /**
     * Remove the add from favorite
     *
     * @param context the context of activity/fragment from where this function will be called
     * @param adId    the Id of the add to be removed from favorite of current user
     */
    public static void removeFromFavorite(Context context, String adId){


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){

            Utils.toast(context, "You're not logged in!");
        } else {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Utils.toast(context, "Uklonjeno");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Utils.toast(context, "Neuspeno "+e.getMessage());
                        }
                    });
        }
    }

    /**
     * Generate Chat Path
     * This will generate chat path by sorting these UIDs and concatenate sorted array of UIDs having _ in between
     * All messages of these 2  users will be saved in this path
     *
     * @param receiptUid The UID of the receipt
     * @param yourUid    The UID of the current logged-in user
     */
    public static String chatPath(String receiptUid, String yourUid){

        String[] arrayUids  = new String[]{receiptUid, yourUid};

        Arrays.sort(arrayUids);

        String chatPath = arrayUids[0] + "_" + arrayUids[1];

        return chatPath;
    }

    /**
     * Launch Call Intent with phone number
     *
     * @param context the context of activity/fragment from where this function will be called
     * @param phone   the phone number that will be opened in call intent
     */
    public static void callIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phone)));
        context.startActivity(intent);
    }

    /**
     * Launch Sms Intent with phone number
     *
     * @param context the context of activity/fragment from where this function will be called
     * @param phone   the phone number that will be opened in sms intent
     */
    public static void smsIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Uri.encode(phone)));
        context.startActivity(intent);
    }

    /**
     * Launch Google Map with input location
     *
     * @param context the context of activity/fragment from where this function will be called
     * @param latitude the latitude of the location to be shown in google map
     * @param longitude the longitude of the location to be shown in google map
     */
    public static void mapIntent(Context context, double latitude, double longitude){



        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude +","+longitude);


        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null){

            context.startActivity(mapIntent);
        } else {

            Utils.toast(context, "Google MAP Not installed!");
        }
    }
}