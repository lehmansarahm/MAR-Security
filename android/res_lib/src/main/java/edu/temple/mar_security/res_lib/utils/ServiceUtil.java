package edu.temple.mar_security.res_lib.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;

import edu.temple.mar_security.res_lib.R;

public class ServiceUtil {

    public static void sendMessageToService(Messenger destMessenger, Messenger sourceMessenger, int messageCode, Bundle data) {
        Message msg = Message.obtain(null, messageCode, 0, 0);
        msg.replyTo = sourceMessenger;
        msg.setData(data);

        try {
            destMessenger.send(msg);
        } catch (NullPointerException ex) {
            Log.e(Constants.LOG_TAG, "Cannot send message to destination messenger if it is null!  Have you started the GTC Annotator app???", ex);
        } catch (RemoteException ex) {
            Log.e(Constants.LOG_TAG, "Failed to send message to Messenger service.", ex);
        }
    }

    public static Notification createNotification(Context context, String contentTitle,
                                                  String contentText, PendingIntent pendingIntent) {
        Log.d(Constants.LOG_TAG, "Creating new service notification.");

        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            NotificationChannel defaultChannel = notificationManager.getNotificationChannel(ResourcePropUtil.CHANNEL_ID(context));
            if (defaultChannel == null) {
                Log.d(Constants.LOG_TAG, "Notification channel for GTC Services does not exist.  Creating new channel.");
                NotificationChannel channel = new NotificationChannel(ResourcePropUtil.CHANNEL_ID(context),
                        ResourcePropUtil.CHANNEL_NAME(context), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(ResourcePropUtil.CHANNEL_DESCRIPTION(context));

                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(channel);
            }
            else Log.d(Constants.LOG_TAG, "Notification channel for GTC Services already exists.  Using existing channel.");
        }

        // Generate the notification
        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context, ResourcePropUtil.CHANNEL_ID(context))
                .setSmallIcon(R.drawable.ic_action_info)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (pendingIntent != null) notiBuilder.setContentIntent(pendingIntent);
        Notification notification = notiBuilder.build();

        // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
        return notification;
    }

    public static boolean isServiceRunning(Context context, String serviceClassName) {
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (serviceClassName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}