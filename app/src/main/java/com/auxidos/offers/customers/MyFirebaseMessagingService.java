package com.auxidos.offers.customers;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String ACTION = "action";
    private static final String USER = "user";
    private static final String ACTION_DESTINATION = "action_destination";
    private static final String CHAT_DATA = "chatData";
    SessionManager session;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        session = new SessionManager(this);
        session.setToken(s);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0)
            handleData(remoteMessage.getData());
        else if (remoteMessage.getNotification() != null)
            handleNotification(remoteMessage.getNotification());
    }
    private void handleNotification(RemoteMessage.Notification RemoteMsgNotification)
    {
        String message = RemoteMsgNotification.getBody();
        String title = RemoteMsgNotification.getTitle();
        NotificationData NotificationData = new NotificationData();
        NotificationData.setTitle(title);
        NotificationData.setMessage(message);

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(NotificationData, resultIntent);
    }
    private void handleData(Map<String, String> data)
    {
        session = new SessionManager(this);
        String title = data.get(TITLE);
        String message = data.get(MESSAGE);
        String action = data.get(ACTION);
        String user = data.get(USER);
        String actionDestination = data.get(ACTION_DESTINATION);
        String chatData = data.get(CHAT_DATA);
        if(user.equalsIgnoreCase(session.getEmail()))
        {
            NotificationData NotificationData = new NotificationData();
            NotificationData.setTitle(title);
            NotificationData.setMessage(message);
            NotificationData.setAction(action);
            NotificationData.setChatData(chatData);
            NotificationData.setActionDestination(actionDestination);

            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.displayNotification(NotificationData, resultIntent);
        }
    }
}