package com.serkantken.ametist.utilities;

import com.serkantken.ametist.models.MessageModel;

public interface MessageListener
{
    void onMessageReplied(MessageModel messageModel, String profilePic, boolean isPhoto);
}
