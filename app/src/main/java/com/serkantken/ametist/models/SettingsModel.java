package com.serkantken.ametist.models;

import java.io.Serializable;

public class SettingsModel implements Serializable
{
    boolean showMessageOnNotif, showFollowernameOnNotif;

    public SettingsModel() {
    }

    public boolean isShowMessageOnNotif() {
        return showMessageOnNotif;
    }

    public void setShowMessageOnNotif(boolean showMessageOnNotif) {
        this.showMessageOnNotif = showMessageOnNotif;
    }

    public boolean isShowFollowernameOnNotif() {
        return showFollowernameOnNotif;
    }

    public void setShowFollowernameOnNotif(boolean showFollowernameOnNotif) {
        this.showFollowernameOnNotif = showFollowernameOnNotif;
    }
}
