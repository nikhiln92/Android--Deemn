package com.auxidos.offers.customers;

/**
 * Created by VX83 on 10/4/2018.
 */

public class NotificationData {
    private String title;
    private String message;
    private String iconUrl;
    private String action;
    private String actionDestination;
    private String chatData;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    String getActionDestination() {
        return actionDestination;
    }
    public void setActionDestination(String actionDestination) {
        this.actionDestination = actionDestination;
    }

    String getChatData() {
        return chatData;
    }
    public void setChatData(String chatData) {
        this.chatData = chatData;
    }
}