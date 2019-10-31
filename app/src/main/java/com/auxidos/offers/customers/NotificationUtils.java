package com.auxidos.offers.customers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class NotificationUtils
{
    private static final int NOTIFICATION_ID = 200;
    private static final String CHANNEL_ID = "NotificationDeemnChannel";
    private static final String URL = "url";
    private static final String ACTIVITY = "activity";
    private Map<String, Class> activityMap = new HashMap<>();
    private Context mContext;
    SessionManager session;

    NotificationUtils(Context mContext)
    {
        this.mContext = mContext;
        session = new SessionManager(mContext);
        activityMap.put("MainActivity", MainActivity.class);
    }

    void displayNotification(NotificationData NotificationData, Intent resultIntent)
    {
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + mContext.getPackageName() + "/" + R.raw.notification);

        String message = NotificationData.getMessage();
        String title = NotificationData.getTitle();
        String action = NotificationData.getAction();
        String destination = NotificationData.getActionDestination();
        final int icon = R.drawable.notification;
        PendingIntent resultPendingIntent;

        if (URL.equals(action))
        {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(destination));
            resultPendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        }
        else if (ACTIVITY.equals(action) && activityMap.containsKey(destination))
        {
            int flag = PendingIntent.FLAG_CANCEL_CURRENT;
            resultIntent = new Intent(mContext, activityMap.get(destination));

            if(destination.equalsIgnoreCase("ChatActivity"))
            {
                String chatData = NotificationData.getChatData();
                try
                {
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    JSONObject data = new JSONObject(chatData);
                    if(data.has("id"))
                        resultIntent.putExtra("id", data.getString("id"));
                    resultIntent.putExtra("name", data.getString("name"));
                    resultIntent.putExtra("email1", session.getEmail());
                    if(data.has("email2"))
                        resultIntent.putExtra("email2", data.getString("email1"));
                    if(data.has("messageKey"))
                        resultIntent.putExtra("messageKey", data.getString("messageKey"));
                    if(data.has("source"))
                        resultIntent.putExtra("source", data.getString("source"));
                    if(data.has("group"))
                        resultIntent.putExtra("group", data.getString("group"));
                    flag = PendingIntent.FLAG_UPDATE_CURRENT;
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            resultIntent,
                            flag
                    );
        }
        else
        {
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );
        }

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Deemn");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(null);
            notificationChannel.setSound(uri, att);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID);
        Notification notification;
        notification = mBuilder.setSmallIcon(icon)
                .setOnlyAlertOnce(false)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setContentTitle(title)
                .setColorized(true)
                .setContentIntent(resultPendingIntent)
                .setSound(uri)
                .setVibrate(null)
                .setSmallIcon(R.drawable.notification)
                .setContentText(message)
                .setChannelId(CHANNEL_ID)
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}