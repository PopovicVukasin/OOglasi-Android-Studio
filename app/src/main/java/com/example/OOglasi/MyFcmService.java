package com.example.OOglasi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.OOglasi.activities.ChatActivity;

import java.util.Random;

public class MyFcmService extends FirebaseMessagingService {
    
    private static final String  TAG  =  "FCM_SERVICE_TAG";
    
    private static final String  ADMIN_CHANNEL_ID  =  "ADMIN_CHANNEL_ID";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        String title = "" + remoteMessage.getNotification().getTitle();
        String body = "" + remoteMessage.getNotification().getBody();
        String senderUid = "" + remoteMessage.getData().get("senderUid");
        String notificationType = "" + remoteMessage.getData().get("notificationType");

        Log.d(TAG, "onMessageReceived: title: "+title);
        Log.d(TAG, "onMessageReceived: body: "+body);
        Log.d(TAG, "onMessageReceived: senderUid: "+senderUid);
        Log.d(TAG, "onMessageReceived: notificationType: "+notificationType);
        
        showChatNotification(title, body, senderUid);
    }


    private void showChatNotification(String notificationTitle, String notificationDescription, String senderUid){
        
        int notificationId = new Random().nextInt(3000);

        
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        setupNotificationChannel(notificationManager);

        
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiptUid", senderUid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void setupNotificationChannel(NotificationManager notificationManager){
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    ADMIN_CHANNEL_ID,
                    "CHAT_CHANNEL",
                    NotificationManager.IMPORTANCE_HIGH
            );

            notificationChannel.setDescription("Show Chat Notifications.");
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
