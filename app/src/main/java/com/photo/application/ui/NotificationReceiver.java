package com.photo.application.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;


public class NotificationReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri selectedUri = intent.getData();
        /*Intent serviceIntent = new Intent(context, MyForegroundService.class);
        serviceIntent.putExtra("URI", selectedUri.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        }*/

        Intent serviceIntent = new Intent ( context, MyForegroundService.class );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService ( serviceIntent );
        } else {
            context.startService ( serviceIntent );
        }
        // Create a notification manager
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // For Android Oreo and above, you need to create a notification channel
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence channelName = "My Channel";
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
//            notificationChannel.setDescription("Channel description");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.enableVibration(true);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        // Retrieve the URI from the intent
//        Uri selectedUri = intent.getData();
//
//        Toast.makeText(context, "Selected URL: " + selectedUri.toString(), Toast.LENGTH_SHORT).show();
//
//        Intent intent1 = new Intent(context, FolderContentActivity.class);
//        intent1.putExtra("folderUri", "content://com.android.externalstorage.documents/tree/primary%3ADownload%2FDooFlix");
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//                .setSmallIcon(android.R.drawable.ic_dialog_info)
//                .setContentTitle("Open Folder")
//                .setContentText("Tap to view folder contents.")
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent);
//
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
