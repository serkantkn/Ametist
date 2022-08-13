package com.serkantken.ametist.utilities;

import java.util.HashMap;

public class Constants
{
    public static final String PREFERENCES = "preferences";
    public static final String IS_BALLOONS_SHOWED = "balloons_showed";
    public static final String PREF_YES = "1";
    public static final String PREF_NO = "0";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

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
