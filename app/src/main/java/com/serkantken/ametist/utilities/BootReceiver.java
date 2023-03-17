package com.serkantken.ametist.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.serkantken.ametist.firebase.MessagingService;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent serviceIntent = new Intent(context, MessagingService.class);
            context.startService(serviceIntent);
        }
    }
}
