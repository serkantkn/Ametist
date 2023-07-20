package com.serkantken.ametist.utilities;

import android.content.Context;

import com.serkantken.ametist.R;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants
{
    public static final String IS_BALLOONS_SHOWED = "balloons_showed";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String DATABASE_PATH_USERS = "Users";
    public static final String DATABASE_PATH_CHATS = "chats";
    public static final String DATABASE_PATH_CONVERSATIONS = "conversations";
    public static final String DATABASE_PATH_POSTS = "Posts";
    public static final String DATABASE_PATH_FOLLOWERS = "followers";
    public static final String DATABASE_PATH_FOLLOWINGS = "followings";
    public static final String DATABASE_PATH_NOTIFICATIONS = "notifications";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String MESSAGE_TYPE_TEXT = "1";
    public static final String MESSAGE_TYPE_FOLLOW = "2";
    public static final String MESSAGE_TYPE_PHOTO = "3";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String TOKEN = "token";
    public static final String MESSAGE = "message";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders()
    {
        if (remoteMsgHeaders == null)
        {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAukvREhQ:APA91bH1HxjP-l054LDvF_XPoMPBWaoc2jkas6hGFfvX_YdnPnlT0CrDT7xZdFfKT6T0NrN5yx-cj8ocVx5uqnK-z9FXN5S3_af34WAMN_0kkuajmUwAJya9pTq2gvEvGC2flJlUk4y4"
            );
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
        }
        return remoteMsgHeaders;
    }
}
