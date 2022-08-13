package com.serkantken.ametist.models;

import java.io.Serializable;

public class NotificationModel implements Serializable
{
    String notifId, userId, username, profilePic, notifMessage;
    long notifDate;
    boolean isRead;

    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(String notifId) {
        this.notifId = notifId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getNotifMessage() {
        return notifMessage;
    }

    public void setNotifMessage(String notifMessage) {
        this.notifMessage = notifMessage;
    }

    public long getNotifDate() {
        return notifDate;
    }

    public void setNotifDate(long notifDate) {
        this.notifDate = notifDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
