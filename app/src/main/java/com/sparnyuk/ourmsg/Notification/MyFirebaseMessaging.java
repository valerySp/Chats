package com.sparnyuk.ourmsg.Notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sparnyuk.ourmsg.MessageActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        SharedPreferences preferences=getSharedPreferences("PREFS", MODE_PRIVATE);
        String curUSer=preferences.getString("currentuser","none");
        String user=message.getData().get("user");

        String sented=message.getData().get("sented");
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null&&sented.equals(firebaseUser.getUid())){
            if (!curUSer.equals(user)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                sendOreNot(message);
            } else {
          sendNotification(message);
            }
            }
        }
    }

    private void sendOreNot(RemoteMessage message){
        String user=message.getData().get("user");
        String icon=message.getData().get("icon");
        String title=message.getData().get("title");
        String body=message.getData().get("body");

        RemoteMessage.Notification notification=message.getNotification();
        int j=Integer.parseInt(user.replaceAll("\\D",""));
        Intent intent=new Intent(this, MessageActivity.class);
        Bundle bundle =new Bundle();
        bundle.putString("userid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this, j, intent,PendingIntent.FLAG_IMMUTABLE );
        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNot oreoNot=new OreoNot(this);
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = oreoNot.getOreoNotification(title,body,pendingIntent,defaultSound,icon);
        }


        int i=0;
        if (j>0){
            i=j;
        }
        oreoNot.getManager().notify(i, builder.build());

    }

    private void sendNotification(RemoteMessage message) {
        String user=message.getData().get("user");
        String icon=message.getData().get("icon");
        String title=message.getData().get("title");
        String body=message.getData().get("body");

        RemoteMessage.Notification notification=message.getNotification();
        int j=Integer.parseInt(user.replaceAll("\\D",""));
        Intent intent=new Intent(this, MessageActivity.class);
        Bundle bundle =new Bundle();
        bundle.putString("userid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent=PendingIntent.getActivity(this, j, intent,PendingIntent.FLAG_ONE_SHOT );

        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i=0;
        if (j>0){
            i=j;
        }
        noti.notify(i, builder.build());
    }
}
/* sendNotification вставить обратно код

        String user=message.getData().get("user");
        String icon=message.getData().get("icon");
        String title=message.getData().get("title");
        String body=message.getData().get("body");

        RemoteMessage.Notification notification=message.getNotification();
        int j=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, MessageActivity.class);
        Bundle bundle =new Bundle();
        bundle.putString("userid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this, j, intent,PendingIntent.FLAG_ONE_SHOT );

        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i=0;
        if (j>0){
            i=j;
        }
        noti.notify(i, builder.build());
*
* super.onMessageReceived(remoteMessage);

        Map<String, String> data_notify = remoteMessage.getData();

//      String sented = remoteMessage.getData().get("sented");
        String user = remoteMessage.getData().get("user");

        SharedPreferences preferences  = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentuser = preferences.getString("currentuser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && data_notify.size() > 0) {
            if (!currentuser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage);
                } else {
                    sendNotification(remoteMessage);
                }
            }
        }*/